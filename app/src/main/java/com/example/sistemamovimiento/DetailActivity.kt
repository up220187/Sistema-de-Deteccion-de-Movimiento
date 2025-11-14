package com.example.sistemamovimiento

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_detail)
        toolbar.setNavigationOnClickListener { finish() }

        val img = findViewById<ImageView>(R.id.image_full_event)
        val progress = findViewById<ProgressBar>(R.id.progress_bar_image)

        // recibir datos del intent
        val title = intent.getStringExtra("title")
        val location = intent.getStringExtra("location")
        val timestamp = intent.getStringExtra("timestamp")
        val imageRes = intent.getIntExtra("image_res", R.drawable.securewatch_logo)

        // Si quieres mostrar el título/location/timestamp, añade TextViews en tu layout y setéalos aquí.
        // Simulamos carga de imagen (1s)
        progress.visibility = View.VISIBLE
        img.postDelayed({
            img.setImageResource(imageRes)
            progress.visibility = View.GONE
        }, 800)
    }
}
