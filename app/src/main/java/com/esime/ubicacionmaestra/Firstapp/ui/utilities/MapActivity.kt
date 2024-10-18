package com.esime.ubicacionmaestra.Firstapp.ui.utilities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esime.ubicacionmaestra.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

// Activity del mapa
class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var selectedLocation: LatLng? = null
    private var geofenceRadius: Float = 100f
    private var geofenceCircle: Circle? = null
    private lateinit var placesClient: PlacesClient

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        supportActionBar?.hide()
        // Inicializar Places API
        Places.initialize(applicationContext, getString(R.string.google_maps_api_key))
        val placesClient = Places.createClient(this)

        // Obtener el mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val btnConfirmLocation: Button = findViewById(R.id.btnConfirmLocation)
        val radiusSeekBar: SeekBar = findViewById(R.id.radiusSeekBar)
        val searchLocation: AutoCompleteTextView = findViewById(R.id.searchLocation)

        // Configurar Autocomplete para búsqueda de lugares
        val autocompleteSessionToken = AutocompleteSessionToken.newInstance()
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line)
        searchLocation.setAdapter(adapter)

        searchLocation.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(autocompleteSessionToken)
                    .setQuery(s.toString())
                    .build()

                placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
                    val suggestions = response.autocompletePredictions.map { it.getFullText(null).toString() }
                    adapter.clear()
                    adapter.addAll(suggestions)
                    adapter.notifyDataSetChanged()
                }.addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        searchLocation.setOnItemClickListener { _, _, position, _ ->
            val selectedPrediction = adapter.getItem(position)
            val request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(autocompleteSessionToken)
                .setQuery(selectedPrediction)
                .build()

            placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
                val prediction = response.autocompletePredictions.firstOrNull { it.getFullText(null).toString() == selectedPrediction }
                prediction?.let {
                    val placeId = it.placeId
                    val placeFields = listOf(Place.Field.LAT_LNG)
                    val fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build()
                    placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener { fetchResponse ->
                        val latLng = fetchResponse.place.latLng
                        latLng?.let {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                            mMap.addMarker(MarkerOptions().position(latLng).title("Ubicación seleccionada"))
                            selectedLocation = latLng
                            updateGeofenceCircle()
                        }
                    }.addOnFailureListener { exception ->
                        exception.printStackTrace()
                    }
                }
            }.addOnFailureListener { exception ->
                exception.printStackTrace()
            }
        }

        btnConfirmLocation.setOnClickListener {
            // Enviar los datos seleccionados a la actividad principal
            selectedLocation?.let {
                val resultIntent = Intent()
                resultIntent.putExtra("latitude", it.latitude)
                resultIntent.putExtra("longitude", it.longitude)
                resultIntent.putExtra("radius", geofenceRadius)
                resultIntent.putExtra("geofenceIndex", intent.getIntExtra("geofenceIndex", -1))
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
        radiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                geofenceRadius = progress.toFloat()
                updateGeofenceCircle()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Configuración inicial del mapa
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMapClickListener { latLng ->
            // Limpiar marcadores previos
            mMap.clear()
            geofenceCircle = null
            // Agregar un nuevo marcador en la ubicación seleccionada
            mMap.addMarker(MarkerOptions().position(latLng).title("Ubicación seleccionada"))
            // Guardar la ubicación seleccionada
            selectedLocation = latLng
            updateGeofenceCircle()
        }
    }

    private fun updateGeofenceCircle() {
        selectedLocation?.let {
            // Eliminar el círculo previo si existe
            geofenceCircle?.remove()
            // Agregar un nuevo círculo con el radio seleccionado
            geofenceCircle = mMap.addCircle(
                CircleOptions()
                    .center(it)
                    .radius(geofenceRadius.toDouble())
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF)
                    .strokeWidth(2f)
            )
        }
    }
}