package com.example.sistemamovimiento.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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

    private var autoRefreshJobRunning = true

    private var lastProcessedEventId: Int = -1

    // Agrega esta función en tu clase HomeFragment
    private fun setupDynamicStats() {
        lifecycleScope.launch {
            // 1. Obtener los conteos de la base de datos
            val stats = repository.getDashboardStats()

            // 2. Preparar formateadores de fecha "bonitos"
            val locale = Locale("es", "ES")
            val todayFormat = SimpleDateFormat("dd 'de' MMMM", locale) // Ej: 12 de Febrero
            val monthFormat = SimpleDateFormat("MMMM", locale)         // Ej: Febrero
            val yearFormat = SimpleDateFormat("yyyy", locale)          // Ej: 2025

            val now = Date()

            // --- TARJETA 1: DÍA ---
            statTodayBinding.textStatValue.text = stats.dayCount.toString()
            // Capitalizamos la primera letra (ej: "febrero" -> "Febrero")
            statTodayBinding.textStatLabel.text = "Hoy, ${todayFormat.format(now).replaceFirstChar { it.uppercase() }}"

            // --- TARJETA 2: MES (Reusamos statWeek) ---
            statWeekBinding.textStatValue.text = stats.monthCount.toString()
            val mesBonito = monthFormat.format(now).replaceFirstChar { it.uppercase() }
            statWeekBinding.textStatLabel.text = "Mes de $mesBonito"

            // --- TARJETA 3: AÑO (Reusamos statActive) ---
            statActiveBinding.textStatValue.text = stats.yearCount.toString()
            statActiveBinding.textStatLabel.text = "Año ${yearFormat.format(now)}"
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        statTodayBinding = CardStatItemBinding.bind(binding.statToday.root)
        statWeekBinding = CardStatItemBinding.bind(binding.statWeek.root)
        statActiveBinding = CardStatItemBinding.bind(binding.statActive.root)

        val db = AppDatabase.getDatabase(requireContext())
        repository = EventRepository(RetrofitClient.instance, db.eventDao())

        barChart = binding.barChartDetections
        setupChartAppearance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!UserSession.isLogged(requireContext())) {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToLoginFragment())
            return
        }

        setupToolbarMenu()

        setupDynamicStats()

        // cargar datos locales al abrir
        loadLocalData()

        // después de setupToolbarMenu() y loadLocalData()
        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                try {
                    val event = repository.fetchAndSaveLastEvent()
                    updateLastEventUI(event)
                    updateChartWithEvents()
                    setupDynamicStats()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }


        // iniciar refresco automático
        startAutoRefresh()
    }

    // ------------------------------
    // AUTO REFRESH CADA 15 SEGUNDOS
    // ------------------------------
    private fun startAutoRefresh() {
        lifecycleScope.launch {
            while (autoRefreshJobRunning) {
                try {
                    val event = repository.fetchAndSaveLastEvent()
                    updateLastEventUI(event)
                    updateChartWithEvents()
                    setupDynamicStats()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(15000) // 15 segundos
            }
        }
    }

    private fun loadLocalData() {
        lifecycleScope.launch {
            val lastEvent = repository.getLastLocalEvent()
            if (lastEvent != null) updateLastEventUI(lastEvent)
            updateChartWithEvents()
        }
    }

    // ------------------------------
    // -------- UI EVENTO ------------
    // ------------------------------
    private fun updateLastEventUI(event: EventEntity) {

        val timestampMillis =
            if (event.timestamp < 1000000000000L) event.timestamp * 1000 else event.timestamp

        val dateObj = Date(timestampMillis)

        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
        val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        binding.textDateDisplay.text = dateFmt.format(dateObj)
        binding.textTimeDisplay.text = timeFmt.format(dateObj)

        val activeList = mutableListOf<String>()
        if (event.ir == 1) activeList.add("Infrarrojo")
        if (event.pir == 1) activeList.add("Movimiento")
        if (event.sound == 1) activeList.add("Sonido")

        val count = activeList.size
        val sensorsText =
            if (activeList.isNotEmpty()) activeList.joinToString(", ") else "Ninguno"
        binding.textSensorsActive.text = sensorsText

        val (priorityText, colorHex) = when (count) {
            3 -> "ALTA" to "#FF5252"
            2 -> "MEDIA" to "#FFD740"
            1 -> "BAJA" to "#69F0AE"
            else -> "INFO" to "#90A4AE"
        }

        binding.textPriorityBadge.text = priorityText
        binding.textPriorityBadge.background.setTint(Color.parseColor(colorHex))
        binding.textPriorityBadge.setTextColor(if (count == 2) Color.BLACK else Color.WHITE)

        binding.lblDetected.text = "Sensor activado"

        // navegar con detalles
        binding.buttonViewDetails.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                title = "Prioridad $priorityText",
                location = "Sensores: $sensorsText",
                timestamp = timestampMillis.toString(),
                imageRes = R.drawable.securewatch_logo,
                irValue = event.ir,
                pirValue = event.pir,
                soundValue = event.sound,
                isHuman = event.isHuman,
                blobUrl = event.blobUrl
            )
            findNavController().navigate(action)
        }

        if (event.id != lastProcessedEventId) {
            lastProcessedEventId = event.id

            // Solo enviamos si la prioridad es MEDIA o ALTA (opcional, para ahorrar saldo)
            // O quita el if si quieres que envie siempre.
            if (event.severity == "Alta" || event.severity == "Media") {
                checkAndSendSms(event)
            }
        }
    }

    // ------------------------------
    // ----------- GRÁFICA ----------
    // ------------------------------
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
        leftAxis.gridColor = Color.parseColor("#33FFFFFF")
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

            val cal = Calendar.getInstance()
            for (i in 6 downTo 0) {
                val d = Calendar.getInstance()
                d.add(Calendar.DAY_OF_YEAR, -i)
                labels.add(SimpleDateFormat("EE", Locale("es", "ES")).format(d.time))

                val valCount = counts[i] ?: 0
                entries.add(BarEntry((6 - i).toFloat(), valCount.toFloat()))
            }

            val set = BarDataSet(entries, "Detecciones")
            set.color = Color.parseColor("#4FC3F7")
            set.valueTextColor = Color.WHITE
            set.valueTextSize = 12f

            val data = BarData(set)
            data.barWidth = 0.5f

            barChart.data = data
            barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            barChart.invalidate()
        }
    }

    private fun aggregateLast7DaysCounts(allEvents: List<EventEntity>): Map<Int, Int> {
        val counts = mutableMapOf<Int, Int>()
        for (i in 0..6) counts[i] = 0

        val now = Calendar.getInstance()

        for (evt in allEvents) {
            val millis =
                if (evt.timestamp < 1_000_000_000_000L) evt.timestamp * 1000 else evt.timestamp
            val cal = Calendar.getInstance().apply { time = Date(millis) }
            val diff = daysBetween(cal, now)
            if (diff in 0..6) counts[diff] = (counts[diff] ?: 0) + 1
        }

        return counts
    }

    private fun daysBetween(day: Calendar, now: Calendar): Int {
        val start = Calendar.getInstance().apply {
            timeInMillis = day.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return ((end.timeInMillis - start.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
    }

    private fun setupToolbarMenu() {
        binding.toolbarMain.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_notifications,
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
        autoRefreshJobRunning = false
        _binding = null
    }

    private fun checkAndSendSms(event: EventEntity) {
        // 1. Verificar si la opción está activada en ajustes
        val prefs = requireContext().getSharedPreferences("SETTINGS",
            AppCompatActivity.MODE_PRIVATE)
        val isSmsEnabled = prefs.getBoolean("alert_sms", false)

        if (!isSmsEnabled) return

        // 2. Verificar si tenemos permiso
        if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.SEND_SMS)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return // No hay permiso, no hacemos nada
        }

        // 3. Obtener teléfono del usuario actual
        val sessionManager = com.example.sistemamovimiento.utils.SessionManager(requireContext())
        val user = sessionManager.getSession() ?: return
        val phoneNumber = user.telefono

        if (phoneNumber.isEmpty()) return

        // 4. Construir mensaje
        val sensores = mutableListOf<String>()
        if (event.ir == 1) sensores.add("IR")
        if (event.pir == 1) sensores.add("Movimiento")
        if (event.sound == 1) sensores.add("Sonido")
        if (event.isHuman == 1) sensores.add("HUMANO DETECTADO")

        val mensaje = "ALERTA SecureWatch: Se detectó actividad (${event.severity}). Sensores: ${sensores.joinToString(", ")}"

        // 5. Enviar SMS
        try {
            val smsManager = android.telephony.SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, mensaje, null, null)
            // Opcional: Toast para avisar que se envió (solo para pruebas)
            // android.widget.Toast.makeText(requireContext(), "SMS enviado a $phoneNumber", android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
