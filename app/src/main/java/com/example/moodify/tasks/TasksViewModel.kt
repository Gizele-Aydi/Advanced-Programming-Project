package com.example.moodify.tasks

// File: tasks/TasksViewModel.kt

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodify.journal.ChatCompletionMessage
import com.example.moodify.journal.ChatCompletionRequest
import com.example.moodify.journal.GroqService
import com.example.moodify.BuildConfig
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class GeneratedTask(
    val task: String,
    val priority: Int,
    val description: String
)

class TasksViewModel : ViewModel() {
    var tasks by mutableStateOf<List<GeneratedTask>>(emptyList())
        private set

    var selectedTasks by mutableStateOf<List<GeneratedTask>>(emptyList())
        private set

    var spinCount by mutableIntStateOf(0)
        private set

    val maxSpins = 3
    private val db     = FirebaseFirestore.getInstance()
    private val uid    get() = FirebaseAuth.getInstance().currentUser?.uid

    private val groqAuthHeader = "Bearer ${BuildConfig.GROQ_API_KEY}"
    private val groqModelId    = "llama3-8b-8192"

    fun generateTasks(summary: String) {
        Log.d("TasksViewModel", "generateTasks summary: $summary")
        viewModelScope.launch {
            try {
                // 1) AI call
                val systemPrompt = """
                    You’re a productivity coach.
                    Return *only* valid JSON—no markdown or backticks.
                    Your response must be a JSON array of exactly 6 objects.
                    Each object must follow this schema and use `\"` to escape any internal quotes:
                    {
                      "task": string,
                      "priority": integer,
                      "description": string
                    }
                    Include one quirky item.
                    Base them on: "$summary"
                """.trimIndent()

                val messages = listOf(
                    ChatCompletionMessage("system", systemPrompt),
                    ChatCompletionMessage("user", summary)
                )
                val resp = GroqService.api.chatCompletions(
                    groqAuthHeader,
                    ChatCompletionRequest(model = groqModelId, messages = messages, temperature = 0.7)
                )

                // 2) Clean and extract array
                val raw = resp.choices.firstOrNull()?.message?.content.orEmpty()
                Log.d("TasksViewModel", "AI raw content: $raw")

                val cleaned = raw.replace("```", "").trim()
                val start = cleaned.indexOf('[')
                val end   = cleaned.lastIndexOf(']')
                require(start != -1 && end > start) { "No JSON array found" }
                val jsonArray = cleaned.substring(start, end + 1)
                Log.d("TasksViewModel", "Parsed jsonArray: $jsonArray")

                // 3) Decode into our data class
                tasks = Json { ignoreUnknownKeys = true }
                    .decodeFromString(ListSerializer(GeneratedTask.serializer()), jsonArray)

            } catch (t: Throwable) {
                Log.e("TasksViewModel", "Error parsing tasks; using placeholders", t)
                // 4) Fallback placeholders
                tasks = List(6) { idx ->
                    GeneratedTask(
                        task        = "Task #${idx + 1}",
                        priority    = 0,
                        description = ""
                    )
                }
            }
            // reset spinner state on new generation
            selectedTasks = emptyList()
            spinCount = 0
        }
    }
    fun saveSelectedTasks(onComplete: (Boolean) -> Unit = {}) {
        val userId = uid ?: run {
            onComplete(false); return
        }

        val toSave = selectedTasks.take(3).map { t ->
            mapOf(
                "task"        to t.task,
                "priority"    to t.priority,
                "description" to t.description
            )
        }

        val doc = mapOf(
            "tasks"   to toSave,
            "savedAt" to Timestamp.now()
        )

        db.collection("users")
            .document(userId)
            .collection("dailyTasks")
            .document("today")
            .set(doc)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
    fun loadSavedTasks() {
        val userId = uid ?: return
        viewModelScope.launch {
            try {
                val doc = db.collection("users")
                    .document(userId)
                    .collection("dailyTasks")
                    .document("today")
                    .get()
                    .await()

                val raw = doc.get("tasks") as? List<Map<String,Any>> ?: emptyList()
                tasks = raw.mapNotNull { m ->
                    val t = m["task"       ] as? String ?: return@mapNotNull null
                    val p = (m["priority"] as? Number)?.toInt() ?: 0
                    val d = m["description"] as? String ?: ""
                    GeneratedTask(task = t, priority = p, description = d)
                }

                // disable any spinning UI
                selectedTasks = tasks.take(3)
                spinCount     = maxSpins

            } catch (e: Exception) {
                Log.e("TasksViewModel", "loadSavedTasks failed", e)
            }
        }
    }
    fun spin() {
        if (spinCount >= maxSpins || tasks.isEmpty()) return
        val remaining = tasks.filter { it !in selectedTasks }
        if (remaining.isEmpty()) return
        val pick = remaining.randomOrNull() ?: return
        selectedTasks = selectedTasks + pick
        spinCount++
    }
}