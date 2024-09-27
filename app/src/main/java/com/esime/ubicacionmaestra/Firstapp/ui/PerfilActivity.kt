package com.esime.ubicacionmaestra.Firstapp.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esime.ubicacionmaestra.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import kotlin.random.Random


class PerfilActivity : AppCompatActivity() {

    // Declaracion de las variables para los elementos de la interfaz
    private lateinit var saveButton: Button
    private lateinit var nombresEditText: EditText
    private lateinit var apellidosEditText: EditText
    private lateinit var telefonoEditText: EditText

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var fileUri: Uri

    private var uid: String? = null
    private var emailCon: String? = null
    private var GrupoIDPublic: String? = null

    // Declaracion del objeto para la base de datos
    private val db = FirebaseFirestore.getInstance()

    // Constantes para el tag de la actividad pra poder usar el LOGCAT
    companion object {
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

    // Funcion que inicia al entrar a la activity
    @SuppressLint("MissingInflatedId", "ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        supportActionBar?.hide()    // Oculta la barra de título

        // Inicializar los elementos de la interfaz con los IDs correspondientes
        saveButton = findViewById(R.id.GuardarDatosButton)
        nombresEditText = findViewById(R.id.Nombres)
        apellidosEditText = findViewById(R.id.Apellidos)
        telefonoEditText = findViewById(R.id.NumTelefono)

        // Inicializar los elementos de la interfaz con los IDs correspondientes
        val JoinGrupoButton = findViewById<Button>(R.id.JoinGrupoButton)
        val CreateGrupoButton = findViewById<Button>(R.id.CreateGrupoButton)
        val SalirGrupoButton = findViewById<Button>(R.id.SalirGrupoButton)
        val IDGrupo = findViewById<TextView>(R.id.IDGrupo)
        val PertenecerGrupo = findViewById<TextView>(R.id.textGrupo)

        // Aparecen muchas veces las declaraciones de los botones y asi pero es para que se muestren en los campos y puedas modificarlos directamente

        val bundle = intent.extras                              // recuperar parametros
        emailCon = bundle?.getString("Email1")              //parametro del home layut "como nombramos al edit text"
        uid = bundle?.getString("UID")

        IDGrupo.transformationMethod = PasswordTransformationMethod.getInstance()   // Para que el ID del grupo este oculto

        // Obtener el documento del usuario en la base de datos y poder usar los datos
        val docRef3 = db.collection("users").document(emailCon!!)
        docRef3.get().addOnSuccessListener { document ->    // Si se encuentra el documento se ejecuta el codigo dentro
            val GrupoID = document.getString("GrupoID") // Gurada el ID del usuario actual
            IDGrupo.text = GrupoID
            if (GrupoID != "-") {
                GrupoIDPublic = GrupoID
                PertenecerGrupo.text = "Pertenece al grupo" // Si tiene un ID de grupo cambia el texto a que si Pertence a un grupo
            }
            else{
                PertenecerGrupo.text = "No perteneces a un grupo"    // Si no tiene un ID de grupo cambia el texto a que no pertenece a un grupo
            }
        }

        // Copiar el ID del grupo al portapapeles
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

        // Botones para crear un grupo
        CreateGrupoButton.setOnClickListener {
            val docRef = db.collection("users").document(emailCon!!)     // Se obtiene el documento del usuario actual
            docRef.get().addOnSuccessListener { document -> // Si se encuentra el documento se ejecuta el codigo dentro
                val GrupoID = document.getString("GrupoID") // Guarda el ID del grupo del usuario actual
                if (GrupoID == "-") {   // Si el ID del grupo del usuario actual es igual a "-" se la asigna un nuevo ID de grupo
                    val randomKey = generateRandomKey() // Genera un nuevo ID de grupo aleatorio
                    db.collection("users").document(emailCon!!)
                        .update("GrupoID", randomKey)   // Actualiza el ID del grupo del usuario actual en la base de datos
                    IDGrupo.text = randomKey
                    PertenecerGrupo.text = "Pertenece al grupo"  // Cambia el texto del botón para indicar que el usuario pertenece al grupo

                    // Datos de la base de datos (Formato)
                    val groupData = hashMapOf(
                        "email" to emailCon
                    )
                    GrupoIDPublic = randomKey
                    db.collection("grupos").document(randomKey).set(groupData)
                }
                else{
                    Toast.makeText(this, "Ya perteneces a un grupo", Toast.LENGTH_SHORT).show()
                }
            }
        }

        JoinGrupoButton.setOnClickListener {    // Boton para unirse a un grupo
            val docRef = db.collection("users").document(emailCon!!)    // Se obtiene el documento del usuario actual
            docRef.get().addOnSuccessListener { document -> // Si se encuentra el documento se ejecuta el codigo dentro
                val GrupoID = document.getString("GrupoID") // Guarda el ID del grupo del usuario actual
                if (GrupoID == "-") {   // Si el ID del grupo del usuario actual es igual a "-" desplegara el menu emergente para ingresar el ID del grupo
                    mostrarMenuEmergente { ID ->
                        db.collection("users").document(emailCon!!).update("GrupoID", ID) // Actualiza el ID del grupo que ingreso en el menu emergente en la base de datos
                        PertenecerGrupo.text = "Pertenece al grupo" // Cambia el texto del botón para indicar que el usuario pertenece al grupo
                        IDGrupo.text = ID

                        val grupoRef = db.collection("grupos").document(ID)

                        GrupoIDPublic = ID

                        grupoRef.get().addOnSuccessListener {grupoDocument ->
                            if (grupoDocument.exists()){

                                val emailCount = grupoDocument.data?.size ?: 0

                                val newEmailField = "email${emailCount + 1}"

                                grupoRef.update(newEmailField, emailCon)
                            }
                        }
                    }

                }
                else{
                    Toast.makeText(this, "Ya perteneces a un grupo, si quieres cambiar a otro grupo por favor salte del grupo actual", Toast.LENGTH_SHORT).show()
                }
            }
        }

        SalirGrupoButton.setOnClickListener {   // Boton para salir de un grupo
            val docRef = db.collection("users").document(emailCon!!)    // Se obtiene el documento del usuario actual
            docRef.get().addOnSuccessListener { document -> // Si se encuentra el documento se ejecuta el codigo dentro
                val GrupoID = document.getString("GrupoID") // Guarda el ID del grupo del usuario actual
                if (GrupoID != "-") {   // Si el ID del grupo del usuario actual es diferente de "-" ejecuta el codigo dentro

                    val grupoRef = db.collection("grupos").document(GrupoID!!)
                    grupoRef.get().addOnSuccessListener {grupoDocument ->
                        if (grupoDocument.exists()){
                            val emailsMap = grupoDocument.data
                            var emailToRemove: String? = null


                            for ((key, value) in emailsMap!!) {
                                if (value == emailCon) {
                                    emailToRemove = key
                                    break
                                }
                            }

                            if (emailToRemove != null) {
                                grupoRef.update(emailToRemove, null).addOnSuccessListener {
                                    Toast.makeText(this, "Has salido del grupo", Toast.LENGTH_SHORT).show()
                                }.addOnFailureListener {
                                    Toast.makeText(this, "Error al salir del grupo", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    db.collection("users").document(emailCon!!).update("GrupoID", "-")    // Actualiza el ID del grupo a "-" (valor default que usamos para el ID del grupo) del usuario actual en la base de datos
                    PertenecerGrupo.text = "No perteneces a un grupo"   // Cambia el texto del botón para indicar que el usuario no pertenece al grupo
                    IDGrupo.text = "-"  // Cambia el ID del grupo del usuario actual a "-" (valor default que usamos para el ID del grupo)
                }
                else{
                    Toast.makeText(this, "No perteneces a un grupo", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Obtener los datos del usuario de la base de datos y mostrarlos en los campos
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

        saveButton.setOnClickListener {// Boton para guardar los datos del usuario
            uploadProfileData(emailCon!!) // Funcion para guardar los datos del usuario en la base de datos
        }



// En tu método onCreate
        val btnSeleccionarFoto: Button = findViewById(R.id.btnSeleccionarFoto)
        btnSeleccionarFoto.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            fileUri = data.data!!
            val imageView: ImageView = findViewById(R.id.imageViewFoto)
            imageView.setImageURI(fileUri)
            subirFoto(fileUri)
        }
    }

    private fun subirFoto(fileUri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        Log.d(TAG, "Subiendo foto para el usuario $userId")

        val storageRef = FirebaseStorage.getInstance().reference.child("fotos/${userId}.jpg")

        // Redimensiona el bitmap antes de subir
        val bitmap = redimensionarBitmap(fileUri)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos) // Puedes ajustar el 80 para cambiar la calidad

        val data = baos.toByteArray()
        val uploadTask = storageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val photoUrl = uri.toString()
                savePhotoUrlToFirestore(photoUrl)
            }
        }.addOnFailureListener {
            Log.e(TAG, "Error al subir la foto: ${it.message}")
        }
    }

    private fun redimensionarBitmap(uri: Uri): Bitmap {
        val originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))

        // Define el tamaño máximo que deseas
        val maxWidth = 150 // Ancho máximo
        val maxHeight = 150 // Alto máximo

        val width = originalBitmap.width
        val height = originalBitmap.height
        val scale = Math.min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)

        // Calcula el nuevo tamaño
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        // Crea el nuevo bitmap redimensionado
        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
    }

    private fun savePhotoUrlToFirestore(photoUrl: String) {
        val firestore = FirebaseFirestore.getInstance()
        val data = hashMapOf("photoUrl" to photoUrl)

        firestore.collection("users").document(emailCon!!)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "URL de la foto guardada exitosamente")
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error al guardar la URL: $e")
            }
    }

    // Funcion para mostrar el menu emergente para ingresar el ID del grupo
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

    // Funcion para validar el formato del ID del grupo
    private fun isValidKeyFormat(key: String): Boolean {
        // Expresión regular para el formato xxxx-xxxx-xxxx
        val regex = Regex("^[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$")
        return key.matches(regex)
    }

    // Funcion para guardar los datos del usuario en la base de datos
    private fun uploadProfileData(emailCon: String) {

            val email = emailCon
            val nombres = nombresEditText.text.toString()
            val apellidos = apellidosEditText.text.toString()
            val telefono = telefonoEditText.text.toString()

            val userData = hashMapOf(   // Formato para la base de datos
                "Nombres" to nombres,
                "Apellidos" to apellidos,
                "Telefono" to telefono,
                "GrupoID" to "$GrupoIDPublic"
            )

            // Conexion con la base de datos para guardar los datos del usuario
            db.collection("users").document(email)
                .update(userData as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d(TAG, "Datos guardados exitosamente")
                    Toast.makeText(this, "Datos guardados exitosamente", Toast.LENGTH_SHORT).show()
                    onBackPressed()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error al guardar datos", e)
                    Toast.makeText(this, "Error al guardar datos. Revise su conexión a internet y vuelva a intentarlo.", Toast.LENGTH_SHORT).show()
                }
            }
}

