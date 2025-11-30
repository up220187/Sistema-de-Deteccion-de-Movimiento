package com.example.sistemamovimiento

// --- Importaciones de Android ---
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.withContext

// --- Importaciones de Firebase ---
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONObject

// --- Importaciones de Corrutinas ---
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

// --- Importaciones de Retrofit y Utilidades ---
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import java.util.Date
import kotlin.random.Random


/**
 * Interfaz de Retrofit para la API de Azure Function.
 * Define el endpoint para obtener el último dato del sensor.
 */
interface EventHubApiService {
    @GET("last")
    suspend fun getLatestSensorData(): String
}

/**
 * Actividad Principal.
 */
class MainActivity : AppCompatActivity() {

    // BASE URL DE TU AZURE FUNCTION
    private val BASE_URL = "https://app-movil-cbgyewhqcshvedew.westus-01.azurewebsites.net/"
    private val POLLING_INTERVAL_MS = 10000L // Intervalo de 10 segundos para la recepción automática

    // Servicios y Componentes de Firebase/Retrofit
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var apiService: EventHubApiService
    private var firestoreListener: ListenerRegistration? = null
    private var autoFetchJob: Job? = null // Job para controlar la tarea de recepción automática

    // Bandera para saber si Firebase se inicializó correctamente
    private var isFirebaseInitialized = false

    // ID del usuario
    private var userId: String = "unknown"

    // Componentes de la UI y Adaptador (marcados con lateinit)
    private lateinit var progressBar: ProgressBar
    private lateinit var statusTextView: TextView
    private lateinit var fetchButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SensorReadingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Inicializar componentes de la UI
        progressBar = findViewById(R.id.progress_bar)
        statusTextView = findViewById(R.id.tv_status)
        fetchButton = findViewById(R.id.btn_fetch_data)
        recyclerView = findViewById(R.id.rv_sensor_history)

        // 2. Deshabilitar el botón y establecer un texto fijo, ya que el proceso es automático.
        fetchButton.text = "RECEPCIÓN AUTOMÁTICA ACTIVA"
        fetchButton.isEnabled = false

        // 3. Configurar el RecyclerView con el Adaptador
        adapter = SensorReadingAdapter(emptyList())
        recyclerView.adapter = adapter

        // 4. Inicializar Firebase App y validar el éxito
        isFirebaseInitialized = initializeFirebase()

