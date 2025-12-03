package com.example.sistemamovimiento.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.sistemamovimiento.R
import com.example.sistemamovimiento.data.UserSession
import com.example.sistemamovimiento.data.local.EventEntity
import com.example.sistemamovimiento.databinding.CardStatItemBinding
import com.example.sistemamovimiento.databinding.FragmentHomeBinding
import com.example.sistemamovimiento.viewmodels.HomeViewModel
import com.example.sistemamovimiento.viewmodels.HomeViewModelFactory
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Correctos ahora según TU layout
    private lateinit var statTodayBinding: CardStatItemBinding
    private lateinit var statWeekBinding: CardStatItemBinding
    private lateinit var statActiveBinding: CardStatItemBinding

    private lateinit var barChart: BarChart

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Bindings EXACTOS a tus IDs
        statTodayBinding = CardStatItemBinding.bind(binding.statToday.root)
        statWeekBinding = CardStatItemBinding.bind(binding.statWeek.root)
        statActiveBinding = CardStatItemBinding.bind(binding.statActive.root)

        barChart = binding.barChartDetections
        setupChartAppearance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Si no hay sesión
        if (!UserSession.isLogged(requireContext())) {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToLoginFragment()
            )
            return
        }


        // Último evento
        viewModel.lastEvent.observe(viewLifecycleOwner) {
            it?.let { event -> updateLastEventUI(event) }
        }

        // Eventos para gráfica
        viewModel.events.observe(viewLifecycleOwner) {
            updateChart()
        }

        // Stats día / mes / año
        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            stats?.let { s ->
                val locale = Locale("es", "ES")
                val now = Date()

                val todayFormat = SimpleDateFormat("dd 'de' MMMM", locale)
                val monthFormat = SimpleDateFormat("MMMM", locale)
                val yearFormat = SimpleDateFormat("yyyy", locale)

                statTodayBinding.textStatValue.text = s.dayCount.toString()
                statTodayBinding.textStatLabel.text =
                    "Hoy, ${todayFormat.format(now).replaceFirstChar { it.uppercase() }}"

                statWeekBinding.textStatValue.text = s.monthCount.toString()
                statWeekBinding.textStatLabel.text =
                    "Mes de ${monthFormat.format(now).replaceFirstChar { it.uppercase() }}"

                statActiveBinding.textStatValue.text = s.yearCount.toString()
                statActiveBinding.textStatLabel.text =
                    "Año ${yearFormat.format(now)}"
            }
        }

        // Swipe refresher
        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                viewModel.fetchAndSaveLastEvent()
                binding.swipeRefresh.isRefreshing = false
            }
        }

        // Datos iniciales
        viewModel.loadLocalData()

        // Refresh instantáneo cada 15 segundos dentro de la app
        viewModel.startInstantRefresh()
    }

    // -----------------------------------------------------
    // ----------- MOSTRAR INFORMACIÓN DEL EVENTO ----------
    // -----------------------------------------------------
    private fun updateLastEventUI(event: EventEntity) {

        val timestampMillis =
            if (event.timestamp < 1_000_000_000_000L) event.timestamp * 1000 else event.timestamp

        val dateObj = Date(timestampMillis)

        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
        val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        binding.textDateDisplay.text = dateFmt.format(dateObj)
        binding.textTimeDisplay.text = timeFmt.format(dateObj)

        val activeList = mutableListOf<String>()
        if (event.ir == 1) activeList.add("Infrarrojo")
        if (event.pir == 1) activeList.add("Movimiento")
        if (event.sound == 1) activeList.add("Sonido")

        val sensorsText =
            if (activeList.isNotEmpty()) activeList.joinToString(", ") else "Ninguno"

        binding.textSensorsActive.text = sensorsText

        val count = activeList.size
        val (priorityText, colorHex) = when (count) {
            3 -> "ALTA" to "#FF5252"
            2 -> "MEDIA" to "#FFD740"
            1 -> "BAJA" to "#69F0AE"
            else -> "INFO" to "#90A4AE"
        }

        binding.textPriorityBadge.text = priorityText
        binding.textPriorityBadge.background.setTint(Color.parseColor(colorHex))
        binding.textPriorityBadge.setTextColor(
            if (count == 2) Color.BLACK else Color.WHITE
        )

        // Botón Detalles
        binding.buttonViewDetails.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                title = "Prioridad $priorityText",
                location = "Sensores: $sensorsText",
                timestamp = timestampMillis.toString(),
                imageRes = R.drawable.securewatch_logo,
                irValue = event.ir,
                pirValue = event.pir,
                soundValue = event.sound
            )
            findNavController().navigate(action)
        }
    }

    // -----------------------------------------------------
    // ------------------------ GRÁFICA ---------------------
    // -----------------------------------------------------
    private fun setupChartAppearance() {
        barChart.setDrawGridBackground(false)
        barChart.setDrawBorders(false)
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false

        barChart.axisRight.isEnabled = false

        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            textColor = Color.WHITE
            granularity = 1f
        }

        barChart.axisLeft.apply {
            setDrawGridLines(true)
            gridColor = Color.parseColor("#33FFFFFF")
            textColor = Color.WHITE
            axisMinimum = 0f
        }
    }

    private fun updateChart() {
        val events = viewModel.events.value ?: emptyList()
        val counts = viewModel.countLast7Days(events)

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        // Últimos 7 días
        for (i in 6 downTo 0) {
            val d = Calendar.getInstance()
            d.add(Calendar.DAY_OF_YEAR, -i)
            labels.add(SimpleDateFormat("EE", Locale("es", "ES")).format(d.time))
            entries.add(BarEntry((6 - i).toFloat(), counts[i]?.toFloat() ?: 0f))
        }

        val set = BarDataSet(entries, "Detecciones").apply {
            color = Color.parseColor("#4FC3F7")
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        barChart.data = BarData(set).apply { barWidth = 0.5f }
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.invalidate()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopInstantRefresh()
        _binding = null
    }
}
