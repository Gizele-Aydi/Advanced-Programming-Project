package com.example.moodify.tasks

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moodify.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.random.Random

// Define colors for the wheel sectors
private val sectorColors = listOf(
    Color(0xFFF88379), // Light Coral
    Color(0xFFADD8E6), // Light Blue
    Color(0xFF98FB98), // Pale Green
    Color(0xFFFFDAB9), // Peach
    Color(0xFFD8BFD8), // Thistle
    Color(0xFFFFA07A), // Light Salmon
    Color(0xFF87CEFA), // Light Sky Blue
    Color(0xFFFFB6C1)  // Light Pink
)

// Define the Poppins font family
private val poppinsFont = FontFamily(
    Font(R.font.poppins, FontWeight.Normal),
    Font(R.font.poppins_light, FontWeight.Light),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@Composable
fun TasksScreen(
    summary: String,
    onBack: () -> Unit,
    vm: TasksViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Kick off either generation or loading
    LaunchedEffect(summary) {
        if (summary.isNotBlank()) vm.generateTasks(summary)
        else vm.loadSavedTasks()
    }

    // Snapshot state
    val tasks by remember { derivedStateOf { vm.tasks } }
    val selected by remember { derivedStateOf { vm.selectedTasks } }
    val spinsLeft by remember { derivedStateOf { vm.maxSpins - vm.spinCount } }
    val canSpin = spinsLeft > 0 && summary.isNotBlank()
    val canConfirm = vm.spinCount >= vm.maxSpins && summary.isNotBlank() && selected.size == 3

    // Spinner animation state
    val rotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    // Background color
    val backgroundColor = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)

    // Function to handle spinning
    fun spinWheel() {
        if (!canSpin || isSpinning) return

        scope.launch {
            isSpinning = true

            // Generate a random spin between 2 and 5 full rotations plus a random offset
            val spinDuration = 3000 // 3 seconds
            val minRotations = 2
            val maxRotations = 5
            val randomRotations = Random.nextDouble(minRotations.toDouble(), maxRotations.toDouble())
            val randomAngle = Random.nextDouble(0.0, 360.0)
            val targetRotation = rotation.value + (randomRotations * 360 + randomAngle).toFloat()

            // Animate the rotation
            rotation.animateTo(
                targetValue = targetRotation,
                animationSpec = tween(
                    durationMillis = spinDuration,
                    easing = FastOutSlowInEasing
                )
            )

            // Calculate the selected task based on the final angle
            val normalizedAngle = (rotation.value % 360 + 360) % 360
            val sectorSize = 360f / tasks.size
            selectedIndex = (normalizedAngle / sectorSize).toInt()

            // Call the ViewModel's spin function to update the selected tasks
            vm.spin()

            delay(500) // Small delay for visual feedback
            isSpinning = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Let's help you feel better!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = poppinsFont,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(16.dp))

            when {
                // 1) Still waiting for data
                tasks.isEmpty() -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFADD8E6),
                            strokeWidth = 3.dp
                        )
                    }
                }

                // 2) Viewing saved tasks from "My Tasks"
                summary.isBlank() -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = cardBackground
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    "Your Tasks",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = poppinsFont,
                                        color = Color.White
                                    ),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                tasks.forEachIndexed { i, t ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF252525)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Text(
                                                "${i + 1}. ${t.task}",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontFamily = poppinsFont,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.White
                                                )
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                t.description,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = poppinsFont,
                                                    color = Color.White.copy(alpha = 0.7f)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFADD8E6),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            "Back",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = poppinsFont,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // 3) Spinning / picking new tasks
                else -> {
                    // Main content with scrolling
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            "Spin the roulette",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = poppinsFont,
                                color = Color.White
                            )
                        )

                        Text(
                            "Complete whichever 3 tasks that you get",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = poppinsFont,
                                color = Color.White.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Spinner Wheel
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(260.dp)
                                .padding(16.dp)
                        ) {
                            // Rotating Wheel
                            Canvas(
                                modifier = Modifier
                                    .size(220.dp)
                                    .rotate(rotation.value)
                                    .shadow(8.dp, CircleShape)
                                    .clip(CircleShape)
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val radius = minOf(canvasWidth, canvasHeight) / 2
                                val center = Offset(canvasWidth / 2, canvasHeight / 2)

                                // Draw sectors
                                val sectorAngle = 360f / tasks.size
                                tasks.forEachIndexed { index, _ ->
                                    val startAngle = index * sectorAngle
                                    drawArc(
                                        color = sectorColors[index % sectorColors.size],
                                        startAngle = startAngle,
                                        sweepAngle = sectorAngle,
                                        useCenter = true,
                                        topLeft = Offset(center.x - radius, center.y - radius),
                                        size = Size(radius * 2, radius * 2)
                                    )
                                }

                                // Draw center circle
                                drawCircle(
                                    color = Color(0xFF252525),
                                    radius = radius * 0.15f,
                                    center = center
                                )

                                // Draw dividing lines
                                for (i in 0 until tasks.size) {
                                    val angle = i * sectorAngle
                                    val radians = angle * (PI / 180f)
                                    val x = cos(radians).toFloat() * radius
                                    val y = sin(radians).toFloat() * radius

                                    drawLine(
                                        color = Color.White.copy(alpha = 0.5f),
                                        start = center,
                                        end = Offset(center.x + x, center.y + y),
                                        strokeWidth = 2f,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }

                            // Center Button
                            Button(
                                onClick = { if (canSpin && !isSpinning) spinWheel() },
                                enabled = canSpin && !isSpinning,
                                modifier = Modifier
                                    .size(80.dp)
                                    .align(Alignment.Center),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFADD8E6),
                                    contentColor = Color.Black,
                                    disabledContainerColor = Color(0xFFADD8E6).copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    "SPIN",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = poppinsFont,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Pointer
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .align(Alignment.TopCenter)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val path = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(size.width / 2, 0f)
                                        lineTo(0f, size.height)
                                        lineTo(size.width, size.height)
                                        close()
                                    }
                                    drawPath(
                                        path = path,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Spins left indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                "Spins left: ",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = poppinsFont,
                                    color = Color.White
                                )
                            )
                            Text(
                                "$spinsLeft",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = poppinsFont,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFADD8E6)
                                )
                            )
                        }

                        // Selected Tasks
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = cardBackground
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    "Selected Tasks",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = poppinsFont,
                                        color = Color.White
                                    ),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // Always show 3 task slots
                                repeat(3) { index ->
                                    val hasTask = index < selected.size
                                    val task = if (hasTask) selected[index] else null

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        // Task number or checkmark
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(
                                                    color = if (hasTask)
                                                        sectorColors[index % sectorColors.size].copy(alpha = 0.2f)
                                                    else
                                                        Color.White.copy(alpha = 0.1f),
                                                    shape = CircleShape
                                                )
                                        ) {
                                            if (hasTask) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = sectorColors[index % sectorColors.size],
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else {
                                                Text(
                                                    "${index + 1}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontFamily = poppinsFont,
                                                        color = Color.White
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(Modifier.width(12.dp))

                                        // Task content or placeholder
                                        if (hasTask) {
                                            Column {
                                                Text(
                                                    task!!.task,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontFamily = poppinsFont,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color.White
                                                    )
                                                )

                                                Text(
                                                    task.description,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = poppinsFont,
                                                        color = Color.White.copy(alpha = 0.7f)
                                                    )
                                                )
                                            }
                                        } else {
                                            Text(
                                                "Task ${index + 1}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = poppinsFont,
                                                    color = Color.White.copy(alpha = 0.5f)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Timer display (just for visual design)
                        Text(
                            "24:00:00",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = poppinsFont,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFADD8E6)
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Confirm button - only show when all 3 tasks are selected and no spins left
                    // Placed outside the scrollable area to ensure it's always visible
                    Spacer(Modifier.height(16.dp))

                    if (canConfirm) {
                        Button(
                            onClick = {
                                vm.saveSelectedTasks { success ->
                                    if (success) onBack()
                                    else Toast.makeText(context, "Save failed", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFADD8E6),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text(
                                "Confirm Tasks",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = poppinsFont,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper extension function for trigonometric calculations
private fun cos(angle: Double): Double = kotlin.math.cos(angle.toDouble())
private fun sin(angle: Double): Double = kotlin.math.sin(angle.toDouble())
