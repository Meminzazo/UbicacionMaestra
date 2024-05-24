package com.esime.ubicacionmaestra.Firstapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esime.ubicacionmaestra.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.switchmaterial.SwitchMaterial


class SaveUbicacionReal : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener  {

    private lateinit var map:GoogleMap

    companion object {
        const val TAG = "SaveUbicacionReal" // Definimos la variable TAG aqui
        const val PREFS_NAME = "SwitchPrefs"
        const val SWITCH_STATE = "switch_state"
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_ubicacion_real)

        requestNotificationPermission()

        supportActionBar?.hide()
        val bundle = intent.extras
        val email = bundle?.getString("Email")
        createFragment()

        val switchUbicacionReal = findViewById<SwitchMaterial>(R.id.UbicacionReal) as SwitchMaterial


        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        switchUbicacionReal.isChecked = sharedPrefs.getBoolean(SWITCH_STATE, false)

        val isCheck = switchUbicacionReal.isChecked

        var flag = false

        switchUbicacionReal.setOnCheckedChangeListener {  _, isChecked ->
            if (isChecked) {
                flag = isChecked
                Log.d(TAG, "Switch activo")
                val intent = Intent(this, UbicacionGuardarService::class.java).apply {
                    putExtra("Flag", flag)
                    putExtra("Email", email)
                }
                Log.d(TAG, "$flag")
                Log.d(TAG,"$email")
                startService(intent)

            }
            else{
                flag = false
                Log.d(TAG, "Switch desactivado")
                val intent2 = Intent(this, UbicacionGuardarService::class.java)
                stopService(intent2)
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
                MenuPrincipalActivity.REQUEST_CODE_LOCATION
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
            MenuPrincipalActivity.REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
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
            "Ubicacion aproximada",
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

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }

    }

    fun onRequestPermissionsResults(requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso otorgado
            } else {
                Toast.makeText(this, "Permission denied to post notifications", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
