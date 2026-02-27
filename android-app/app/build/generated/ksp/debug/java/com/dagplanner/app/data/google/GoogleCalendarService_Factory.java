package com.dagplanner.app.data.google;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class GoogleCalendarService_Factory implements Factory<GoogleCalendarService> {
  private final Provider<Context> contextProvider;

  public GoogleCalendarService_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public GoogleCalendarService get() {
    return newInstance(contextProvider.get());
  }

  public static GoogleCalendarService_Factory create(Provider<Context> contextProvider) {
    return new GoogleCalendarService_Factory(contextProvider);
  }

  public static GoogleCalendarService newInstance(Context context) {
    return new GoogleCalendarService(context);
  }
}
