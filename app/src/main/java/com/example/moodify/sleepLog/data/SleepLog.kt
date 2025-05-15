package com.example.moodify.sleepLog.data

import com.google.firebase.Timestamp
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
@Serializable
data class SleepLog(
    val date:    String,
    @Contextual val sleepAt: Timestamp,
    @Contextual val wakeAt:  Timestamp
)