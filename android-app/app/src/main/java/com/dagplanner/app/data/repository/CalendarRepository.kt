package com.dagplanner.app.data.repository

import com.dagplanner.app.data.google.GoogleCalendarService
import com.dagplanner.app.data.local.CalendarDao
import com.dagplanner.app.data.model.CalendarEvent
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepository @Inject constructor(
    private val calendarDao: CalendarDao,
    private val googleCalendarService: GoogleCalendarService,
) {
    /** Evenementen voor een specifieke datum (uit lokale cache) */
    fun getEventsForDate(date: LocalDate): Flow<List<CalendarEvent>> =
        calendarDao.getEventsForDate(date)

    /** Evenementen binnen een datumbereik (uit lokale cache) */
    fun getEventsInRange(from: LocalDate, to: LocalDate): Flow<List<CalendarEvent>> =
        calendarDao.getEventsInRange(from, to)

    /**
     * Synchroniseert Google Agenda evenementen en slaat ze op in de lokale database.
     * @param accountName Het Google-account emailadres
     * @param from Startdatum (standaard: begin van huidige maand - 1 maand)
     * @param to Einddatum (standaard: einde van volgende maand)
     */
    suspend fun syncGoogleCalendar(
        accountName: String,
        from: LocalDate = LocalDate.now().withDayOfMonth(1).minusMonths(1),
        to: LocalDate = LocalDate.now().withDayOfMonth(1).plusMonths(2)
    ): Result<Int> {
        val result = googleCalendarService.fetchEvents(accountName, from, to)
        return result.map { events ->
            calendarDao.clearAll()
            calendarDao.insertEvents(events)
            events.size
        }
    }
}
