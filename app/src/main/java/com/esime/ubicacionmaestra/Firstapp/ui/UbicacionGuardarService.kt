package com.esime.ubicacionmaestra.Firstapp.ui

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.esime.ubicacionmaestra.Firstapp.ui.ConsultAppR.Companion
import com.esime.ubicacionmaestra.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class UbicacionGuardarService : Service() {

    data class User(
        val goevallaname: String? = null,
        val latidudgeovalla: String? = null,
        val longitudgeovalla: String? = null,
        val alertageovalla: String? = null
    )

    private lateinit var geofencingClient: GeofencingClient
    private val geoFenceId = "Pruebas"
    private val geoFenceCenterLat = 19.4976
    private val geoFenceCenterLng = -99.1356
    private val geoFenceRadius = 100.0

    private lateinit var database: DatabaseReference


    val TAG = "UbicacionGuardarService" // Definimos la variable TAG para el Logcat

    // Creamos una instancia de LocationService para la consulta de ubicación
    private val locationService:LocationService = LocationService()

    //Creamos una instancia de FirebaseFirestore para guardar los datos en la base de datos
    val db = FirebaseFirestore.getInstance()

    // Variables para la corrutina del servicio
    private var serviceJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    // Era algo del servicio pero no recuerdo que era pero nada relevante
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    // Función con la que empieza el como onCreate de una activity
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val bundle = intent?.extras
        val email = bundle?.getString("Email")  // Obtén el valor de email del intent
        val shouldTrackLocation = bundle?.getBoolean("Flag") ?: false   // Obtén el valor de shouldTrackLocation del intent para el While

        if (shouldTrackLocation && email != null) { // Verifica si shouldTrackLocation es true y si email no es nulo para el while
            startForeground(1, createNotification())    // Crea la notificación para el servicio en segundo plano
            startLocationTraking(email) // Inicia la corrutina para la consulta de ubicación
        }
        else{
            stopLocationTraking()   // Si shouldTrackLocation es false, detiene la corrutina y detiene el servicio en segundo plano
        }
        return START_STICKY // Indica que el servicio debe reiniciarse si se detiene
    }

    // Función para detener el servicio
    private fun stopLocationTraking() {
        serviceJob?.cancel()    // Cancela la corrutina del servicio
        stopForeground(true)    // Detiene el servicio en segundo plano
        stopSelf()  // Detiene el servicio
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence() {
        val geofence = Geofence.Builder()
            .setRequestId(geoFenceId)
            .setCircularRegion(
                geoFenceCenterLat,
                geoFenceCenterLng,
                geoFenceRadius.toFloat()
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        Log.d(ConsultAppR.TAG, "Geofence in process to add")

        val geofencePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
            .addOnSuccessListener {
                Toast.makeText(this, "Geovalla añadida correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                val errorMessage = when (e) {
                    is ApiException -> {
                        when (e.statusCode) {
                            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "Geofence no disponible"
                            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Demasiadas geovallas"
                            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "Demasiados PendingIntents"
                            else -> "Error desconocido: ${e.statusCode}"
                        }
                    }
                    else -> "Error desconocido: ${e.localizedMessage}"
                }
                Toast.makeText(this, "Error añadiendo geovalla: $errorMessage", Toast.LENGTH_LONG).show()
                Log.e("Geofence", errorMessage, e)
            }
    }

    // Función para iniciar el servicio
    private fun startLocationTraking(email: String) {
        serviceJob = serviceScope.launch {  // Inicia la corrutina del servicio
            while (isActive) {  // Mientras la corrutina esté activa
                val result = locationService.getUserLocation(this@UbicacionGuardarService)  // Obtiene la ubicación del usuario de la LocationService
                if (result != null) {   // Verifica si la ubicación no es nula



                    //Función cuando obtienes una nueva ubicación
                    saveLocation(email, result.latitude, result.longitude)

                    //Actualizar la ubicación en la base de datos
                    db.collection("users").document("$email").update(
                        mapOf(  // Actualiza los datos en la base de datos en un arreglo de esta forma
                            "Latitud" to "${result?.latitude}",
                            "Longitud" to "${result?.longitude}"
                        )
                    )
                }
                delay(30000)    // Espera 30 segundos antes de la próxima consulta de ubicación
            }
        }
    }

    // Función para destruir el servicio
    override fun onDestroy() {
        stopLocationTraking()   // Detiene la corrutina del servicio
        super.onDestroy()   // Llama a la función onDestroy de la superclase (Detener el servicio pues)
    }

    // Función para crear la notificación para el servicio en segundo plano
    private fun createNotification(): Notification {
        val notificationChannelId = "UBICACION_GUARDAR_CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                notificationChannelId,
                "Ubicacion Guardar Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, SaveUbicacionReal::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Ubicación en Tiempo Real Activa")
            .setContentText("La ubicación se está guardando en segundo plano.")
            .setSmallIcon(R.drawable.ic_location)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    // Funcion para guardar el historial de ubicación en la base de datos en un documento con la fecha de cuando se esta guardando
    fun saveLocation(email: String, latitude: Double, longitude: Double) {
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val locationData = hashMapOf(   // Datos a guardar en la base de datos con ese formato
            "latitude" to latitude,
            "longitude" to longitude,
            "timestamp" to System.currentTimeMillis()
        )

        // Conexion con la base dde datos para que se guarden
        db.collection("users").document(email).collection(currentDate)
            .add(locationData)
            .addOnSuccessListener {
                Log.d(TAG, "Location successfully saved!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error saving location", e)
            }
    }
}