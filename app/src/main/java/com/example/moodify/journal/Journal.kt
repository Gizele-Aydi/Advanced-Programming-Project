// Journal.kt – using Emotion + BlenderBot 3B
package com.example.moodify.journal

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.moodify.BuildConfig
import com.example.moodify.tasks.TasksScreen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

@Serializable
data class HfRequest(val inputs: String)

@Serializable
data class EmotionLabel(val label: String, val score: Double)

// ——— Groq/OpenAI‐style chat types ———

@Serializable
data class ChatCompletionMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatCompletionMessage>,
    val temperature: Double = 0.7
)

@Serializable
data class ChatCompletionChoice(
    val index: Int,
    val message: ChatCompletionMessage
)

@Serializable
data class ChatCompletionResponse(
    val id: String,

    @SerialName("object")
    val objectType: String,    // now reads from the JSON field "object"

    val created: Long,
    val choices: List<ChatCompletionChoice>
)

// ——— Retrofit interfaces ———

interface HuggingFaceApi {
    @POST("models/{modelId}")
    suspend fun analyzeEmotion(
        @Header("Authorization") auth: String,
        @Path("modelId") modelId: String,
        @Body body: HfRequest
    ): List<List<EmotionLabel>>
}

object HfService {
    private val json = Json { ignoreUnknownKeys = true }
    @OptIn(ExperimentalSerializationApi::class)
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api-inference.huggingface.co/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val api: HuggingFaceApi = retrofit.create(HuggingFaceApi::class.java)
}

interface GroqApi {
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") auth: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}

object GroqService {
    private val json = Json { ignoreUnknownKeys = true }
    @OptIn(ExperimentalSerializationApi::class)
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.groq.com/openai/v1/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val api: GroqApi = retrofit.create(GroqApi::class.java)
}

// ——— Retry helper ———

suspend fun <T> retryOn503(
    times: Int = 3,
    initialDelayMs: Long = 1000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var delayMs = initialDelayMs
    repeat(times - 1) {
        try {
            return block()
        } catch (e: HttpException) {
            if (e.code() != 503) throw e
            kotlinx.coroutines.delay(delayMs)
            delayMs = (delayMs * factor).toLong()
        }
    }
    return block()
}

private const val TAG = "JournalEmotion"

class EmotionViewModel : ViewModel() {
    var inputText by mutableStateOf("")
        private set

    var results by mutableStateOf<List<EmotionLabel>>(emptyList())
        private set

    var blenderReply by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    private val authHeader     = "Bearer ${BuildConfig.HF_API_TOKEN}"
    private val emotionModelId = "j-hartmann/emotion-english-distilroberta-base"
    private val groqAuthHeader = "Bearer ${BuildConfig.GROQ_API_KEY}"
    private val groqModelId    = "llama3-8b-8192"

    private val db      = FirebaseFirestore.getInstance()
    private val userUid get() = FirebaseAuth.getInstance().currentUser?.uid

    fun onInputChange(text: String) {
        inputText = text
    }

    fun analyzeAndSave() {
        val uid = userUid ?: return
        if (inputText.isBlank()) return

        isLoading = true
        viewModelScope.launch {
            try {
                // 1) Emotion classification
                val nested = retryOn503 {
                    HfService.api.analyzeEmotion(authHeader, emotionModelId, HfRequest(inputText))
                }
                results = nested.flatten()

                // 2) Therapist‐style reply via Groq
                val systemPrompt = "You’re a caring therapist. Respond briefly with insight and advice on this journal entry."

                val messages = listOf(
                    ChatCompletionMessage("system", systemPrompt),
                    ChatCompletionMessage("user", inputText)
                )
                val groqResp = retryOn503 {
                    GroqService.api.chatCompletions(
                        groqAuthHeader,
                        ChatCompletionRequest(
                            model       = groqModelId,
                            messages    = messages,
                            temperature = 0.7
                        )
                    )
                }
                blenderReply = groqResp.choices.firstOrNull()?.message?.content

                // 3) Persist to Firestore
                val entryData = hashMapOf(
                    "text"          to inputText,
                    "createdAt"     to Timestamp.now(),
                    "emotions"      to results.map { mapOf("label" to it.label, "score" to it.score) },
                    "blenderReply"  to blenderReply,
                    "suggestedTasks" to emptyList<String>()
                )
                db.collection("users").document(uid)
                    .collection("journalEntries")
                    .add(entryData)
                    .addOnSuccessListener { Log.d(TAG, "Entry saved") }
                    .addOnFailureListener { e -> Log.e(TAG, "Save failed", e) }

            } catch (e: Exception) {
                Log.e(TAG, "Error in analyzeAndSave", e)
                results       = listOf(EmotionLabel("Error", 0.0))
                blenderReply = null
            } finally {
                isLoading = false
            }
        }
    }
}

@Composable
fun EmotionScreen(
    vm: EmotionViewModel = viewModel(),
    navController: NavHostController
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("What's on your mind?", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = vm.inputText,
            onValueChange = vm::onInputChange,
            label = { Text("Journal...") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { vm.analyzeAndSave() },
            enabled = !vm.isLoading,
            modifier = Modifier.align(Alignment.End)
        ) {
            if (vm.isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Analyze & Save")
        }
        Spacer(Modifier.height(16.dp))

        if (vm.results.isNotEmpty()) {
            Text("Emotion Results:", style = MaterialTheme.typography.titleMedium)
            vm.results.forEach { (label, score) ->
                Text("$label: ${(score * 100).toInt()}%")
            }
        }

        vm.blenderReply?.let { reply ->
            Spacer(Modifier.height(12.dp))
            Text("Reflection by AI:", style = MaterialTheme.typography.titleMedium)
            Text(reply, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val encoded = Uri.encode(reply)
                    navController.navigate("tasks?summary=$encoded")
                }
            ) {
                Text("Generate Tasks")
            }
        }
    }
}


fun NavGraphBuilder.journalGraph(navController: NavHostController) {
    composable("journal") {
        EmotionScreen(vm = viewModel(), navController = navController)
    }
    composable(
        route = "tasks?summary={summary}",
        arguments = listOf(navArgument("summary") {
            type = NavType.StringType
            defaultValue = ""
        })
    ){ backStackEntry ->
        val summary = backStackEntry.arguments?.getString("summary").orEmpty()
        com.example.moodify.tasks.TasksScreen(
            summary = summary,
            onBack = { navController.popBackStack() }
        )
    }
}
