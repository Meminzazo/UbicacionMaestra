    package com.esime.ubicacionmaestra.Firstapp.ui

    import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
        private val db = FirebaseFirestore.getInstance()
        private var currentMarker: Marker? = null


        companion object {
            const val TAG = "ConsultarUbicacionReal" // Definimos la variable TAG aqui
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

            val switchConsultar = findViewById<SwitchMaterial>(R.id.ConsultarUbicacion) as SwitchMaterial
            val sharedPrefs = getSharedPreferences(MenuPrincipalActivity.PREFS_NAME, Context.MODE_PRIVATE)
            switchConsultar.isChecked = sharedPrefs.getBoolean(MenuPrincipalActivity.SWITCH_STATE, false)
            var emailCon = ""
            createFragment()

            var flag = false

            switchConsultar.setOnCheckedChangeListener { _, isChecked ->


                if (isChecked && emailUbicacion.text.isNotEmpty()) {

                    emailCon = emailUbicacion.text.toString()
                    flag = isChecked
                    lifecycleScope.launch {
                        val docRef = db.collection("users").document(emailCon)
                        while (flag) {
                            docRef.get().addOnSuccessListener { document ->
                                if (document != null) {

                                    val LatitudString = document.getString("Latitud")
                                    val LognitudString = document.getString("Longitud")

                                    val LatitudDouble = LatitudString!!.toDouble()
                                    val LongitudDouble = LognitudString!!.toDouble()

                                    if (LatitudDouble != null && LongitudDouble != null) {

                                        val coordinates = LatLng(LatitudDouble, LongitudDouble)

                                        currentMarker?.remove()

                                        // Crear un nuevo marcador y actualizar currentMarker
                                        currentMarker = map.addMarker(
                                            MarkerOptions()
                                                .position(coordinates)
                                                .title("Aprox")
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                                .flat(true)
                                        )

                                        map.animateCamera(
                                            CameraUpdateFactory.newLatLngZoom(coordinates, 18f),
                                            5000,
                                            null
                                        )




                                        Log.d(TAG, "Latitud: ${LatitudDouble}")
                                        Log.d(TAG, "Longitud: ${LongitudDouble}")
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

                            delay(20000)
                        }
                    }

                } else {
                    Toast.makeText(this, "Ingresa una direccion de correo valida", Toast.LENGTH_LONG).show()
                    flag = false
                    currentMarker?.remove() // Si el switch se apaga, elimina el marcador actual
                    switchConsultar.isChecked = false
                }
                with(sharedPrefs.edit()) {
                    putBoolean(MenuPrincipalActivity.SWITCH_STATE, isChecked)
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
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Toast.makeText(
                    this,
                    "Activa el permiso de ubicacion para poder usar esta caracteristica",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
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
            when (requestCode) {
                MenuPrincipalActivity.REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
            Toast.makeText(
                this,
                "Estas en ${p0.latitude}, ${p0.longitude}",
                Toast.LENGTH_SHORT
            ).show()
        }


    }