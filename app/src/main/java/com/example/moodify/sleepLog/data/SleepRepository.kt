// SleepRepository.kt
package com.example.moodify.sleepLog.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SleepRepository {
    private val db   = FirebaseFirestore.getInstance()
    private val uid  get() = FirebaseAuth.getInstance().currentUser?.uid

    /** Save or overwrite a schedule */
    suspend fun saveSchedule(schedule: SleepSchedule) {
        val u = uid ?: return
        val col = db.collection("users/$u/sleepSchedules")

        // pull out id once so Kotlin can smart-cast it
        val id = schedule.id
        if (id.isNullOrBlank()) {
            // no existing doc ID → create a new one
            col.add(schedule).await()
        } else {
            // non-null, non-blank ID → update the existing doc
            col.document(id).set(schedule).await()
        }
    }


    /** Stream all schedules */
    fun getSchedulesFlow(): Flow<List<SleepSchedule>> = callbackFlow {
        val u = uid ?: run { close(); return@callbackFlow }
        val sub = db.collection("users/$u/sleepSchedules")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents
                    ?.mapNotNull { it.toObject(SleepSchedule::class.java)?.copy(id = it.id) }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { sub.remove() }
    }

    /** Record the actual start of sleep */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun recordSleepStart(at: Timestamp) {
        val u = uid ?: return
        val doc = db.collection("users/$u/sleepLogs")
            .document(at.toDate().toInstant().toString().substring(0,10))
        // create or merge
        doc.set(mapOf("date" to at.toDate().toInstant().toString().substring(0,10),
            "sleepAt" to at), SetOptions.merge()).await()
    }

    /** Record the actual wake up */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun recordWakeTime(at: Timestamp) {
        val u = uid ?: return
        val doc = db.collection("users/$u/sleepLogs")
            .document(at.toDate().toInstant().toString().substring(0,10))
        doc.set(mapOf("wakeAt" to at), SetOptions.merge()).await()
    }

    /** Stream all logs */
    fun getSleepLogsFlow(): Flow<List<SleepLog>> = callbackFlow {
        val u = uid ?: run { close(); return@callbackFlow }
        val sub = db.collection("users/$u/sleepLogs")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents
                    ?.mapNotNull { it.toObject(SleepLog::class.java) }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { sub.remove() }
    }
}
