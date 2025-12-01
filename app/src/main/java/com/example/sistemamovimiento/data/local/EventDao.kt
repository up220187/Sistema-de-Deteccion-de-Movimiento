package com.example.sistemamovimiento.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Query("SELECT * FROM events ORDER BY timestamp DESC")
    suspend fun getAllEvents(): List<EventEntity>

    @Query("SELECT * FROM events ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastEvent(): EventEntity?
}

