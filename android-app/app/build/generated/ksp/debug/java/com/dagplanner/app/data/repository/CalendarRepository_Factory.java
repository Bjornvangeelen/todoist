package com.dagplanner.app.data.repository;

import com.dagplanner.app.data.google.GoogleCalendarService;
import com.dagplanner.app.data.local.CalendarDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class CalendarRepository_Factory implements Factory<CalendarRepository> {
  private final Provider<CalendarDao> calendarDaoProvider;

  private final Provider<GoogleCalendarService> googleCalendarServiceProvider;

  public CalendarRepository_Factory(Provider<CalendarDao> calendarDaoProvider,
      Provider<GoogleCalendarService> googleCalendarServiceProvider) {
    this.calendarDaoProvider = calendarDaoProvider;
    this.googleCalendarServiceProvider = googleCalendarServiceProvider;
  }

  @Override
  public CalendarRepository get() {
    return newInstance(calendarDaoProvider.get(), googleCalendarServiceProvider.get());
  }

  public static CalendarRepository_Factory create(Provider<CalendarDao> calendarDaoProvider,
      Provider<GoogleCalendarService> googleCalendarServiceProvider) {
    return new CalendarRepository_Factory(calendarDaoProvider, googleCalendarServiceProvider);
  }

  public static CalendarRepository newInstance(CalendarDao calendarDao,
      GoogleCalendarService googleCalendarService) {
    return new CalendarRepository(calendarDao, googleCalendarService);
  }
}
