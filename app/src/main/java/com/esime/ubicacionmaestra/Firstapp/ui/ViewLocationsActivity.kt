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
    // Definimos el objeto GoogleMap para usar el mapa
    private lateinit var map: GoogleMap

    // Definimos la instancia de FirebaseFirestore para acceder a la base de datos Firestore
    private val db = FirebaseFirestore.getInstance()

    // Definimos la etiqueta para el registro de logcat
    private val TAG = "ViewLocationsActivity"

    // Funcion que se ejecuta al inicio de la activity
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_locations)

        supportActionBar?.hide()    // Oculta la barra de acción

        val bundle = intent.extras
        val email = bundle?.getString("Email")  // Obtiene el email del intent

        // Inicializamos los elementos de la interfaz de usuario (Botones, EditText)
        val ButtonHistorialUbicacion = findViewById<EditText>(R.id.eTHisUbicacion)
        val selectDateButton = findViewById<Button>(R.id.selectDateButton)


        selectDateButton.setOnClickListener {   // Cuando se hace click en el boton de la seleccion de fecha
            if(ButtonHistorialUbicacion.text.isNotEmpty()){ // Si el EditText no esta vacio


                val docRef = db.collection("users").document(email!!)   // Obtiene el documento del email del usuario actual
                val emailHisUbi = ButtonHistorialUbicacion.text.toString()
                val docRef2 = db.collection("users").document(emailHisUbi)  // Obtiene el documento del email del usuario que se esta buscando

                docRef.get().addOnSuccessListener { document -> // Si el documento del usuario actual se obtiene

                    val GroupIDPropio = document.getString("GrupoID")   // Guarda el GroupID del usuario actual

                    docRef2.get().addOnSuccessListener { document2 ->   // Si el documento del usuario que se esta buscando se obtiene

                        val GroupIDHis = document2.getString("GrupoID") // Guarda el GroupID del usuario que se esta buscando

                        if(GroupIDPropio == GroupIDHis){    // Comprueba que esten en el mismo grupo
                            showDatePickerDialog(emailHisUbi)   // Muestra el Selector del dia
                        }
                        else{
                            Toast.makeText(this, "El email ingresado es incorrecto o no pertenecen al mismo grupo!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            else{
                Toast.makeText(this, "Ingrese un email valido!", Toast.LENGTH_LONG).show()
            }

        }
        // Llamamos a la funcion para crear el mapa
        createMapFragment()
    }
///////////////////////////////////////////// FUNCIONES DEL MAPA ////////////////////////////////////////////////////
    private fun createMapFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Funcion para mostrar el Selector del Mapa
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

    // Funcion para cargar las ubicaciones guardadas en la base de datos
    private fun loadLocationsForDate(date: String, emailhis: String?) {

            // Conexiona a la base de datos Firestore
            db.collection("users").document(emailhis!!).collection(date)
            .orderBy("timestamp")   // Ordena por fecha en orden ascendente deacuerdo con el timestamp
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
                    showLocationHistoryOnMap(latLngList)    // Muestra la ubicacion en el mapa
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting documents: ", e)
            }
        Log.d(TAG, "loadLocationsForDate: $date")

    }

    // Funcion para mostrar las ubicaciones en el mapa
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
