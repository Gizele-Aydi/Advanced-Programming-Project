package com.example.moodify.sleepLog.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.moodify.R
import com.example.moodify.sleepLog.ui.SleepModeActivity

object SleepLogConstants {
    const val CHANNEL_ID   = "sleep_reminder_channel"
    private const val CHANNEL_NAME = "Sleep Reminders"

    /** Call once at app startup to register the channel on O+ */
    fun createNotificationChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = ctx.getSystemService(NotificationManager::class.java)
            mgr?.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }
}
class SleepAlarmReceiver: BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        // 1) Get the scheduleId so notifications don’t collide
        val id = intent.getIntExtra("scheduleId", 0)

        val notification = NotificationCompat.Builder(ctx, SleepLogConstants.CHANNEL_ID)
            .setContentTitle("Upcoming Bedtime")
            .setContentText("Your scheduled sleep is in 1 hour")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(
                PendingIntent.getActivity(
                    ctx, 0,
                    Intent(ctx, SleepModeActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                ), true
            )
            .addAction(
                android.R.drawable.ic_lock_idle_alarm,
                "Start Sleep Mode",
                PendingIntent.getActivity(
                    ctx, 1,
                    Intent(ctx, SleepModeActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .build()

        // 2) Check notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) return
        }

        NotificationManagerCompat.from(ctx).notify(id, notification)
    }
}
