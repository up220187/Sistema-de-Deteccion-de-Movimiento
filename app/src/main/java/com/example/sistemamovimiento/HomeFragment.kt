package com.example.sistemamovimiento.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.sistemamovimiento.R
import com.example.sistemamovimiento.data.UserSession
import com.example.sistemamovimiento.data.local.AppDatabase
import com.example.sistemamovimiento.data.local.EventEntity
import com.example.sistemamovimiento.databinding.CardStatItemBinding
import com.example.sistemamovimiento.databinding.FragmentHomeBinding
import com.example.sistemamovimiento.network.RetrofitClient
import com.example.sistemamovimiento.repository.EventRepository
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Bindings para las tarjetitas de arriba
    private lateinit var statTodayBinding: CardStatItemBinding
    private lateinit var statWeekBinding: CardStatItemBinding
    private lateinit var statActiveBinding: CardStatItemBinding

    private lateinit var repository: EventRepository
    private lateinit var barChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Vincular layouts incluidos
        statTodayBinding = CardStatItemBinding.bind(binding.statToday.root)
        statWeekBinding = CardStatItemBinding.bind(binding.statWeek.root)
        statActiveBinding = CardStatItemBinding.bind(binding.statActive.root)

        // Inicializar repositorio
        val db = AppDatabase.getDatabase(requireContext())
        repository = EventRepository(RetrofitClient.instance, db.eventDao())

        barChart = binding.barChartDetections
        setupChartAppearance() // Configuración bonita de la gráfica

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!UserSession.isLogged(requireContext())) {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToLoginFragment())
            return
        }


        setupToolbarMenu() // ¡Importante! Ya no olvides esto para que funcionen los menús
        //setupStats()

        // 1. Cargar datos locales al entrar (para que no se vea vacío)
        loadLocalData()

        // 2. Configurar el botón de refrescar
        binding.btnRefresh.setOnClickListener {
            refreshData()
        }
    }

    private fun refreshData() {
        // Opcional: Mostrar un Toast o animar el botón para indicar carga
        // binding.btnRefresh.animate().rotationBy(360f).setDuration(1000).start()

        lifecycleScope.launch {
            try {
                val event = repository.fetchAndSaveLastEvent()
                updateLastEventUI(event)     // Actualiza tarjeta
                updateChartWithEvents()      // Actualiza gráfica
                // Opcional: Mensaje de éxito
                // Toast.makeText(context, "Datos actualizados", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                // Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para cargar DATOS QUE YA TIENES guardados (al abrir la app)
    private fun loadLocalData() {
        lifecycleScope.launch {
            val lastEvent = repository.getLastLocalEvent() // Asegúrate de tener esta función en tu Repo
            if (lastEvent != null) {
                updateLastEventUI(lastEvent)
            }
            updateChartWithEvents()
        }
    }

    // --- LÓGICA PRINCIPAL DE UI ---
    private fun updateLastEventUI(event: EventEntity) {

        // 1. Formato de Fecha y Hora
        val timestampMillis = if (event.timestamp < 1000000000000L) event.timestamp * 1000 else event.timestamp
        val dateObj = Date(timestampMillis)

        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
        val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        binding.textDateDisplay.text = dateFmt.format(dateObj)
        binding.textTimeDisplay.text = timeFmt.format(dateObj)

        // 2. Determinar Sensores Activos
        val activeList = mutableListOf<String>()
        if (event.ir == 1) activeList.add("Infrarrojo")
        if (event.pir == 1) activeList.add("Movimiento")
        if (event.sound == 1) activeList.add("Sonido")

        val count = activeList.size
        val sensorsText = if (activeList.isNotEmpty()) activeList.joinToString(", ") else "Ninguno"
        binding.textSensorsActive.text = sensorsText

        // 3. Lógica de Prioridad (Semáforo)
        val (priorityText, colorHex) = when (count) {
            3 -> "ALTA" to "#FF5252"   // Rojo
            2 -> "MEDIA" to "#FFD740"  // Amarillo
            1 -> "BAJA" to "#69F0AE"   // Verde
            else -> "INFO" to "#90A4AE" // Gris
        }

        binding.textPriorityBadge.text = priorityText
        binding.textPriorityBadge.background.setTint(Color.parseColor(colorHex))
        // Si es amarillo, texto negro para leer mejor; sino blanco
        binding.textPriorityBadge.setTextColor(if (count == 2) Color.BLACK else Color.WHITE)

        binding.lblDetected.text = "Sensor activado"

        // 4. Configurar Botón con Navigation Safe Args
        binding.buttonViewDetails.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                title = "Prioridad $priorityText",
                location = "Sensores: $sensorsText",
                timestamp = timestampMillis.toString(), // Pasamos como string para parsear allá
                imageRes = R.drawable.securewatch_logo,

                // Pasamos los valores individuales
                irValue = event.ir,
                pirValue = event.pir,
                soundValue = event.sound
            )
            findNavController().navigate(action)
        }
    }

    // --- GRÁFICA BONITA ---
    private fun setupChartAppearance() {
        barChart.setDrawGridBackground(false)
        barChart.setDrawBorders(false)
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.WHITE
        xAxis.granularity = 1f

        val leftAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.parseColor("#33FFFFFF") // Líneas sutiles
        leftAxis.textColor = Color.WHITE
        leftAxis.axisMinimum = 0f

        barChart.axisRight.isEnabled = false
    }

    private fun updateChartWithEvents() {
        lifecycleScope.launch {
            val events = repository.getLocalEvents()
            val counts = aggregateLast7DaysCounts(events)

            val entries = ArrayList<BarEntry>()
            val labels = ArrayList<String>()

            // Construir datos de los últimos 7 días (De hace 6 días hasta hoy)
            val cal = Calendar.getInstance()
            for (i in 6 downTo 0) {
                val d = Calendar.getInstance()
                d.add(Calendar.DAY_OF_YEAR, -i)
                labels.add(SimpleDateFormat("EE", Locale("es", "ES")).format(d.time)) // "Lun", "Mar"

                // En nuestro mapa 'counts', 0 es hoy. Así que 'i' es la diferencia de días.
                val valCount = counts[i] ?: 0
                entries.add(BarEntry((6-i).toFloat(), valCount.toFloat()))
            }

            val set = BarDataSet(entries, "Detecciones")
            set.color = Color.parseColor("#4FC3F7") // Azul claro
            set.valueTextColor = Color.WHITE
            set.valueTextSize = 12f

            val data = BarData(set)
            data.barWidth = 0.5f

            barChart.data = data
            barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            barChart.invalidate()
            barChart.animateY(1000)
        }
    }

    // (Mantén tus funciones auxiliares aggregateLast7DaysCounts, daysBetween, setupStats y setupToolbarMenu iguales)
    // Solo asegúrate de que aggregateLast7DaysCounts devuelva el mapa correctamente.

    private fun aggregateLast7DaysCounts(allEvents: List<EventEntity>): Map<Int, Int> {
        val counts = mutableMapOf<Int, Int>()
        for (i in 0..6) counts[i] = 0
        val now = Calendar.getInstance()
        for (evt in allEvents) {
            val millis = if (evt.timestamp < 1_000_000_000_000L) evt.timestamp * 1000 else evt.timestamp
            val cal = Calendar.getInstance().apply { time = Date(millis) }
            val diff = daysBetween(cal, now)
            if (diff in 0..6) {
                counts[diff] = (counts[diff] ?: 0) + 1
            }
        }
        return counts
    }

    private fun daysBetween(day: Calendar, now: Calendar): Int {
        val start = Calendar.getInstance().apply { timeInMillis = day.timeInMillis; set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)}
        val end = Calendar.getInstance().apply { timeInMillis = now.timeInMillis; set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)}
        return ((end.timeInMillis - start.timeInMillis) / (1000*60*60*24)).toInt()
    }

    private fun setupToolbarMenu() {
        binding.toolbarMain.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_notifications -> {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToHistoryFragment()
                    )
                    true
                }
                R.id.menu_history -> {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToHistoryFragment()
                    )
                    true
                }
                R.id.menu_settings -> {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToSettingsFragment()
                    )
                    true
                }
                R.id.menu_logout -> {
                    UserSession.logout(requireContext())
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToLoginFragment()
                    )
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
