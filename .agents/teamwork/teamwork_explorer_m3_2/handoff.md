# Handoff Report — Milestone 3 (Automatic Gameplay Landscape Orientation)

## 1. Observation

Direct code examination of `app/src/main/java/com/example/ui/GamePlayScreen.kt`, `app/src/main/AndroidManifest.xml`, `app/src/main/java/com/example/ui/MainFeatureScreen.kt`, and `app/src/main/java/com/example/MainActivity.kt` revealed the following exact lines and behaviors:

### 1.1 Fragmented Orientation & Window Lifecycle in `GamePlayScreen.kt` (Lines 197–226)
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

### 1.2 Missing Activity-Level Portrait Declaration in `AndroidManifest.xml` (Lines 23–28)
```xml
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApplication">
```
`android:screenOrientation="portrait"` is currently missing from `MainActivity` in `AndroidManifest.xml`. As a result, `activity?.requestedOrientation` evaluates to `ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED` (-1).

### 1.3 Accelerometer Tilt Detection Implementation (Lines 310–350)
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
```
The sensor logic relies exclusively on normal Z-axis acceleration `event.values[2]`.

### 1.4 MainFeatureScreen Mount/Unmount Flow (Lines 311–329)
```kotlin
if (activePlayingDeck != null) {
  GamePlayScreen(
    deck = activePlayingDeck!!,
    gameMode = activeGameMode,
    roundDurationSeconds = activeRoundDuration,
    onFinishGame = { session -> ... },
    onExit = {
      activePlayingDeck = null
    }
  )
} else {
  Scaffold(...)
}
```
When gameplay exits, `activePlayingDeck = null`, causing `GamePlayScreen` to be unmounted immediately and `Scaffold` (containing the 3-tab navigation bar and `LobbyScreen`) to be mounted in the same composition frame.

---

## 2. Logic Chain

### 2.1 Flaws of Fragmented DisposableEffects and Root Cause of Leaked States
From **Observation 1.1**:
1. **Empty `onDispose` in Orientation Effect**:
   `DisposableEffect(activity, shouldUseLandscape)` has `onDispose { }` (empty). If the composable unmounts while `shouldUseLandscape == true` (e.g. user presses hardware Back button or taps exit in top bar during active gameplay), this effect performs **zero** orientation restoration!
2. **Desynchronized Window Insets & WakeLock**:
   `DisposableEffect(view, activity)` is keyed only on `(view, activity)`, so it executes once on initial composition and never reacts to changes in `shouldUseLandscape`. Consequently:
   - System bars are hidden immediately even when `TiltGuideScreen` is displayed in portrait.
   - During `isTeamIntermission` and `isGameOver`, system bars remain hidden even though those screens are displayed in portrait.
   - `view.keepScreenOn = true` remains permanently active even during non-gameplay screens within `GamePlayScreen`.
3. **The `initialOrientation` Trap**:
   `initialOrientation` captures `activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED`. Because `AndroidManifest.xml` currently lacks `android:screenOrientation="portrait"` (**Observation 1.2**), `initialOrientation` resolves to `SCREEN_ORIENTATION_UNSPECIFIED` (-1). Restoring `initialOrientation` restores an unconstrained orientation state rather than forcing portrait mode. If the user tilts their phone sideways after exiting to the lobby, the lobby will rotate to landscape.
4. **Scattered Responsibility**:
   `activity?.requestedOrientation = initialOrientation` is executed inside the `onDispose` of the *second* effect (`DisposableEffect(view, activity)`), creating a brittle dependency between two unrelated effects.

### 2.2 Trace of `val shouldUseLandscape = isGuideShown && !isTeamIntermission && !isGameOver`
The boolean `shouldUseLandscape` perfectly partitions the 8 lifecycle states of `GamePlayScreen`:

| # | State / Screen | `isGuideShown` | `isTeamIntermission` | `isGameOver` | `shouldUseLandscape` | Target Orientation | Target System Bars | KeepScreenOn |
|---|---|---|---|---|---|---|---|---|
| 1 | **Tilt Tutorial / Guide** (`TiltGuideScreen`) | `false` | `false` | `false` | **`false`** | Portrait | Visible | `false` |
| 2 | **3-2-1 Countdown** (user tapped "I'm Ready") | `true` | `false` | `false` | **`true`** | Sensor Landscape | Hidden (transient swipe) | `true` |
| 3 | **Active Word Guessing** (`isPlaying = true`) | `true` | `false` | `false` | **`true`** | Sensor Landscape | Hidden (transient swipe) | `true` |
| 4 | **Party Teams Halftime Switch** | `true` | `true` | `false` | **`false`** | Portrait | Visible | `false` |
| 5 | **Team B Active Round** | `true` | `false` | `false` | **`true`** | Sensor Landscape | Hidden (transient swipe) | `true` |
| 6 | **Game Over / Final Score Telemetry** | `true` | `false` | `true` | **`false`** | Portrait | Visible | `false` |
| 7 | **Play Again** (re-initializes round from Game Over) | `true` | `false` | `false` | **`true`** | Sensor Landscape | Hidden (transient swipe) | `true` |
| 8 | **Exit to Lobby** (Back button / Exit icon / Exit button) | *Unmounts* | *Unmounts* | *Unmounts* | *DisposableEffect.onDispose* | Portrait | Visible | `false` |

- **Step 1**: On initial launch of `GamePlayScreen`, `isGuideShown` is `false`. The user reads vertical instructional cards ("Place Sideways on Forehead", "Tilt Down = Correct", "Tilt Up = Pass", and the 6 Mzansi Physical Cues) in normal portrait orientation with visible system bars.
- **Step 2**: When the user taps "I'm Ready, Start Countdown" (`start_game_button`), `isGuideShown` becomes `true`. `shouldUseLandscape` transitions from `false` to `true`. The consolidated `DisposableEffect` fires, immediately rotating the screen to `ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE`, hiding system bars, and enabling `keepScreenOn`. The 3-2-1 countdown view adapts to landscape (`StripiGradientMeshHero` Row layout).
- **Step 3**: During active guessing, `shouldUseLandscape` remains `true`. The word card is rendered with landscape typography (34sp to 60sp).
- **Step 4**: In Party Teams mode, when Team A finishes cards or round timer expires, `isTeamIntermission` becomes `true`. `shouldUseLandscape` transitions from `true` to `false`. The screen smoothly rotates to portrait, system bars are restored, and wake lock is released so players can comfortably pass the phone to Team B.
- **Step 5**: When Team B taps "Start Team B Turn", `isTeamIntermission` becomes `false`. `shouldUseLandscape` transitions from `false` to `true`, rotating back to sensor landscape.
- **Step 6**: When the match finishes (time expires or cards exhausted), `isGameOver` becomes `true`. `shouldUseLandscape` transitions to `false`. The screen rotates to portrait, showing the score cards and "Me and My POV" card in a vertical scroll view with visible system bars.
- **Step 7**: Tapping "Play Again" sets `isGameOver = false`, flipping `shouldUseLandscape` back to `true` for a fresh round in landscape without re-showing the guide.
- **Step 8**: Tapping "Back to Lobby" or pressing hardware Back triggers `onExit()` -> `activePlayingDeck = null` in `MainFeatureScreen.kt` (**Observation 1.4**). `GamePlayScreen` is removed from composition. The consolidated `DisposableEffect` executes `onDispose`, guaranteeing portrait mode, restored system bars, and disabled wake lock.

### 2.3 Sensor Invariance Analysis
From **Observation 1.3**:
- The tilt detector reads the normal Z-axis component $z = \text{event.values}[2]$.
- An in-plane rotation by angle $\theta$ around the Z-axis (normal to the screen) is represented by the 3D rotation matrix:
  $$R_z(\theta) = \begin{pmatrix} \cos\theta & -\sin\theta & 0 \\ \sin\theta & \cos\theta & 0 \\ 0 & 0 & 1 \end{pmatrix}$$
- Multiplying $R_z(\theta)$ by any acceleration vector $(x, y, z)^T$ yields:
  $$z' = 0 \cdot x + 0 \cdot y + 1 \cdot z = z$$
- Therefore, the Z-axis acceleration value is **strictly invariant** under any 2D rotation around the screen normal.
- When using `ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE`, the device can be held in standard landscape ($\theta = 90^\circ$) or reverse landscape ($\theta = 270^\circ$). In both orientations:
  - Phone upright on forehead (screen facing forward): $z \approx 0\text{ m/s}^2$ ($|z| < 5.0\text{ m/s}^2$, arming tilt).
  - Nodding head down (screen facing floor): $z < -7.0\text{ m/s}^2$ (triggers `onCorrect()`).
  - Tilting head up (screen facing ceiling): $z > 7.0\text{ m/s}^2$ (triggers `onPass()`).
- Sensor behavior requires **zero modifications** when switching to `SCREEN_ORIENTATION_SENSOR_LANDSCAPE`.

---

## 3. Caveats

1. **Activity Context Resolution**: Compose previews or non-Activity contexts (e.g. pure isolated unit tests) may have a null `Activity`. The consolidated `DisposableEffect` employs null-safe navigation (`activity?.requestedOrientation = ...`, `activity?.window?.let { ... }`), ensuring safe execution without throwing `NullPointerException`.
2. **Activity-level Configuration Changes**: `MainActivity` in `AndroidManifest.xml` has `android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"`. This prevents Android from destroying and recreating `MainActivity` upon rotation, allowing Compose to re-layout smoothly without re-shuffling cards or resetting timer state.
3. No other caveats.

