package com.dagplanner.app.ui.screens.settings;

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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<CalendarRepository> calendarRepositoryProvider;

  public SettingsViewModel_Factory(Provider<UserPreferences> userPreferencesProvider,
      Provider<CalendarRepository> calendarRepositoryProvider) {
    this.userPreferencesProvider = userPreferencesProvider;
    this.calendarRepositoryProvider = calendarRepositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(userPreferencesProvider.get(), calendarRepositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<UserPreferences> userPreferencesProvider,
      Provider<CalendarRepository> calendarRepositoryProvider) {
    return new SettingsViewModel_Factory(userPreferencesProvider, calendarRepositoryProvider);
  }

  public static SettingsViewModel newInstance(UserPreferences userPreferences,
      CalendarRepository calendarRepository) {
    return new SettingsViewModel(userPreferences, calendarRepository);
  }
}
