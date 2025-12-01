package com.example.sistemamovimiento.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sistemamovimiento.FakeData
import com.example.sistemamovimiento.R
import com.example.sistemamovimiento.data.UserSession
import com.example.sistemamovimiento.databinding.CardStatItemBinding
import com.example.sistemamovimiento.databinding.FragmentHomeBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var statTodayBinding: CardStatItemBinding
    private lateinit var statWeekBinding: CardStatItemBinding
    private lateinit var statActiveBinding: CardStatItemBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        statTodayBinding = CardStatItemBinding.bind(binding.statToday.root)
        statWeekBinding = CardStatItemBinding.bind(binding.statWeek.root)
        statActiveBinding = CardStatItemBinding.bind(binding.statActive.root)

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

        setupStats()
        setupLastEventCard() // Nueva función
        setupChart()         // Nueva función para la gráfica
        setupToolbarMenu()
    }

    private fun setupStats() {
        statTodayBinding.textStatValue.text = FakeData.stats.today.toString()
        statTodayBinding.textStatLabel.text = "Hoy"

        statWeekBinding.textStatValue.text = FakeData.stats.week.toString()
        statWeekBinding.textStatLabel.text = "Semana"

        statActiveBinding.textStatValue.text = FakeData.stats.active.toString()
        statActiveBinding.textStatLabel.text = "Activos"
    }

    private fun setupLastEventCard() {
        // Obtenemos el evento más reciente (el primero de la lista)
        val lastEvent = FakeData.events.firstOrNull()

        if (lastEvent != null) {
            binding.textLastEventTitle.text = lastEvent.title
            binding.textLastEventLocation.text = lastEvent.location
            binding.textLastEventTime.text = lastEvent.timestamp

            binding.buttonViewDetails.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                    title = lastEvent.title,
                    location = lastEvent.location,
                    timestamp = lastEvent.timestamp,
                    imageRes = lastEvent.imageRes ?: R.drawable.securewatch_logo
                )
                findNavController().navigate(action)
            }
        } else {
            // Manejo si no hay eventos
            binding.textLastEventTitle.text = "Sin actividad reciente"
            binding.textLastEventLocation.text = "Todo tranquilo"
            binding.textLastEventTime.text = ""
            binding.buttonViewDetails.isEnabled = false
        }
    }

    private fun setupChart() {
        val chart = binding.barChartDetections
        val detections = FakeData.weeklyDetections

        // 1. Convertir datos a BarEntry
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        detections.forEachIndexed { index, dayDetection ->
            entries.add(BarEntry(index.toFloat(), dayDetection.count.toFloat()))
            labels.add(dayDetection.day)
        }

        // 2. Configurar el DataSet (Colores y estilo de barras)
        val dataSet = BarDataSet(entries, "Detecciones")
        dataSet.color = Color.parseColor("#4FC3F7") // Un azul claro (accent_blue)
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        // 3. Configurar Ejes
        // Eje X (Días)
        val xAxis = chart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.setDrawGridLines(false) // Quitar cuadrícula vertical
        xAxis.granularity = 1f

        // Eje Y (Izquierda)
        val axisLeft = chart.axisLeft
        axisLeft.textColor = Color.WHITE
        axisLeft.axisMinimum = 0f // Empezar en 0

        // Eje Y (Derecha) - Desactivar
        chart.axisRight.isEnabled = false

        // 4. Configurar apariencia general del gráfico
        chart.description.isEnabled = false // Quitar texto "Description Label"
        chart.legend.isEnabled = false      // Quitar leyenda si solo hay un tipo de dato
        chart.setFitBars(true)              // Ajustar barras
        chart.animateY(1000)                // Animación al cargar

        // 5. Asignar datos
        val data = BarData(dataSet)
        data.barWidth = 0.6f // Ancho de las barras
        chart.data = data
        chart.invalidate() // Refrescar
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
