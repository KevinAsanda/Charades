## 2026-11-12T20:57:12Z

You are Sub-Orchestrator for Milestone 1 (M1: AI Feature Stripping, Navigation Verification & Cleanliness).

Your mission:
Execute Milestone 1 per the Project Pattern iteration loop (Explorer -> Worker -> Reviewer -> Challenger -> Auditor -> Gate check).

Paths to inspect:
- ORIGINAL_REQUEST.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\ORIGINAL_REQUEST.md
- PROJECT.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\orchestrator_1\PROJECT.md
- Explorer 2 Survey Findings: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\teamwork_explorer_survey_2\handoff.md
- Working directory: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades
- Your state folder: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\sub_orch_m1

Scope of Milestone 1:
1. Delete dead AI source files:
   - `app/src/main/java/com/example/ui/AiCoachScreen.kt`
   - `app/src/main/java/com/example/ui/DeckStudioScreen.kt`
   - `app/src/main/java/com/example/data/repository/AiRepository.kt`
   - `app/src/main/res/drawable/ic_magic.xml`
2. Remove `implementation(libs.firebase.ai)` from `app/build.gradle.kts`.
3. Clean up `MainFeatureScreen.kt` and `LobbyScreen.kt` by removing unused `onNavigateToStudio` and AI plumbing.
4. Verify that `MainFeatureScreen.kt` `NavigationBar` strictly contains 3 tabs: Play (`nav_tab_play`), Store (`nav_tab_store`), Profile (`nav_tab_profile`).
5. Fix `app/src/test/java/com/example/ExampleRobolectricTest.kt` line 19 assertion string from "Mzansi Charades" to "Lekker Charades".
