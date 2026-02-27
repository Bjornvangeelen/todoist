package com.dagplanner.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey
    val id: String,
    val calendarId: String,
    val title: String,
    val description: String? = null,
    val location: String? = null,
    val startDate: LocalDate,
    val startTime: LocalTime? = null,      // null = hele dag
    val endDate: LocalDate,
    val endTime: LocalTime? = null,
    val isAllDay: Boolean = false,
    val colorHex: String? = null,
    val calendarName: String = "Google Agenda",
    val isRecurring: Boolean = false,
    val htmlLink: String? = null,
)
