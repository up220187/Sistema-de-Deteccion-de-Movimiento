package com.example.sistemamovimiento.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sistemamovimiento.data.local.AppDatabase
import com.example.sistemamovimiento.network.RetrofitClient
import com.example.sistemamovimiento.repository.EventRepository
import com.example.sistemamovimiento.util.NotificationHelper

class MotionWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val repo = EventRepository(RetrofitClient.instance, db.eventDao())

            val previous = db.eventDao().getLastEvent()
            val current = repo.fetchAndSaveLastEvent()

            val isNew = previous == null || previous.sequenceNumber != current.sequenceNumber

            if (isNew) {
                // Solo notificar si ALTA (NotificationHelper lo revisa)
                NotificationHelper.showNewEventNotification(applicationContext, current)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
