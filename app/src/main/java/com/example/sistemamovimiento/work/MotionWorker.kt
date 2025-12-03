package com.example.sistemamovimiento.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sistemamovimiento.data.local.AppDatabase
import com.example.sistemamovimiento.network.RetrofitClient
import com.example.sistemamovimiento.repository.EventRepository
import com.example.sistemamovimiento.util.NotificationHelper

class MotionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val repo = EventRepository(
                RetrofitClient.instance,
                db.eventDao()
            )

            val previous = repo.getLastLocalEvent()
            val current = repo.fetchAndSaveLastEvent()

            val isNew = previous?.sequenceNumber != current.sequenceNumber

            // 🔥 SOLO prioridad ALTA: notificación
            if (isNew && current.severity == "Alta") {
                NotificationHelper.showNewEventNotification(applicationContext, current)
            }

            Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
