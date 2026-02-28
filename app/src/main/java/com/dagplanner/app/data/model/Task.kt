package com.dagplanner.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

enum class TaskPriority(val label: String) {
    NONE("Geen"),
    LOW("Laag"),
    MEDIUM("Middel"),
    HIGH("Hoog"),
}

enum class TaskType { TASK, SHOPPING }

enum class TaskRecurrence(val label: String) {
    NONE("Geen"),
    DAILY("Dagelijks"),
    WEEKLY("Wekelijks"),
    MONTHLY("Maandelijks"),
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val taskType: TaskType = TaskType.TASK,
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val label: String? = null,
    val priority: TaskPriority = TaskPriority.NONE,
    val deadline: LocalDate? = null,
    val location: String? = null,
    val reminder: String? = null,
    val recurrence: TaskRecurrence = TaskRecurrence.NONE,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
