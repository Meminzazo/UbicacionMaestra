package com.esime.ubicacionmaestra.Firstapp.ui.utilities.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.esime.ubicacionmaestra.Firstapp.ui.utilities.EarthquakeApiService
import com.esime.ubicacionmaestra.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class EarthquakeMonitoringService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val TAG = "EarthquakeService"

    // Variables para la corrutina del servicio
    private var serviceJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    // Retrofit para conectarse con la API de USGS
    val retrofit = Retrofit.Builder()
        .baseUrl("https://earthquake.usgs.gov/earthquakes/feed/v1.0/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Interface para la API
    val earthquakeApi = retrofit.create(EarthquakeApiService::class.java)

    // Cuando se crea el servicio
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel() // Crear canal de notificaciones
    }

    // Método llamado cuando se inicia el servicio
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Servicio de monitoreo de sismos iniciado")

        // Iniciar la tarea en segundo plano para obtener datos de sismos simulados
        serviceJob = serviceScope.launch {
            fetchMockEarthquakeData()
        }

        // Devolver START_STICKY para mantener el servicio corriendo en segundo plano
        return START_STICKY
    }

    // Método onBind (para completar los métodos abstractos de la clase Service)
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun fetchEarthquakeData() {
        val call = earthquakeApi.getRecentEarthquakes()

        call.enqueue(object : Callback<EarthquakeApiService.EarthquakeResponse> {
            override fun onResponse(
                call: Call<EarthquakeApiService.EarthquakeResponse>,
                response: Response<EarthquakeApiService.EarthquakeResponse>
            ) {
                if (response.isSuccessful) {
                    val earthquakes = response.body()?.features ?: emptyList()
                    checkIfEarthquakeNearby(earthquakes)
                }
            }

            override fun onFailure(call: Call<EarthquakeApiService.EarthquakeResponse>, t: Throwable) {
                Log.e(TAG, "Error al obtener datos de sismos: ${t.message}")
            }
        })
    }

    // Simulación de datos de terremotos para pruebas
    private fun fetchMockEarthquakeData() {
        val mockEarthquake = EarthquakeApiService.EarthquakeFeature(
            properties = EarthquakeApiService.EarthquakeProperties(
                mag = 6.0,
                place = "Simulated Location",
                time = System.currentTimeMillis()  // Usamos la hora actual para simular el tiempo del terremoto
            ),
            geometry = EarthquakeApiService.EarthquakeGeometry(
                coordinates = listOf(-99.13449, 19.49942)  // Coordenadas cercanas simuladas
            )
        )

        // Verificar si está cerca del usuario
        checkIfEarthquakeNearby(listOf(mockEarthquake))
    }

    @SuppressLint("MissingPermission")
    private fun checkIfEarthquakeNearby(earthquakes: List<EarthquakeApiService.EarthquakeFeature>) {
        // Obtener la ubicación del usuario
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                earthquakes.forEach { earthquake ->
                    val lat = earthquake.geometry.coordinates[1]
                    val lng = earthquake.geometry.coordinates[0]

                    // Calcular la distancia entre el terremoto y la ubicación del usuario
                    val distance = calculateDistance(location.latitude, location.longitude, lat, lng)

                    if (distance <= 100) { // Si el terremoto está a menos de 100 km
                        notifyUser(earthquake)  // Notificar al usuario
                        //startLocationService()  // Iniciar el servicio de guardado de ubicación
                    }
                }
            } else {
                Log.e(TAG, "No se pudo obtener la ubicación del usuario")
            }
        }
    }

    // Iniciar el servicio de ubicación (UbicacionGuardarService)
    private fun startLocationService() {
        val intent = Intent(this, UbicacionGuardarService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    // Calcular la distancia entre dos puntos geográficos
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371 // Radio de la tierra en kilómetros
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c // Distancia en kilómetros
    }

    // Notificar al usuario sobre el sismo
    @SuppressLint("MissingPermission")
    private fun notifyUser(earthquake: EarthquakeApiService.EarthquakeFeature) {
        val notification = NotificationCompat.Builder(this, "EarthquakeChannel")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("¡Sismo detectado cerca de ti!")
            .setContentText("Magnitud: ${earthquake.properties.mag}, Lugar: ${earthquake.properties.place}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(this).notify(1, notification)
    }

    // Crear el canal de notificaciones para el servicio
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "EarthquakeChannel",
                "Earthquake Monitoring Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
