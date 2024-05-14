package com.esime.ubicacionmaestra.Firstapp.ui

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        setContentView(R.layout.activity_menu_principal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val bundle = intent.extras
        val email = bundle?.getString("Email1")

        // consultar opciones de ubicacion
       val consultButton = findViewById<Button>(R.id.consultButton)
        consultButton.setOnClickListener{
            val intent1 = Intent (this, ConsultAppR::class.java)
            startActivity(intent1)
        }
       val saveUbi = findViewById<Button>(R.id.saveUbi)
        saveUbi.setOnClickListener{
            val intent = Intent (this, SaveUbicacionReal::class.java).apply{
                putExtra("Email", email)
            }
            Log.d("MenuPrincipalActivity", "Email: $email")
            startActivity(intent)
        }


    }





}