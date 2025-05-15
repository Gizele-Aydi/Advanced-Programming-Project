package com.example.moodify.sleepLog.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodify.sleepLog.data.SleepRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class SleepLogViewModel : ViewModel() {
    private val repo = SleepRepository()

    val logs = repo.getSleepLogsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}