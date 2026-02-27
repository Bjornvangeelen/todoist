package com.dagplanner.app.di

import android.content.Context
import androidx.room.Room
import com.dagplanner.app.data.local.AppDatabase
import com.dagplanner.app.data.local.CalendarDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "dagplanner.db"
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideCalendarDao(db: AppDatabase): CalendarDao = db.calendarDao()
}
