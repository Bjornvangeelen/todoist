package com.dagplanner.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dagplanner.app.notification.ReminderWorker
import com.dagplanner.app.worker.CalendarSyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class DagPlannerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleBackgroundCalendarSync()
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                ReminderWorker.CHANNEL_ID,
                "Herinneringen",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Taak- en boodschappenherinneringen" }
        )
    }

    private fun scheduleBackgroundCalendarSync() {
        val request = PeriodicWorkRequestBuilder<CalendarSyncWorker>(2, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "calendar_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
