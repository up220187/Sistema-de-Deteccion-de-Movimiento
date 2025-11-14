package com.example.sistemamovimiento

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Toolbar (si quieres manejar navegación de menú en el futuro)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        // Stat cards: los includes tienen id stat_today, stat_week, stat_active
        val statToday = findViewById<android.view.View>(R.id.stat_today)
        val statWeek = findViewById<android.view.View>(R.id.stat_week)
        val statActive = findViewById<android.view.View>(R.id.stat_active)

        // Poner valores falsos
        statToday.findViewById<TextView>(R.id.text_stat_value).text = FakeData.stats.today.toString()
        statToday.findViewById<TextView>(R.id.text_stat_label).text = "Hoy"

        statWeek.findViewById<TextView>(R.id.text_stat_value).text = FakeData.stats.week.toString()
        statWeek.findViewById<TextView>(R.id.text_stat_label).text = "Semana"

        statActive.findViewById<TextView>(R.id.text_stat_value).text = FakeData.stats.active.toString()
        statActive.findViewById<TextView>(R.id.text_stat_label).text = "Activos"

        // Botones
        findViewById<Button>(R.id.button_view_details).setOnClickListener {
            // Abrir detalle (simulamos paso de datos)
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("title", FakeData.events[0].title)
            intent.putExtra("location", FakeData.events[0].location)
            intent.putExtra("timestamp", FakeData.events[0].timestamp)
            startActivity(intent)
        }

        findViewById<Button>(R.id.button_deactivate).setOnClickListener {
            android.widget.Toast.makeText(this, "Sensor desactivado (simulado)", android.widget.Toast.LENGTH_SHORT).show()
        }

        // Accesos directos al historial y configuración desde la app (puedes añadir botones/menu si los agregas)
        // Aquí se asume que el usuario usará el menú o botones que hayas puesto en la UI.
    }
}
