# Dispatch: Worker M1

## Mission
Execute Milestone 1 (M1: AI Feature Stripping, Navigation Verification & Cleanliness):
1. Delete dead AI source files and drawables:
   - `app/src/main/java/com/example/ui/AiCoachScreen.kt`
   - `app/src/main/java/com/example/ui/DeckStudioScreen.kt`
   - `app/src/main/java/com/example/data/repository/AiRepository.kt`
   - `app/src/main/res/drawable/ic_magic.xml`
2. Remove `implementation(libs.firebase.ai)` from `app/build.gradle.kts` (line 121) and remove `firebase-ai` catalog entry from `gradle/libs.versions.toml` (line 91).
3. Clean up `LobbyScreen.kt` by removing the unused `onNavigateToStudio: () -> Unit = {},` parameter at line 153.
4. Verify that `MainFeatureScreen.kt` navigation strictly maintains 3 tabs: Play, Store, Profile.
5. Update `app/src/test/java/com/example/ExampleRobolectricTest.kt` line 19 from `assertEquals("Mzansi Charades", appName)` to `assertEquals("Lekker Charades", appName)`.

## Exclusive File Ownership
Worker M1 owns:
- `app/src/main/java/com/example/ui/AiCoachScreen.kt`
- `app/src/main/java/com/example/ui/DeckStudioScreen.kt`
- `app/src/main/java/com/example/data/repository/AiRepository.kt`
- `app/src/main/res/drawable/ic_magic.xml`
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- `app/src/main/java/com/example/ui/LobbyScreen.kt`
- `app/src/test/java/com/example/ExampleRobolectricTest.kt`

## Mandatory Integrity Warning
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

## Context & Explorer Reports
- ORIGINAL_REQUEST.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\ORIGINAL_REQUEST.md
- PROJECT.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\orchestrator_1\PROJECT.md
- SCOPE.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\sub_orch_m1\SCOPE.md
- Explorer M1.1 Handoff: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\explorer_m1_1\handoff.md
- Explorer M1.2 Handoff: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\explorer_m1_2\handoff.md
- Explorer M1.3 Handoff: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\explorer_m1_3\handoff.md
- State folder: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\worker_m1

## Verification Requirement
Run git status and git diff / verification commands to confirm all changes are cleanly applied, no orphan references remain, and write a thorough handoff report to `c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\worker_m1\handoff.md`. Send a completion message when finished.


## 2026-11-12T21:09:04Z
You are Worker M1 executing Milestone 1 (M1: AI Feature Stripping, Navigation Verification & Cleanliness).

MANDATORY INTEGRITY WARNING:
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

Context Files:
- ORIGINAL_REQUEST.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\ORIGINAL_REQUEST.md
- PROJECT.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\orchestrator_1\PROJECT.md
- SCOPE.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\sub_orch_m1\SCOPE.md
- DISPATCH.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\worker_m1\DISPATCH.md
- Explorer M1.1 report: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\explorer_m1_1\handoff.md
- Explorer M1.2 report: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\explorer_m1_2\handoff.md
- Explorer M1.3 report: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\explorer_m1_3\handoff.md
- State folder: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\worker_m1
- Working directory: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades

Your assigned tasks:
1. Delete dead AI source files and drawables:
   - `app/src/main/java/com/example/ui/AiCoachScreen.kt`
   - `app/src/main/java/com/example/ui/DeckStudioScreen.kt`
   - `app/src/main/java/com/example/data/repository/AiRepository.kt`
   - `app/src/main/res/drawable/ic_magic.xml`
2. Remove `implementation(libs.firebase.ai)` from `app/build.gradle.kts` (line 121) and remove `firebase-ai` from `gradle/libs.versions.toml` (line 91).
3. In `app/src/main/java/com/example/ui/LobbyScreen.kt`, remove the unused parameter `onNavigateToStudio: () -> Unit = {},` at line 153.
4. Verify that `MainFeatureScreen.kt` `NavigationBar` strictly contains 3 tabs: Play (`nav_tab_play`), Store (`nav_tab_store`), Profile (`nav_tab_profile`).
5. Update `app/src/test/java/com/example/ExampleRobolectricTest.kt` line 19 assertion string from `"Mzansi Charades"` to `"Lekker Charades"`.
6. Run verification commands (git status, search for orphan references, etc.), document results, and write a full handoff report to `c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\worker_m1\handoff.md`.
7. Send a message to your parent when done.
