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
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val context = applicationContext


            val db = AppDatabase.getDatabase(context)
            val api = RetrofitClient.instance
            val repository = EventRepository(api, db.eventDao())


            val event = repository.fetchAndSaveLastEvent()


            if (event.severity == "Alta") {

                NotificationHelper.createChannelIfNeeded(context)

                val activeSensors = mutableListOf<String>()
                if(event.isHuman == 1) activeSensors.add("HUMANO")
                if(event.ir == 1) activeSensors.add("IR")
                if(event.pir == 1) activeSensors.add("Movimiento")

                val body = "¡Alerta Crítica! Detectado: ${activeSensors.joinToString(", ")}"

                NotificationHelper.showHighPriorityNotification(
                    context,
                    "Seguridad - Prioridad Alta",
                    body
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
