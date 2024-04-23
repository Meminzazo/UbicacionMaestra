package com.esime.ubicacionmaestra.Firstapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esime.ubicacionmaestra.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private lateinit var map:GoogleMap

    companion object {
        const val REQUEST_CODE_LOCATION = 0
        const val TAG = "MainActivity" // Definimos la variable TAG aqui

    }

    private lateinit var database: DatabaseReference

    val db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createFragment()
        supportActionBar?.hide()
        val buttonPanel = findViewById<AppCompatButton>(R.id.buttonPanel)

        buttonPanel.setOnClickListener{
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }


    }

    private fun createFragment(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        //createMarker()
        enableLocation()
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
    }
    private fun createMarker(){
        val coordinates = LatLng(19.49939, -99.13450)
        val marker = MarkerOptions().position(coordinates).title("LA MEJOR ESCUELA")
        map.addMarker(marker)
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(coordinates,13f),
            5000,
            null
        )
    }

    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED


    private fun enableLocation(){
        if(!::map.isInitialized) return
        if(isLocationPermissionGranted()){
            map.isMyLocationEnabled = true
        }else{
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(
                this,
                "Activa el permiso de ubicacion para poder usar esta caracteristica",
                Toast.LENGTH_SHORT
            ).show()
        }else{
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                map.isMyLocationEnabled = true
            }else{
                Toast.makeText(
                    this,
                    "Porfavor activa el permiso de ubicacion para poder usar esta caracteristica",
                    Toast.LENGTH_SHORT
                ).show()
            }else -> {}
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if(!::map.isInitialized) return
        if(!isLocationPermissionGranted()){
            map.isMyLocationEnabled = false
            Toast.makeText(
                this,
                "Porfavor activa el permiso de ubicacion para poder usar esta caracteristica",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(
            this,
            "Boton pulsado",
            Toast.LENGTH_SHORT
        ).show()
        return false
    }



    override fun onMyLocationClick(p0: Location) {
        val lat = p0.latitude
        val lon = p0.longitude

        Toast.makeText(
            this,
            "Estas en ${p0.latitude}, ${p0.longitude}",
            Toast.LENGTH_SHORT
        ).show()
        actualizarColletion(lat, lon)
    }

    fun actualizarColletion(lat: Double, lon: Double) {

        //Create a reference to the document
        val latitud = db.collection("users").document("xLlujJR3FXL2u15NDckC")
        val longitud = db.collection("users").document("xLlujJR3FXL2u15NDckC")

        // Set the "born" field of the user "miles"
        latitud.update("Latitud", lat)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating document", e)
            }
        // Set the "born" field of the user "miles"
        longitud.update("Latitud", lon)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating document", e)
            }

    }
}