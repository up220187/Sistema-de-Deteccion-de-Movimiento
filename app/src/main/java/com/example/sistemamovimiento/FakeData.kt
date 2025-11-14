package com.example.sistemamovimiento

// Modelo usado por EventAdapter
data class FakeEvent(
    val title: String,
    val location: String,
    val timestamp: String,
    val imageRes: Int? = null
)

// Estadísticas simples
data class Stats(
    val today: Int,
    val week: Int,
    val active: Int
)

// Datos falsos centralizados
object FakeData {
    val stats = Stats(
        today = 12,
        week = 87,
        active = 5
    )

    // 10 eventos falsos con imageRes opcional (usa tu drawable como placeholder)
    val events: List<FakeEvent> = List(10) { index ->
        FakeEvent(
            title = "Movimiento #${index + 1} - Entrada Principal",
            location = "Sensor ID: SENSOR-00${index + 1}",
            timestamp = "10 Nov 2025, 18:${30 + index}:00",
            imageRes = R.drawable.ic_secure_watch_logo
        )
    }
}
