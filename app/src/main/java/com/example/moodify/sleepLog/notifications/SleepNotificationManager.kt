package com.example.moodify.sleepLog.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

class SleepNotificationManager(val context: Context) {
    fun scheduleReminder(id: Int, bedtimeHour: Int, bedtimeMin: Int) {
        // calculate trigger = tomorrow at (bedtimeHour-1):(bedtimeMin)
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, bedtimeHour - 1)
            set(Calendar.MINUTE, bedtimeMin)
            // if in past, add one day
            if (timeInMillis < System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        val intent = Intent(context, SleepAlarmReceiver::class.java)
            .putExtra("scheduleId", id)
        val pi = PendingIntent.getBroadcast(
            context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val am = context.getSystemService(AlarmManager::class.java)!!
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!am.canScheduleExactAlarms()) {
                    //TODO: prompt user to grant “Alarm & reminder” permission in settings
                }
            }
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
        } catch (e: SecurityException) {
            // handle or log: your app lacks SCHEDULE_EXACT_ALARM
        }

    }
}
