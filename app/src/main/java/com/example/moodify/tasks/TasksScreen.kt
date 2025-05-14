package com.example.moodify.tasks

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
@Composable
fun TasksScreen(
    summary: String,
    onBack:   () -> Unit,
    vm:       TasksViewModel = viewModel()
) {
    val context = LocalContext.current

    // Kick off either generation or loading
    LaunchedEffect(summary) {
        if (summary.isNotBlank()) vm.generateTasks(summary)
        else                    vm.loadSavedTasks()
    }

    // Snapshot state
    val tasks        by remember { derivedStateOf { vm.tasks } }
    val selected     by remember { derivedStateOf { vm.selectedTasks } }
    val spinsLeft    by remember { derivedStateOf { vm.maxSpins - vm.spinCount } }
    val canSpin      = spinsLeft > 0 && summary.isNotBlank()
    val canConfirm   = vm.spinCount >= vm.maxSpins && summary.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Your Daily Tasks", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        when {
            // 1) Still waiting for data
            tasks.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            // 2) Viewing saved tasks from "My Tasks"
            summary.isBlank() -> {
                tasks.forEachIndexed { i, t ->
                    Text("${i + 1}. ${t.task}", style = MaterialTheme.typography.titleMedium)
                    Text(t.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.weight(1f))  // push button to bottom
                Button(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Back")
                }
            }

            // 3) Spinning / picking new tasks
            else -> {
                Text("Spins left: $spinsLeft")
                Spacer(Modifier.height(8.dp))

                Button(onClick = { vm.spin() }, enabled = canSpin) {
                    Text("Spin")
                }

                Spacer(Modifier.height(16.dp))
                selected.forEachIndexed { i, t ->
                    Text("${i+1}. ${t.task}", style = MaterialTheme.typography.titleMedium)
                    Text(t.description,     style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                }

                if (canConfirm) {
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = {
                            vm.saveSelectedTasks { success ->
                                if (success) onBack()
                                else Toast.makeText(context, "Save failed", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Confirm Tasks")
                    }
                }
            }
        }
    }
}
