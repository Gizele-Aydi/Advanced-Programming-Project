package com.example.moodify.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodify.R
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(
    onNavigateToSignIn: () -> Unit,
    onNavigateToHome: () -> Unit,
    auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    // State for staggered animations
    var showLogo by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    // Trigger animations with staggered delays
    LaunchedEffect(Unit) {
        showLogo = true
        kotlinx.coroutines.delay(400)
        showTitle = true
        kotlinx.coroutines.delay(300)
        showSubtitle = true
        kotlinx.coroutines.delay(400)
        showButton = true
    }

    // Define colors
    val overlayColor = Color(0x66000F24) // Semi-transparent navy
    val lightCoral = Color(0xFFCCE6FF)
    val buttonColor = Color(0xFFF29F9F) // Pink color for the button

    val density = LocalDensity.current

    val infiniteTransition = rememberInfiniteTransition(label = "LogoPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoScale"
    )

    val poppinsSemiBold = FontFamily(
        Font(R.font.poppins_semibold, FontWeight.SemiBold)
    )
    val poppinsItalic = FontFamily(
        Font(R.font.poppins_italic, FontWeight.Normal)
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Full-screen background image with blur
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(3.dp)
        )

        // Semi-transparent overlay for better text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overlayColor)
        )

        // Content column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Logo with fade-in and scale animation
            AnimatedVisibility(
                visible = showLogo,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 800,
                        easing = FastOutSlowInEasing
                    )
                ) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(
                        durationMillis = 800,
                        easing = FastOutSlowInEasing
                    )
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.lg),
                    contentDescription = "Moodify Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 32.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }

            // App title with slide-up and fade-in animation
            AnimatedVisibility(
                visible = showTitle,
                enter = slideInVertically(
                    initialOffsetY = { with(density) { 40.dp.roundToPx() } },
                    animationSpec = tween(
                        durationMillis = 600,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 600,
                        easing = FastOutSlowInEasing
                    )
                )
            ) {
                Text(
                    text = "Moodify",
                    fontFamily = poppinsSemiBold,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Subtitle with fade-in animation
            AnimatedVisibility(
                visible = showSubtitle,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 800,
                        easing = FastOutSlowInEasing
                    )
                )
            ) {
                Text(
                    text = "Your local Mood companion",
                    fontFamily = poppinsItalic,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light,
                    fontStyle = FontStyle.Italic,
                    color = lightCoral,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 48.dp)
                )
            }

            // Get Started Button with animation
            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 800,
                        easing = FastOutSlowInEasing
                    )
                ) + slideInVertically(
                    initialOffsetY = { with(density) { 40.dp.roundToPx() } },
                    animationSpec = tween(
                        durationMillis = 800,
                        easing = FastOutSlowInEasing
                    )
                )
            ) {
                Button(
                    onClick = {
                        // Check if user is already authenticated
                        if (auth.currentUser != null) {
                            onNavigateToHome()
                        } else {
                            onNavigateToSignIn()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(28.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth(0.7f) // 70% of screen width for better proportions
                ) {
                    Text(
                        text = "Get Started",
                        fontFamily = poppinsSemiBold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
