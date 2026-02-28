package com.dagplanner.app.data.repository

import com.dagplanner.app.data.local.TaskDao
import com.dagplanner.app.data.model.Task
import com.dagplanner.app.data.model.TaskType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    fun getTasksByType(type: TaskType): Flow<List<Task>> = taskDao.getTasksByType(type)

    suspend fun upsertTask(task: Task) = taskDao.upsertTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun toggleComplete(task: Task) = taskDao.upsertTask(task.copy(isCompleted = !task.isCompleted))
}
