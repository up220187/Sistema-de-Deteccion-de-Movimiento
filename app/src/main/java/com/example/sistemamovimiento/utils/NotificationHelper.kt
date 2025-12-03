package com.example.sistemamovimiento.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.sistemamovimiento.MainActivity
import com.example.sistemamovimiento.R
import com.example.sistemamovimiento.data.local.EventEntity

object NotificationHelper {

    private const val CHANNEL_ID = "motion_channel_v1"
    private const val CHANNEL_NAME = "Alertas de Movimiento"

    fun createChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificaciones de detección de movimiento"
                    enableLights(true)
                    enableVibration(true)
                }
                nm.createNotificationChannel(channel)
            }
        }
    }

    /** Notificación SOLO para ALTA prioridad */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNewEventNotification(context: Context, entity: EventEntity) {
        createChannelIfNeeded(context)

        val tsMillis =
            if (entity.timestamp < 1_000_000_000_000L) entity.timestamp * 1000 else entity.timestamp

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            putExtra("open_detail", true)
            putExtra("detail_title", "Prioridad ${entity.severity.uppercase()}")
            putExtra(
                "detail_location",
                "Sensores: IR ${entity.ir}, PIR ${entity.pir}, SONIDO ${entity.sound}"
            )
            putExtra("detail_timestamp", tsMillis.toString())
            putExtra("detail_ir", entity.ir)
            putExtra("detail_pir", entity.pir)
            putExtra("detail_sound", entity.sound)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("¡ALERTA DE PRIORIDAD ALTA!")
            .setContentText("Se activaron los 3 sensores")
            .setSound(sound)
            .setVibrate(longArrayOf(0, 300, 200, 300))  // vibración fuerte
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(9999, notification)
    }
}
