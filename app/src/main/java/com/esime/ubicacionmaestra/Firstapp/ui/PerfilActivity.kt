package com.esime.ubicacionmaestra.Firstapp.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esime.ubicacionmaestra.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random


class PerfilActivity : AppCompatActivity() {

    private lateinit var saveButton: Button
    private lateinit var nombresEditText: EditText
    private lateinit var apellidosEditText: EditText
    private lateinit var telefonoEditText: EditText

    private val db = FirebaseFirestore.getInstance()

    companion object{
        const val TAG = "PerfilActivity"
    }

    // Función para generar una clave aleatoria con el formato xxxx-xxxx-xxxx
    private fun generateRandomKey(): String {
        val random = Random.Default
        return buildString {
            repeat(4) {
                append(random.nextChar())
            }
            append('-')
            repeat(4) {
                append(random.nextChar())
            }
            append('-')
            repeat(4) {
                append(random.nextChar())
            }
        }
    }
    // Función de extensión para generar un carácter aleatorio entre 'A' y 'Z' o entre '0' y '9'
    fun Random.nextChar(): Char {
        val chars = ('A'..'Z') + ('0'..'9')
        return chars.random(this)
    }

    @SuppressLint("MissingInflatedId", "ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        supportActionBar?.hide()

        saveButton = findViewById(R.id.GuardarDatosButton)
        nombresEditText = findViewById(R.id.Nombres)
        apellidosEditText = findViewById(R.id.Apellidos)
        telefonoEditText = findViewById(R.id.NumTelefono)

        val JoinGrupoButton = findViewById<Button>(R.id.JoinGrupoButton)
        val CreateGrupoButton = findViewById<Button>(R.id.CreateGrupoButton)
        val SalirGrupoButton = findViewById<Button>(R.id.SalirGrupoButton)
        val IDGrupo = findViewById<TextView>(R.id.IDGrupo)
        val PertenecerGrupo = findViewById<TextView>(R.id.textGrupo)

        val bundle = intent.extras                              // recuperar parametros
        val emailCon = bundle?.getString("Email1")              //parametro del home layut "como nombramos al edit text"

        IDGrupo.transformationMethod = PasswordTransformationMethod.getInstance()


        val docRef3 = db.collection("users").document(emailCon!!)
        docRef3.get().addOnSuccessListener { document ->
            val GrupoID = document.getString("GrupoID")
            IDGrupo.text = GrupoID
            if (GrupoID != "-") {
                PertenecerGrupo.text = "Pertenece al grupo"
            }
            else{
                PertenecerGrupo.text = "No perteneces a un grupo"
            }
        }
        IDGrupo.setOnClickListener {
            // Mostrar el contenido del TextView
            IDGrupo.transformationMethod = null
            val ID = IDGrupo.text.toString()
            // Copiar el contenido al portapapeles
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("GrupoID", ID)
            clipboard.setPrimaryClip(clip)

            // Notificar al usuario que el contenido se ha copiado
            Toast.makeText(this, "Grupo ID copiado al portapapeles", Toast.LENGTH_SHORT).show()
        }

        CreateGrupoButton.setOnClickListener {
            val docRef = db.collection("users").document(emailCon!!)
            docRef.get().addOnSuccessListener { document ->
                val GrupoID = document.getString("GrupoID")
                if (GrupoID == "-") {
                    val randomKey = generateRandomKey()
                    db.collection("users").document(emailCon)
                        .update("GrupoID", randomKey)
                    IDGrupo.text = randomKey
                    PertenecerGrupo.text = "Pertenece al grupo"
                }
                else{
                    Toast.makeText(this, "Ya perteneces a un grupo", Toast.LENGTH_SHORT).show()
                }
            }
        }

        JoinGrupoButton.setOnClickListener {
            val docRef = db.collection("users").document(emailCon!!)
            docRef.get().addOnSuccessListener { document ->
                val GrupoID = document.getString("GrupoID")
                if (GrupoID == "-") {
                    mostrarMenuEmergente { ID ->
                        Log.d(TAG, "Grupo ID: $ID")
                        db.collection("users").document(emailCon).update("GrupoID", ID)
                        PertenecerGrupo.text = "Pertenece al grupo"
                        IDGrupo.text = ID
                    }

                }
                else{
                    Toast.makeText(this, "Ya perteneces a un grupo, si quieres cambiar a otro grupo por favor salte del grupo actual", Toast.LENGTH_SHORT).show()
                }
            }
        }

        SalirGrupoButton.setOnClickListener {
            val docRef = db.collection("users").document(emailCon!!)
            docRef.get().addOnSuccessListener { document ->
                val GrupoID = document.getString("GrupoID")
                if (GrupoID != "-") {
                    db.collection("users").document(emailCon).update("GrupoID", "-")
                    PertenecerGrupo.text = "No perteneces a un grupo"
                    IDGrupo.text = "-"
                }
                else{
                    Toast.makeText(this, "No perteneces a un grupo", Toast.LENGTH_SHORT).show()
                }
            }
        }


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

    private fun mostrarMenuEmergente(onDataEntered: (String) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ingrese el ID del grupo")

        // Crear un EditText para que el usuario ingrese el dato
        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Guardar") { _, _ ->
            // Obtener el dato ingresado por el usuario y llamarlo a través del callback
            val datoIngresado = input.text.toString()

            if (isValidKeyFormat(datoIngresado)) {
                // Si el formato es válido, llamar al callback con el dato ingresado
                onDataEntered(datoIngresado)
            } else {
                // Si el formato no es válido, mostrar un mensaje de error
                Toast.makeText(this, "Formato de clave inválido. El ID del Grupo debe tener el formato xxxx-xxxx-xxxx", Toast.LENGTH_LONG).show()
                // Volver a mostrar el menú emergente
                mostrarMenuEmergente(onDataEntered)
            }

            Log.d(TAG, "Dato ingresado: $datoIngresado")
        }

        builder.setCancelable(true) // Para evitar que se cierre el diálogo al tocar fuera de él

        val dialog = builder.create()
        dialog.show()
    }

    private fun isValidKeyFormat(key: String): Boolean {
        // Expresión regular para el formato xxxx-xxxx-xxxx
        val regex = Regex("^[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$")
        return key.matches(regex)
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

