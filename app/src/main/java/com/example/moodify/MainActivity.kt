package com.example.moodify


import SignUpScreen
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.moodify.auth.SignInScreen
import com.example.moodify.journal.EmotionScreen
import com.example.moodify.sleepLog.ui.SleepChartScreen
import com.example.moodify.sleepLog.ui.SleepLogScreen
import com.example.moodify.sleepLog.ui.SleepScheduleScreen
import com.example.moodify.tasks.TasksScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoodifyApp()
        }
    }
}
@Composable
fun HomeScreen(onNavigateToJournal: () -> Unit,
               onNavigateToSleepSchedule: () -> Unit  // ← new
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to Moodify!", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Track your mood and discover insights", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onNavigateToJournal) {
                Text("Go to Journal")
            }
            Button(onClick = onNavigateToSleepSchedule) {
                Text("Sleep Schedule")
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodifyApp() {
    val firebaseAuth = remember { FirebaseAuth.getInstance() }

    // 1) Track currentUser in a Compose state
    var currentUser by remember { mutableStateOf(firebaseAuth.currentUser) }
    DisposableEffect(firebaseAuth) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        firebaseAuth.addAuthStateListener(listener)
        onDispose { firebaseAuth.removeAuthStateListener(listener) }
    }

    val navController = rememberNavController()
    val scope         = rememberCoroutineScope()
    val drawerState   = rememberDrawerState(DrawerValue.Closed)

    // 2) Build a single NavHost with all routes
    @Composable
    fun Host(navController: NavHostController, currentUser: FirebaseUser?) {
        NavHost(
            navController    = navController,
            startDestination = if (currentUser != null) "journal" else "signup"
        ) {
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


    if (currentUser != null) {
        // 3) Only show drawer when logged in
        ModalNavigationDrawer(
            drawerState   = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    DrawerContent(navController, firebaseAuth) {
                        scope.launch { drawerState.close() }
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title          = { Text("Moodify") },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open()
                                    else drawerState.close()
                                }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            ) { padding ->
                Box(Modifier.padding(padding)) {
                    Host(navController, currentUser)
                }
            }
        }
    } else {
        // 4) No drawer for signed-out state
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Host(navController, currentUser)
        }
    }
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
    // 1) Emotion screen route
    composable("journal") {
        EmotionScreen(vm = viewModel(), navController = navController)
    }

    // 2) Tasks screen route (indented to match the journal route)
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
            onNavigateToJournal      = { navController.navigate("journal") },
            onNavigateToSleepSchedule = { navController.navigate("sleepSchedule") }
        )    }
}

@Composable
fun DrawerContent(
    navController: NavHostController,
    auth: FirebaseAuth,
    onItemClick: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        TextButton(onClick = {
            navController.navigate("home") { popUpTo(0) }
            onItemClick()
        }) { Text("Home") }
        Spacer(Modifier.height(8.dp))

        // ← NEW: My Tasks
        TextButton(onClick = {
            // Navigate to tasks without a summary → loads saved ones
            navController.navigate("tasks?summary=") {
                popUpTo("journal") { inclusive = false }
            }
            onItemClick()
        }) {
            Text("My Tasks")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = {
            navController.navigate("sleepSchedule")
            onItemClick()
        }) { Text("Sleep Schedule") }

        TextButton(onClick = {
            auth.signOut()
            navController.navigate("signin") { popUpTo(0) }
            onItemClick()
        }) { Text("Sign Out") }
    }
}
