package com.dagplanner.app.data.repository

import com.dagplanner.app.data.model.Task
import com.dagplanner.app.data.model.TaskPriority
import com.dagplanner.app.data.model.TaskType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreShoppingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun shoppingCollection(householdCode: String) =
        firestore.collection("households").document(householdCode).collection("shopping")

    fun getItems(householdCode: String): Flow<List<Task>> = callbackFlow {
        val listener = shoppingCollection(householdCode)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { it.toTask() } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun upsertItem(householdCode: String, task: Task) {
        shoppingCollection(householdCode)
            .document(task.id)
            .set(task.toMap(), SetOptions.merge())
            .await()
    }

    suspend fun deleteItem(householdCode: String, task: Task) {
        shoppingCollection(householdCode)
            .document(task.id)
            .delete()
            .await()
    }

    suspend fun householdExists(householdCode: String): Boolean {
        val doc = firestore.collection("households").document(householdCode).get().await()
        return doc.exists()
    }

    suspend fun createHousehold(householdCode: String) {
        firestore.collection("households").document(householdCode)
            .set(mapOf("createdAt" to System.currentTimeMillis()))
            .await()
    }
}

private fun Task.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "title" to title,
    "date" to date?.toString(),
    "time" to time?.toString(),
    "label" to label,
    "priority" to priority.name,
    "deadline" to deadline?.toString(),
    "location" to location,
    "reminder" to reminder,
    "isCompleted" to isCompleted,
    "createdAt" to createdAt,
)

private fun com.google.firebase.firestore.DocumentSnapshot.toTask(): Task? {
    return try {
        Task(
            id = getString("id") ?: id,
            title = getString("title") ?: return null,
            taskType = TaskType.SHOPPING,
            date = getString("date")?.let { LocalDate.parse(it) },
            time = getString("time")?.let { LocalTime.parse(it) },
            label = getString("label"),
            priority = try {
                TaskPriority.valueOf(getString("priority") ?: "NONE")
            } catch (e: Exception) {
                TaskPriority.NONE
            },
            deadline = getString("deadline")?.let { LocalDate.parse(it) },
            location = getString("location"),
            reminder = getString("reminder"),
            isCompleted = getBoolean("isCompleted") ?: false,
            createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
        )
    } catch (e: Exception) {
        null
    }
}
