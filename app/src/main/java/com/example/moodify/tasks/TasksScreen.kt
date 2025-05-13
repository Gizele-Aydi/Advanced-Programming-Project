package com.example.moodify.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TasksScreen(
    summary: String,
    onBack: () -> Unit,
    vm: TasksViewModel = viewModel()
) {
    // Kick off task generation when the summary changes
    LaunchedEffect(summary) {
        vm.generateTasks(summary)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Your Daily Tasks",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(12.dp))

        if (vm.tasks.isEmpty()) {
            // Still waiting on the AI
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Text("Spins left: ${vm.maxSpins - vm.spinCount}")
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { vm.spin() },
                enabled = vm.spinCount < vm.maxSpins
            ) {
                Text("Spin")
            }

            Spacer(Modifier.height(16.dp))

            // Display the picked tasks with title + description
            vm.selectedTasks.forEachIndexed { index, generatedTask ->
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "${index + 1}. ${generatedTask.task}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = generatedTask.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Once you've spun three times, show Confirm
            if (vm.spinCount >= vm.maxSpins) {
                Spacer(Modifier.height(20.dp))
                Button(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Confirm Tasks")
                }
            }
        }
    }
}