package com.esime.ubicacionmaestra.Firstapp.ui.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.esime.ubicacionmaestra.Firstapp.ui.home.MenuPrincipalActivity
import com.esime.ubicacionmaestra.Firstapp.ui.utilities.broadcasts.BatteryDarkModeReceiver
import com.esime.ubicacionmaestra.Firstapp.ui.utilities.services.EarthquakeMonitoringService
import com.esime.ubicacionmaestra.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.switchmaterial.SwitchMaterial

class preferenceUserActivity : AppCompatActivity() {

    private lateinit var spinnerMapType: Spinner
    private lateinit var switchTraffic: SwitchMaterial
    private lateinit var switchDarkMode: SwitchMaterial
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var imageMap: ImageView
    private lateinit var batteryDarkModeReceiver: BatteryDarkModeReceiver
    private lateinit var sharedPreferences1: SharedPreferences
    lateinit var sismosSwitch: SwitchMaterial

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()
        sharedPreferences = getSharedPreferences("MapSettings", Context.MODE_PRIVATE)
        sharedPreferences1 = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

        spinnerMapType = findViewById(R.id.spinner_map_type)
        switchTraffic = findViewById(R.id.switch_traffic)
        switchDarkMode = findViewById(R.id.switch_dark_mode)
        imageMap = findViewById(R.id.imageMap)
        sismosSwitch = findViewById<SwitchMaterial>(R.id.sismosSwitch)

        // Verificar el estado guardado en SharedPreferences y actualizar el Switch
        val isEarthquakeMonitoringEnabled = sharedPreferences.getBoolean("earthquake_monitoring", false)
        sismosSwitch.isChecked = isEarthquakeMonitoringEnabled

        // Configurar el listener para el cambio de estado del Switch
        sismosSwitch.setOnCheckedChangeListener { _, isChecked ->
            val intent = Intent(this, EarthquakeMonitoringService::class.java)
            if (isChecked) {
                startService(intent)
            } else {
                stopService(intent)
            }
            // Guardar la preferencia en SharedPreferences
            saveEarthquakeMonitoringPreference(isChecked)
        }

        // Iniciar o detener el servicio según la preferencia guardada al iniciar la app
        val intent = Intent(this, EarthquakeMonitoringService::class.java)
        if (isEarthquakeMonitoringEnabled) {
            startService(intent)
        } else {
            stopService(intent)
        }

        // Configurar el spinner para seleccionar el tipo de mapa
        ArrayAdapter.createFromResource(
            this,
            R.array.map_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerMapType.adapter = adapter
        }
        // Verificar el estado guardado en SharedPreferences y actualizar el Switch
        val isDarkModeEnabled = sharedPreferences1.getBoolean("dark_mode", false)
        switchDarkMode.isChecked = isDarkModeEnabled

        // Configurar el listener para el cambio de estado del Switch
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Activar el modo oscuro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                saveDarkModePreference(true)
                saveUserSetDarkMode(true) // El usuario cambió manualmente el modo
            } else {
                // Desactivar el modo oscuro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                saveDarkModePreference(false)
                saveUserSetDarkMode(true) // El usuario cambió manualmente el modo
            }
        }
        // Registrar el BroadcastReceiver para el nivel de batería (modo oscuro)
        batteryDarkModeReceiver = BatteryDarkModeReceiver()
        val batteryStatusIntentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryDarkModeReceiver, batteryStatusIntentFilter)

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
                // Cambiar la imagen del ImageView según el tipo de mapa seleccionado
                when (mapType) {
                    GoogleMap.MAP_TYPE_NORMAL -> imageMap.setImageResource(R.drawable.normal)
                    GoogleMap.MAP_TYPE_SATELLITE -> imageMap.setImageResource(R.drawable.satellite)
                    GoogleMap.MAP_TYPE_TERRAIN -> imageMap.setImageResource(R.drawable.terrain)
                    GoogleMap.MAP_TYPE_HYBRID -> imageMap.setImageResource(R.drawable.hybrid)
                    else -> imageMap.setImageResource(R.drawable.normal) // Default en caso de que no coincida
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        switchTraffic.setOnCheckedChangeListener { _, isChecked ->
            saveTrafficEnabled(isChecked)
        }
    }
    // Guardar la preferencia en SharedPreferences
    private fun saveEarthquakeMonitoringPreference(isEnabled: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("earthquake_monitoring", isEnabled)
        editor.apply()
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
    // Guardar la preferencia en SharedPreferences
    private fun saveDarkModePreference(isEnabled: Boolean) {
        val editor = sharedPreferences1.edit()
        editor.putBoolean("dark_mode", isEnabled)
        editor.apply()
    }

    // Guardar la preferencia de si el usuario configuró manualmente el modo oscuro
    private fun saveUserSetDarkMode(isUserSet: Boolean) {
        val editor = sharedPreferences1.edit()
        editor.putBoolean("user_set_dark_mode", isUserSet)
        editor.apply()
    }
}