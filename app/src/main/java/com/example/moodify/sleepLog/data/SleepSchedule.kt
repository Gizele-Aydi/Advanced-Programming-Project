package com.example.moodify.sleepLog.data

import kotlinx.serialization.Serializable

@Serializable
data class SleepSchedule(
    val id:        String? = null,  // doc ID
    val type:      String? = null,  // "DAILY" | "WEEKDAYS" | …
    val dayOfWeek: Int?    = null,  // 1..7 for DAILY
    val date:      String? = null,  // ISO date for ONCE
    val bedtime:   String? = null,  // e.g. "22:30"
    val wakeup:    String? = null   // e.g. "06:30"
)