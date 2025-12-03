package com.example.sistemamovimiento

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Procesar intent al abrir la app desde notificación
        handleIntent(intent)
    }

    // ✅ FIRMA CORRECTA EN KOTLIN
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        // Notificación pidió abrir el detalle
        if (!intent.getBooleanExtra("open_detail", false)) return

        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val navController = navHost?.navController ?: return

        // Recuperar datos enviados desde la notificación
        val title = intent.getStringExtra("detail_title") ?: "Detalle"
        val location = intent.getStringExtra("detail_location") ?: ""
        val timestamp = intent.getStringExtra("detail_timestamp") ?: "-"
        val blobUrl = intent.getStringExtra("detail_blobUrl")
        val ir = intent.getIntExtra("detail_ir", 0)
        val pir = intent.getIntExtra("detail_pir", 0)
        val sound = intent.getIntExtra("detail_sound", 0)

        // Guardar temporalmente blobUrl en el intent.
        // El DetailFragment lo va a leer desde la activity.
        intent.removeExtra("open_detail")

        // Navegación GLOBAL al detailFragment
        val action = NavGraphDirections.actionGlobalDetailFragment(
            title = title,
            location = location,
            timestamp = timestamp,
            imageRes = R.drawable.securewatch_logo,
            irValue = ir,
            pirValue = pir,
            soundValue = sound
        )

        navController.navigate(
            action,
            navOptions {
                launchSingleTop = true
                popUpTo(R.id.homeFragment) { inclusive = false }
            }
        )
    }
}
