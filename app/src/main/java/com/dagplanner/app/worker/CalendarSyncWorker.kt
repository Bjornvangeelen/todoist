package com.dagplanner.app.worker

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dagplanner.app.data.google.GoogleCalendarService
import com.dagplanner.app.data.local.AppDatabase
import com.dagplanner.app.data.preferences.UserPreferences
import com.dagplanner.app.data.preferences.dataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class CalendarSyncWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val accountName = context.dataStore.data
            .map { it[UserPreferences.GOOGLE_ACCOUNT_NAME] }
            .firstOrNull() ?: return Result.success()

        val db = Room.databaseBuilder(context, AppDatabase::class.java, "dagplanner.db")
            .fallbackToDestructiveMigration()
            .build()

        return try {
            val calendarService = GoogleCalendarService(context)
            val from = LocalDate.now().withDayOfMonth(1).minusMonths(1)
            val to = LocalDate.now().withDayOfMonth(1).plusMonths(3)
            val result = calendarService.fetchEvents(accountName, from, to)
            result.getOrNull()?.let { events ->
                db.calendarDao().clearAll()
                db.calendarDao().insertEvents(events)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        } finally {
            db.close()
        }
    }
}
