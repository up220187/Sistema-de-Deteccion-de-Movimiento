package com.example.sistemamovimiento.repository

import com.example.sistemamovimiento.data.local.EventDao
import com.example.sistemamovimiento.data.local.EventEntity
import com.example.sistemamovimiento.network.EventApiService
import java.util.*

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

        // Notar: body.timestamp está en segundos
        val entity = EventEntity(
            ir = body.ir,
            pir = body.pir,
            sound = body.sound,
            timestamp = body.timestamp,
            enqueuedTime = response.data.enqueuedTime,
            sequenceNumber = response.data.sequenceNumber,
            severity = severity,
            isHuman = body.isHuman,
            blobUrl = body.blobUrl,
            extras = body.extras?.let { GsonHolder.gson.toJson(it) } ?: "{}"
        )

        dao.insertEvent(entity)
        return entity
    }

    suspend fun getLocalEvents(): List<EventEntity> = dao.getAllEvents()

    suspend fun getLastLocalEvent(): EventEntity? = dao.getLastEvent()

    suspend fun getDashboardStats(): DashboardStats {
        val calendar = Calendar.getInstance()

        // Start of day (millis -> convert to seconds)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis / 1000

        // Start of month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = calendar.timeInMillis / 1000

        // Start of year
        calendar.set(Calendar.MONTH, Calendar.JANUARY)
        val startOfYear = calendar.timeInMillis / 1000

        val dayCount = dao.getCountSince(startOfDay)
        val monthCount = dao.getCountSince(startOfMonth)
        val yearCount = dao.getCountSince(startOfYear)

        return DashboardStats(dayCount, monthCount, yearCount)
    }
}

// pequeño holder para Gson sin repetir import en repo
object GsonHolder {
    val gson = com.google.gson.Gson()
}
