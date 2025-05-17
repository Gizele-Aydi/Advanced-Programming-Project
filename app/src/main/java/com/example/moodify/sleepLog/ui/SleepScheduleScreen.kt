package com.example.moodify.sleepLog.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moodify.sleepLog.data.SleepSchedule
import com.example.moodify.sleepLog.viewmodel.SleepScheduleViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScheduleScreen(vm: SleepScheduleViewModel = viewModel()) {
    val schedules by vm.schedules.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<SleepSchedule?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Sleep Schedules",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editing = null
                    showDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Schedule")
            }
        }
    ) { paddingValues ->
        if (schedules.isEmpty()) {
            EmptyScheduleView(
                onAddClick = {
                    editing = null
                    showDialog = true
                },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(schedules) { schedule ->
                    ScheduleCard(
                        schedule = schedule,
                        onEdit = {
                            editing = schedule
                            showDialog = true
                        }
                    )
                }
            }
        }
    }

    // Edit/Add dialog
    if (showDialog) {
        SleepScheduleDialog(
            initial = editing,
            onSave = { schedule ->
                vm.save(schedule)
                showDialog = false
            },
            onCancel = { showDialog = false }
        )
    }
}

@Composable
fun EmptyScheduleView(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "No sleep schedules yet",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "Add a sleep schedule to track your sleep patterns",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Schedule")
            }
        }
    }
}

@Composable
fun ScheduleCard(
    schedule: SleepSchedule,
    onEdit: () -> Unit
) {
    val scheduleInfo = when(schedule.type) {
        "ONCE" -> "Once on ${schedule.date ?: ""}"
        "DAILY" -> "Every day"
        "WEEKLY" -> {
            val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            "Every ${days[(schedule.dayOfWeek ?: 1) - 1]}"
        }
        "WEEKDAYS" -> "Weekdays (Mon-Fri)"
        "WEEKENDS" -> "Weekends (Sat-Sun)"
        else -> schedule.type
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (scheduleInfo != null) {
                    Text(
                        scheduleInfo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Bedtime",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        schedule.bedtime?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.width(16.dp))

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Wakeup",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        schedule.wakeup?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScheduleDialog(
    initial: SleepSchedule?,
    onSave: (SleepSchedule) -> Unit,
    onCancel: () -> Unit
) {
    val ctx = LocalContext.current

    // Formatter for ISO date strings
    val isoFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayFormat = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) }

    // 1) Type selection
    val types = listOf("DAILY", "WEEKLY", "WEEKDAYS", "WEEKENDS", "ONCE")
    val typeLabels = listOf("Daily", "Weekly", "Weekdays", "Weekends", "Once")
    var selectedTypeIndex by remember {
        mutableIntStateOf(types.indexOf(initial?.type).takeIf { it >= 0 } ?: 0)
    }
    val selectedType = types[selectedTypeIndex]

    // 2) If WEEKLY, pick one weekday by name
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    var selectedDayIndex by remember {
        mutableIntStateOf(
            initial?.dayOfWeek?.let { (it - 1).coerceIn(0, 6) } ?: 0
        )
    }

    // 3) If ONCE, pick a single date
    var selectedDateIso by remember {
        mutableStateOf(initial?.date ?: isoFormat.format(Date()))
    }
    val selectedDateFormatted = remember(selectedDateIso) {
        try {
            val date = isoFormat.parse(selectedDateIso)
            displayFormat.format(date)
        } catch (e: Exception) {
            selectedDateIso
        }
    }

    // 4) Always pick bedtime/wakeup times
    var bedtime by remember { mutableStateOf(initial?.bedtime ?: "22:30") }
    var wakeup by remember { mutableStateOf(initial?.wakeup ?: "06:30") }

    AlertDialog(
        onDismissRequest = onCancel,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        title = {
            Text(
                if (initial == null) "Add Sleep Schedule" else "Edit Sleep Schedule",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Schedule Type Selector
                Text(
                    "Schedule Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    typeLabels.forEachIndexed { index, label ->
                        FilterChip(
                            selected = index == selectedTypeIndex,
                            onClick = { selectedTypeIndex = index },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Day Selection for Weekly
                if (selectedType == "WEEKLY") {
                    Text(
                        "Day of Week",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    ) {
                        items(daysOfWeek) { day ->
                            val index = daysOfWeek.indexOf(day)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedDayIndex = index }
                                    .background(
                                        if (index == selectedDayIndex)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            Color.Transparent
                                    )
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = index == selectedDayIndex,
                                    onClick = { selectedDayIndex = index }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    day,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (index == selectedDayIndex)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Date Picker for ONCE
                if (selectedType == "ONCE") {
                    Text(
                        "Date",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDatePicker(ctx, selectedDateIso) { newDate ->
                                    selectedDateIso = newDate
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                selectedDateFormatted,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // Time Selection
                Text(
                    "Sleep Times",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Bedtime selector
                    OutlinedCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                showTimePicker(ctx, bedtime) { h, m ->
                                    bedtime = "%02d:%02d".format(h, m)
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Bedtime",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                bedtime,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Wakeup selector
                    OutlinedCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                showTimePicker(ctx, wakeup) { h, m ->
                                    wakeup = "%02d:%02d".format(h, m)
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Wakeup",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                wakeup,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        SleepSchedule(
                            id = initial?.id,
                            type = types[selectedTypeIndex],
                            dayOfWeek = if (types[selectedTypeIndex] == "WEEKLY") selectedDayIndex + 1 else null,
                            date = if (types[selectedTypeIndex] == "ONCE") selectedDateIso else null,
                            bedtime = bedtime,
                            wakeup = wakeup
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}

private fun showDatePicker(
    context: Context,
    initialDate: String,
    onDateSelected: (String) -> Unit
) {
    val cal = Calendar.getInstance().apply {
        try {
            val parts = initialDate.split("-").map { it.toInt() }
            set(parts[0], parts[1] - 1, parts[2])
        } catch (_: Exception) {}
    }
    DatePickerDialog(
        context,
        { _, y, m, d ->
            onDateSelected(String.format("%04d-%02d-%02d", y, m + 1, d))
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun showTimePicker(
    context: Context,
    initialTime: String,
    onTimeSelected: (Int, Int) -> Unit
) {
    val parts = initialTime.split(":").map { it.toIntOrNull() ?: 0 }
    TimePickerDialog(
        context,
        { _, hour, min -> onTimeSelected(hour, min) },
        parts.getOrElse(0) { 22 },
        parts.getOrElse(1) { 30 },
        true
    ).show()
}
