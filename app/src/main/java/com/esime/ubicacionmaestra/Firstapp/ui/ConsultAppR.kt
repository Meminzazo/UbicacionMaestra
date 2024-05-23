package com.esime.ubicacionmaestra.Firstapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.esime.ubicacionmaestra.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConsultAppR : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {
    private lateinit var map: GoogleMap
    private val locationService: LocationService = LocationService()
    val db = FirebaseFirestore.getInstance()
    private var currentmarker: Marker? = null


    companion object {
        const val TAG = "ConsultarUbicacionReal" // Definimos la variable TAG aqui
        const val PREFS_NAME = "SwitchPrefs"
        const val SWITCH_STATE = "switch_state"
        const val REQUEST_CODE_LOCATION = 0
    }


    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consult_app_r)
        enableEdgeToEdge()
        supportActionBar?.hide()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val emailUbicacion = findViewById<EditText>(R.id.emailUbicacion)
        val consultButton = findViewById<AppCompatButton>(R.id.consultButton)
        var saveUbi = findViewById<Button>(R.id.saveUbi)
        val switchConsultar =
            findViewById<SwitchMaterial>(R.id.ConsultarUbicacion) as SwitchMaterial
        val sharedPrefs = getSharedPreferences(MainActivity2.PREFS_NAME, Context.MODE_PRIVATE)
        switchConsultar.isChecked = sharedPrefs.getBoolean(MainActivity2.SWITCH_STATE, false)
        var emailCon = ""
        val ConsultarUbicacionEmail = findViewById<AppCompatButton>(R.id.ConsultarUbicacionEmail)
        createFragment()

        var flag = false


        ConsultarUbicacionEmail.setOnClickListener {
            if (emailUbicacion.text.isNotEmpty()) {
                emailCon = emailUbicacion.text.toString()
                Log.d(TAG, "email a consultar es: ${emailCon}")
            } else {
                Toast.makeText(
                    this,
                    "Ingresa una direccion de correo valida",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        switchConsultar.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                flag = isChecked
                lifecycleScope.launch {
                    val docRef = db.collection("users").document("$emailCon")
                    while (flag) {
                        docRef.get().addOnSuccessListener { document ->
                            if (document != null) {

                                var Latitud = document.getString("Latitud")
                                var Lognitud = document.getString("Longitud")

                                var LatitudDouble = Latitud!!.toDouble()
                                var LontiudDouble = Lognitud!!.toDouble()

                                if (Latitud != null && Lognitud != null) {

                                    val coordinates = LatLng(LatitudDouble, LontiudDouble)

                                    currentmarker?.remove()

                                    val marker =
                                        MarkerOptions().position(coordinates).title("Aprox")
                                    map.addMarker(marker)
                                    MarkerOptions().icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_AZURE
                                        )
                                    ).position(coordinates).flat(true)

                                    currentmarker = map.addMarker(marker)

                                    map.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(coordinates, 15f),
                                        5000,
                                        null
                                    )



                                    Log.d(TAG, "Latitud: ${Latitud}")
                                    Log.d(TAG, "Longitud: ${Lognitud}")
                                } else {
                                    Log.d(TAG, "No hay Latitud ni Longitud")
                                }
                            } else {
                                Log.d(TAG, "No such document")
                            }
                        }
                            .addOnFailureListener { excepcion ->
                                Log.d(TAG, "get failed with ", excepcion)
                            }

                        delay(5000)
                    }
                }

            } else {
                flag = false
//                marker?.remove() // Si el switch se apaga, elimina el marcador actual

            }
            with(sharedPrefs.edit()) {
                putBoolean(MainActivity2.SWITCH_STATE, isChecked)
                apply()
            }
        }

    }


    private fun createFragment() {
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
    private fun enableLocation() {
        if (!::map.isInitialized) return
        if (isLocationPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            Toast.makeText(
                this,
                "Activa el permiso de ubicacion para poder usar esta caracteristica",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
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
        when (requestCode) {
            MainActivity.REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
            } else {
                Toast.makeText(
                    this,
                    "Porfavor activa el permiso de ubicacion para poder usar esta caracteristica",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {}
        }
    }

    @SuppressLint("MissingPermission")
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::map.isInitialized) return
        if (!isLocationPermissionGranted()) {
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