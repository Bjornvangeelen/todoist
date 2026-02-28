package com.dagplanner.app.data.local

import androidx.room.TypeConverter
import com.dagplanner.app.data.model.TaskPriority
import com.dagplanner.app.data.model.TaskType
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String = priority.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority =
        try { TaskPriority.valueOf(value) } catch (e: Exception) { TaskPriority.NONE }

    @TypeConverter
    fun fromTaskType(type: TaskType): String = type.name

    @TypeConverter
    fun toTaskType(value: String): TaskType =
        try { TaskType.valueOf(value) } catch (e: Exception) { TaskType.TASK }
}
