package com.dagplanner.app.di;

import com.dagplanner.app.data.local.AppDatabase;
import com.dagplanner.app.data.local.CalendarDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class AppModule_ProvideCalendarDaoFactory implements Factory<CalendarDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideCalendarDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public CalendarDao get() {
    return provideCalendarDao(dbProvider.get());
  }

  public static AppModule_ProvideCalendarDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideCalendarDaoFactory(dbProvider);
  }

  public static CalendarDao provideCalendarDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideCalendarDao(db));
  }
}
