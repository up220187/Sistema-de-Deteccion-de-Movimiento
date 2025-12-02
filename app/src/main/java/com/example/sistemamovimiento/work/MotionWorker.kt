package com.example.sistemamovimiento.work

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sistemamovimiento.data.local.AppDatabase
import com.example.sistemamovimiento.network.RetrofitClient
import com.example.sistemamovimiento.repository.EventRepository
import com.example.sistemamovimiento.util.NotificationHelper

class MotionWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        try {
            val db = AppDatabase.getDatabase(applicationContext)
            val dao = db.eventDao()
            val repo = EventRepository(RetrofitClient.instance, dao)

            val previous = dao.getLastEvent() // puede ser null
            val current = repo.fetchAndSaveLastEvent()

            val prevSeq = previous?.sequenceNumber
            val currSeq = current.sequenceNumber

            // si no existe prev o cambió la secuencia -> nuevo evento
            val isNew = prevSeq == null || prevSeq != currSeq

            if (isNew) {
                // enviar notificación
                NotificationHelper.showNewEventNotification(
                    context = applicationContext,
                    title = "Se detectó un movimiento nuevo!",
                    text = "Toca para abrir y ver el evento."
                )
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
