# Explorer 3 Investigation Report: R4 (Automatic Landscape Orientation) & R5 (Local Build Verification)

**Author**: Explorer 3  
**Target Project**: Lekker Charades (`c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades`)  
**State Folder**: `.agents/teamwork/teamwork_explorer_survey_3`  
**Date**: 2026-11-12  

---

## 1. Observation

### 1.1 Architecture & State Management (No GameViewModel)
- Direct inspection of the codebase confirmed that **no `GameViewModel.kt` exists**.
- Searching across all Kotlin files for `ViewModel` returned zero matches:
  ```powershell
  Get-ChildItem -Path app\src\main -Filter "*ViewModel*" -Recurse # returned empty
  Get-ChildItem -Path app\src -Recurse -Filter *.kt | Select-String -Pattern "ViewModel" # 0 matches
  ```
- All active gameplay state (timer, scores, cards, tilt status, guide visibility, reactions) is held in `app/src/main/java/com/example/ui/GamePlayScreen.kt` using Jetpack Compose `remember { mutableStateOf(...) }` and `mutableIntStateOf(...)` (lines 149–188).
- Shared and persistent session results are passed via callbacks (`onFinishGame: (GameSession) -> Unit`, `onExit: () -> Unit`) to `MainFeatureScreen.kt` (lines 316–327), which records them in `LocalPlayerStore.kt` and `GameSessionRepository.kt`.

### 1.2 Screen Orientation Configuration in AndroidManifest & MainActivity
- In `app/src/main/AndroidManifest.xml` (lines 23–34):
  ```xml
  <activity
      android:name=".MainActivity"
      android:exported="true"
      android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
      android:label="@string/app_name"
      android:theme="@style/Theme.MyApplication">
      <intent-filter>
          <action android:name="android.intent.action.MAIN" />
          <category android:name="android.intent.category.LAUNCHER" />
      </intent-filter>
  </activity>
  ```
  - **No `android:screenOrientation` attribute is declared** on `MainActivity`.
  - Crucially, `android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"` **is present**. This guarantees that changing orientation does **not** destroy and recreate `MainActivity`, allowing Jetpack Compose to recompose smoothly without losing local in-memory state.
- In `app/src/main/java/com/example/MainActivity.kt` (lines 26–45):
  - `MainActivity` extends `ComponentActivity`, calls `enableEdgeToEdge()`, and sets `MzansiCharadesTheme { Surface { AppContent() } }`.
  - It does not modify orientation in `onCreate`.

### 1.3 Existing Orientation Handling in GamePlayScreen.kt
- In `app/src/main/java/com/example/ui/GamePlayScreen.kt` (lines 198–226):
  ```kotlin
  // Immersive full-screen mode and automatic landscape orientation during active play
  val view = LocalView.current
  val activity = remember(context, view) { view.context.findActivity() ?: context.findActivity() }
  val initialOrientation = remember(activity) {
    activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }
  val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
  val shouldUseLandscape = isGuideShown && !isTeamIntermission && !isGameOver

  DisposableEffect(activity, shouldUseLandscape) {
    activity?.requestedOrientation = if (shouldUseLandscape) {
      ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    } else {
      initialOrientation
    }
    onDispose { }
  }

  DisposableEffect(view, activity) {
    val window = activity?.window
    val controller = window?.let { WindowCompat.getInsetsController(it, view) }
    controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    controller?.hide(WindowInsetsCompat.Type.systemBars())
    view.keepScreenOn = true
    onDispose {
      activity?.requestedOrientation = initialOrientation
      controller?.show(WindowInsetsCompat.Type.systemBars())
      view.keepScreenOn = false
    }
  }
  ```
- **Observations on current behavior**:
  1. Helper `Context.findActivity()` (lines 122–126) unwraps `ContextWrapper` safely.
  2. Because `AndroidManifest.xml` does not set `android:screenOrientation="portrait"`, `activity?.requestedOrientation` evaluates to `ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED` (`-1`).
  3. `shouldUseLandscape` is gated by `isGuideShown && !isTeamIntermission && !isGameOver`.
     - Upon entering `GamePlayScreen`, `isGuideShown` is `false`. The initial tutorial (`TiltGuideScreen`) displays in portrait.
     - When the player taps "I'm Ready, Start Countdown", `isGuideShown` becomes `true`, switching orientation to `ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE` for both the 3-2-1 countdown (lines 394–468) and active word guessing (lines 868–1324).
     - When the round ends (`timeLeft <= 0`), `isGameOver = true`, triggering `shouldUseLandscape = false`, flipping orientation back to portrait for the vertical Game Over summary and TikTok share card (lines 560–866).
     - In "Party Teams" mode, halftime switch (`isTeamIntermission = true`) reverts to portrait so players can hand the phone upright to Team B.
  4. **Fragmented cleanup**: The first `DisposableEffect` has an empty `onDispose { }`. The second `DisposableEffect` restores `initialOrientation` on unmount. If `initialOrientation` is `SCREEN_ORIENTATION_UNSPECIFIED` and the phone is held horizontally on exit, Android keeps the Lobby in landscape unless explicitly forced to portrait.

