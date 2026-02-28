package com.dagplanner.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        val taskType = inputData.getString(KEY_TYPE) ?: "Taak"

        val nm = context.getSystemService(NotificationManager::class.java)
        ensureChannel(nm)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Herinnering: $taskType")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify(title.hashCode(), notification)
        return Result.success()
    }

    private fun ensureChannel(nm: NotificationManager) {
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Herinneringen",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply { description = "Taak- en boodschappenherinneringen" }
            )
        }
    }

    companion object {
        const val CHANNEL_ID = "reminders"
        const val KEY_TITLE = "title"
        const val KEY_TYPE = "taskType"
    }
}
