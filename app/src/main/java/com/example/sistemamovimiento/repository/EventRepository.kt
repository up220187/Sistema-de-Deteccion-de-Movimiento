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

    suspend fun fetchAndSaveLastEvent(): EventEntity {
        val response = api.getLastEvent()
        val body = response.data.body

        val existing = dao.getLastEvent()
        if (existing != null && existing.sequenceNumber == response.data.sequenceNumber) {
            return existing
        }

        // PRIORIDAD:
        val activeCount = listOf(body.ir, body.pir, body.sound).count { it == 1 }
        val severity = when (activeCount) {
            3 -> "Alta"
            2 -> "Media"
            1 -> "Baja"
            else -> "Ninguna"
        }

        val entity = EventEntity(
            ir = body.ir,
            pir = body.pir,
            sound = body.sound,

            timestamp = body.timestamp,        // <-- nombre correcto del JSON
            enqueuedTime = response.data.enqueuedTime,
            sequenceNumber = response.data.sequenceNumber,
            severity = severity,

            isHuman = body.isHuman,            // <-- NEW
            blobUrl = body.blobUrl             // <-- NEW
        )

        dao.insertEvent(entity)
        return entity
    }

    suspend fun getLocalEvents(): List<EventEntity> = dao.getAllEvents()

    suspend fun getLastLocalEvent(): EventEntity? = dao.getLastEvent()

    suspend fun getDashboardStats(): DashboardStats {
        val calendar = java.util.Calendar.getInstance()

        calendar.apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startDay = calendar.timeInMillis / 1000

        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        val startMonth = calendar.timeInMillis / 1000

        calendar.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY)
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        val startYear = calendar.timeInMillis / 1000

        return DashboardStats(
            dayCount = dao.getCountSince(startDay),
            monthCount = dao.getCountSince(startMonth),
            yearCount = dao.getCountSince(startYear)
        )
    }
}
