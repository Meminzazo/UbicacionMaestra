package com.esime.ubicacionmaestra.Firstapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.esime.ubicacionmaestra.Firstapp.ui.homeActivity.HomeActivity
import com.esime.ubicacionmaestra.Firstapp.ui.homeActivity.ProviderType
import com.esime.ubicacionmaestra.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class AuthActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth   //variable de autenticacion

    // Declaracion de la base de datos
    val db = FirebaseFirestore.getInstance()

    // Datos de la base de datos (Formato)
    val user = hashMapOf(
        "Latitud" to "-",
        "Longitud" to "-",
        "Numero" to "-",
        "Nombre" to "-",
        "Apellido" to "-",
        "GrupoID" to "-"
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

        registrarButtom.setOnClickListener() {  // al hacer click en el boton de registrar hace lo que esta dentro
            val email = emailEditText.text.toString()
            crearcolletion(email)
            if(emailEditText.text.isNotEmpty() && passEditText.text.isNotEmpty())           //comprueba si los campos son vacios
            {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailEditText.text.toString(),        //servicio de firebase autentication
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

        // hacer lo mismo con el boton de login
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

    fun crearcolletion(email: String){

        // Add a new document with a generated ID

        db.collection("users").document(email)
            .set(user)
            .addOnSuccessListener { Log.d(HomeActivity.TAG, "Documento creado exitosamente") }
            .addOnFailureListener { e -> Log.w(HomeActivity.TAG, "Error al crear el documento", e) }
    }

}