package com.dagplanner.app.ui.screens.calendar;

import com.dagplanner.app.data.preferences.UserPreferences;
import com.dagplanner.app.data.repository.CalendarRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class CalendarViewModel_Factory implements Factory<CalendarViewModel> {
  private final Provider<CalendarRepository> repositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  public CalendarViewModel_Factory(Provider<CalendarRepository> repositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.repositoryProvider = repositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public CalendarViewModel get() {
    return newInstance(repositoryProvider.get(), userPreferencesProvider.get());
  }

  public static CalendarViewModel_Factory create(Provider<CalendarRepository> repositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new CalendarViewModel_Factory(repositoryProvider, userPreferencesProvider);
  }

  public static CalendarViewModel newInstance(CalendarRepository repository,
      UserPreferences userPreferences) {
    return new CalendarViewModel(repository, userPreferences);
  }
}
