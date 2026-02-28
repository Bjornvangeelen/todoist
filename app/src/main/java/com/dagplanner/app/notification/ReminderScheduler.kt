package com.dagplanner.app.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.dagplanner.app.data.model.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun schedule(task: Task) {
        val reminder = task.reminder ?: run { cancel(task.id); return }
        val taskDate = task.date ?: run { cancel(task.id); return }
        val taskTime = task.time ?: LocalTime.of(9, 0)

        val offsetMinutes = when (reminder) {
            "5min"  -> 5L
            "15min" -> 15L
            "30min" -> 30L
            "1h"    -> 60L
            "1d"    -> 60L * 24L
            else    -> return
        }

        val triggerAt = LocalDateTime.of(taskDate, taskTime).minusMinutes(offsetMinutes)
        val delayMs = ChronoUnit.MILLIS.between(LocalDateTime.now(), triggerAt)
        if (delayMs <= 0) return

        val typeLabel = if (task.taskType.name == "SHOPPING") "Boodschap" else "Taak"
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    ReminderWorker.KEY_TITLE to task.title,
                    ReminderWorker.KEY_TYPE to typeLabel,
                )
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork("reminder_${task.id}", ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(taskId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("reminder_$taskId")
    }
}
