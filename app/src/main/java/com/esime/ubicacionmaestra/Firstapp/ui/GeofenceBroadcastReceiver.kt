package com.esime.ubicacionmaestra.Firstapp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import android.widget.Toast
import com.google.android.gms.location.GeofenceStatusCodes

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "GeofenceReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

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
}
