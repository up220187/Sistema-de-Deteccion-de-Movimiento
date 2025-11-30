package com.example.sistemamovimiento

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/**
 * 1. MODELO DE DATOS: Define cómo se ve el mensaje que esperamos del Backend.
 *
 * NOTA IMPORTANTE: Estamos usando una API de prueba (jsonplaceholder) que devuelve objetos
 * con campos "userId", "id", "title" y "body". Mapearemos estos campos a nuestro modelo
 * de EventMessage para que Retrofit pueda procesar la respuesta.
 */
data class EventMessage(
    @SerializedName("id")
    val eventId: Int, // ID único del evento.
    @SerializedName("title")
    val source: String, // Simula la fuente (ej. un sensor).
    @SerializedName("body")
    val payload: String // Contenido principal del evento (el mensaje).
    // Nota: El campo 'timestamp' no existe en la API de prueba, pero sería necesario en una API real.
)

/**
 * 2. INTERFAZ DE RETROFIT: Define la URL y el método de la llamada.
 */
interface EventHubApiService {
    // Usamos el endpoint 'posts' de JSONPlaceholder como simulación.
    // En tu API real de Azure, sería algo como @GET("api/latestEvents")
    @GET("posts")
    suspend fun getLatestEvents(): List<EventMessage>
}

/**
 * 3. ACTIVIDAD PRINCIPAL: Inicializa la red y hace la llamada.
 */
class MainActivity : AppCompatActivity() {

    // Componentes de la UI
    private lateinit var progressBar: ProgressBar
    private lateinit var eventDataTextView: TextView

    // URL base de la API de backend (la que sirve los datos del Event Hubs).
    // Usamos JSONPlaceholder para simular una API REST

    // REMPLAZAAAAR por el linksito de azure algo asi : https://<nombre-de-tu-app-function>.azurewebsites.net/api/GetLatestEvents?code=XXXXXXXXXXX
    private val BASE_URL = "https://jsonplaceholder.typicode.com/"

    // Cliente de Retrofit.
    private lateinit var apiService: EventHubApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar componentes de la UI
        progressBar = findViewById(R.id.progress_bar)
        eventDataTextView = findViewById(R.id.tv_event_data)

        // 1. Configuración de Retrofit
        setupRetrofit()

        // 2. Llamada para consumir los datos
        fetchEvents()
    }

    /**
     * Configura el cliente HTTP de Retrofit.
     */
    private fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Agrega el convertidor para que Retrofit pueda mapear JSON a objetos Kotlin (EventMessage)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Creamos la instancia del servicio API
        apiService = retrofit.create(EventHubApiService::class.java)
    }

    /**
     * Ejecuta la llamada a la API de forma asíncrona.
     */
    private fun fetchEvents() {
        // Muestra el indicador de carga
        progressBar.visibility = View.VISIBLE
        eventDataTextView.text = "Cargando eventos..."

        // Usamos lifecycleScope para lanzar una corrutina en el hilo de IO (Red)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // **LLAMADA A LA API**
                val events = apiService.getLatestEvents()

                // Volvemos al hilo principal para actualizar la UI
                launch(Dispatchers.Main) {
                    onSuccess(events)
                }
            } catch (e: Exception) {
                // Manejo de errores
                launch(Dispatchers.Main) {
                    onError("Error de conexión o de datos: ${e.message}")
                }
            } finally {
                // Oculta el indicador de carga
                launch(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    /**
     * Procesa los eventos recibidos exitosamente y actualiza la UI.
     */
    private fun onSuccess(events: List<EventMessage>) {
        if (events.isNotEmpty()) {
            val totalCount = events.size
            val displayText = StringBuilder()

            // Construye el texto para mostrar. Muestra solo los primeros 5 por limpieza.
            displayText.append("✅ Éxito: Se recibieron $totalCount mensajes del Event Hub (vía API).\n\n")
            displayText.append("--- Últimos 5 Eventos ---\n")

            events.take(5).forEach { event ->
                displayText.append("ID: ${event.eventId}\n")
                displayText.append("Fuente: ${event.source}\n")
                // Truncamos el cuerpo para que sea legible
                displayText.append("Mensaje: ${event.payload.substring(0, minOf(event.payload.length, 50))}...\n")
                displayText.append("--------------------------\n")
            }

            eventDataTextView.text = displayText.toString()
        } else {
            eventDataTextView.text = "Advertencia: La API del Backend no devolvió eventos."
        }
    }

    /**
     * Muestra un mensaje de error al usuario.
     */
    private fun onError(message: String) {
        eventDataTextView.text = "❌ ERROR AL OBTENER DATOS: \n$message"
    }
}