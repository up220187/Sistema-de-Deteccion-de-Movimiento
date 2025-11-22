package com.example.sistemamovimiento

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Toolbar back
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_history)
        toolbar.setNavigationOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.recycler_view_events)
        recycler.layoutManager = LinearLayoutManager(this)

        val adapter = EventAdapter(FakeData.events) { event ->
            // abrir detalle con datos del evento
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("title", event.title)
            intent.putExtra("location", event.location)
            intent.putExtra("timestamp", event.timestamp)
            intent.putExtra("image_res", R.drawable.securewatch_logo)
            startActivity(intent)
        }

        recycler.adapter = adapter
    }
}
