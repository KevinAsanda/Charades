# Forensic Audit Report: Milestone 3 (Automatic Gameplay Landscape Orientation)

**Work Product**: `app/src/main/AndroidManifest.xml` and `app/src/main/java/com/example/ui/GamePlayScreen.kt`  
**Profile**: General Project  
**Integrity Mode**: Development (from `ORIGINAL_REQUEST.md`)  
**Gate Verdict**: **CLEAN**

---

### Phase Results
- **Scope Compliance Check**: PASS — Strictly `AndroidManifest.xml` and `GamePlayScreen.kt` were modified during Milestone 3. Zero untracked files in the source tree; `.agents/teamwork/` contains exclusively agent metadata.
- **Hardcoded Output Detection**: PASS — Zero hardcoded test return values, mock responses, or expected output string literals.
- **Facade Detection**: PASS — Genuine integration with `Activity.requestedOrientation`, `WindowInsetsControllerCompat`, and `view.keepScreenOn`. No dummy or no-op facades.
- **Pre-populated Artifact Detection**: PASS — No pre-populated `.log`, test results, or attestation files exist in the project repository.
- **Sensor Logic & Physics Invariance Check**: PASS — Accelerometer normal Z-axis tilt detection (`event.values[2]`) is intact with `abs(z) < 5.0f` arming threshold, `-7.0f` (correct), `+7.0f` (pass), and `1100L` debounce. Invariance under 2D landscape rotation verified mathematically.
- **Code Syntax & Structure Check**: PASS — XML parses cleanly with namespace `tools` and attributes `android:screenOrientation="portrait"` and `tools:ignore="LockedOrientationActivity"`. Kotlin brackets, parens, and syntax are balanced (Braces 236/236, Parens 766/766, Square Brackets 4/4).

---

## 1. Observation

### 1.1 Git Status & Scope Verification
`git status --porcelain` and file write timestamps confirmed that in the Milestone 3 execution window:
- ONLY `app/src/main/AndroidManifest.xml` (modified 2026/11/12 23:46:08) and `app/src/main/java/com/example/ui/GamePlayScreen.kt` (modified 2026/11/12 23:46:31) were touched.
- Previous changes in the workspace (`AiRepository.kt`, `AiCoachScreen.kt`, `DeckStudioScreen.kt`, `ic_magic.xml`, `build.gradle.kts`, `libs.versions.toml`, `ExampleRobolectricTest.kt`) were verified to belong to Milestone 1 (23:11 - 23:12), and `LobbyScreen.kt` belongs to Milestone 2 (23:12:08).
- `git status -uall` confirmed zero untracked source or test files. Only metadata markdown files exist under `.agents/teamwork/`.

### 1.2 AndroidManifest.xml Modifications
```xml
diff --git a/app/src/main/AndroidManifest.xml b/app/src/main/AndroidManifest.xml
index c63dbbf..30a9ad7 100644
--- a/app/src/main/AndroidManifest.xml
+++ b/app/src/main/AndroidManifest.xml
@@ -24,6 +24,8 @@
             android:name=".MainActivity"
             android:exported="true"
             android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
+            android:screenOrientation="portrait"
+            tools:ignore="LockedOrientationActivity"
             android:label="@string/app_name"
             android:theme="@style/Theme.MyApplication">
             <intent-filter>
```
- Line 27: Explicitly configures `android:screenOrientation="portrait"`.
- Line 28: Adds `tools:ignore="LockedOrientationActivity"` to prevent build-time linter warnings.
- Preserves `android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"`.

### 1.3 GamePlayScreen.kt Orientation Lifecycle Consolidation
Lines 197–224:
```kotlin
  // Immersive full-screen mode and automatic landscape orientation during active play
  val view = LocalView.current
  val activity = remember(context, view) { view.context.findActivity() ?: context.findActivity() }
  val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
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
- `initialOrientation` state was completely deleted (0 occurrences in entire codebase).
- Consolidated lifecycle cleanly maps:
  - Active gameplay (`shouldUseLandscape == true`): locks to `ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE`, sets `systemBarsBehavior` to `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`, hides system bars, sets `view.keepScreenOn = true`.
  - Non-active states (`isGuideShown == false`, `isTeamIntermission == true`, `isGameOver == true`): restores `ActivityInfo.SCREEN_ORIENTATION_PORTRAIT`, shows system bars, sets `view.keepScreenOn = false`.
  - `onDispose`: restores `ActivityInfo.SCREEN_ORIENTATION_PORTRAIT`, shows system bars, sets `view.keepScreenOn = false`.

### 1.4 Accelerometer Logic Verification
Lines 308–347:
```kotlin
  // Tilt Sensor Listener: works across all modes when phone is held upright on forehead
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
      // ...
    }
  }
