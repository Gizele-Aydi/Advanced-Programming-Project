// Journal.kt – using Emotion + BlenderBot 3B
package com.example.moodify.journal

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.moodify.BuildConfig
import com.example.moodify.R
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

// Define colors
private val DarkBackground = Color(0xFF121212)
private val DarkCardBackground = Color(0xFF252525)
private val LightBlue = Color(0xFFADD8E6)
private val LightCoral = Color(0xFFF88379)

// Define font
private val Prociono = FontFamily(
    Font(R.font.prociono)
)

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
                val systemPrompt = "You're a caring therapist. Respond briefly with insight and advice on this journal entry."

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmotionScreen(
    vm: EmotionViewModel = viewModel(),
    navController: NavHostController
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "What's on your mind?",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = Prociono,
                    fontSize = 32.sp,
                    color = Color.White.copy(alpha = 0.8f)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Journal input field
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkCardBackground
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "How do you feel?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = Prociono,
                            color = LightBlue,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val focusManager = LocalFocusManager.current

                    OutlinedTextField(
                        value = vm.inputText,
                        onValueChange = vm::onInputChange,
                        placeholder = {
                            Text(
                                "Write your thoughts here...",
                                fontFamily = Prociono,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            cursorColor = LightBlue,
                            focusedBorderColor = LightBlue,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = Prociono,
                            color = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Text
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            }
                        )
                    )



                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { vm.analyzeAndSave() },
                            enabled = !vm.isLoading && vm.inputText.isNotBlank(),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LightBlue,
                                contentColor = Color.Black,
                                disabledContainerColor = LightBlue.copy(alpha = 0.5f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            ),
                            modifier = Modifier.height(48.dp)
                        ) {
                            if (vm.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Analyze & Save",
                                        fontFamily = Prociono,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 16.sp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // AI Response section
            if (vm.blenderReply != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkCardBackground
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            "Reflection",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = Prociono,
                                color = LightCoral,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            vm.blenderReply ?: "",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = Prociono,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 24.sp,
                                fontStyle = FontStyle.Italic
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                val encoded = Uri.encode(vm.blenderReply)
                                navController.navigate("tasks?summary=$encoded")
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LightBlue,
                                contentColor = Color.Black
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                "Generate Tasks",
                                fontFamily = Prociono,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Emotion Results section (more subtle)
            if (vm.results.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkCardBackground.copy(alpha = 0.7f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Emotion Analysis",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = Prociono,
                                color = LightBlue,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            vm.results.forEach { (label, score) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = Prociono,
                                            color = Color.White
                                        )
                                    )

                                    Text(
                                        "${(score * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = Prociono,
                                            color = LightBlue
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
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
