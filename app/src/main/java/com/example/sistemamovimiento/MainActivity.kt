package com.example.sistemamovimiento

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.sistemamovimiento.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleNotificationIntent()
    }

    private fun handleNotificationIntent() {
        val openDetail = intent.getBooleanExtra("open_detail", false)
        if (!openDetail) return

        val title = intent.getStringExtra("detail_title") ?: "Evento"
        val location = intent.getStringExtra("detail_location") ?: "Ubicación"
        val timestamp = intent.getStringExtra("detail_timestamp") ?: "0"
        val ir = intent.getIntExtra("detail_ir", 0)
        val pir = intent.getIntExtra("detail_pir", 0)
        val sound = intent.getIntExtra("detail_sound", 0)

        val navController = findNavController(R.id.nav_host_fragment)

        navController.navigate(
            R.id.detailFragment,
            Bundle().apply {
                putString("title", title)
                putString("location", location)
                putString("timestamp", timestamp)
                putInt("irValue", ir)
                putInt("pirValue", pir)
                putInt("soundValue", sound)
                putInt("imageRes", R.drawable.securewatch_logo)
            }
        )
    }
}
