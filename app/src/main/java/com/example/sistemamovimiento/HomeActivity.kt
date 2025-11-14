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
        barChart = findViewById(R.id.bar_chart_detections) // ID de la vista que definimos en el XML

        // --- TOOLBAR ---
        val toolbar = findViewById<Toolbar>(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        // --- STAT CARDS ---
        // ... (Tu código actual para stat cards) ...
        val statToday = findViewById<android.view.View>(R.id.stat_today)
        val statWeek = findViewById<android.view.View>(R.id.stat_week)
        val statActive = findViewById<android.view.View>(R.id.stat_active)

        // Valores falsos desde FakeData
        statToday.findViewById<TextView>(R.id.text_stat_value).text =
            FakeData.stats.today.toString()
        statToday.findViewById<TextView>(R.id.text_stat_label).text = "Hoy"

        statWeek.findViewById<TextView>(R.id.text_stat_value).text = FakeData.stats.week.toString()
        statWeek.findViewById<TextView>(R.id.text_stat_label).text = "Semana"

        statActive.findViewById<TextView>(R.id.text_stat_value).text =
            FakeData.stats.active.toString()
        statActive.findViewById<TextView>(R.id.text_stat_label).text = "Activos"
        // ... (Fin de tu código actual para stat cards) ...


        // --- BOTONES ---
        // ... (Tu código actual para botones) ...
        findViewById<Button>(R.id.button_view_details).setOnClickListener {
            val e = FakeData.events[0]  // evento falso
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("title", e.title)
            intent.putExtra("location", e.location)
            intent.putExtra("timestamp", e.timestamp)
            startActivity(intent)
        }

        findViewById<Button>(R.id.button_deactivate).setOnClickListener {
            Toast.makeText(this, "Sensor desactivado (simulado)", Toast.LENGTH_SHORT).show()
        }
        // ... (Fin de tu código actual para botones) ...

        // LLAMADA PARA CONFIGURAR EL GRÁFICO
        setupBarChart()
    }

    // -------------------------------
    // FUNCIÓN PARA CONFIGURAR EL GRÁFICO DE BARRAS
    // -------------------------------
    private fun setupBarChart() {
        val days = FakeData.weeklyDetections.map { it.day }
        val entries: ArrayList<BarEntry> = ArrayList()

        // 1. Convertir los datos a entradas (Entries) para el gráfico
        FakeData.weeklyDetections.forEachIndexed { index, detection ->
            // x = posición en el eje, y = valor de la detección
            entries.add(BarEntry(index.toFloat(), detection.count.toFloat()))
        }

        // 2. Configurar el conjunto de datos (DataSet)
        val dataSet = BarDataSet(entries, "Detecciones por Día")
        dataSet.color = getColor(R.color.accent_blue) // Utiliza tu color de acento
        dataSet.valueTextColor = Color.WHITE // Color del valor encima de la barra
        dataSet.valueTextSize = 12f // Tamaño del texto de valor

        // 3. Crear el objeto BarData
        val barData = BarData(dataSet)
        barData.barWidth = 0.9f // Ancho de las barras

        // 4. Configurar el BarChart
        barChart.data = barData
        barChart.setFitBars(true) // Ajustar las barras

        // Configuración de la apariencia
        barChart.description.isEnabled = false // Ocultar la descripción
        barChart.legend.isEnabled = false // Ocultar la leyenda

        // Eje X (Días de la semana)
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(days) // Formatear para mostrar "Lun", "Mar", etc.
        xAxis.position = XAxis.XAxisPosition.BOTTOM // Posición en la parte inferior
        xAxis.setDrawGridLines(false) // Ocultar líneas de la cuadrícula
        xAxis.textColor = Color.WHITE
        xAxis.granularity = 1f // Intervalo mínimo entre valores

        // Eje Y (Izquierda - Conteo de Detecciones)
        val yAxisLeft = barChart.axisLeft
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.gridColor = Color.parseColor("#404040") // Color de la cuadrícula
        yAxisLeft.textColor = Color.WHITE
        yAxisLeft.axisMinimum = 0f // Iniciar desde cero
        yAxisLeft.granularity = 1f // Mostrar solo números enteros

        // Eje Y (Derecha - Ocultar)
        barChart.axisRight.isEnabled = false

        // Configuración de interacción (opcional)
        barChart.setTouchEnabled(true)
        barChart.isDragEnabled = true
        barChart.setScaleEnabled(true)
        barChart.setPinchZoom(false)

        // 5. Actualizar el gráfico
        barChart.invalidate() // Redibujar el gráfico
    }


    // -------------------------------
    // MENU SUPERIOR (Mantenemos tu código)
    // -------------------------------
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_notifications -> {
                // Redirigir a la pantalla de detail
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.menu_history -> {
                // Redirigir a la pantalla de history
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.menu_settings -> {
                // Redirigir a la pantalla de config
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.menu_logout -> {
                // Redirigir a la pantalla de login (LoginActivity)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}