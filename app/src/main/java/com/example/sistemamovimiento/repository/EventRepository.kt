package com.example.sistemamovimiento.repository

import com.example.sistemamovimiento.data.local.EventDao
import com.example.sistemamovimiento.data.local.EventEntity
import com.example.sistemamovimiento.network.EventApiService

data class DashboardStats(
    val dayCount: Int,
    val monthCount: Int,
    val yearCount: Int
)

class EventRepository(
    private val api: EventApiService,
    private val dao: EventDao
) {

    // 🔥 Ahora esta función SÍ retorna el EventEntity guardado
    suspend fun fetchAndSaveLastEvent(): EventEntity {
        val response = api.getLastEvent()
        val body = response.data.body

        val existingEvent = dao.getLastEvent()

        if (existingEvent != null && existingEvent.sequenceNumber == response.data.sequenceNumber) {
            return existingEvent
        }


        val activeCount = listOf(body.ir, body.pir, body.sound).count { it == 1 }

        val severity = when (activeCount) {
            1 -> "Baja"
            2 -> "Media"
            3 -> "Alta"
            else -> "Ninguna"
        }

        val entity = EventEntity(
            ir = body.ir,
            pir = body.pir,
            sound = body.sound,
            timestamp = body.ts,
            enqueuedTime = response.data.enqueuedTime,
            sequenceNumber = response.data.sequenceNumber,
            severity = severity
        )

        dao.insertEvent(entity)

        return entity     // <-- Ya regresa la entidad guardada
    }

    suspend fun getLocalEvents(): List<EventEntity> = dao.getAllEvents()

    suspend fun getLastLocalEvent(): EventEntity? = dao.getLastEvent()

    suspend fun getDashboardStats(): DashboardStats {
        val calendar = java.util.Calendar.getInstance()

        // Nota: Tu DB guarda en SEGUNDOS (según tu código anterior).
        // Calendar usa MILISEGUNDOS. Debemos convertir dividiendo entre 1000.

        // 1. Inicio del Día (Hoy 00:00:00)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis / 1000

        // 2. Inicio del Mes (Día 1 del mes actual)
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = calendar.timeInMillis / 1000

        // 3. Inicio del Año (1 de Enero del año actual)
        calendar.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY)
        val startOfYear = calendar.timeInMillis / 1000

        // Hacemos las 3 consultas (usando corrutinas son muy rápidas)
        val dayCount = dao.getCountSince(startOfDay)
        val monthCount = dao.getCountSince(startOfMonth)
        val yearCount = dao.getCountSince(startOfYear)

        return DashboardStats(dayCount, monthCount, yearCount)
    }
}
