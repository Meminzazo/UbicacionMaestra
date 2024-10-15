package com.esime.ubicacionmaestra.Firstapp.ui.utilities

import retrofit2.Call
import retrofit2.http.GET

interface EarthquakeApiService {

    // Método para obtener los terremotos recientes
    @GET("summary/all_day.geojson")  // Endpoint de la API de USGS para obtener terremotos de las últimas 24 horas
    fun getRecentEarthquakes(): Call<EarthquakeResponse>

    // Clase que representa la respuesta de la API
    data class EarthquakeResponse(
        val features: List<EarthquakeFeature>
    )

    // Clase que representa un terremoto
    data class EarthquakeFeature(
        val properties: EarthquakeProperties,
        val geometry: EarthquakeGeometry
    )

    // Propiedades del terremoto, como la magnitud y la ubicación
    data class EarthquakeProperties(
        val mag: Double,       // Magnitud del terremoto
        val place: String,     // Lugar del terremoto
        val time: Long         // Tiempo del terremoto (en milisegundos desde el epoch)
    )

    // Geometría del terremoto, que contiene las coordenadas (latitud y longitud)
    data class EarthquakeGeometry(
        val coordinates: List<Double> // Lista con la longitud, latitud y profundidad
    )
}
