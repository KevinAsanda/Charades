# Scope: Milestone 3 (Automatic Gameplay Landscape Orientation)

## Architecture
- **Screen Orientation Configuration**: `app/src/main/AndroidManifest.xml` explicitly sets `android:screenOrientation="portrait"` on `MainActivity`, preserving `android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"`.
- **Compose Lifecycle Management**: `app/src/main/java/com/example/ui/GamePlayScreen.kt` consolidates orientation and window insets / screen on into a single `DisposableEffect(activity, view, shouldUseLandscape)`.
- **Sensor Invariance**: Accelerometer normal Z-axis reading (`event.values[2]`) remains invariant under 2D rotation into landscape / sensor landscape.

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 9 | MainActivity Default Portrait | Declare android:screenOrientation="portrait" on MainActivity in AndroidManifest.xml | M3 | Survey (Explorer 3) |
| 10 | Active Gameplay Auto-Landscape | Consolidated DisposableEffect locking to SCREEN_ORIENTATION_SENSOR_LANDSCAPE during play | M3 | Survey (Explorer 3) |
| 11 | Gameplay Portrait Restoration | Guaranteed restoration to SCREEN_ORIENTATION_PORTRAIT in onDispose upon gameplay exit | M3 | Survey (Explorer 3) |
| 12 | Sensor Tilt Stability | Validation of Z-axis forehead tilt gesture invariance under landscape | M3 | Survey (Explorer 3) |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 3 | Automatic Gameplay Landscape Orientation | AndroidManifest.xml, GamePlayScreen.kt orientation lifecycle, sensor validation | M1 | DONE |

## Interface Contracts
### GamePlayScreen ↔ Android Activity
```kotlin
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

## Code Layout Ownership
- `app/src/main/AndroidManifest.xml`: Owned exclusively by M3 Worker (DONE)
- `app/src/main/java/com/example/ui/GamePlayScreen.kt`: Owned exclusively by M3 Worker (DONE)