### 1.4 Accelerometer Tilt Sensor Logic
- In `app/src/main/java/com/example/ui/GamePlayScreen.kt` (lines 310–350):
  ```kotlin
  DisposableEffect(gameMode, isPlaying) {
    if (isPlaying && sensorManager != null) {
      val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
      val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
          val now = System.currentTimeMillis()
          val z = event.values[2]

          if (abs(z) < 5.0f) {
            isTiltArmed = true
            return
          }

          if (!isTiltArmed || now - lastTiltTime < 1100L) return

          if (z < -7.0f) {
            isTiltArmed = false
            lastTiltTime = now
            onCorrect()
          } else if (z > 7.0f) {
            isTiltArmed = false
            lastTiltTime = now
            onPass()
          }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
      }
      accelerometer?.let {
        sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
      }
      onDispose {
        isTiltArmed = false
        sensorManager.unregisterListener(listener)
      }
    } else {
      onDispose {}
    }
  }
  ```
- **Sensor axis analysis**:
  - The detection relies solely on `event.values[2]`, which is the **Z-axis** of `Sensor.TYPE_ACCELEROMETER`.
  - In Android's sensor coordinates, Z is normal (perpendicular) to the screen face.
  - When the phone is held against the forehead facing forward, the phone screen is vertical: gravity projection on Z is near zero (`abs(z) < 5.0f`), arming the sensor (`isTiltArmed = true`).
  - Tilting the forehead down points the screen towards the ground, producing `z < -7.0f` -> triggers `onCorrect()`.
  - Tilting the forehead up points the screen towards the ceiling, producing `z > 7.0f` -> triggers `onPass()`.
  - Rotating the phone between portrait, landscape, or reverse landscape rotates solely around the Z-axis vector; **the Z-axis reading is completely invariant under planar orientation changes**.
  - No gyroscope is used; accelerometer suffices across all devices.
  - Touch button fallbacks ("Pass" and "Correct" buttons) are rendered when `hasAccelerometer == false` (lines 1280–1320).

### 1.5 Gradle Build Environment & Prerequisites (R5)
- **Root build configuration**:
  - `build.gradle.kts`: Declares AGP, Kotlin Compose, KSP, Secrets, and Google Services plugins via `libs.plugins`.
  - `settings.gradle.kts`: Configures Foojay resolver `1.0.0`, repositories `google()` and `mavenCentral()`, and `rootProject.name = "Mzansi Charades"`.
  - `gradle.properties`:
    - `org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8`
    - `org.gradle.parallel=true`
    - `kotlin.compiler.execution.strategy=in-process`
    - `googleServices.missing.passthrough=true`
- **App build configuration (`app/build.gradle.kts`)**:
  - `compileSdk { version = release(36) { minorApiLevel = 1 } }`
  - `minSdk = 24`, `targetSdk = 36`
  - `JavaVersion.VERSION_11` compileOptions
  - `buildFeatures { compose = true; buildConfig = true }`
  - `signingConfigs.debugConfig`: Points to `${rootDir}/debug.keystore`, storePassword `"android"`, keyAlias `"androiddebugkey"`, keyPassword `"android"`.
  - `debug.keystore` is present in root (2,666 bytes).
  - `googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }` ensures missing `google-services.json` generates a warning rather than failing the build.
- **Dependency & Plugin Versions (`gradle/libs.versions.toml`)**:
  - Android Gradle Plugin: `agp = "9.1.1"`
  - Kotlin: `kotlin = "2.2.10"`
  - Compose BOM: `composeBom = "2024.09.00"`
  - KSP: `googleDevtoolsKsp = "2.3.5"`
  - Gradle Distribution (`gradle/wrapper/gradle-wrapper.properties`): `gradle-9.3.1-bin.zip`
- **Missing Wrapper Tooling**:
  - `gradlew` (Unix script) is **missing** from root.
  - `gradlew.bat` (Windows script) is **missing** from root.
  - `gradle/wrapper/gradle-wrapper.jar` is **missing** from `gradle/wrapper/`.
