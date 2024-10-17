package com.esime.ubicacionmaestra.Firstapp.ui.config

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.esime.ubicacionmaestra.Firstapp.ui.home.MenuPrincipalActivity
import com.esime.ubicacionmaestra.Firstapp.ui.saveLocation.SaveUbicacionReal
import com.esime.ubicacionmaestra.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.switchmaterial.SwitchMaterial

class preferenceUserActivity : AppCompatActivity() {

    private lateinit var spinnerMapType: Spinner
    private lateinit var switchTraffic: SwitchMaterial
    private lateinit var switchDarkMode: SwitchMaterial
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferences = getSharedPreferences("MapSettings", Context.MODE_PRIVATE)

        spinnerMapType = findViewById(R.id.spinner_map_type)
        switchTraffic = findViewById(R.id.switch_traffic)
        switchDarkMode = findViewById(R.id.switch_dark_mode)

        // Configurar el spinner para seleccionar el tipo de mapa
        ArrayAdapter.createFromResource(
            this,
            R.array.map_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerMapType.adapter = adapter
        }


        // Cargar las preferencias guardadas
        val mapType = sharedPreferences.getInt("map_type", GoogleMap.MAP_TYPE_NORMAL)
        spinnerMapType.setSelection(mapType - 1) // Ajusta el índice

        val trafficEnabled = sharedPreferences.getBoolean("traffic_enabled", false)
        switchTraffic.isChecked = trafficEnabled

        // Guardar las preferencias cuando cambien
        spinnerMapType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val mapType = position + 1 // El índice comienza en 0
                saveMapType(mapType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        switchTraffic.setOnCheckedChangeListener { _, isChecked ->
            saveTrafficEnabled(isChecked)
        }
    }
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val intent = Intent(this, MenuPrincipalActivity::class.java)
        startActivity(intent)
        finish() // Cierra la Activity
    }
    private fun saveMapType(mapType: Int) {
        with(sharedPreferences.edit()) {
            putInt("map_type", mapType)
            apply()
        }
    }

    private fun saveTrafficEnabled(enabled: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("traffic_enabled", enabled)
            apply()
        }
    }
}