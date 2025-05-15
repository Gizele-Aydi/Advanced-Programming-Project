package com.example.moodify.sleepLog.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.moodify.sleepLog.data.SleepRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SleepUsageReceiver: BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(ctx: Context, intent: Intent) {
        val repo = SleepRepository()
        val now  = Timestamp.now()
        // Fire off in the IO dispatcher
        CoroutineScope(Dispatchers.IO).launch {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> repo.recordSleepStart(now)
                Intent.ACTION_SCREEN_ON  -> repo.recordWakeTime(now)
            }
        }
    }
}

