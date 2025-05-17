package com.example.moodify.sleepLog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moodify.sleepLog.viewmodel.SleepLogViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SleepChartScreen(vm: SleepLogViewModel = viewModel()) {
    // 1) Observe the logs
    val logs by vm.logs.collectAsState(initial = emptyList())

    // 2) Prepare old‐style date parser/formatter
    val dateParser = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }
    val dateFormatter = remember {
        SimpleDateFormat("MM/dd", Locale.getDefault())
    }

    // 3) Build your entries list: Pair<Label, Hours>
    val entries: List<Pair<String, Float>> = remember(logs) {
        logs.mapNotNull { log ->
            val startMs = log.sleepAt.toDate().time
            val endMs   = log.wakeAt.toDate().time
            val hours   = (endMs - startMs).toFloat() / (1000 * 60 * 60)
            val dateObj = dateParser.parse(log.date) ?: return@mapNotNull null
            val label   = dateFormatter.format(dateObj)
            if (hours >= 0f) label to hours else null
        }
    }

    // 4) Compute max for scaling (at least 8h)
    val maxH = (entries.maxOfOrNull { it.second } ?: 8f).coerceAtLeast(8f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Last ${entries.size} days", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            // 5) Use the items(list) overload
            items(entries) { (day, hrs) ->
                val barHeight = (hrs / maxH * 180).dp
                Column(
                    modifier = Modifier
                        .width(24.dp)
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 6) Height only takes Dp now
                    Box(
                        Modifier
                            .height(barHeight)
                            .fillMaxWidth()
                            .background(Color(0xFF2196F3))
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(day, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
