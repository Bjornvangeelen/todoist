package com.dagplanner.app.data.local

import androidx.room.*
import com.dagplanner.app.data.model.Task
import com.dagplanner.app.data.model.TaskType
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE taskType = :type ORDER BY isCompleted ASC, date ASC NULLS LAST, time ASC NULLS LAST, createdAt ASC")
    fun getTasksByType(type: TaskType): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}
