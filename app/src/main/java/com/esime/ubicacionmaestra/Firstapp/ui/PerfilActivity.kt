package com.esime.ubicacionmaestra.Firstapp.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esime.ubicacionmaestra.R
import com.google.firebase.firestore.FirebaseFirestore

class PerfilActivity : AppCompatActivity() {

    private lateinit var saveButton: Button
    private lateinit var nombresEditText: EditText
    private lateinit var apellidosEditText: EditText
    private lateinit var telefonoEditText: EditText

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        supportActionBar?.hide()

        saveButton = findViewById(R.id.GuardarDatosButton)
        nombresEditText = findViewById(R.id.Nombres)
        apellidosEditText = findViewById(R.id.Apellidos)
        telefonoEditText = findViewById(R.id.NumTelefono)

        val bundle = intent.extras                              // recuperar parametros
        val emailCon = bundle?.getString("Email1")              //parametro del home layut "como nombramos al edit text"

        val docRef = db.collection("users").document(emailCon!!)
        docRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val nombres = document.getString("Nombres")
                val apellidos = document.getString("Apellidos")
                val telefono = document.getString("Telefono")
                nombresEditText.setText(nombres)
                apellidosEditText.setText(apellidos)
                telefonoEditText.setText(telefono)
            } else {
                Log.d("PerfilActivity", "No se encontró el documento")
            }
        }

        saveButton.setOnClickListener {
            uploadProfileData(emailCon)
        }
    }

    private fun uploadProfileData(emailCon: String) {

            val email = emailCon
            val nombres = nombresEditText.text.toString()
            val apellidos = apellidosEditText.text.toString()
            val telefono = telefonoEditText.text.toString()

            val userData = hashMapOf(
                "Nombres" to nombres,
                "Apellidos" to apellidos,
                "Telefono" to telefono,
                "Latitud" to "-",
                "Longitud" to "-",
                "GrupoID" to "-"
            )


            db.collection("users").document(email)
                .set(userData)
                .addOnSuccessListener {
                    Log.d("PerfilActivity", "Datos guardados exitosamente")
                    Toast.makeText(this, "Datos guardados exitosamente", Toast.LENGTH_SHORT).show()
                    onBackPressed()
                }
                .addOnFailureListener { e ->
                    Log.w("PerfilActivity", "Error al guardar datos", e)
                    Toast.makeText(this, "Error al guardar datos. Revise su conexión a internet y vuelva a intentarlo.", Toast.LENGTH_SHORT).show()
                }
            }
}