        // 5. SOLO PROCEDER si Firebase se inicializó correctamente
        if (isFirebaseInitialized) {
            initializeServices()
            authenticateAndSetup()
        } else {
            // Si la inicialización falló, el onError ya fue llamado y la app no crashea
            Log.e("EventApp", "Configuración de Firebase fallida. Servicios de red y DB deshabilitados.")
        }
    }

    /**
     * Función CRÍTICA para inicializar la aplicación de Firebase con la configuración inyectada.
     * @return Boolean true si la inicialización fue exitosa, false en caso contrario.
     */
    private fun initializeFirebase(): Boolean {
        // La variable 'packageName' se obtiene automáticamente del contexto de Android
        val firebaseConfigJson = getEnvironmentVariable("__firebase_config")

        if (firebaseConfigJson.isBlank()) {
            onError("ERROR: La configuración de Firebase está vacía o no se encontró. Revise la variable de entorno.")
            return false // Falla
        }

        try {
            val config = JSONObject(firebaseConfigJson)

            // Construye las opciones de Firebase a partir del JSON
            val options = FirebaseOptions.Builder()
                .setApiKey(config.getString("apiKey"))
                // Usamos el nombre del paquete de Android como ApplicationId si no está en el config
                .setApplicationId(config.optString("appId", packageName))
                .setDatabaseUrl(config.optString("databaseURL", null))
                .setGcmSenderId(config.optString("messagingSenderId", null))
                .setProjectId(config.getString("projectId"))
                .setStorageBucket(config.optString("storageBucket", null))
                .build()

            // Inicializa la app de Firebase
            FirebaseApp.initializeApp(this, options)
            Log.i("FirebaseInit", "FirebaseApp inicializada con éxito.")
            return true // Éxito

        } catch (e: Exception) {
            onError("Error al parsear o inicializar Firebase: ${e.message}")
            return false // Falla
        }
    }


    /**
     * Inicializa los servicios de Firebase y Retrofit.
     */
    private fun initializeServices() {
        // Asume que initializeFirebase() fue exitoso
        try {
            auth = Firebase.auth
            db = Firebase.firestore

            // Configuración de Retrofit. Aquí se define cómo interactuar con el link de Azure.
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // Lee la respuesta como String
                .build()
            apiService = retrofit.create(EventHubApiService::class.java)
        } catch (e: Exception) {
            // Esto solo debería ocurrir si initializeFirebase tuvo un problema oculto
            onError("Error fatal al obtener instancias de Auth/Firestore: ${e.message}")
            isFirebaseInitialized = false
        }
    }

    /**
     * Realiza la autenticación en Firebase, configura el listener de Firestore,
     * e INICIA LA RECEPCIÓN AUTOMÁTICA.
     */
    private fun authenticateAndSetup() {
        if (!isFirebaseInitialized) return

        showLoading("1. Iniciando autenticación en Firebase...")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Acceso seguro a la variable __initial_auth_token
                val initialAuthToken = getEnvironmentVariable("__initial_auth_token")

                if (initialAuthToken.isNotBlank()) {
                    auth.signInWithCustomToken(initialAuthToken).await()
                } else {
                    auth.signInAnonymously().await()
                }

                userId = auth.currentUser?.uid ?: "anon_${Random.nextLong()}"

                // Cambia al hilo principal para actualizar la UI e iniciar el proceso
                withContext(Dispatchers.Main) {
                    statusTextView.text = "2. Usuario autenticado: $userId. Iniciando recepción automática..."
                    setupFirestoreListener()
                    startAutoFetching() // <-- INICIO AUTOMÁTICO
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Error de Autenticación: ${e.message}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    /**
     * Inicia el proceso de consulta automática a la Azure Function en un loop continuo.
     * Este proceso se ejecuta hasta que la actividad se destruye.
     */
    private fun startAutoFetching() {
        if (!isFirebaseInitialized) return

        // Aseguramos que sólo un Job esté corriendo
        autoFetchJob?.cancel()

        autoFetchJob = lifecycleScope.launch(Dispatchers.IO) {
            showLoading("Recepción automática iniciada (cada ${POLLING_INTERVAL_MS / 1000}s)...")

            while (isActive) { // Mientras el Job esté activo
                fetchAndSaveData()
                delay(POLLING_INTERVAL_MS) // Espera el intervalo definido (10 segundos)
            }
        }
    }

    /**
     * Llama a la Azure Function (el link) y guarda el resultado en Firestore.
     * El link se usa a través de la interfaz Retrofit.
     */
    private suspend fun fetchAndSaveData() {
        if (!isFirebaseInitialized) return

        withContext(Dispatchers.Main) {
            statusTextView.text = "Obteniendo datos de Azure Function..."
        }

        try {
            // El link completo es: BASE_URL + "last" -> https://app-movil-cbgyewhqcshvedew.westus-01.azurewebsites.net/last
            val rawData = apiService.getLatestSensorData()
            saveDataToFirestore(rawData)

        } catch (e: Exception) {
            // Si falla, cancelamos el Job y notificamos el error.
            withContext(Dispatchers.Main) {
                autoFetchJob?.cancel() // Cancelamos el loop automático en caso de fallo de red/función.
                autoFetchJob = null
                onError("Error de Red/Función. Deteniendo recepción: ${e.message}")
                fetchButton.text = "RECEPCIÓN DETENIDA POR ERROR"
            }
        }
    }

    /**
     * Guarda la cadena de datos recibida en Firebase Firestore.
     */
    private suspend fun saveDataToFirestore(rawData: String) {
        if (!isFirebaseInitialized || rawData.isBlank() || userId == "unknown") {
            withContext(Dispatchers.Main) {
                onError("Advertencia: No hay datos o la base de datos no está lista.")
            }
            return
        }

        // 1. Preparar el documento
        val sensorDataMap = hashMapOf(
            "timestamp" to Date().time,
            "raw_payload" to rawData,
            "received_by_user" to userId
        )

        // 2. Obtiene el ID de la app de forma segura
        val appId = getEnvironmentVariable("__app_id", "default-app-id")

        // 3. Construir la ruta de la colección privada
        val collectionPath = "artifacts/$appId/users/$userId/sensor_readings"

        try {
            // 4. Escribir el documento en la colección
            db.collection(collectionPath)
                .add(sensorDataMap)
                .await()

            withContext(Dispatchers.Main) {
                // Esta línea se actualizará inmediatamente por el Listener si fue exitosa.
                // Mantenemos el estado de la UI como 'Esperando'
                statusTextView.text = "✅ Dato guardado. Esperando próxima lectura..."
                Log.i("EventApp", "Dato guardado y esperando la siguiente lectura...")
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Error al guardar en Firestore: ${e.message}")
            }
        }
    }

    /**
     * Configura la escucha en tiempo real de los datos de Firestore (Listener).
     */
    private fun setupFirestoreListener() {
        if (!isFirebaseInitialized || userId == "unknown") return

        // Obtiene el ID de la app de forma segura
        val appId = getEnvironmentVariable("__app_id", "default-app-id")

        val collectionPath = "artifacts/$appId/users/$userId/sensor_readings"

        val readingsCollection = db.collection(collectionPath)

        // Añade el listener de Firestore (onSnapshot)
        firestoreListener = readingsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, e ->

                if (e != null) {
                    Log.w("FirestoreListener", "Error al escuchar cambios: $e")
                    return@addSnapshotListener
                }

                val newReadings = mutableListOf<SensorReading>()
                if (snapshot != null) {
                    for (doc in snapshot.documents) {
                        // Mapeo del documento a nuestro modelo de datos (SensorReading)
                        val reading = SensorReading(
                            id = doc.id,
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            raw_payload = doc.getString("raw_payload") ?: "N/A",
                            received_by_user = doc.getString("received_by_user") ?: ""
                        )
                        newReadings.add(reading)
                    }
                }

                // Actualiza el RecyclerView
                adapter.updateReadings(newReadings)

                // Actualiza el estado de la lectura si no está en medio de una operación.
                if (newReadings.isNotEmpty() && autoFetchJob?.isActive == true) {
                    // El estado se mantiene en 'Esperando próxima lectura...'
                } else if (newReadings.isNotEmpty()) {
                    statusTextView.text = "✅ Historial en tiempo real (${newReadings.size} lecturas)."
                }
            }
    }

    /**
     * FUNCIÓN UTILITARIA CLAVE: Accede de forma segura a las variables inyectadas del entorno.
     */
    private fun getEnvironmentVariable(name: String, defaultValue: String = ""): String {
        return try {
            val fullClassName = "${packageName}.MainActivity"
            val field = Class.forName(fullClassName).getField(name)
            (field.get(null) as? String) ?: defaultValue
        } catch (e: Exception) {
            Log.e("EnvVarAccess", "Fallo al acceder a la variable de entorno '$name': ${e.message}")
            defaultValue
        }
    }

    /**
     * Limpia el listener y cancela el Job de recepción automática cuando la actividad se destruye.
     */
    override fun onDestroy() {
        super.onDestroy()
        firestoreListener?.remove()
        autoFetchJob?.cancel()
    }

    // --- Utilidades de UI (showLoading y onError) ---

    private fun showLoading(message: String) {
        progressBar.visibility = View.VISIBLE
        statusTextView.text = message
    }

    private fun onError(message: String) {
        statusTextView.text = "❌ ERROR: $message"
        progressBar.visibility = View.GONE
        Log.e("EventApp", message)
    }
}