package com.example.sistemamovimiento.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sistemamovimiento.data.local.AppDatabase
import com.example.sistemamovimiento.network.RetrofitClient
import com.example.sistemamovimiento.repository.EventRepository

class EventWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val repo = EventRepository(
            api = RetrofitClient.instance,
            eventDao = db.eventDao()
        )

        return try {
            repo.fetchAndSaveLastEvent()   // ← obtiene evento, calcula severity y guarda
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
