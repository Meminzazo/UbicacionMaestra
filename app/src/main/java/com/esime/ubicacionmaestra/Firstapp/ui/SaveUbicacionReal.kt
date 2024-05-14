package com.esime.ubicacionmaestra.Firstapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.esime.ubicacionmaestra.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SaveUbicacionReal : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener  {

    private lateinit var map:GoogleMap
    private lateinit var database: DatabaseReference
    private val locationService:LocationService = LocationService()

    companion object {
        const val TAG = "MainActivity" // Definimos la variable TAG aqui
        const val PREFS_NAME = "SwitchPrefs"
        const val SWITCH_STATE = "switch_state"
    }

    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_ubicacion_real)

        supportActionBar?.hide()
        val bundle = intent.extras
        val email = bundle?.getString("Email")
        createFragment()

        val switchUbicacionReal = findViewById<SwitchMaterial>(R.id.UbicacionReal) as SwitchMaterial


        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        switchUbicacionReal.isChecked = sharedPrefs.getBoolean(SWITCH_STATE, false)

        val isCheck = switchUbicacionReal.isChecked

        var flag = false

        switchUbicacionReal.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                flag = isChecked

                Log.d(TAG, "la Flag: $flag")

                lifecycleScope.launch {
                    while(flag){
                        Log.d(TAG, "Entrando al while $email")
                        val result = locationService.getUserLocation(this@SaveUbicacionReal)
                        db.collection("users").document("$email").update(
                            mapOf(
                                "Latitud" to "${result?.latitude}",
                                "Longitud" to "${result?.longitude}"
                            )
                        )
                        delay(5000)
                    }
                }
            }
            else{
                    flag = false
            }
            with(sharedPrefs.edit()){
                putBoolean(SWITCH_STATE, isChecked)
                apply()
            }
        }


    }

    private fun createFragment(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableLocation()
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
    }
    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
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
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                MainActivity.REQUEST_CODE_LOCATION
            )
        }
    }

    @SuppressLint("MissingPermission", "MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            MainActivity.REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
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

    @SuppressLint("MissingPermission")
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
    }
}
