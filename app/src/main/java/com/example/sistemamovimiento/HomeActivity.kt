package com.example.sistemamovimiento

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

// Importaciones necesarias para MPAndroidChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.components.XAxis

class HomeActivity : AppCompatActivity() {

    // Referencia al gráfico
    private lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Inicializar el gráfico después de setContentView
        barChart = findViewById(R.id.bar_chart_detections)

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        // cards
        val statToday = findViewById<android.view.View>(R.id.stat_today)
        val statWeek = findViewById<android.view.View>(R.id.stat_week)
        val statActive = findViewById<android.view.View>(R.id.stat_active)

        statToday.findViewById<TextView>(R.id.text_stat_value).text =
            FakeData.stats.today.toString()
        statToday.findViewById<TextView>(R.id.text_stat_label).text = "Hoy"

        statWeek.findViewById<TextView>(R.id.text_stat_value).text = FakeData.stats.week.toString()
        statWeek.findViewById<TextView>(R.id.text_stat_label).text = "Semana"

        statActive.findViewById<TextView>(R.id.text_stat_value).text =
            FakeData.stats.active.toString()
        statActive.findViewById<TextView>(R.id.text_stat_label).text = "Activos"

        // botones
        findViewById<Button>(R.id.button_view_details).setOnClickListener {
            val e = FakeData.events[0]
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("title", e.title)
            intent.putExtra("location", e.location)
            intent.putExtra("timestamp", e.timestamp)
            startActivity(intent)
        }

        findViewById<Button>(R.id.button_deactivate).setOnClickListener {
            Toast.makeText(this, "Sensor desactivado (simulado)", Toast.LENGTH_SHORT).show()
        }

        // Cargar gráfico
        setupBarChart()
    }


    private fun setupBarChart() {
        val days = FakeData.weeklyDetections.map { it.day }
        val entries: ArrayList<BarEntry> = ArrayList()

        FakeData.weeklyDetections.forEachIndexed { index, detection ->
            entries.add(BarEntry(index.toFloat(), detection.count.toFloat()))
        }

        val dataSet = BarDataSet(entries, "Detecciones por Día")
        dataSet.color = getColor(R.color.accent_blue)
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.setFitBars(true)

        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(days)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.WHITE
        xAxis.granularity = 1f

        val yAxisLeft = barChart.axisLeft
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.gridColor = Color.parseColor("#404040")
        yAxisLeft.textColor = Color.WHITE
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.granularity = 1f

        barChart.axisRight.isEnabled = false

        barChart.setTouchEnabled(true)
        barChart.isDragEnabled = true
        barChart.setScaleEnabled(true)
        barChart.setPinchZoom(false)

        barChart.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_notifications -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }

            R.id.menu_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }

            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }

            R.id.menu_logout -> {
                performLogout()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun performLogout() {
        val intent = Intent(this, MainActivity::class.java)

        // LIMPIA TODA LA PILA Y EVITA REGRESAR
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP

        startActivity(intent)
        finish() // destruye esta pantalla también
    }
}