- **Local Host Environment (Windows)**:
  - System default Java on PATH: Oracle JDK 26 (`java version "26.0.2"`).
  - Installed OpenJDK 21 LTS: `C:\Program Files\Android\openjdk\jdk-21.0.8` (Microsoft OpenJDK 21.0.8+9-LTS).
  - Android Studio JBR: `C:\Program Files\Android\Android Studio\jbr` (OpenJDK 25.0.4).
  - Android SDK: Configured in `local.properties` as `sdk.dir=C:\\Users\\kevin\\AppData\\Local\\Android\\Sdk`.
    - Installed platforms: `platforms\android-37.0`.
    - Installed build-tools: `build-tools\36.0.0`.
    - Licenses accepted: `licenses\android-sdk-license` present.

---

## 2. Logic Chain

```
[Observation 1.1: No GameViewModel]
  └─> Game state is purely in GamePlayScreen Compose state.
      No ViewModel refactoring needed; orientation should be managed via Composable lifecycle effects.

[Observation 1.2 & 1.3: AndroidManifest lacks screenOrientation & initialOrientation is UNSPECIFIED]
  └─> Default requestedOrientation is -1 (UNSPECIFIED).
      When the user plays in landscape and exits back to Lobby while holding the phone horizontally,
      the system keeps the app in landscape if auto-rotate is enabled.
      └─> Conclusion: MainActivity in AndroidManifest.xml should declare android:screenOrientation="portrait"
          (or GamePlayScreen should explicitly restore SCREEN_ORIENTATION_PORTRAIT).

[Observation 1.3: GamePlayScreen uses SCREEN_ORIENTATION_SENSOR_LANDSCAPE]
  └─> SCREEN_ORIENTATION_SENSOR_LANDSCAPE allows both 90° and 270° rotations.
      This supports users holding the phone left-handed or right-handed (USB port facing left or right).
      configChanges="orientation|screenSize|screenLayout" prevents Activity recreation,
      so game state and timer survive the rotation seamlessly.

[Observation 1.4: Tilt logic relies on Z-axis (event.values[2])]
  └─> Z-axis is normal to the phone screen.
      Rotation between portrait and landscape rotates about the Z vector.
      Values on Z do not invert, swap, or degrade when rotating to landscape.
      Forehead tilt gestures (nod down = correct, tilt up = pass) operate reliably in landscape.

[Observation 1.5: Missing gradlew, gradlew.bat, gradle-wrapper.jar]
  └─> Running ./gradlew assembleDebug fails immediately because the wrapper executable is absent.
      └─> Solution: Restore standard Gradle wrapper binaries (gradlew, gradlew.bat, gradle-wrapper.jar).
          Taxido_driver/android contains standard gradle-wrapper.jar and gradlew scripts.

[Observation 1.5: JDK & Toolchain Compatibility]
  └─> Oracle JDK 26 on system PATH is too experimental for AGP 9.1.1 / Gradle 9.3.1.
      Microsoft OpenJDK 21 LTS is already installed at C:\Program Files\Android\openjdk\jdk-21.0.8.
      Setting JAVA_HOME to OpenJDK 21 LTS provides a compliant, stable compilation environment.
      local.properties points to Android SDK with build-tools 36.0.0 and accepted licenses.
      assembleDebug task will compile and produce app/build/outputs/apk/debug/app-debug.apk.
```

---

## 3. Caveats

1. **Pre-Game Guide vs Immediate Landscape**:
   - In `GamePlayScreen.kt`, the tutorial (`TiltGuideScreen`) is currently shown in portrait before the countdown.
   - If requirement R4 ("Navigating into active gameplay triggers landscape orientation automatically") strictly means from the very instant `LobbyScreen` navigates to `GamePlayScreen`, `shouldUseLandscape` can be simplified to `!isGameOver && !isTeamIntermission` (or `TiltGuideScreen` can adapt to landscape). If `TiltGuideScreen` is retained as a portrait onboarding step, the transition happens when clicking "Start Countdown". Both designs are documented below.
2. **Missing SDK Platform 36 vs Platform 37**:
   - `C:\Users\kevin\AppData\Local\Android\Sdk\platforms` currently contains `android-37.0`.
   - `app/build.gradle.kts` specifies `compileSdk { version = release(36) { minorApiLevel = 1 } }`.
   - Modern AGP auto-downloads missing platforms when an internet connection and valid license exist, or `compileSdk` can be aligned if offline.
3. **Robolectric Linux Path**:
   - `app/build.gradle.kts` lines 87–88 contain `/home/appuser/.m2/...`. This only affects the `test` task, not `assembleDebug`. `assembleDebug` skips unit tests entirely.

---

## 4. Conclusion & Recommended Implementation Plan

### 4.1 R4 Implementation Blueprint (Landscape Orientation)

