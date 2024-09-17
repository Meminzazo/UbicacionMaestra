package com.esime.ubicacionmaestra.Firstapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.esime.ubicacionmaestra.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore




class AuthActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth   //variable de autenticacion

    // Declaracion de la base de datos
    val db = FirebaseFirestore.getInstance()
    private lateinit var database: DatabaseReference

    companion object {
        const val TAG = "AuthActivity" // Definimos la variable TAG aqui
    }
    // Datos de la base de datos (Formato)
    val user = hashMapOf(
        "ID" to "-",
        "Latitud" to "-",
        "Longitud" to "-",
        "Numero" to "-",
        "Nombre" to "-",
        "Apellido" to "-",
        "GrupoID" to "-",
    )

    data class UserUbi(
        val latitud: String? = "-",
        val longitud: String? = "-",
    )

    // Clase para manejar los datos de la geovalla
    data class GeofenceD(
        val name: String = "-",
        val latitude: String = "-",
        val longitude: String = "-",
        val radius: String  = "-",
        val transitionTypes: String = "-"
    )

    // Funcion que se inicia al entrar a la activity
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()    // oculta la barra de titulo

        setup() // llama a la funcion setup que inicia la magia
    }

    // Funcion que hace la magia
    private fun setup(){

        // Declaracion de lo botones de la interfaz
        val registrarButtom = findViewById<Button>(R.id.registrarButton)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passEditText = findViewById<EditText>(R.id.passEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)

        auth = FirebaseAuth.getInstance()

        ///////// implementacion de la autenticacion
        auth = FirebaseAuth.getInstance()   //creamos una autenticacion
        val currentUser = auth.currentUser
        if (currentUser != null) {
            //si el usuario esta autenticasdo, redirige a home
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) //flag para definir la actividad actual y limpiar la anterior
            startActivity(intent)   // inicia la activity
            finish()  //finaliza la pila de actividad anterior
        }
        else{
            //no hace nada y pasa a las demas actividades
        }


        // ACCIONES AL PULSAR LE BOTON DE REGISTRARSE
        registrarButtom.setOnClickListener() {  // al hacer click en el boton de registrar hace lo que esta dentro
            val email = emailEditText.text.toString()
                if (emailEditText.text.isNotEmpty() && passEditText.text.isNotEmpty()){           //comprueba si los campos son vacios
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                        emailEditText.text.toString(),        //servicio de firebase autentication
                        passEditText.text.toString()
                    ).addOnCompleteListener(){    //notifica si el registro a sido satisfactorio
                        if (it.isSuccessful){ //si la operacion se completa correctamente ...
                            val UID = getUserId()

                            database = FirebaseDatabase.getInstance().reference

                            Log.d(TAG, "UID: $UID")

                            database.child("users").child(UID!!).setValue(UserUbi())

                            crearcolletion(email, UID)  // solo crea a base de datos si los edit text no estan vacios

                            showHome(
                                it.result?.user?.email ?: "",
                                ProviderType.BASIC
                            )   //en caso de no existir email manda un vacio, si no da error
                        } else  //alerta de que ha pasado algo si no ...
                        {
                            showAlert()
                        }
                    }
                }
            // en caso de que los edit text esten vacios agrega esta alerta

            else {
                    Toast.makeText(this, "Porfavor Ingrese Datos", Toast.LENGTH_SHORT)
                        .show()    // mensaje en caso de estar vacio
                }
        }




        // ACCIONES AL PULSAR LE BOTON DE INGRESAR
       loginButton.setOnClickListener()
        {
            if(emailEditText.text.isNotEmpty() && passEditText.text.isNotEmpty())           //comprueba si los campos son vacios
            {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEditText.text.toString(),        //ahora ingresa
                    passEditText.text.toString()).addOnCompleteListener()    //notifica si el registro a sido satisfactorio
                {
                    if (it.isSuccessful) //si la operacion se completa correctamente ...
                    {
                        showHome(it.result?.user?.email?:"", ProviderType.BASIC)   //en caso de no existir email manda un vacio, si no da error
                    }
                    else  //alerta de que ha pasado algo si no ...
                    {
                        showAlert()
                    }
                }
            }
        }







    }




    // FUNCIONES AUXIALIARES EN CASO DE CUALQUIER ACCION ANTERIOR
    private fun showAlert()
    {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("¡Error O_O!")
        builder.setMessage("Se ha producido un erro de autenticacion al usuario X_X")
        builder.setPositiveButton("aceptar",null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome (email: String, provider: ProviderType)
    {
        val homeIntent = Intent (this, HomeActivity::class.java).apply {
            putExtra("Email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }

    fun crearcolletion(email: String, UID: String){
        db.collection("users").document(email)
            .set(user)
            .addOnSuccessListener { Log.d(HomeActivity.TAG, "Documento creado exitosamente") }
            .addOnFailureListener { e -> Log.w(HomeActivity.TAG, "Error al crear el documento", e) }

        db.collection("users").document(email).update("ID", UID)

        database = FirebaseDatabase.getInstance().reference

        database.child("users").child(UID).setValue(UserUbi())

        database.child("users").child(UID).child("Geovallas").setValue(GeofenceD())
    }

    fun getUserId(): String? {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid // El UID generado es único para cada usuario
    }

}