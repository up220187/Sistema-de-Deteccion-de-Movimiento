package com.example.sistemamovimiento

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
}
