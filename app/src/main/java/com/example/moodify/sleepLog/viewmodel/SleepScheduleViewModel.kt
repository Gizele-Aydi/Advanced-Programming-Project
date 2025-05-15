package com.example.moodify.sleepLog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodify.sleepLog.data.SleepRepository
import com.example.moodify.sleepLog.data.SleepSchedule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SleepScheduleViewModel : ViewModel() {
    private val repo = SleepRepository()

    val schedules = repo.getSchedulesFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun save(schedule: SleepSchedule) = viewModelScope.launch {
        repo.saveSchedule(schedule)
    }
}