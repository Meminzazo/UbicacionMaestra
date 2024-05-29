package com.esime.ubicacionmaestra.Firstapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.esime.ubicacionmaestra.R


class MenuPrincipalActivity : AppCompatActivity() {

    // TAG para el log
    val TAG = "MenuPrincipalActivity"

    // NAda relevante
    companion object {
        const val REQUEST_CODE_LOCATION = 0
        const val PREFS_NAME = "SwitchPrefs"
        const val SWITCH_STATE = "switch_state"
    }
    // Funcion cuando inicia la activity
    @SuppressLint("MissingInflatedId", "LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        supportActionBar?.hide()    // Oculta la barra de título

        setContentView(R.layout.activity_menu_principal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener el email del intent
        val bundle = intent.extras
        val email = bundle?.getString("Email1")

        // Definimos los botones para navegar a otras activitys
        val consultButton = findViewById<Button>(R.id.consultButton)
        val saveUbi = findViewById<Button>(R.id.saveUbi)
        val viewLocationsButton = findViewById<Button>(R.id.viewsLocationsButton)

        consultButton.setOnClickListener{   // Cuando se hace click en el boton entra a la ConsultAppR
            Log.d("MenuPrincipalActivity", "to ConsultAppR Email: $email")
            val intent1 = Intent (this, ConsultAppR::class.java).apply {
                putExtra("Email", email)    // Pasamos el email al intent
            }
            startActivity(intent1)  // Lanzamos la activity
        }

        saveUbi.setOnClickListener{ // Cuando se hace click en el boton entra a la SaveUbicacionReal
            Log.d("MenuPrincipalActivity", "to SaveUbicacionReal Email: $email")
            val intent = Intent (this, SaveUbicacionReal::class.java).apply{
                putExtra("Email", email)    // Pasamos el email al intent
            }
            startActivity(intent)   // Lanzamos la activity
        }

        viewLocationsButton.setOnClickListener {    // Cuando se hace click en el boton entra a la ViewLocations
            val intent = Intent(this, ViewLocationsActivity::class.java).apply{
                putExtra("Email", email)    // Pasamos el email al intent
            }
            startActivity(intent)   // Lanzamos la activity
        }
    }
}