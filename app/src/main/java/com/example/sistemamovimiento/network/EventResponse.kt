package com.example.sistemamovimiento.network

import okhttp3.HttpUrl

data class EventResponse(
    val message: String,
    val data: EventData
)

data class EventData(
    val body: EventBody,
    val enqueuedTime: String,
    val partition: String,
    val sequenceNumber: Int
)

data class EventBody(
    val ir: Int,
    val pir: Int,
    val sound: Int,
    val timestamp: Long,
    val isHuman: Int,
    val blobUrl: String?
)
