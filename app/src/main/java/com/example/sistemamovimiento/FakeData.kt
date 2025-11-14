package com.example.sistemamovimiento

// 1. Clases de datos (DayDetection, FakeEvent, FakeStats)
data class DayDetection(
    val day: String, // Ejemplo: "Lun", "Mar"
    val count: Int   // Número de detecciones
)

data class FakeEvent(
    val title: String,
    val location: String,
    val timestamp: String,
    val imageRes: Int? = null
)

data class FakeStats(
    val today: Int,
    val week: Int,
    val active: Int
)

// 2. Objeto principal FakeData (una sola vez)
object FakeData {

    val stats = FakeStats(
        today = 12,
        week = 48,
        active = 5
    )

    val events = listOf(
        FakeEvent(
            "Movimiento detectado",
            "Puerta principal",
            "2025-02-12 15:32",
            R.drawable.ic_secure_watch_logo
        ),
        FakeEvent(
            "Actividad inusual",
            "Patio trasero",
            "2025-02-11 08:12",
            R.drawable.ic_secure_watch_logo
        )
    )

    // DATOS PARA EL GRÁFICO DE BARRAS (Ahora directamente accesible)
    val weeklyDetections = listOf(
        DayDetection("Lun", 5),
        DayDetection("Mar", 12),
        DayDetection("Mié", 8),
        DayDetection("Jue", 15),
        DayDetection("Vie", 22),
        DayDetection("Sáb", 7),
        DayDetection("Dom", 3)
    )
}