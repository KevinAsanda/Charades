# Scope: Milestone 1 (M1: AI Feature Stripping, Navigation Verification & Cleanliness)

## Architecture
- UI Framework: Jetpack Compose with Material 3.
- Navigation: 3-tab navigation in `MainFeatureScreen.kt` (Play, Store, Profile).
- AI Deletion: Complete elimination of dead AI screens (`AiCoachScreen.kt`, `DeckStudioScreen.kt`), repository (`AiRepository.kt`), unused drawable (`ic_magic.xml`), and dependency (`libs.firebase.ai`).
- Test Update: Updating outdated app name assertion in `ExampleRobolectricTest.kt`.

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | AI Feature Stripping | Remove dead AI screens (`AiCoachScreen.kt`, `DeckStudioScreen.kt`), repository (`AiRepository.kt`), unused parameters, and dependencies | M1 | Survey (Explorer 2) |
| 2 | Navigation Restriction | Verify and guarantee strictly 3 navigation tabs: Play, Store, Profile | M1 | Survey (Explorer 2) |
| 3 | Test Identity Correction | Update obsolete test assertion in `ExampleRobolectricTest.kt` from "Mzansi Charades" to "Lekker Charades" | M1 | Survey (Explorer 2) |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M1.1 | Investigation & Planning | Explore files, confirm dependencies and exact line references | none | DONE |
| M1.2 | Implementation | File deletion, build.gradle.kts update, UI cleaning, test fix | M1.1 | DONE |
| M1.3 | Review & Challenge | 2 Reviewers + 2 Challengers verify cleanliness, build, and tests | M1.2 | DONE |
| M1.4 | Integrity Forensic Audit | Forensic auditor checks genuine implementation & no mock/stub cheats | M1.3 | DONE |
| M1.5 | Gate Evaluation | Synthesize verdicts, gate pass/fail, handoff to parent | M1.4 | DONE |

Milestone 1 Overall Status: **DONE**

## Interface Contracts
### LobbyScreen ↔ MainFeatureScreen
- `onNavigateToStudio` parameter removed from `LobbyScreen.kt`.
- `MainFeatureScreen.kt` passes clean parameters without AI studio callbacks.
