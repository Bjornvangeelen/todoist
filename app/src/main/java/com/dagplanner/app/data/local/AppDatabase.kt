package com.dagplanner.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dagplanner.app.data.model.CalendarEvent
import com.dagplanner.app.data.model.Task

@Database(
    entities = [CalendarEvent::class, Task::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calendarDao(): CalendarDao
    abstract fun taskDao(): TaskDao
}
