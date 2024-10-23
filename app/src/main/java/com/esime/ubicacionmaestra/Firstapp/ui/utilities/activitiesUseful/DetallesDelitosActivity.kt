package com.esime.ubicacionmaestra.Firstapp.ui.utilities.activitiesUseful

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.esime.ubicacionmaestra.Firstapp.ui.saveLocation.SaveUbicacionReal
import com.esime.ubicacionmaestra.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DetallesDelitosActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var delitosCercanos: ArrayList<SaveUbicacionReal.Delito>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalles_delitos)
        supportActionBar?.hide()
        // Obtener los datos del Intent
        delitosCercanos = intent.getSerializableExtra("delitosCercanos") as? ArrayList<SaveUbicacionReal.Delito>

        // Inicializar el mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapdelitos) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        // Mostrar los markers de los delitos cercanos en el mapa
        delitosCercanos?.let { listaDelitos ->
            if (listaDelitos.isNotEmpty()) {
                for (delito in listaDelitos) {
                    val ubicacionDelito = LatLng(delito.latitud, delito.longitud)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(ubicacionDelito)
                            .title("${delito.categoriaDelito} - ${delito.delito}")
                    )
                }

                // Mover la cámara al primer marcador
                val firstDelito = listaDelitos[0]
                val firstLocation = LatLng(firstDelito.latitud, firstDelito.longitud)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 12f))
            }
        }
    }
}