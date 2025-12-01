package com.example.sistemamovimiento


import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.sistemamovimiento.work.EventWorker
import java.util.concurrent.TimeUnit

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val request = PeriodicWorkRequestBuilder<EventWorker>(15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "EventBackgroundWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