---

## 4. Conclusion

1. Consolidate orientation control, system bar insets, and `keepScreenOn` in `app/src/main/java/com/example/ui/GamePlayScreen.kt` into a single, cohesive `DisposableEffect(activity, view, shouldUseLandscape)`.
2. Explicitly enforce `ActivityInfo.SCREEN_ORIENTATION_PORTRAIT` in both the `else` branch and `onDispose`, eliminating the flawed `initialOrientation` variable.
3. Declare `android:screenOrientation="portrait"` on `MainActivity` in `app/src/main/AndroidManifest.xml` to anchor the default orientation across the app.

---

## 5. Exact Recommended Code Changes for Worker

### 5.1 File 1: `app/src/main/AndroidManifest.xml`
Add `android:screenOrientation="portrait"` to `MainActivity`:

```xml
<<<<
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApplication">
====
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:screenOrientation="portrait"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApplication">
>>>>
```

### 5.2 File 2: `app/src/main/java/com/example/ui/GamePlayScreen.kt`
Replace lines 197–226 (removing `initialOrientation` and replacing both `DisposableEffect`s with the consolidated effect):

```kotlin
<<<<
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
====
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
>>>>
```

---

## 6. Verification Method

### 6.1 Static Inspection Commands
1. **Verify AndroidManifest declaration**:
   ```powershell
   Select-String -Path 'app/src/main/AndroidManifest.xml' -Pattern 'screenOrientation="portrait"'
   ```
   *Expected*: Matches `.MainActivity`.

