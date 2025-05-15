package com.example.moodify.sleepLog.ui


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moodify.sleepLog.viewmodel.SleepLogViewModel

@Composable
fun SleepLogScreen(vm: SleepLogViewModel = viewModel()) {
    val logs by vm.logs.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Sleep Logs", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        logs.forEach { l ->
            Text("- ${l.date}: slept @ ${l.sleepAt} woke @ ${l.wakeAt}")
        }
    }
}