# JARVIS AI (Android)

A personal voice assistant for Android. Speak to it and it can open apps,
tap buttons, scroll, read the screen, type text and search YouTube, but only
after you turn on its Accessibility permission.

## Features in this version
- Wake word "Hey Jarvis" (English and Bangla text match), continuous listening
- Speaks replies (Text To Speech; Bangla voice when the reply is Bangla)
- Offline commands, no paid API needed:
  - `open <app name>`
  - `back`, `home`, `scroll down`, `scroll up`
  - `read screen`, `tap <text on screen>`, `type <text>`
  - `play <song>` and `youtube <search>` (opens YouTube search)
  - `remember <something>` (saved in the Room database)
- Confirmation before sensitive words (send, delete, pay, transfer, uninstall)
- EMERGENCY STOP button that halts actions and listening
- Chat history saved with Room
- Black and neon-blue screen with an animated circle
- Settings: Bangla recognition toggle, shortcut to Accessibility settings

## Not yet included (next steps)
- Optional AI brain (Gemini or OpenAI). Keep the key in `local.properties`, never in code.
- Porcupine wake-word engine for true always-on detection.
- Choosing YouTube results by number, and playback buttons inside YouTube.

## How to build (beginner steps)
1. Install Android Studio (latest stable) from developer.android.com/studio.
2. Unzip `JarvisAI.zip`. In Android Studio choose **File > Open** and pick the `JarvisAI` folder.
3. Wait for the progress bar at the bottom ("Gradle sync") to finish. If it offers to
   create a Gradle wrapper, accept.
4. Connect your phone with USB debugging on, or use an emulator. Press the green **Run** arrow.
5. For an APK file: **Build > Build App Bundle(s) / APK(s) > Build APK(s)**.
   Android Studio shows where the `.apk` was saved.
6. In the app, allow microphone access, then tap **Settings > Enable accessibility control**
   and turn on **JARVIS AI**.
7. Tap **Mic** and say "Hey Jarvis, open YouTube".

## Privacy and safety
- Accessibility control works only while you have enabled it, and only on screens you can see.
- It does not bypass lock screens, banking protections or Android security.
- Keep EMERGENCY STOP within reach while testing.
