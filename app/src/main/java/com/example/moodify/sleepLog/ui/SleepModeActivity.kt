package com.example.moodify.sleepLog.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class SleepModeActivity: ComponentActivity() {
    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        setContent {
            SleepModeScreen(
                onBypass = { finish() },
                onDiscard = { finish() }
            )
        }
    }
}
