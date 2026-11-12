## 2026-11-12T21:33:19Z
You are Sub-Orchestrator for Milestone 3 (M3: Automatic Gameplay Landscape Orientation).

Your mission:
Execute Milestone 3 per the Project Pattern iteration loop (Explorer -> Worker -> Reviewer -> Challenger -> Auditor -> Gate check).

Paths to inspect:
- ORIGINAL_REQUEST.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\ORIGINAL_REQUEST.md
- PROJECT.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\orchestrator_1\PROJECT.md
- Explorer 3 Survey Handoff: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\teamwork_explorer_survey_3\handoff.md
- Working directory: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades
- Your state folder: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\sub_orch_m3

Scope of Milestone 3:
You own `app/src/main/AndroidManifest.xml` and `app/src/main/java/com/example/ui/GamePlayScreen.kt` exclusively.
1. In `app/src/main/AndroidManifest.xml`:
   - Set `android:screenOrientation="portrait"` on `MainActivity`.
   - Maintain existing `android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"`.
2. In `app/src/main/java/com/example/ui/GamePlayScreen.kt`:
   - Consolidate window and orientation handling into a single `DisposableEffect(activity, view, shouldUseLandscape)`.
   - Ensure that during countdown and active gameplay (`shouldUseLandscape == true`), the screen is locked to `ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE`, immersive fullscreen is enabled with transient bars, and `view.keepScreenOn = true`.
   - Ensure that upon game completion, intermission, or navigating away (in `onDispose`), the screen is explicitly restored to `ActivityInfo.SCREEN_ORIENTATION_PORTRAIT`, system bars are restored, and `view.keepScreenOn = false`.
3. Validate and verify that accelerometer Z-axis tilt detection (`event.values[2]`) operates reliably in landscape mode without inversion.

Iteration Loop Instructions:
- Include the MANDATORY INTEGRITY WARNING in the Worker's dispatch prompt:
  "DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected."
- Run Worker, Reviewer(s), Challenger(s), and Forensic Auditor (`teamwork_auditor`).
- Gate check: Record verdicts in `GATE_STATUS.md`. All criteria must pass. Auditor veto is absolute.
- Upon passing gate check, write `handoff.md` in your state folder summarizing the completed work, files changed, and verification results.
- Send a completion message back to your parent orchestrator with your handoff path.
