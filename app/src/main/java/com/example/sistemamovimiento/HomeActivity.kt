package com.example.sistemamovimiento

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // --- TOOLBAR ---
        val toolbar = findViewById<Toolbar>(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        // --- STAT CARDS ---
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

        // --- BOTÓN VER DETALLES ---
        findViewById<Button>(R.id.button_view_details).setOnClickListener {
            val e = FakeData.events[0]  // evento falso
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("title", e.title)
            intent.putExtra("location", e.location)
            intent.putExtra("timestamp", e.timestamp)
            startActivity(intent)
        }

        // --- BOTÓN DESACTIVAR ---
        findViewById<Button>(R.id.button_deactivate).setOnClickListener {
            Toast.makeText(this, "Sensor desactivado (simulado)", Toast.LENGTH_SHORT).show()
        }
    }

    // -------------------------------
    // MENU SUPERIOR
    // -------------------------------
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_notifications -> {
                // Redirigir a la pantalla de detail
                val intent = Intent(this, DetailActivity::class.java)
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