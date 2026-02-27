# DagPlanner – Android App Setup

## Vereisten

- Android Studio Ladybug (2024.2.1) of nieuwer
- JDK 17
- Android SDK 35
- Google Cloud Console account

---

## Stap 1: Google Cloud Console instellen

1. Ga naar [Google Cloud Console](https://console.cloud.google.com)
2. Maak een nieuw project aan (bijv. "DagPlanner")
3. Activeer de **Google Calendar API**:
   - Ga naar "APIs & Services" → "Library"
   - Zoek "Google Calendar API" en klik "Enable"

### OAuth 2.0 configureren

4. Ga naar "APIs & Services" → "Credentials"
5. Klik "Create Credentials" → "OAuth 2.0 Client IDs"
6. Kies **Android** als applicatietype
7. Vul in:
   - **Package name**: `com.dagplanner.app`
   - **SHA-1 fingerprint**: Voer het SHA-1 certificaat van je keystore in
     ```bash
     # Debug keystore (voor testen):
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
8. Maak ook een **Web application** OAuth Client ID aan (nodig voor de Calendar API scope)
9. Kopieer de **Web Client ID**

### google-services.json

10. Ga naar "Project settings" → "Add Firebase" ÓFTEWEL gebruik de OAuth client ID direct
11. Download `google-services.json` en plaats deze in `app/google-services.json`

---

## Stap 2: OAuth Client ID instellen

In `app/build.gradle.kts`, vervang:
```kotlin
manifestPlaceholders["googleOAuthClientId"] = "YOUR_GOOGLE_OAUTH_CLIENT_ID"
```
door jouw Web Client ID.

---

## Stap 3: Project openen

1. Open Android Studio
2. Kies "Open" → selecteer de `android-app/` map
3. Wacht tot Gradle sync klaar is
4. Voer de app uit op een emulator of fysiek apparaat

---

## Architectuur

```
app/
├── data/
│   ├── google/          # Google Calendar API service
│   ├── local/           # Room database (offline cache)
│   ├── model/           # Data models
│   ├── preferences/     # DataStore gebruikersvoorkeuren
│   └── repository/      # Repository pattern
├── di/                  # Hilt dependency injection
└── ui/
    ├── navigation/      # Navigatie structuur
    ├── screens/
    │   ├── calendar/    # Maandkalender + ViewModel
    │   ├── agenda/      # Dagoverzicht (Google Agenda stijl)
    │   ├── tasks/       # Taken (komende versie)
    │   └── settings/    # Instellingen + Google koppeling
    └── theme/           # Material Design 3 thema
```

## Technologieën

| Technologie | Gebruik |
|---|---|
| Jetpack Compose | UI |
| Material Design 3 | Design systeem |
| Hilt | Dependency injection |
| Room | Lokale database |
| DataStore | Gebruikersvoorkeuren |
| Google Calendar API | Agenda synchronisatie |
| Google Sign-In | OAuth authenticatie |
| Coroutines + Flow | Async operations |
| Navigation Compose | Navigatie |

---

## Play Store voorbereiden

1. Genereer een release keystore:
   ```bash
   keytool -genkey -v -keystore dagplanner-release.keystore \
     -alias dagplanner -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Configureer signing in `build.gradle.kts`
3. Build de release APK/AAB:
   - Build → Generate Signed Bundle/APK
4. Upload naar [Google Play Console](https://play.google.com/console)
