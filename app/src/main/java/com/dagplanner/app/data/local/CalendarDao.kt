package com.dagplanner.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dagplanner.app.data.model.CalendarEvent
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_events WHERE startDate >= :from AND startDate <= :to ORDER BY startDate ASC, startTime ASC")
    fun getEventsInRange(from: LocalDate, to: LocalDate): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE startDate = :date ORDER BY isAllDay DESC, startTime ASC")
    fun getEventsForDate(date: LocalDate): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CalendarEvent>)

    @Query("DELETE FROM calendar_events")
    suspend fun clearAll()

    @Query("DELETE FROM calendar_events WHERE calendarId = :calendarId")
    suspend fun clearByCalendar(calendarId: String)
}
