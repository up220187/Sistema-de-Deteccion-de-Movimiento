package com.example.sistemamovimiento

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Constraints
import androidx.work.WorkManager
import com.example.sistemamovimiento.util.NotificationHelper
import com.example.sistemamovimiento.work.MotionWorker
import java.util.concurrent.TimeUnit

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // crear canal (por si se quieren ver notificaciones cuando app esté abierta)
        NotificationHelper.createChannelIfNeeded(this)

        // Constraints: necesita red
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // PeriodicWork - 15 minutos mínimo por WorkManager
        val workRequest = PeriodicWorkRequestBuilder<MotionWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "motion_periodic_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
