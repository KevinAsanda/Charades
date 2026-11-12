## 2026-11-12T21:50:44Z
You are Challenger 2 for Milestone 3 (Automatic Gameplay Landscape Orientation).

Your working directory is: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades
Your state folder is: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\teamwork_challenger_m3_2

MANDATORY FIRST STEPS:
1. Read ORIGINAL_REQUEST.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\ORIGINAL_REQUEST.md
2. Read SCOPE.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\sub_orch_m3\SCOPE.md
3. Read PROJECT.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\orchestrator_1\PROJECT.md
4. Read Worker Handoff: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\teamwork_worker_m3_1\handoff.md

YOUR MISSION:
Empirically stress-test and challenge the Accelerometer Z-axis sensor model and thresholds:
1. Write and execute an adversarial verification script (e.g. Python, PowerShell, or standalone Kotlin/Java) testing:
   - Mathematical invariance of $z = \text{event.values}[2]$ under rotations $\theta \in \{0^\circ, 90^\circ, 180^\circ, 270^\circ\}$.
   - Edge case gravity projections: neutral forehead ($|z| < 5.0$), nod down ($z < -7.0$), tip up ($z > 7.0$), and table-rest ($z \approx \pm 9.81$).
   - Debounce window ($1100\text{ms}$) and single-shot arming latch under high-frequency simulated jitter / spam.
   - Fallback button visibility and behavior when sensor is absent.
2. Confirm zero regressions or coordinate inversions under `SCREEN_ORIENTATION_SENSOR_LANDSCAPE`.
3. Issue a definitive Gate Verdict: APPROVE or CHALLENGE_FAILED.

OUTPUT:
Write your complete challenger report to:
`c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\teamwork_challenger_m3_2\handoff.md`

When complete, send a message to the orchestrator with your verdict (APPROVE or CHALLENGE_FAILED), summary, and handoff path.
