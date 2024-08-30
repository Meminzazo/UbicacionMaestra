package com.esime.ubicacionmaestra.Firstapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
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

    // Variables para el mapa
    private lateinit var map: GoogleMap
    private var currentMarker: Marker? = null

    // Variables para la base de datos
    private val db = FirebaseFirestore.getInstance()

    // Definimos la variable TAG para ubicar mas facil en el Logcat
    companion object {
        const val TAG = "ConsultarUbicacionReal" // Definimos la variable TAG aqui
    }

    private lateinit var geofencingClient: GeofencingClient
    private val geoFenceId = "Pruebas"
    private val geoFenceCenterLat = 19.4976
    private val geoFenceCenterLng = -99.1356
    private val geoFenceRadius = 100.0

    // Funcion que se ejecuta al entrar a la activity
    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consult_app_r)
        enableEdgeToEdge()
        supportActionBar?.hide()

        // Obtener el email del intent
        val bundle = intent.extras
        val email = bundle?.getString("Email")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener referencias a los elementos de la interfaz de usuario (botones,EditText,etc)
        val emailUbicacion = findViewById<EditText>(R.id.emailUbicacion)
        val switchConsultar =
            findViewById<SwitchMaterial>(R.id.ConsultarUbicacion) as SwitchMaterial
        val sharedPrefs =
            getSharedPreferences(MenuPrincipalActivity.PREFS_NAME, Context.MODE_PRIVATE)
        switchConsultar.isChecked =
            sharedPrefs.getBoolean(MenuPrincipalActivity.SWITCH_STATE, false)
        var emailCon = ""

        // El mapa
        createFragment()

        var flag = false

        geofencingClient = LocationServices.getGeofencingClient(this)

        addGeofence()

        // Cambio de estado del switch
        switchConsultar.setOnCheckedChangeListener { _, isChecked ->
            if (emailUbicacion.text.toString()
                    .isNotEmpty()
            ) { // Verificar si el EditText no está vacío

                val docRef = db.collection("users")
                    .document(email!!)   // Obtener la referencia al documento del usuario en Firestore
                val emailUbicacion2 =
                    emailUbicacion.text.toString()    // Obtener el valor del EditText
                val docRef2 = db.collection("users")
                    .document(emailUbicacion2)  // Obtener la referencia al documento del usuario a consultar en Firestore

                docRef.get()
                    .addOnSuccessListener { document -> // Si obtiene el documento del usuario de manera exitosa

                        val GrupoIDPropio =
                            document.getString("GrupoID")   // Obtener el GrupoID del usuario actual

                        docRef2.get()
                            .addOnSuccessListener { document1 ->    // Si obtiene el documento del usuario a consultar de manera exitosa

                                val GrupoID =
                                    document1.getString("GrupoID") // Obtener el GrupoID del usuario a consultar

                                if (GrupoIDPropio == GrupoID) { // Verificar si pertenecen al mismo grupo
                                    if (isChecked) {    // Si el switch está activado
                                        emailCon =
                                            emailUbicacion.text.toString()   // Obtener el valor del EditText
                                        flag = isChecked
                                        lifecycleScope.launch {
                                            val docRef = db.collection("users")
                                                .document(emailCon)  // Obtener la referencia al documento del usuario a consultar en Firestore
                                            while (flag) {
                                                docRef.get().addOnSuccessListener { document ->
                                                    if (document != null) {

                                                        val LatitudString =
                                                            document.getString("Latitud")   // Obtener la latitud del documento del usuario a consultar
                                                        val LognitudString =
                                                            document.getString("Longitud") // Obtener la longitud del documento del usuario a consultar

                                                        val LatitudDouble =
                                                            LatitudString!!.toDouble()  // Convertir las coordenadas a Double
                                                        val LongitudDouble =
                                                            LognitudString!!.toDouble()    // Convertir las coordenadas a Double

                                                        if (LatitudDouble != null && LongitudDouble != null) {  // Verificar si las coordenadas son nulas

                                                            val coordinates =
                                                                LatLng(
                                                                    LatitudDouble,
                                                                    LongitudDouble
                                                                )

                                                            currentMarker?.remove() // Eliminar el marcador actual si existe

                                                            // Crear un nuevo marcador y actualizar currentMarker
                                                            currentMarker = map.addMarker(
                                                                MarkerOptions()     // Opciones del marcador para personalizarlo
                                                                    .position(coordinates)
                                                                    .title("Aprox")
                                                                    .icon(
                                                                        BitmapDescriptorFactory.defaultMarker(
                                                                            BitmapDescriptorFactory.HUE_AZURE
                                                                        )
                                                                    )
                                                                    .flat(true)
                                                            )

                                                            map.animateCamera(      // Animacion del mapa en el punto
                                                                CameraUpdateFactory.newLatLngZoom(
                                                                    coordinates,
                                                                    18f
                                                                ),
                                                                5000,
                                                                null
                                                            )

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

                                                delay(20000)    // Espera 20 segundos antes de volver a consultar
                                            }
                                        }

                                    } else {
                                        flag = false
                                        currentMarker?.remove() // Si el switch se apaga, elimina el marcador actual
                                        switchConsultar.isChecked = false // Desmarca el switch
                                    }
                                } else {
                                    Toast.makeText(
                                        this,
                                        "No pertenecen al mismo grupo",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                    flag = false
                                    currentMarker?.remove() // Si el switch se apaga, elimina el marcador actual
                                    switchConsultar.isChecked = false   // Desmarca el switch
                                }
                            }
                    }
            } else {
                Toast.makeText(this, "Ingresa una direccion de correo valida", Toast.LENGTH_LONG)
                    .show()
                flag = false
                currentMarker?.remove() // Si el switch se apaga, elimina el marcador actual
                switchConsultar.isChecked = false   // Desmarca el switch
            }
            with(sharedPrefs.edit()) {
                putBoolean(MenuPrincipalActivity.SWITCH_STATE, isChecked)
                apply()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence() {
        val geofence = Geofence.Builder()
            .setRequestId(geoFenceId)
            .setCircularRegion(
                geoFenceCenterLat,
                geoFenceCenterLng,
                geoFenceRadius.toFloat()
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        Log.d(TAG, "Geofence in process to add")

        val geofencePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
            .addOnSuccessListener {
                Toast.makeText(this, "Geovalla añadida correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                val errorMessage = when (e) {
                    is ApiException -> {
                        when (e.statusCode) {
                            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "Geofence no disponible"
                            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Demasiadas geovallas"
                            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "Demasiados PendingIntents"
                            else -> "Error desconocido: ${e.statusCode}"
                        }
                    }
                    else -> "Error desconocido: ${e.localizedMessage}"
                }
                Toast.makeText(this, "Error añadiendo geovalla: $errorMessage", Toast.LENGTH_LONG).show()
                Log.e("Geofence", errorMessage, e)
            }
    }



    ////////////////////////////////// COSAS QUE HACEN QUE FUNCIONE EL MAPA ///////////////////////////////////////////////
    private fun createFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableLocation()
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        map.isTrafficEnabled = true
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Verificar el estado del permiso de ubicación
    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    // Solicitar si la ubicacion esta activa
    @SuppressLint("MissingPermission")
    private fun enableLocation() {
        if (!::map.isInitialized) return
        if (isLocationPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    // Solicitar el permiso de ubicación
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

    // Respuesta del permiso de ubicación
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

    //////////////////////////////////////////////// MAS PARA EL MAPA ////////////////////////////////////////////////////////////////
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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Boton de ubicacion para saber tu ubicacion
    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(
            this,
            "Ubicación Aproximada",
            Toast.LENGTH_SHORT
        ).show()
        return false
    }

    // Cuando se hace click en la ubicacion muestra las coordenadas
    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(
            this,
            "Estas en ${p0.latitude}, ${p0.longitude}",
            Toast.LENGTH_SHORT
        ).show()
    }
}