# Tourist Guide Chat (J.A.R.V.I.S)

An Android chat app where the user talks to **J.A.R.V.I.S**, an AI-powered virtual assistant that acts as a tourist guide. The app sends each message, together with the device's location and the recent chat history, to a backend agent. The agent answers with recommendations about nearby places (restaurants, landmarks, events, etc., within roughly 1000 m).

This repository contains the **Android client only**. The backend (`/api/chat`) is a separate service and is not part of this project.

## Features

- Chat UI built with Jetpack Compose and Material 3, with a landscape/tablet-aware layout.
- Location-aware answers: the last known device location (lat/long) is sent with every request.
- Agent replies rendered as Markdown.
- "Thinking…" placeholder and loading indicator while waiting for the agent.
- Friendly error message when the request fails.
- Local chat history in a Room database, **expiring after 24 hours**. Expired messages are removed whenever the app resumes.
- Info dialog explaining what J.A.R.V.I.S can do.
- Anonymous per-install user ID (random UUID stored in `SharedPreferences`).

## Tech stack

| Area | Library |
|---|---|
| Language | Kotlin 2.3.0 |
| UI | Jetpack Compose (BOM 2024.09.00), Material 3 |
| Architecture | MVVM (`ViewModel` + `StateFlow`) |
| Networking | Retrofit 3.0.0, Gson converter, OkHttp |
| Persistence | Room 2.8.4 (via KSP) |
| Location | Google Play Services Location (`FusedLocationProviderClient`) |
| Markdown | `compose-markdown` (jeziellago, via JitPack) |
| Images | Coil Compose |

Build config: Android Gradle Plugin 8.13.2, `compileSdk`/`targetSdk` 36, `minSdk` 26, Java/JVM target 11.

## Project structure

```
app/src/main/java/com/cristobal/chat/
├── MainActivity.kt          # Entry point; requests location permissions, hosts ChatRoomView,
│                            # purges expired messages on resume
├── LMApplication.kt         # Application singleton (provides global context)
├── api_services/
│   ├── ChatService.kt       # Retrofit interface: POST chat
│   └── RetrofitInstance.kt  # Retrofit/OkHttp setup, base URL, auth header interceptor
├── components/
│   └── ChatRoomView.kt      # Compose UI: title, message list, input box, info dialog
├── models/
│   ├── Message.kt           # Room entity (id, role, content, timestamp)
│   ├── MessageRequest.kt    # Request payload sent to the backend
│   └── MessageResponse.kt   # Response payload (agent_response)
├── services/
│   └── LocationService.kt   # Wraps FusedLocationProviderClient (last known location)
├── storage/
│   ├── DAOs/MessageDao.kt
│   ├── repositories/MessageRepository.kt
│   └── rooms/               # AppDatabase + DatabaseBuilder (DB name: "user-database")
├── ui/theme/                # Compose theme (Color, Type, Theme)
├── utils/AppUtils.kt        # Orientation helper
└── viewmodels/
    └── ChatRoomsViewModel.kt  # Chat state, sending messages, persistence
```

## How it works

1. On launch, `MainActivity` asks for fine/coarse location permission. If both are denied, the app closes.
2. `ChatRoomsViewModel` loads the stored messages from Room and fetches the last known location.
3. When the user sends a message, it is added to the list along with a "Thinking…" placeholder, then posted to the backend.
4. On success, the placeholder is replaced by the agent's reply, and both the user message and the reply are saved to the local database. On failure, the placeholder is replaced by an error message (not persisted).
5. Messages older than 24 hours are deleted when the app is resumed.

## Backend API

`POST {BASE_URL}chat` with headers `Authorization: Bearer <token>` and `Content-Type: application/json`.

Request body:

```json
{
  "userId": "uuid-of-this-install",
  "message": { "id": "...", "role": "user", "content": "Where can I eat nearby?", "timestamp": 1700000000000 },
  "lat": 40.4168,
  "long": -3.7038,
  "chatHistory": [ { "id": "...", "role": "user | model", "content": "...", "timestamp": 1700000000000 } ]
}
```

Response body:

```json
{ "agent_response": "Markdown text from the agent" }
```

## Getting started

### Prerequisites

- Android Studio (recent version compatible with AGP 8.13.2) and JDK 11+
- An Android device or emulator running API 26+ (set a mock location on the emulator for meaningful results)
- A running backend that implements the API above

### Configure the backend

The backend address is currently hard-coded for a local network:

- Base URL: `RetrofitInstance.kt` (`http://localhost:8000/api/`)
- Cleartext HTTP allowed for that host: `app/src/main/res/xml/network_security_config.xml`
- Auth token: passed to `AuthInterceptor` in `RetrofitInstance.kt`

Update the base URL and the `network_security_config.xml` domain to match your server (or switch to HTTPS and remove the cleartext exception).

### Build and run

```bash
./gradlew assembleDebug        # build a debug APK
./gradlew installDebug         # install on a connected device/emulator
./gradlew test                 # unit tests
./gradlew connectedAndroidTest # instrumented tests (needs a device/emulator)
```

Or open the project in Android Studio and press **Run**.

## Known limitations / notes

- The backend URL and bearer token are hard-coded in source. Before publishing, move them to build config or secrets, and rotate the token if the repository is shared.
- Expiry is based on the oldest stored message: when it is older than 24 hours, all stored messages are deleted.
- If no location is available, `0.0, 0.0` is sent to the backend.
- Tests are still the Android Studio templates (`ExampleUnitTest`, `ExampleInstrumentedTest`).
- Release builds have minification disabled.
