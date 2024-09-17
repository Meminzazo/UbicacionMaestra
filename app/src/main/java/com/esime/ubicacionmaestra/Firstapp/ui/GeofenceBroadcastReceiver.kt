package com.esime.ubicacionmaestra.Firstapp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import android.widget.Toast
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "GeofenceReceiver"
        const val EXTRA_GEOFENCE_NAME = "Nombre" // Asegúrate de usar la misma clave
    }

    data class User(
        val name: String? = null,
        val latitud: String? = null,
        val longitud: String? = null,
        val radius: String? = null,
        val transitionTypes: String? = "null"
    )

    private lateinit var database: DatabaseReference

    override fun onReceive(context: Context, intent: Intent) {

        database = Firebase.database.reference


        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        Log.e(TAG, "Entrando a onReceive")

        if (geofencingEvent!!.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, "Error de geovalla: $errorMessage")
            return
        }

        val name = intent.getStringExtra(EXTRA_GEOFENCE_NAME)

        Log.e(TAG, "Nombre de geovalla recibido: $name")


        // Obtener el tipo de transición
        val geofenceTransition = geofencingEvent.geofenceTransition

        if (name != null) {
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                val update = mapOf("transitionTypes" to "true")
                database.child("users").child("hmaury10").child("Geovallas").child(name).updateChildren(update)
                Log.i(TAG, "Entrando en la geovalla: $name")
                Toast.makeText(context, "Entrando en la geovalla: $name", Toast.LENGTH_SHORT).show()

            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

                val update = mapOf("transitionTypes" to "false")
                database.child("users").child("hmaury10").child("Geovallas").child(name).updateChildren(update)
                Log.i(TAG, "Saliendo de la geovalla: $name")
                Toast.makeText(context, "Saliendo de la geovalla: $name", Toast.LENGTH_SHORT).show()

            } else {

                Log.e(TAG, "Transición no válida")

            }
        } else {

            Log.e(TAG, "Nombre de geovalla no encontrado en el Intent")

        }
    }
}