# Project: Lekker Charades Main Screen Redesign & Gameplay Modernization

## Architecture
- **UI Framework**: Jetpack Compose with Material 3 and Stripi Design System tokens (`Color.kt`, `Type.kt`, `StripiComponents.kt`).
- **Navigation Architecture**: State-driven in Jetpack Compose (`MainActivity.kt` switches between Auth and MainFeatureScreen; `MainFeatureScreen.kt` maintains a 3-tab `NavigationBar` [Play, Store, Profile] and conditionally mounts full-screen `GamePlayScreen.kt`).
- **Orientation Architecture**: Activity-level configuration in `AndroidManifest.xml` (`android:screenOrientation="portrait"`) with dynamic, transient lifecycle-controlled rotation to `ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE` in `GamePlayScreen.kt` during active gameplay, cleanly reverting to portrait on exit.
- **Sensor Architecture**: Forehead tilt detection via `Sensor.TYPE_ACCELEROMETER` measuring normal Z-axis (`event.values[2]`), which is strictly invariant to 2D landscape/reverse-landscape rotation.
- **Build System**: Android Gradle Plugin 9.1.1, Gradle 9.3.1, Kotlin 2.2.10, Compose Compiler, Java 21 LTS runtime.

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | AI Feature Stripping | Remove dead AI screens (`AiCoachScreen.kt`, `DeckStudioScreen.kt`), repository (`AiRepository.kt`), unused parameters, and dependencies | M1 | Survey (Explorer 2) |
| 2 | Navigation Restriction | Verify and guarantee strictly 3 navigation tabs: Play, Store, Profile | M1 | Survey (Explorer 2) |
| 3 | Test Identity Correction | Update obsolete test assertion in `ExampleRobolectricTest.kt` from "Mzansi Charades" to "Lekker Charades" | M1 | Survey (Explorer 2) |
| 4 | Compact Decks Header Bar | Compact ~52dp header with bold white "Decks" title seamlessly blended into canvas | M2 | Survey (Explorer 1) |
| 5 | 2-Column Full-Bleed Grid | Full-bleed 2-column vertical deck grid with 12dp horizontal and 10dp vertical spacing | M2 | Survey (Explorer 1) |
| 6 | "Unlock All Decks" Anchor Card | Amber/gold anchor card at Row 0, Column 1 with padlock icon, title, and direct Store CTA | M2 | Survey (Explorer 1) |
| 7 | Deck Grid Card Anatomy | Uniform 215dp height cards with corner badge, circular artwork container, bold title, and 2-3 line description | M2 | Survey (Explorer 1) |
| 8 | Deck Quick Play Modal | Lightweight mode selection (Act It Out, Hit the Beat, etc.) and timer pill selector before launching game | M2 | Survey (Explorer 1) |
| 9 | MainActivity Default Portrait | Declare `android:screenOrientation="portrait"` on MainActivity in AndroidManifest.xml | M3 | Survey (Explorer 3) |
| 10 | Active Gameplay Auto-Landscape | Consolidated `DisposableEffect` locking to `SCREEN_ORIENTATION_SENSOR_LANDSCAPE` during play | M3 | Survey (Explorer 3) |
| 11 | Gameplay Portrait Restoration | Guaranteed restoration to `SCREEN_ORIENTATION_PORTRAIT` in `onDispose` upon gameplay exit | M3 | Survey (Explorer 3) |
| 12 | Sensor Tilt Stability | Validation of Z-axis forehead tilt gesture invariance under landscape | M3 | Survey (Explorer 3) |
| 13 | Gradle Wrapper Restoration | Restore missing `gradlew`, `gradlew.bat`, and `gradle-wrapper.jar` | M4 | Survey (Explorer 3) |
| 14 | Local Debug APK Assembly | Compile verified debug APK using Java 21 LTS and AGP 9.1.1 via `./gradlew.bat assembleDebug` | M4 | Survey (Explorer 3) |
| 15 | E2E & Final Verification | Comprehensive UI, navigation, orientation, and build verification against all acceptance criteria | M5 | Synthesis |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M1 | AI Feature Stripping & Navigation Cleanliness | Remove dead AI files, unneeded parameters, verify 3-tab nav, fix test string | none | DONE |
| M2 | Main Screen 2-Column Deck Grid Redesign | Redesign `LobbyScreen.kt` with compact "Decks" bar, 2-column grid, Unlock All card, card anatomy, quick play modal | M1 | IN_PROGRESS (sub_orch_m2: 126d271d-46a4-444f-a0aa-41ed4198dbf1) |
| M3 | Automatic Gameplay Landscape Orientation | Set portrait in Manifest, consolidate orientation lifecycle in `GamePlayScreen.kt` | M1 | DONE |
| M4 | Gradle Wrapper Restoration & Local APK Build | Restore wrapper files, verify Java 21 environment, compile debug APK | M2, M3 | PLANNED |
| M5 | Final Review, Audit & Acceptance Verification | Run unit tests, verify APK output, audit code integrity and layout | M4 | PLANNED |

## Code Layout
- `app/src/main/java/com/example/ui/LobbyScreen.kt`: Owned exclusively by M2 Worker (2-column deck grid)
- `app/src/main/java/com/example/ui/MainFeatureScreen.kt`: Navigation plumbing
- `app/src/main/java/com/example/ui/GamePlayScreen.kt`: Owned exclusively by M3 Worker (landscape orientation lifecycle)
- `app/src/main/AndroidManifest.xml`: Owned exclusively by M3 Worker (screenOrientation)
- `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`: Owned exclusively by M4 Worker (wrapper restoration)

## Interface Contracts
### LobbyScreen ↔ MainFeatureScreen
```kotlin
@Composable
fun LobbyScreen(
  userProfile: UserProfile?,
  customCards: List<CustomCard> = emptyList(),
  onStartGame: (Deck, String, Long) -> Unit,
  onNavigateToStore: () -> Unit,
  onOpenPaymentSetup: () -> Unit = {}
)
```
- Parameters: `onNavigateToStudio` removed.
- `onStartGame`: triggered when user selects a deck, chooses game mode & duration in `DeckQuickPlayDialog`, and taps "Start Playing" (`testTag("start_match_button")`).
- `onNavigateToStore`: triggered when tapping "Unlock All Decks" anchor card or any locked expansion deck.

### GamePlayScreen ↔ Android Activity
```kotlin
// Consolidated DisposableEffect
DisposableEffect(activity, view, shouldUseLandscape) {
  if (shouldUseLandscape) {
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    // immersive fullscreen
  } else {
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    // restore system bars
  }
  onDispose {
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    // restore system bars
  }
}
```
