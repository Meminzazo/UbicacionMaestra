package com.esime.ubicacionmaestra.Firstapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.esime.ubicacionmaestra.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

enum class ProviderType()
{
    BASIC
}
class HomeActivity : AppCompatActivity() {
   /* val bundle1 = intent.extras
    val email2 = bundle1?.getString("Email") */
    private lateinit var auth: FirebaseAuth

    val db = FirebaseFirestore.getInstance()
    val user = hashMapOf(
        "Latitud" to "-",
        "Longitud" to "-",
        "Numero" to "-",
        "Nombre" to "-",
        "Apellido" to "-",
        "GrupoID" to "-"
    )
    companion object {
        const val TAG = "HomeActivity" // Definimos la variable TAG aqui
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()



        val sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE)
        val emailP = sharedPreferences.getString("emailPersistente", "Guest")?:"Guest" // "Guest" es el valor por defecto



        // Setup
        val bundle = intent.extras                              // recuperar parametros
        val email = bundle?.getString("Email")  ?: emailP            //parametro del home layut "como nombramos al edit text"
        val provider = bundle?.getString("provider") ?:""
        setup(email,provider)  //en caso de no existir se manda algo vacio
    }

    private fun setup(email: String, provider: String)
    {
        title = "inicio"
        val emailTextView = findViewById<TextView>(R.id.emailTextView)
        val providerTextView= findViewById<TextView>(R.id.providerTextView)
        val logoutBottom = findViewById<Button>(R.id.logoutButtom)
        val goButton = findViewById<Button>(R.id.goButton)

        val sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("emailPersistente", email)
        editor.apply()

        val perfilButton = findViewById<Button>(R.id.perfilButton)

        emailTextView.text = email
        providerTextView.text = provider

        logoutBottom.setOnClickListener()
        {
            auth.signOut()
            val intent = Intent(this, AuthActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) //flag para definir la actividad actual y limpiar la anterior
            startActivity(intent)
            finish() //mata la actividad anterior
        }

        perfilButton.setOnClickListener{
            val intent = Intent(this, PerfilActivity::class.java).apply {
                putExtra("Email1", email)
                Log.d("HomeActivity", "Email: $email")
            }
            startActivity(intent)
        }


        // lanza la aplicacion
        goButton.setOnClickListener{
            val intent = Intent(this, MenuPrincipalActivity::class.java).apply {
                putExtra("Email1", email)
                Log.d("HomeActivity", "Email: $email")
            }
            startActivity(intent)
        }

            // Nota, el email no se manda y solo se ve el zzz pipipipi pero ya enlace las pantallas solo falta un boton que haga salir la app
    }

}