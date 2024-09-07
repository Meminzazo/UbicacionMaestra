package com.esime.ubicacionmaestra.Firstapp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import android.widget.Toast
import com.esime.ubicacionmaestra.Firstapp.ui.ConsultAppR.User
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "GeofenceReceiver"
    }

    data class User(
        val goevallaname: String? = null,
        val latidudgeovalla: String? = null,
        val longitudgeovalla: String? = null,
        val alertageovalla: String? = null
    )

    private lateinit var database: DatabaseReference

    private lateinit var geofencingClient: GeofencingClient
    private val geoFenceId = "Pruebas"
    private val geoFenceCenterLat = 19.4976
    private val geoFenceCenterLng = -99.1356
    private val geoFenceRadius = 100.0

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        //geofencingClient = LocationServices.getGeofencingClient(this)
        //writeNewUser("hmaury10@gmailcom", "19.499428247356565", "-99.13468323947379","true")

        Log.e(TAG, "Entrando a onReceive")

        if (geofencingEvent!!.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, "Error de geovalla: $errorMessage")
            return
        }

        // Obtener el tipo de transición
        val geofenceTransition = geofencingEvent.geofenceTransition
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {



            Log.i(TAG, "Entrando en la geovalla")
            Toast.makeText(context, "Entrando en la geovalla", Toast.LENGTH_SHORT).show()
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.i(TAG, "Saliendo de la geovalla")
            Toast.makeText(context, "Saliendo de la geovalla", Toast.LENGTH_SHORT).show()
        } else {
            Log.e(TAG, "Transición no válida")
        }
    }

    fun writeNewUser(email: String, geofenceName: String, latitudgeofence: String, longitudgeofence: String, alertageofence: String) {

        database = Firebase.database.reference

        val user = User(latitudgeofence, longitudgeofence, alertageofence)

        database.child("users").child(email).child(geofenceName).setValue(user)
    }
}
