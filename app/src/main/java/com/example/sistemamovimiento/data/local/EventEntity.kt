package com.example.sistemamovimiento.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ir: Int,
    val pir: Int,
    val sound: Int,
    val timestamp: Long,     // formato en SEGS (como manda tu backend)
    val enqueuedTime: String,
    val sequenceNumber: Int,
    val severity: String,
    val isHuman: Int = 0,
    val blobUrl: String? = null,
    val extras: String? = "{}"
)
