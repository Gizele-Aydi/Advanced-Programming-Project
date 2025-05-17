package com.example.moodify

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.moodify.account.AccountScreen
import com.example.moodify.auth.SignInScreen
import com.example.moodify.auth.SplashScreen
import com.example.moodify.auth.StartScreen
import com.example.moodify.journal.EmotionScreen
import com.example.moodify.sleepLog.ui.SleepChartScreen
import com.example.moodify.sleepLog.ui.SleepLogScreen
import com.example.moodify.sleepLog.ui.SleepScheduleScreen
import com.example.moodify.tasks.TasksScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import SignUpScreen
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

// Define the app's color palette
val LightBlue = Color(0xFFADD8E6)
val LightCoral = Color(0xFFF88379)
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkCardBackground = Color(0xFF252525)

// Define the Poppins font family
val PoppinsFont = FontFamily(
    Font(R.font.poppins, FontWeight.Normal),
    Font(R.font.poppins_light, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_thin, FontWeight.Thin)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply dark theme with custom colors
            MoodifyTheme {
                MoodifyApp()
            }
        }
    }
}

@Composable
fun MoodifyTheme(content: @Composable () -> Unit) {
    val darkColorScheme = darkColorScheme(
        primary = LightCoral,
        secondary = LightBlue,
        background = DarkBackground,
        surface = DarkSurface,
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White
    )

    MaterialTheme(
        colorScheme = darkColorScheme,
        typography = Typography(
            displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = PoppinsFont),
            displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = PoppinsFont),
            displaySmall = MaterialTheme.typography.displaySmall.copy(fontFamily = PoppinsFont),
            headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = PoppinsFont),
            headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = PoppinsFont),
            headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontFamily = PoppinsFont),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = PoppinsFont),
            titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = PoppinsFont),
            titleSmall = MaterialTheme.typography.titleSmall.copy(fontFamily = PoppinsFont),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = PoppinsFont),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFont),
            bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFont),
            labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = PoppinsFont),
            labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = PoppinsFont),
            labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = PoppinsFont)
        ),
        shapes = Shapes(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(16.dp)
        ),
        content = content
    )
}

