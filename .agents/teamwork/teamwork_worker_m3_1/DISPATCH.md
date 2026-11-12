## 2026-11-12T21:44:03Z
You are the Worker agent for Milestone 3 (Automatic Gameplay Landscape Orientation).

Your working directory is: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades
Your state folder is: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\teamwork_worker_m3_1

MANDATORY INTEGRITY WARNING:
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

MANDATORY FIRST STEPS:
1. Read ORIGINAL_REQUEST.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\ORIGINAL_REQUEST.md
2. Read SCOPE.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\sub_orch_m3\SCOPE.md
3. Read PROJECT.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\orchestrator_1\PROJECT.md
4. Read Explorer Handoffs:
   - c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\teamwork_explorer_m3_1\handoff.md
   - c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\teamwork_explorer_m3_2\handoff.md
   - c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\teamwork_explorer_m3_3\handoff.md

FILE OWNERSHIP:
You own EXCLUSIVELY:
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/example/ui/GamePlayScreen.kt`
Do NOT edit any other files.

TASKS TO IMPLEMENT:
1. In `app/src/main/AndroidManifest.xml`:
   Add `android:screenOrientation="portrait"` and `tools:ignore="LockedOrientationActivity"` to `.MainActivity`. Keep existing `android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"`.
2. In `app/src/main/java/com/example/ui/GamePlayScreen.kt`:
   Consolidate orientation and window lifecycle (lines 197–226) into a single cohesive `DisposableEffect(activity, view, shouldUseLandscape)`.
   - Remove `initialOrientation`.
   - When `shouldUseLandscape == true`:
     - `activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE`
     - `controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`
     - `controller?.hide(WindowInsetsCompat.Type.systemBars())`
     - `view.keepScreenOn = true`
   - When `shouldUseLandscape == false`:
     - `activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT`
     - `controller?.show(WindowInsetsCompat.Type.systemBars())`
     - `view.keepScreenOn = false`
   - In `onDispose`:
     - `activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT`
     - `controller?.show(WindowInsetsCompat.Type.systemBars())`
     - `view.keepScreenOn = false`
3. Leave accelerometer tilt detection logic (lines 310–350) intact as verified by Explorer 3.

VERIFICATION:
Execute verification commands in PowerShell to confirm:
- `app/src/main/AndroidManifest.xml` contains `android:screenOrientation="portrait"`.
- `GamePlayScreen.kt` contains `DisposableEffect(activity, view, shouldUseLandscape)` and zero occurrences of `initialOrientation`.
- Both files parse cleanly and have no syntax errors.

OUTPUT:
Write your full report to:
`c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\teamwork_worker_m3_1\handoff.md`

When complete, send a message to the orchestrator with your handoff path and summary.
