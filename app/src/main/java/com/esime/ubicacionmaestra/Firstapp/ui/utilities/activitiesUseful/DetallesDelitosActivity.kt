package com.esime.ubicacionmaestra.Firstapp.ui.utilities.activitiesUseful

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
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
    private lateinit var database: DatabaseReference
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalles_delitos)

        // Inicializar Firebase
        database = FirebaseDatabase.getInstance().reference.child("denuncias")
        createFragment()

    }
    private fun createFragment(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapdelitos) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Obtener la ubicación del usuario y mostrar los delitos cercanos
        lifecycleScope.launch(Dispatchers.IO) {
            val snapshot = database.get().await()
            withContext(Dispatchers.Main) {
                if (snapshot.exists()) {
                    for (delitoSnapshot in snapshot.children) {
                        val latitud = delitoSnapshot.child("latitud").getValue(Double::class.java)
                        val longitud = delitoSnapshot.child("longitud").getValue(Double::class.java)
                        val categoriaDelito = delitoSnapshot.child("categoria_delito").getValue(String::class.java)

                        if (latitud != null && longitud != null && categoriaDelito != null && categoriaDelito != "Hecho no delictivo") {
                            val ubicacionDelito = LatLng(latitud, longitud)
                            mMap.addMarker(MarkerOptions().position(ubicacionDelito).title("\$categoriaDelito - " + delitoSnapshot.child("delito").getValue(String::class.java)))
                        }
                    }
                    // Mover la cámara al primer marcador
                    if (snapshot.children.iterator().hasNext()) {
                        val firstLat = snapshot.children.iterator().next().child("latitud").getValue(Double::class.java)
                        val firstLng = snapshot.children.iterator().next().child("longitud").getValue(Double::class.java)
                        if (firstLat != null && firstLng != null) {
                            val firstLocation = LatLng(firstLat, firstLng)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 12f))
                        }
                    }
                }
            }
        }
    }
}