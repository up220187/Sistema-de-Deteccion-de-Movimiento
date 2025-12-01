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

        // Bindings de los include
        statTodayBinding = CardStatItemBinding.bind(binding.statToday.root)
        statWeekBinding = CardStatItemBinding.bind(binding.statWeek.root)
        statActiveBinding = CardStatItemBinding.bind(binding.statActive.root)

        // Crear repositorio (Room + Retrofit)
        val db = AppDatabase.getDatabase(requireContext())
        repository = EventRepository(
            api = RetrofitClient.instance,
            dao = db.eventDao()
        )

        // Chart
        barChart = binding.barChartDetections

        setupChartAppearance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Verificar sesión
        if (!UserSession.isLogged(requireContext())) {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToLoginFragment()
            )
            return
        }

        setupToolbarMenu()
        setupStats()

        // Iniciar actualización automática (trae evento y guarda en Room)
        observeLastEvent()
    }

    // ==============================
    //  🔄 ACTUALIZAR ÚLTIMO EVENTO CADA 15s
    // ==============================
    private fun observeLastEvent() {
        lifecycleScope.launch {
            while (true) {
                try {
                    // fetchAndSaveLastEvent devuelve la entidad creada y guardada
                    val event: EventEntity = repository.fetchAndSaveLastEvent()

                    // Actualizar UI con los datos del evento
                    updateLastEventUI(
                        event.timestamp,
                        event.severity
                    )

                    // Actualizar gráfico con los eventos guardados
                    updateChartWithEvents()

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                delay(15_000) // cada 15 segundos
            }
        }
    }

    // ==============================
    //  🎨 ACTUALIZAR UI DEL EVENTO
    // ==============================
    private fun updateLastEventUI(timestamp: Long, severity: String) {

        binding.textLastEventTime.text = formatDate(timestamp)
        binding.textLastEventSeverity.text = "Severidad: $severity"

        // Navegación: convertir timestamp a String (porque tu action espera String)
        binding.buttonViewDetails.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                title = "Última alarma",
                location = severity,              // ejemplo: pasar la severidad al detalle
                timestamp = timestamp.toString()  // <-- convertir a String
            )
            findNavController().navigate(action)
        }
    }

    // ==============================
    //  📅 FORMATO DE FECHA
    // ==============================
    private fun formatDate(time: Long): String {
        // Si tu timestamp está en segundos, multiplícalo por 1000
        val millis = if (time < 1_000_000_000_000L) time * 1000 else time
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    // ==============================
    //  📊 CHART: CONFIG Y ACTUALIZACIÓN
    // ==============================
    private fun setupChartAppearance() {
        // Config básico del chart
        barChart.setDrawGridBackground(false)
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.axisRight.isEnabled = false

        val x = barChart.xAxis
        x.position = XAxis.XAxisPosition.BOTTOM
        x.granularity = 1f
        x.setDrawGridLines(false)

        val left = barChart.axisLeft
        left.axisMinimum = 0f
    }

    private fun updateChartWithEvents() {
        lifecycleScope.launch {
            try {
                val event: EventEntity = repository.fetchAndSaveLastEvent()
                val last7Counts = aggregateLast7DaysCounts(repository.getLocalEvents())

                // crear entradas
                val entries = ArrayList<BarEntry>()
                val labels = ArrayList<String>()

                // labels: últimos 7 días (de -6 a 0), 0 = hoy
                val cal = Calendar.getInstance()
                for (i in 6 downTo 0) {
                    val d = Calendar.getInstance()
                    d.add(Calendar.DAY_OF_YEAR, -i)
                    val label = SimpleDateFormat("dd/MM", Locale.getDefault()).format(d.time)
                    labels.add(label)
                }

                // last7Counts es mapa: index 0..6 -> count (0 = hoy, 6 = hace 6 días)
                // Usamos entries en x: 0..6
                for (i in 0..6) {
                    val count = last7Counts[i] ?: 0
                    entries.add(BarEntry(i.toFloat(), count.toFloat()))
                }

                val set = BarDataSet(entries, "Detecciones (7 días)")
                set.valueTextSize = 10f
                set.color = Color.parseColor("#CF3476") // si prefieres usar tu color
                val data = BarData(set)
                data.barWidth = 0.9f

                barChart.data = data
                barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                barChart.xAxis.labelRotationAngle = -45f
                barChart.invalidate()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Recibe todos los eventos y los agrupa por día en los últimos 7 días.
     * Retorna un map index->count donde index 0 = hoy, 1 = ayer, ... 6 = hace 6 días
     */
    private fun aggregateLast7DaysCounts(allEvents: List<EventEntity>): Map<Int, Int> {
        val counts = mutableMapOf<Int, Int>()
        for (i in 0..6) counts[i] = 0

        val now = Calendar.getInstance()

        for (evt in allEvents) {
            val millis = if (evt.timestamp < 1_000_000_000_000L) evt.timestamp * 1000 else evt.timestamp
            val cal = Calendar.getInstance().apply { time = Date(millis) }

            val diffDays = daysBetween(cal, now)
            if (diffDays in 0..6) {
                // diffDays 0 = same day (hoy), 1 = ayer, ...
                val current = counts[diffDays] ?: 0
                counts[diffDays] = current + 1
            }
        }

        // Queremos que el chart muestre días en orden (hace 6 ... hoy), pero en updateChartWithEvents invertimos etiquetas.
        // Aquí retornamos el map (0 = hoy)
        return counts
    }

    private fun daysBetween(day: Calendar, now: Calendar): Int {
        val start = Calendar.getInstance().apply {
            time = Date(day.timeInMillis)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val end = Calendar.getInstance().apply {
            time = Date(now.timeInMillis)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val diff = (end.timeInMillis - start.timeInMillis) / (24 * 60 * 60 * 1000)
        return diff.toInt()
    }

    // ==============================
    //  📊 ESTADÍSTICAS SIMPLES (se pueden ligar con Room después)
    // ==============================
    private fun setupStats() {
        // puedes cambiar por queries reales a Room si lo deseas
        statTodayBinding.textStatValue.text = "—"
        statTodayBinding.textStatLabel.text = "Hoy"

        statWeekBinding.textStatValue.text = "—"
        statWeekBinding.textStatLabel.text = "Semana"

        statActiveBinding.textStatValue.text = "—"
        statActiveBinding.textStatLabel.text = "Activos"
    }

    // ==============================
    //  ☰ MENÚ SUPERIOR (Toolbar)
    // ==============================
    private fun setupToolbarMenu() {
        binding.toolbarMain.setOnMenuItemClickListener { item ->
            when (item.itemId) {

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
