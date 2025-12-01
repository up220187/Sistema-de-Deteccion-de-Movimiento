package com.example.sistemamovimiento.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ir: Int,
    val pir: Int,
    val sound: Int,
    val timestamp: Long,
    val enqueuedTime: String,
    val sequenceNumber: Int,
    val severity: String   // <-- AGREGADO
)