#### Step 1: Set Default Portrait in AndroidManifest.xml
In `app/src/main/AndroidManifest.xml`, add `android:screenOrientation="portrait"` to `MainActivity`:
```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:screenOrientation="portrait"
    android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
    android:label="@string/app_name"
    android:theme="@style/Theme.MyApplication">
```
*Rationale*: Ensures the Lobby, Store, and Profile are strictly portrait, and guarantees that when gameplay resets to portrait, the device snaps upright regardless of how the user holds the phone.

#### Step 2: Streamline Orientation Lifecycle in GamePlayScreen.kt
Consolidate orientation and window controller handling into a single, clean `DisposableEffect` in `GamePlayScreen.kt`:
```kotlin
// Consolidated Window & Orientation Lifecycle Management
val view = LocalView.current
val activity = remember(context, view) { view.context.findActivity() ?: context.findActivity() }
val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

// Automatic landscape orientation during active gameplay & countdown
val shouldUseLandscape = isGuideShown && !isTeamIntermission && !isGameOver

DisposableEffect(activity, view, shouldUseLandscape) {
  val window = activity?.window
  val controller = window?.let { WindowCompat.getInsetsController(it, view) }

  if (shouldUseLandscape) {
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    controller?.hide(WindowInsetsCompat.Type.systemBars())
    view.keepScreenOn = true
  } else {
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    controller?.show(WindowInsetsCompat.Type.systemBars())
    view.keepScreenOn = false
  }

  onDispose {
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    controller?.show(WindowInsetsCompat.Type.systemBars())
    view.keepScreenOn = false
  }
}
```
*Benefits*:
- Entering active play locks orientation to `SCREEN_ORIENTATION_SENSOR_LANDSCAPE`.
- Exiting gameplay (via Back button, arrow back icon, or completing game) immediately forces portrait restoration via `SCREEN_ORIENTATION_PORTRAIT`.
- Zero orientation state leakage across composable recompositions.

### 4.2 R5 Implementation Blueprint (Local Build Verification)

#### Step 1: Restore Gradle Wrapper Tooling
Copy/create the missing wrapper files in `Charades`:
1. `gradle/wrapper/gradle-wrapper.jar` (standard launcher jar, e.g. from `Taxido_driver/android/gradle/wrapper/gradle-wrapper.jar`).
2. `gradlew` (executable bash script for Linux/macOS).
3. `gradlew.bat` (batch script for Windows).

#### Step 2: Configure Environment for Java 21 LTS
Prior to running the build, configure the process environment in PowerShell:
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\openjdk\jdk-21.0.8"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:ANDROID_HOME = "C:\Users\kevin\AppData\Local\Android\Sdk"
```

#### Step 3: Run AssembleDebug Build
Execute:
```powershell
.\gradlew.bat assembleDebug --stacktrace
```
*Expected artifact*: `app\build\outputs\apk\debug\app-debug.apk`.

---

## 5. Verification Method

### 5.1 Orientation Verification (R4)
1. **Code Inspection**:
   - Check `app/src/main/AndroidManifest.xml` line 26 has `android:screenOrientation="portrait"`.
   - Check `app/src/main/java/com/example/ui/GamePlayScreen.kt` has the consolidated `DisposableEffect` with `SCREEN_ORIENTATION_SENSOR_LANDSCAPE` and `onDispose { requestedOrientation = SCREEN_ORIENTATION_PORTRAIT }`.
2. **Behavioral Trace**:
   - Navigating to `GamePlayScreen`: `requestedOrientation` switches to `SCREEN_ORIENTATION_SENSOR_LANDSCAPE` (8).
   - Rotating phone 180° in landscape: screen rotates smoothly between reverse landscape and landscape without crashing or resetting game state.
   - Nodding head down: `event.values[2] < -7.0f` triggers `onCorrect()`.
   - Nodding head up: `event.values[2] > 7.0f` triggers `onPass()`.
   - Exiting game: `onDispose` resets `requestedOrientation = SCREEN_ORIENTATION_PORTRAIT` (1), returning Lobby to portrait.

### 5.2 Build Verification (R5)
1. Verify presence of wrapper files:
   ```powershell
   Test-Path gradlew.bat
   Test-Path gradle\wrapper\gradle-wrapper.jar
   ```
2. Verify Java 21 environment:
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Android\openjdk\jdk-21.0.8"
   & "$env:JAVA_HOME\bin\java.exe" -version
   ```
3. Execute local debug compilation:
   ```powershell
   .\gradlew.bat assembleDebug
   ```
4. Confirm APK artifact exists and is non-empty:
   ```powershell
   Get-Item app\build\outputs\apk\debug\app-debug.apk | Select-Object Name, Length, LastWriteTime
   ```
   **Invalidation condition**: Any compilation error, KSP generation error, resource missing error, or exit code != 0.
