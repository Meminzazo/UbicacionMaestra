package com.esime.ubicacionmaestra.Firstapp.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esime.ubicacionmaestra.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

class ViewLocationsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "ViewLocationsActivity"



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_locations)

        supportActionBar?.hide()

        val ButtonHistorialUbicacion = findViewById<EditText>(R.id.eTHisUbicacion)

        val selectDateButton = findViewById<Button>(R.id.selectDateButton)
        selectDateButton.setOnClickListener {
            val emailHisUbi = ButtonHistorialUbicacion.text.toString()

            Log.d(TAG, "Historial de ubicaciones de: $emailHisUbi")
            if(emailHisUbi.isEmpty()){
                Toast.makeText(this, "Ingrese un email valido!", Toast.LENGTH_LONG).show()
            }
            else{
                showDatePickerDialog(emailHisUbi)
            }
        }

        createMapFragment()
    }

    private fun createMapFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

    private fun showDatePickerDialog(emailhis: String?) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%d", dayOfMonth, month + 1, year)
                loadLocationsForDate(selectedDate,emailhis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun loadLocationsForDate(date: String, emailhis: String?) {

            Log.d(TAG, "Cargando ubicaciones para la fecha: $date y el email: $emailhis")
            db.collection("users").document(emailhis!!).collection(date)
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d(TAG, "No se encontraron documentos para la fecha: $date")
                    return@addOnSuccessListener
                }
                else{
                    val latLngList = mutableListOf<LatLng>()
                    for (document in documents) {
                        val lat = document.getDouble("latitude")
                        val lng = document.getDouble("longitude")
                        if (lat != null && lng != null) {
                            latLngList.add(LatLng(lat, lng))
                            Log.d(TAG, "Ubicacion agregada: $lat, $lng")
                        }
                        Log.d(TAG, "Documento recuperado: $document")
                    }
                    showLocationHistoryOnMap(latLngList)
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting documents: ", e)
            }
        Log.d(TAG, "loadLocationsForDate: $date")

    }

    private fun showLocationHistoryOnMap(latLngList: List<LatLng>) {
        map.clear() // Limpia el mapa antes de agregar la ruta

        if (latLngList.isNotEmpty()) {
            val options = PolylineOptions().width(5f).color(Color.BLUE).geodesic(true)
            for (latLng in latLngList) {
                options.add(latLng)
            }
            map.addPolyline(options)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngList.first(), 15f))

            // Opcional: agrega marcadores en cada punto
            for (latLng in latLngList) {
                map.addMarker(MarkerOptions().position(latLng).title("Past Location"))
            }
        }
    }
}