2. **Verify Single Consolidated DisposableEffect in `GamePlayScreen.kt`**:
   ```powershell
   Select-String -Path 'app/src/main/java/com/example/ui/GamePlayScreen.kt' -Pattern 'DisposableEffect\(activity, view, shouldUseLandscape\)'
   ```
   *Expected*: Exactly 1 match.

3. **Verify Elimination of `initialOrientation`**:
   ```powershell
   Select-String -Path 'app/src/main/java/com/example/ui/GamePlayScreen.kt' -Pattern 'initialOrientation'
   ```
   *Expected*: 0 matches.

4. **Verify SENSOR_LANDSCAPE and PORTRAIT Usage**:
   ```powershell
   Select-String -Path 'app/src/main/java/com/example/ui/GamePlayScreen.kt' -Pattern 'SCREEN_ORIENTATION_SENSOR_LANDSCAPE|SCREEN_ORIENTATION_PORTRAIT'
   ```
   *Expected*: Exactly 1 match for `SCREEN_ORIENTATION_SENSOR_LANDSCAPE` (inside `if (shouldUseLandscape)`) and 2 matches for `SCREEN_ORIENTATION_PORTRAIT` (in `else` and `onDispose`).

### 6.2 Sensor Mathematics Verification
- Verify that `event.values[2]` is used for tilt detection and not X (`values[0]`) or Y (`values[1]`).
- Confirm that normal Z-axis rotation matrix $R_z(\theta)$ leaves the Z-axis invariant:
  $$\begin{pmatrix} 0 & 0 & 1 \end{pmatrix} \begin{pmatrix} x \\ y \\ z \end{pmatrix} = z$$

### 6.3 Runtime & Device Verification
1. **App Launch**: Verify app opens in Portrait (LobbyScreen). Holding phone horizontally before launching does not cause Lobby to open in landscape.
2. **Tilt Guide Screen**: Select any deck and tap "Start Playing". Verify `TiltGuideScreen` displays in Portrait with system bars visible and scrollable content.
3. **Countdown & Active Play**: Tap "I'm Ready, Start Countdown". Verify device rotates automatically to Landscape, system bars are hidden (revealed only temporarily on edge swipe), and screen stays awake (`keepScreenOn`).
4. **Sensor Landscape Inversion**: Rotate phone 180° sideways during play. Verify UI rotates to reverse landscape while active game timer and card state continue uninterrupted.
5. **Mid-Game Exit**: Tap the back arrow icon or hardware Back button during active gameplay. Verify app instantly returns to Lobby in Portrait with permanent system bars restored and wake lock released.
6. **Party Teams Halftime Switch**: Play Party Teams mode until Team A completes round. Verify screen automatically returns to Portrait for "Pass Phone to Team B" screen. Tapping "Start Team B Turn" automatically rotates back to Landscape.
7. **Game Over Screen**: Let time expire. Verify screen automatically returns to Portrait, showing score card and "Me and My POV" card with visible system bars.
8. **Play Again**: Tap "Play Again" on Game Over screen. Verify screen rotates back to Landscape for the 3-2-1 countdown and next round.

### 6.4 Invalidation Conditions
- Any code where `onDispose` fails to set `activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT`.
- Any code restoring `initialOrientation` or `SCREEN_ORIENTATION_UNSPECIFIED`.
- Any code hiding system bars or enabling `keepScreenOn` while `shouldUseLandscape == false`.
