# Project Specification: Aevum

## 1. Vision & Identity
- **Name:** Aevum
- **Theme:** Strict **Dark Mode UI**. Use a deep charcoal background (#121212) with neon cyan or electric violet accents for interactive elements.
- **Tone:** Minimalist, high-contrast, and "Agentic" (sleek and modern).

## 2. Technical Requirements

### A. Days Counter (The "Origin" Tab)
- **Function:** Tracks days elapsed since a user-defined event.
- **Persistence:** Must use `DataStore` or `SharedPreferences` to ensure the "Start Date" survives app kills or reboots.
- **UI:** A large, centered numerical display. Below it, a "Reset to Today" button with a confirmation haptic vibration.

### B. Voice Stopwatch (The "Vocal" Tab)
- **Voice Commands:**
    - "Start" -> Begin timer.
    - "Stop" / "Freeze" -> Pause timer.
    - "Reset" -> Clear timer.
- **Voice Feedback (TTS):** - The app must announce the time in English (e.g., "Five minutes, zero seconds").
    - **Frequency:** Announce every 1 minute by default, or provide a toggle for "10-second intervals."
- **Continuous Listening:** Implement `SpeechRecognizer` with a loop to ensure it stays active without the user needing to press a button repeatedly.

## 3. Implementation Instructions for Antigravity Agent

### Step 1: UI Foundation
"Initialize a Jetpack Compose project. Set the theme to a permanent Dark Theme using Material3. Force Dark Mode colors even if the system is in Light Mode. Create a Bottom Navigation bar with two items: 'Origin' and 'Vocal'."

### Step 2: Build 'Origin' (Days Counter)
"Create the Days Counter logic. When the user hits 'Reset', store `System.currentTimeMillis()` in persistent storage. Calculate the difference between 'Now' and the stored time, converting it to an integer (Days)."

### Step 3: Build 'Vocal' (Voice Stopwatch)
"Implement a `Service` to handle the stopwatch so it runs accurately in the background. Integrate `TextToSpeech` for the audible count. Use `RecognitionListener` for voice commands. Ensure the UI shows a 'Listening...' status when the microphone is active."

### Step 4: Edge Case Handling
- **Permission Flow:** Add logic to request `RECORD_AUDIO` permission immediately when the 'Vocal' tab is opened.
- **Audio Focus:** Ensure the TTS (speaking the time) doesn't trigger the SpeechRecognizer (listening for commands) to prevent an infinite loop of the app 'talking to itself'.

### Step 5: Finalization & APK
"Optimize the build. Once complete, run `./gradlew assembleDebug` and provide the download link for the Aevum_debug.apk."