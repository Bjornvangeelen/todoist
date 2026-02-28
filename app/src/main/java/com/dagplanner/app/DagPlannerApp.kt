package com.dagplanner.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.dagplanner.app.notification.ReminderWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DagPlannerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
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
}