@Composable
fun HomeScreen(
    onNavigateToJournal: () -> Unit,
    onNavigateToSleepSchedule: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkCardBackground
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    DarkCardBackground,
                                    DarkCardBackground.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            buildAnnotatedString {
                                append("Welcome to ")
                                withStyle(style = SpanStyle(color = LightCoral, fontWeight = FontWeight.SemiBold)) {
                                    append("Moodify")
                                }
                                append("!")
                            },
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Track your mood and discover insights",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Journal button
            Button(
                onClick = onNavigateToJournal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightCoral
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Book,
                        contentDescription = "Journal Icon",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Go to Journal",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            // Sleep Schedule button
            Button(
                onClick = onNavigateToSleepSchedule,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightBlue
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Bedtime,
                        contentDescription = "Sleep Icon",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Sleep Schedule",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodifyApp() {
    val firebaseAuth = remember { FirebaseAuth.getInstance() }

    // Track currentUser in a Compose state
    var currentUser by remember { mutableStateOf(firebaseAuth.currentUser) }
    DisposableEffect(firebaseAuth) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        firebaseAuth.addAuthStateListener(listener)
        onDispose { firebaseAuth.removeAuthStateListener(listener) }
    }

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    // Build NavHost with all routes
    @Composable
    fun Host(navController: NavHostController, currentUser: FirebaseUser?) {
        NavHost(
            navController = navController,
            startDestination = "splash",
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            // Splash screen route
            composable("splash") {
                SplashScreen(
                    onTimeout = {
                        // Navigate to Get Started screen
                        navController.navigate("getStarted") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            // Get Started screen route
            composable("getStarted") {
                StartScreen(
                    onNavigateToSignIn = {
                        navController.navigate("signin") {
                            popUpTo("getStarted") { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate("home") {
                            popUpTo("getStarted") { inclusive = true }
                        }
                    }
                )
            }

            // Account screen route
            composable("account") {
                AccountScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            authGraph(navController)
            journalGraph(navController)
            appGraph(navController)

            composable("sleepSchedule") {
                SleepScheduleScreen(vm = viewModel())
            }
            composable("sleepLog") {
                SleepLogScreen(vm = viewModel())
            }
            composable("sleepChart") {
                SleepChartScreen() // no VM here
            }
        }
    }

    // Determine if we should show the drawer based on current route
    val currentRoute = currentBackStackEntryAsState(navController).value?.destination?.route
    val showDrawer = currentUser != null &&
            currentRoute != "splash" &&
            currentRoute != "getStarted" &&
            currentRoute != "signin" &&
            currentRoute != "signup"

    if (showDrawer) {
        // Only show drawer when logged in and not on auth screens
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = DarkSurface,
                    drawerContentColor = Color.White
                ) {
                    DrawerContent(navController, firebaseAuth) {
                        scope.launch { drawerState.close() }
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            // Remove title text, show logo on the right
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo),
                                    contentDescription = "Moodify Logo",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(end = 16.dp)
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open()
                                    else drawerState.close()
                                }
                            }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = DarkBackground,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { padding ->
                Box(Modifier.padding(padding)) {
                    Host(navController, currentUser)
                }
            }
        }
    } else {
        // No drawer for signed-out state or auth screens
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Host(navController, currentUser)
        }
    }
}

// Helper function to get current route
@Composable
private fun currentBackStackEntryAsState(navController: NavHostController): State<androidx.navigation.NavBackStackEntry?> {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return remember { derivedStateOf { navBackStackEntry } }
}

fun NavGraphBuilder.authGraph(navController: NavHostController) {
    composable("signup") {
        SignUpScreen(
            navToSignIn = {
                navController.navigate("signin") { popUpTo("signup") { inclusive = true } }
            }
        )
    }
    composable("signin") {
        SignInScreen(
            navToSignUp = {
                navController.navigate("signup") { popUpTo("signin") { inclusive = true } }
            },
            navToHome = {
                navController.navigate("home") { popUpTo("signin") { inclusive = true } }
            }
        )
    }
}

fun NavGraphBuilder.journalGraph(navController: NavHostController) {
    // Emotion screen route
    composable("journal") {
        EmotionScreen(vm = viewModel(), navController = navController)
    }

    // Tasks screen route
    composable(
        route = "tasks?summary={summary}",
        arguments = listOf(navArgument("summary") {
            type = NavType.StringType
            defaultValue = ""
        })
    ) { backStackEntry ->
        val summary = backStackEntry.arguments?.getString("summary").orEmpty()
        Log.d("TasksScreen", "Received summary: $summary")
        TasksScreen(
            summary = summary,
            onBack = { navController.popBackStack() }
        )
    }
}

fun NavGraphBuilder.appGraph(navController: NavHostController) {
    composable("home") {
        HomeScreen(
            onNavigateToJournal = { navController.navigate("journal") },
            onNavigateToSleepSchedule = { navController.navigate("sleepSchedule") }
        )
    }
}

@Composable
fun DrawerContent(
    navController: NavHostController,
    auth: FirebaseAuth,
    onItemClick: () -> Unit
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val db = Firebase.firestore
    var displayName by remember { mutableStateOf("") }

    // Fetch user display name
    LaunchedEffect(auth.currentUser) {
        auth.currentUser?.let { user ->
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        displayName = document.getString("displayName") ?: user.email ?: ""
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(DarkSurface)
            .padding(vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = LightCoral.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.take(1).uppercase(),
                        color = LightCoral,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        fontFamily = PoppinsFont
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontFamily = PoppinsFont
                    ),
                    color = Color.White
                )
            }

            // Right: App Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Moodify Logo",
                modifier = Modifier
                    .size(30.dp)
            )
        }


        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = Color.White.copy(alpha = 0.1f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Section
        Text(
            text = "NAVIGATION",
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = PoppinsFont
            ),
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        // Home Button
        NavigationDrawerItem(
            label = {
                Text(
                    "Home",
                    fontFamily = PoppinsFont
                )
            },
            selected = currentRoute == "home",
            onClick = {
                navController.navigate("home") { popUpTo(0) }
                onItemClick()
            },
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = LightBlue.copy(alpha = 0.15f),
                unselectedContainerColor = Color.Transparent,
                selectedIconColor = LightBlue,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                selectedTextColor = Color.White,
                unselectedTextColor = Color.White.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Journal Button
        NavigationDrawerItem(
            label = {
                Text(
                    "Journal",
                    fontFamily = PoppinsFont
                )
            },
            selected = currentRoute == "journal",
            onClick = {
                navController.navigate("journal") { popUpTo(0) }
                onItemClick()
            },
            icon = {
                Icon(
                    Icons.Default.Book,
                    contentDescription = null
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = LightBlue.copy(alpha = 0.15f),
                unselectedContainerColor = Color.Transparent,
                selectedIconColor = LightBlue,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                selectedTextColor = Color.White,
                unselectedTextColor = Color.White.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // My Tasks Button
        NavigationDrawerItem(
            label = {
                Text(
                    "My Tasks",
                    fontFamily = PoppinsFont
                )
            },
            selected = currentRoute?.startsWith("tasks") == true,
            onClick = {
                navController.navigate("tasks?summary=") {
                    popUpTo("journal") { inclusive = false }
                }
                onItemClick()
            },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = LightBlue.copy(alpha = 0.15f),
                unselectedContainerColor = Color.Transparent,
                selectedIconColor = LightBlue,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                selectedTextColor = Color.White,
                unselectedTextColor = Color.White.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Sleep Schedule Button
        NavigationDrawerItem(
            label = {
                Text(
                    "Sleep Schedule",
                    fontFamily = PoppinsFont
                )
            },
            selected = currentRoute == "sleepSchedule",
            onClick = {
                navController.navigate("sleepSchedule")
                onItemClick()
            },
            icon = {
                Icon(
                    Icons.Default.Bedtime,
                    contentDescription = null
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = LightBlue.copy(alpha = 0.15f),
                unselectedContainerColor = Color.Transparent,
                selectedIconColor = LightBlue,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                selectedTextColor = Color.White,
                unselectedTextColor = Color.White.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = Color.White.copy(alpha = 0.1f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Account Section
        Text(
            text = "ACCOUNT",
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = PoppinsFont
            ),
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        // My Account Button
        NavigationDrawerItem(
            label = {
                Text(
                    "My Account",
                    fontFamily = PoppinsFont
                )
            },
            selected = currentRoute == "account",
            onClick = {
                navController.navigate("account")
                onItemClick()
            },
            icon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = LightBlue.copy(alpha = 0.15f),
                unselectedContainerColor = Color.Transparent,
                selectedIconColor = LightBlue,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                selectedTextColor = Color.White,
                unselectedTextColor = Color.White.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Sign Out Button
        NavigationDrawerItem(
            label = {
                Text(
                    "Sign Out",
                    fontFamily = PoppinsFont
                )
            },
            selected = false,
            onClick = {
                auth.signOut()
                navController.navigate("signin") { popUpTo(0) }
                onItemClick()
            },
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                unselectedTextColor = Color.White.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // App version at bottom
        Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = PoppinsFont
            ),
            color = Color.White.copy(alpha = 0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            textAlign = TextAlign.Center
        )
    }
}
