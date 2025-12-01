package com.example.sistemamovimiento.repository

import com.example.sistemamovimiento.data.local.EventDao
import com.example.sistemamovimiento.data.local.EventEntity
import com.example.sistemamovimiento.network.EventApiService

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
}
