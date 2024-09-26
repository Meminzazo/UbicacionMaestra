package com.esime.ubicacionmaestra.Firstapp.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esime.ubicacionmaestra.Firstapp.ui.ConsultAppR.Companion
import com.esime.ubicacionmaestra.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date


class ViewLocationsActivity : AppCompatActivity(), OnMapReadyCallback {
    // Definimos el objeto GoogleMap para usar el mapa
    private lateinit var map: GoogleMap

    private var uid: String? = null
    private var emailPropio: String? = null
    private var emailCon: String? = null

    private lateinit var bitmap: Bitmap // Declaración global del bitmap

    // Definimos la instancia de FirebaseFirestore para acceder a la base de datos Firestore
    private val db = FirebaseFirestore.getInstance()

    // Definimos la etiqueta para el registro de logcat
    private val TAG = "ViewLocationsActivity"

    // Funcion que se ejecuta al inicio de la activity
    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_locations)

        supportActionBar?.hide()    // Oculta la barra de acción

        val bundle = intent.extras
        emailPropio = bundle?.getString("Email")  // Obtiene el email del intent
        uid = bundle?.getString("UID")

        // Inicializamos los elementos de la interfaz de usuario (Botones, EditText)
        //val ButtonHistorialUbicacion = findViewById<EditText>(R.id.eTHisUbicacion)
        val spinnerHisUbi = findViewById<Spinner>(R.id.emailHisSpinner)
        val selectDateButton = findViewById<Button>(R.id.selectDateButton)

        var grupoID: String? = null
        val docRefHis = db.collection("users").document(emailPropio!!)
        val emailsList = mutableListOf<String>()
        Log.i(TAG, emailPropio!!)

        docRefHis.get().addOnSuccessListener { document ->
            val GrupoID = document.getString("GrupoID")
            val nuevaPhoto = document.getString("photoUrl")
            if (nuevaPhoto != null) {
                cargarFotoEnMarker(nuevaPhoto)
            }
            if (GrupoID != "-") {
                grupoID = GrupoID!!
            }

            Log.i(TAG,"GrupoID: $grupoID")

            val docRef = db.collection("grupos").document(grupoID!!)
            docRef.get().addOnSuccessListener { document1 ->
                if (document1 != null && document1.exists()) {

                    emailsList.clear()

                    for (field in document1.data?.keys.orEmpty()) {
                        // Aquí asumimos que los campos son email1, email2, etc.
                        val email = document1.getString(field)
                        if (!email.isNullOrEmpty()) {
                            emailsList.add(email)
                        }
                    }

                    Log.i(TAG, "Correos disponibles: $emailsList")

                    if (emailsList.isNotEmpty()) {
                        // Configura el adaptador del Spinner
                        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, emailsList)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerHisUbi.adapter = adapter
                    }else{
                        Toast.makeText(this, "No hay correos disponibles", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "No se encontró el grupo", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Log.w(TAG, "Error al obtener el documento", e)
            }
        }


            selectDateButton.setOnClickListener {   // Cuando se hace click en el boton de la seleccion de fecha

                val emailUbiHist = spinnerHisUbi.selectedItem.toString() ?: ""
                Log.i(TAG, "Email a consultar: $emailUbiHist")


                if(emailUbiHist.isEmpty()){ // Si el EditText no esta vacio
                    Toast.makeText(this, "Seleccione un email valido!", Toast.LENGTH_LONG).show()

                }else{
                    showDatePickerDialog(emailUbiHist)   // Muestra el Selector del dia
                }

            }
        // Llamamos a la funcion para crear el mapa
        createMapFragment()
    }

    private fun cargarFotoEnMarker(photoUrl: String) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl)
        val localFile = File.createTempFile("tempImage", "jpg")

        storageRef.getFile(localFile).addOnSuccessListener {
            bitmap = BitmapFactory.decodeFile(localFile.absolutePath) // Actualiza el bitmap global
        }.addOnFailureListener {
            Log.e(TAG, "Error al cargar la imagen: ${it.message}")
        }
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
                    val timestampList = mutableListOf<Long>()
                    for (document in documents) {
                        val lat = document.getDouble("latitude")
                        val lng = document.getDouble("longitude")
                        val timestamp = document.getLong("timestamp")
                        if (lat != null && lng != null && timestamp != null) {
                            latLngList.add(LatLng(lat, lng))

                            timestampList.add(timestamp)
                            Log.d(TAG, "Ubicacion agregada: $lat, $lng")

                        }
                        Log.d(TAG, "Documento recuperado: $document")
                    }
                    showLocationHistoryOnMap(latLngList,timestampList)    // Muestra la ubicacion en el mapa
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting documents: ", e)
            }
        Log.d(TAG, "loadLocationsForDate: $date")

    }

    // Funcion para mostrar las ubicaciones en el mapa
    private fun showLocationHistoryOnMap(latLngList: List<LatLng>, timestampList: MutableList<Long>) {
        map.clear() // Limpia el mapa antes de agregar la ruta

        if (latLngList.isNotEmpty()) {
            val options = PolylineOptions().width(5f).color(Color.BLUE).geodesic(true)
            for (latLng in latLngList) {
                options.add(latLng)
            }
            map.addPolyline(options)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngList.first(), 15f))

            // Opcional: agrega marcadores en cada punto
            for (i in latLngList.indices) {
                val horaFormateada = convertirTimestamp(timestampList[i])
                map.addMarker(MarkerOptions().position(latLngList[i]).title(horaFormateada).icon(BitmapDescriptorFactory.fromBitmap(bitmap)))
            }
        }
    }

    private fun convertirTimestamp(timestamp: Long): String {
        return try {
            // Crea una instancia de Date usando el timestamp en milisegundos
            val date = Date(timestamp)

            // Define el formato de salida que será HH:mm:ss
            val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            // Retorna la fecha formateada
            format.format(date)
        } catch (e: Exception) {
            "Hora no disponible" // En caso de error
        }
    }
}
