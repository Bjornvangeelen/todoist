package com.dagplanner.app.data.google

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.dagplanner.app.data.model.CalendarEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleCalendarService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val APP_NAME = "DagPlanner"
        val SCOPES = listOf(
            CalendarScopes.CALENDAR
        )
    }

    /**
     * Bouwt de Google Calendar API client op basis van het gekoppelde account.
     */
    private fun buildCalendarService(accountName: String): Calendar {
        val credential = GoogleAccountCredential.usingOAuth2(context, SCOPES).apply {
            selectedAccountName = accountName
        }
        return Calendar.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName(APP_NAME).build()
    }

    /**
     * Haalt alle agenda-evenementen op binnen een bepaald datumbereik.
     * @param accountName Het Google-account emailadres
     * @param from Startdatum
     * @param to Einddatum
     */
    suspend fun fetchEvents(
        accountName: String,
        from: LocalDate,
        to: LocalDate
    ): Result<List<CalendarEvent>> = withContext(Dispatchers.IO) {
        try {
            val service = buildCalendarService(accountName)
            val zone = ZoneId.systemDefault()

            val timeMin = from.atStartOfDay(zone).toInstant()
                .let { com.google.api.client.util.DateTime(it.toEpochMilli()) }
            val timeMax = to.plusDays(1).atStartOfDay(zone).toInstant()
                .let { com.google.api.client.util.DateTime(it.toEpochMilli()) }

            val allEvents = mutableListOf<CalendarEvent>()

            // Haal alle agenda's op
            val calendars = service.calendarList().list().execute()

            for (calendarEntry in calendars.items ?: emptyList()) {
                val calendarId = calendarEntry.id
                val calendarName = calendarEntry.summary ?: "Agenda"
                val calendarColor = calendarEntry.backgroundColor

                var pageToken: String? = null
                do {
                    val eventsResponse = service.events().list(calendarId)
                        .setTimeMin(timeMin)
                        .setTimeMax(timeMax)
                        .setSingleEvents(true)
                        .setOrderBy("startTime")
                        .setMaxResults(250)
                        .setPageToken(pageToken)
                        .execute()

                    eventsResponse.items?.forEach { event ->
                        val mapped = mapEvent(event, calendarId, calendarName, calendarColor)
                        if (mapped != null) allEvents.add(mapped)
                    }

                    pageToken = eventsResponse.nextPageToken
                } while (pageToken != null)
            }

            Result.success(allEvents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapEvent(
        event: Event,
        calendarId: String,
        calendarName: String,
        calendarColor: String?
    ): CalendarEvent? {
        val id = event.id ?: return null
        val title = event.summary ?: "(Geen titel)"
        val zone = ZoneId.systemDefault()

        return try {
            val isAllDay = event.start?.date != null

            val startDate: LocalDate
            val startTime: LocalTime?
            val endDate: LocalDate
            val endTime: LocalTime?

            if (isAllDay) {
                val startRaw = event.start.date.toStringRfc3339()
                startDate = LocalDate.parse(startRaw)
                startTime = null
                val endRaw = event.end?.date?.toStringRfc3339() ?: startRaw
                endDate = LocalDate.parse(endRaw).minusDays(1) // Google geeft exclusieve einddatum
                endTime = null
            } else {
                val startMs = event.start?.dateTime?.value ?: return null
                val startZdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startMs), zone)
                startDate = startZdt.toLocalDate()
                startTime = startZdt.toLocalTime()

                val endMs = event.end?.dateTime?.value ?: startMs
                val endZdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(endMs), zone)
                endDate = endZdt.toLocalDate()
                endTime = endZdt.toLocalTime()
            }

            CalendarEvent(
                id = "${calendarId}_${id}",
                calendarId = calendarId,
                title = title,
                description = event.description,
                location = event.location,
                startDate = startDate,
                startTime = startTime,
                endDate = endDate,
                endTime = endTime,
                isAllDay = isAllDay,
                colorHex = event.colorId?.let { mapGoogleColorId(it) } ?: calendarColor,
                calendarName = calendarName,
                isRecurring = event.recurringEventId != null,
                htmlLink = event.htmlLink,
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Maakt een nieuw evenement aan in Google Agenda.
     * @param calendarId Agenda-ID (standaard "primary")
     */
    suspend fun createEvent(
        accountName: String,
        calendarId: String = "primary",
        title: String,
        description: String?,
        location: String?,
        isAllDay: Boolean,
        startDate: LocalDate,
        startTime: LocalTime?,
        endDate: LocalDate,
        endTime: LocalTime?,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val service = buildCalendarService(accountName)
            val zone = ZoneId.systemDefault()

            val event = Event().apply {
                summary = title
                if (!description.isNullOrBlank()) this.description = description
                if (!location.isNullOrBlank()) this.location = location
            }

            if (isAllDay) {
                event.start = EventDateTime().setDate(com.google.api.client.util.DateTime(startDate.toString()))
                event.end = EventDateTime().setDate(com.google.api.client.util.DateTime(endDate.plusDays(1).toString()))
            } else {
                val startMs = startDate.atTime(startTime ?: LocalTime.of(9, 0)).atZone(zone).toInstant().toEpochMilli()
                val endMs = endDate.atTime(endTime ?: LocalTime.of(10, 0)).atZone(zone).toInstant().toEpochMilli()
                event.start = EventDateTime().setDateTime(com.google.api.client.util.DateTime(startMs))
                event.end = EventDateTime().setDateTime(com.google.api.client.util.DateTime(endMs))
            }

            service.events().insert(calendarId, event).execute()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Wijzigt een bestaand evenement in Google Agenda.
     */
    suspend fun updateEvent(
        accountName: String,
        event: CalendarEvent,
        title: String,
        description: String?,
        location: String?,
        isAllDay: Boolean,
        startDate: LocalDate,
        startTime: LocalTime?,
        endDate: LocalDate,
        endTime: LocalTime?,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val service = buildCalendarService(accountName)
            val zone = ZoneId.systemDefault()
            val googleEventId = event.id.removePrefix("${event.calendarId}_")

            val existing = service.events().get(event.calendarId, googleEventId).execute()
            existing.summary = title
            existing.description = if (description.isNullOrBlank()) null else description
            existing.location = if (location.isNullOrBlank()) null else location

            if (isAllDay) {
                existing.start = EventDateTime().setDate(com.google.api.client.util.DateTime(startDate.toString()))
                existing.end = EventDateTime().setDate(com.google.api.client.util.DateTime(endDate.plusDays(1).toString()))
            } else {
                val startMs = startDate.atTime(startTime ?: LocalTime.of(9, 0)).atZone(zone).toInstant().toEpochMilli()
                val endMs = endDate.atTime(endTime ?: LocalTime.of(10, 0)).atZone(zone).toInstant().toEpochMilli()
                existing.start = EventDateTime().setDateTime(com.google.api.client.util.DateTime(startMs))
                existing.end = EventDateTime().setDateTime(com.google.api.client.util.DateTime(endMs))
            }

            service.events().update(event.calendarId, googleEventId, existing).execute()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verwijdert een evenement uit Google Agenda.
     */
    suspend fun deleteEvent(
        accountName: String,
        event: CalendarEvent,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val service = buildCalendarService(accountName)
            val googleEventId = event.id.removePrefix("${event.calendarId}_")
            service.events().delete(event.calendarId, googleEventId).execute()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Vertaalt Google Calendar kleur-ID naar hex kleurcode */
    private fun mapGoogleColorId(colorId: String): String? = when (colorId) {
        "1" -> "#7986CB"   // Lavendel
        "2" -> "#33B679"   // Salie
        "3" -> "#8E24AA"   // Druif
        "4" -> "#E67C73"   // Flamingo
        "5" -> "#F6BF26"   // Banaan
        "6" -> "#F4511E"   // Mandarijn
        "7" -> "#039BE5"   // Pauw
        "8" -> "#616161"   // Grafiet
        "9" -> "#3F51B5"   // Bosbei
        "10" -> "#0B8043"  // Basilicum
        "11" -> "#D50000"  // Tomaat
        else -> null
    }
}