```
- Purely genuine sensor event processing reading normal Z-axis `event.values[2]`.
- No mock return values, no fake test hooks.

---

## 2. Logic Chain

1. **Resolution of Orientation Drift**:
   By explicitly defining `android:screenOrientation="portrait"` on `.MainActivity` and setting `activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT` in both `else` and `onDispose`, any previous drift into `SCREEN_ORIENTATION_UNSPECIFIED` is eliminated. The app remains reliably in portrait orientation across all non-gameplay screens.

2. **Synchronization of Full-Screen Insets and Keep-Screen-On**:
   Consolidating the orientation and insets controller into a single `DisposableEffect(activity, view, shouldUseLandscape)` prevents race conditions or desynchronization when navigating in and out of active gameplay.

3. **Sensor Invariance Under 2D Planar Rotation**:
   The accelerometer coordinate system defines the Z-axis perpendicular to the screen face. For any 2D planar rotation $\theta$ around the Z-axis:
   $$R_Z(\theta) \begin{bmatrix} x \\ y \\ z \end{bmatrix} = \begin{bmatrix} x \cos\theta - y \sin\theta \\ x \sin\theta + y \cos\theta \\ z \end{bmatrix}$$
   The Z component remains mathematically identical ($z' = z$). Thus, holding the device on the forehead in landscape or reverse landscape produces identical tilt responses for both nod-down (correct: $z < -7.0\text{ m/s}^2$) and tilt-up (pass: $z > 7.0\text{ m/s}^2$).

4. **Integrity Rule Compliance**:
   Under `development` integrity mode (and all stricter modes), hardcoded outputs, fake implementations, and pre-populated result artifacts are prohibited. All forensic checks confirmed zero instances of prohibited patterns.

---

## 3. Caveats

1. **Gradle Build Tooling**:
   Execution of `./gradlew.bat assembleDebug` and Robolectric test suite execution depend on Milestone 4 (Gradle wrapper restoration).
2. **Device Hardware Sensors in Emulators**:
   In emulators lacking a physical accelerometer, `hasAccelerometer` will be `false`, and Compose falls back to on-screen tap controls (`onCorrect` / `onPass` buttons) as designed.

---

## 4. Conclusion

Milestone 3 passes all forensic integrity checks:
1. Strictly authorized files were modified.
2. No shortcuts, mocked returns, or facade components were used.
3. Genuine Android platform APIs interface with orientation, insets, and wake locks.
4. Accelerometer tilt physics are preserved and verified invariant.

Gate Verdict: **CLEAN**

---

## 5. Verification Method

### 5.1 Independent Commands Executed
1. **XML Parsing & Attribute Extraction**:
   ```python
   import xml.etree.ElementTree as ET
   tree = ET.parse("app/src/main/AndroidManifest.xml")
   activity = tree.getroot().find("application").find("activity")
   assert activity.get("{http://schemas.android.com/apk/res/android}screenOrientation") == "portrait"
   assert activity.get("{http://schemas.android.com/tools}ignore") == "LockedOrientationActivity"
   ```
   *Result*: PASS.

2. **Kotlin Syntax & Bracket Matching**:
   ```python
   # Evaluated on app/src/main/java/com/example/ui/GamePlayScreen.kt
   # Code-only brackets: Braces 236/236, Parens 766/766, Square Brackets 4/4
   ```
   *Result*: PASS (All balanced, zero syntax mismatch).

3. **Orientation Reference Audit**:
   ```powershell
   Get-ChildItem -Path "app/src/main/java" -Recurse -Filter "*.kt" | Select-String -Pattern "SCREEN_ORIENTATION|requestedOrientation"
   ```
   *Result*: Exactly 3 occurrences, all in `GamePlayScreen.kt` inside the consolidated `DisposableEffect`. Zero occurrences of `initialOrientation`.

4. **Scope Audit**:
   ```powershell
   git status --porcelain
   ```
   *Result*: Zero untracked files outside `.agents/teamwork/`. Only `AndroidManifest.xml` and `GamePlayScreen.kt` modified for M3.
