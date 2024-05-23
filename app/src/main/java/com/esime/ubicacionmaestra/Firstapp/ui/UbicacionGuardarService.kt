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
import kotlinx.coroutines.launch


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
        val flags = bundle?.getBoolean("Flag")

        if (flags!! && email != null) {
            startForeground(1, createNotification())
            serviceJob = serviceScope.launch {
                Log.d(TAG, "Entrando al coroutine $email")

                while (flags!!) {

                    Log.d(TAG, "Entrando al while $email")

                    val result = locationService.getUserLocation(this@UbicacionGuardarService)
                    db.collection("users").document("$email").update(
                        mapOf(
                            "Latitud" to "${result?.latitude}",
                            "Longitud" to "${result?.longitude}"
                        )
                    )
                    delay(30000)
                    // Llama a esta función cuando obtienes una nueva ubicación
                    saveLocationToFirestore(email, result?.latitude!!, result?.longitude!!)
                }
            }
        }
        else{
            serviceJob?.cancel()
            stopForeground(true)
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {

        serviceJob?.cancel()
        super.onDestroy()
    }
    // Guardar ubicación en Firestore
    fun saveLocationToFirestore(email: String, lat: Double, lng: Double) {
        val locationData = hashMapOf(
            "Latitud" to lat,
            "Longitud" to lng,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users").document(email).collection("locations")
            .add(locationData)
            .addOnSuccessListener { documentReference ->
                Log.d(ConsultAppR.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(ConsultAppR.TAG, "Error adding document", e)
            }
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

}