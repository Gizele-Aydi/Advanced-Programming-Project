package com.example.moodify.sleepLog.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun SleepModeScreen(
    onBypass: () -> Unit,
    onDiscard: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sleep Mode Active",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onBypass,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Bypass Sleep Mode")
            }
            Button(
                onClick = onDiscard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Start Sleep Session")
            }
        }
    }
}