package com.esime.ubicacionmaestra.Firstapp.ui

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
import androidx.core.app.NotificationCompat
import com.esime.ubicacionmaestra.R
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

    val TAG = "UbicacionGuardarService" // Definimos la variable TAG aqui

    private val locationService:LocationService = LocationService()
    val db = FirebaseFirestore.getInstance()
    private var serviceJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val bundle = intent?.extras
        val email = bundle?.getString("Email")
        val shouldTrackLocation = bundle?.getBoolean("Flag") ?: false

        if (shouldTrackLocation && email != null) {
            startForeground(1, createNotification())
            startLocationTraking(email)
        }
        else{
            stopLocationTraking()
        }
        return START_STICKY
    }

    private fun stopLocationTraking() {
        serviceJob?.cancel()
        stopForeground(true)
        stopSelf()
    }

    private fun startLocationTraking(email: String) {
        serviceJob = serviceScope.launch {
            Log.d(TAG, "Entrando al coroutine $email")

            while (isActive) {
                Log.d(TAG, "Entrando al while $email")

                val result = locationService.getUserLocation(this@UbicacionGuardarService)
                if (result != null) {
                    //Función cuando obtienes una nueva ubicación
                    saveLocation(email, result.latitude, result.longitude)
                    db.collection("users").document("$email").update(
                        mapOf(
                            "Latitud" to "${result?.latitude}",
                            "Longitud" to "${result?.longitude}"
                        )
                    )
                }
                delay(30000)
            }
        }
    }

    override fun onDestroy() {
        stopLocationTraking()
        super.onDestroy()
    }

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
    fun saveLocation(email: String, latitude: Double, longitude: Double) {
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val locationData = hashMapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "timestamp" to System.currentTimeMillis()
        )

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