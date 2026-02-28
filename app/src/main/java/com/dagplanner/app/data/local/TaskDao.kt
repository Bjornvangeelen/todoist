package com.dagplanner.app.data.local

import androidx.room.*
import com.dagplanner.app.data.model.Task
import com.dagplanner.app.data.model.TaskType
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE taskType = :type ORDER BY isCompleted ASC, CASE WHEN date IS NULL THEN 1 ELSE 0 END ASC, date ASC, CASE WHEN time IS NULL THEN 1 ELSE 0 END ASC, time ASC, createdAt ASC")
    fun getTasksByType(type: TaskType): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}
