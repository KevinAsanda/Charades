# BRIEFING — 2026-11-12T20:48:00Z

## Mission
Investigate the Lekker Charades codebase for R3 (Feature Simplification and AI Removal) & Navigation Structure.

## 🔒 My Identity
- Archetype: explorer
- Roles: Teamwork explorer
- Working directory: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades
- State folder: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\teamwork_explorer_survey_2
- Original parent: 6450275f-106a-472f-9007-9f352e3667c5
- Milestone: Investigation for R3 (AI Removal & Navigation Simplification)

## 🔒 Key Constraints
- Read-only investigation — do NOT implement
- Map all navigation components, bottom nav bar, routes
- Identify and catalog all AI features and UI entry points
- Verify 3-tab navigation structure (Play, Store, Profile)
- Catalog branding assets/logos/icons

## Current Parent
- Conversation ID: 6450275f-106a-472f-9007-9f352e3667c5
- Updated: 2026-11-12T20:41:00Z

## Investigation State
- **Explored paths**:
  - `MainActivity.kt`
  - `MainFeatureScreen.kt`
  - `LobbyScreen.kt`
  - `StoreScreen.kt`
  - `ProfileScreen.kt`
  - `AuthScreen.kt`
  - `GamePlayScreen.kt`
  - `AiCoachScreen.kt`
  - `DeckStudioScreen.kt`
  - `AiRepository.kt`
  - `CustomCard.kt` & `CustomCardRepository.kt`
  - `StorePack.kt` & `Deck.kt`
  - `app/build.gradle.kts` & `libs.versions.toml`
  - `AndroidManifest.xml`, `strings.xml`, `themes.xml`, `colors.xml`, drawables & fonts
  - `ExampleRobolectricTest.kt` & `MzansiRepositoryRuleTest.kt`
- **Key findings**:
  - Navigation is state-driven in Compose: `AuthScreen` (when logged out), `MainFeatureScreen` (when logged in).
  - `MainFeatureScreen` switches between active gameplay (`GamePlayScreen`) and a 3-tab NavigationBar (Play=0, Store=1, Profile=2).
  - Extra tabs previously existed (Coach and Studio), but were removed in commit `cc871e5`.
  - Dead files remain: `AiCoachScreen.kt`, `DeckStudioScreen.kt`, `AiRepository.kt`.
  - Parameter `onNavigateToStudio` in `LobbyScreen.kt` is dead.
  - Zero visible AI features exist on the main screens.
  - Branding is cleanly set to "Lekker Charades", with assets in `res/drawable-nodpi/` (`logo_emblem.png`, `logo_full.png`, `logo_wordmark.png`, `splash_icon.png`).
  - Unit test `ExampleRobolectricTest.kt` asserts outdated "Mzansi Charades" instead of "Lekker Charades".
- **Unexplored areas**: Implementation of R2 grid (Explorer 1's domain), landscape sensor handling (Explorer 3's domain).

## Key Decisions Made
- Confirmed exactly 3-tab navigation structure.
- Identified all AI classes to be removed/isolated.
- Verified absence of visible AI buttons, banners, or entry points on Play, Store, and Profile.

## Artifact Index
- handoff.md — Comprehensive findings and handoff report
- progress.md — Liveness heartbeat
- DISPATCH.md — Dispatch log
