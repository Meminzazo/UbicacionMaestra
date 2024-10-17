package com.esime.ubicacionmaestra.Firstapp.ui.panic

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.esime.ubicacionmaestra.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class panicBttonActivity : AppCompatActivity() {

    private lateinit var timeTextView: TextView
    private lateinit var dateTextView: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()    // Oculta la barra de título
        setContentView(R.layout.activity_panic_btton)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val panicButton = findViewById<Button>(R.id.panicButton)
        timeTextView = findViewById(R.id.currentTime)
        dateTextView = findViewById(R.id.currentDate)


        // Iniciar la actualización de hora y fecha en tiempo real
        updateTimeAndDate()

        // Acción del botón de pánico
        panicButton.setOnClickListener {
            // Aquí puedes añadir la funcionalidad que desees cuando se pulse el botón
            Toast.makeText(this, "Pánico activado!", Toast.LENGTH_SHORT).show()
        }


    }

    private fun updateTimeAndDate() {
        // Formato para la hora en formato de 12 horas con AM/PM
        val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())

        // Formato para la fecha con día de la semana, mes en texto y año
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())

        // Handler para actualizar la fecha y hora en tiempo real
        val handler = Handler(Looper.getMainLooper())

        handler.post(object : Runnable {
            override fun run() {
                // Obtener la hora y fecha actuales
                val currentTime = Calendar.getInstance().time
                val time = timeFormat.format(currentTime)
                val date = dateFormat.format(currentTime)

                // Actualizar los TextViews
                timeTextView.text = time
                dateTextView.text = date

                // Volver a ejecutar el Runnable después de 1 segundo
                handler.postDelayed(this, 1000)
            }
        })
    }


}