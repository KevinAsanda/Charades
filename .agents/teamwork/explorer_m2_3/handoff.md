# Handoff Report: Milestone 2 — Test Suites, Caller Integration & Validation Criteria

**Author**: Explorer 3  
**Target Milestone**: Milestone 2 (Main Screen 2-Column Deck Grid Redesign)  
**State Folder**: `c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\explorer_m2_3`  
**Date**: 2026-11-12  

---

## 1. Observation

### 1.1 Caller Integration in `MainFeatureScreen.kt`
Direct inspection of `app/src/main/java/com/example/ui/MainFeatureScreen.kt` (lines 386–406) reveals the exact invocation of `LobbyScreen`:

```kotlin
392:        when (currentTab) {
393:          0 -> LobbyScreen(
394:            userProfile = userProfile,
395:            customCards = customCards,
396:            onStartGame = { deck, mode, duration ->
397:              activePlayingDeck = deck
398:              activeGameMode = mode
399:              activeRoundDuration = duration
400:            },
401:            onNavigateToStore = { currentTab = 1 },
402:            onOpenPaymentSetup = {
403:              pendingCheckoutItem = null
404:              showPaymentModal = true
405:            }
406:          )
```

- **Exact Parameter Types & Signatures**:
  1. `userProfile: UserProfile?` — from `localProfile` (`LocalPlayerStore`).
  2. `customCards: List<CustomCard>` — merged local and remote custom cards.
  3. `onStartGame: (Deck, String, Long) -> Unit` — sets `activePlayingDeck = deck`, `activeGameMode = mode`, and `activeRoundDuration = duration`, which immediately mounts `GamePlayScreen` at lines 311–328:
     ```kotlin
     if (activePlayingDeck != null) {
       GamePlayScreen(
         deck = activePlayingDeck!!,
         gameMode = activeGameMode,
         roundDurationSeconds = activeRoundDuration,
         ...
       )
     }
     ```
  4. `onNavigateToStore: () -> Unit` — switches navigation state to `currentTab = 1` (`StoreScreen`).
  5. `onOpenPaymentSetup: () -> Unit` — opens the in-app payment modal.
- **Dead Parameter Removal**:
  - The obsolete parameter `onNavigateToStudio` was purged in Milestone 1 and is **not** passed by `MainFeatureScreen.kt`.
- **Single Caller**:
  - Searching the entire repository for `LobbyScreen` confirmed that `MainFeatureScreen.kt:393` is the **only** caller across the codebase:
    ```powershell
    Get-ChildItem -Path app\src -Recurse -File | Select-String -Pattern "LobbyScreen"
    # Result:
    # app\src\main\java\com\example\ui\LobbyScreen.kt:148:fun LobbyScreen(
    # app\src\main\java\com\example\ui\MainFeatureScreen.kt:393:          0 -> LobbyScreen(
    ```

---

### 1.2 Existing Unit & Robolectric Tests in `app/src/test/`
Inspection of `app/src/test/` located 4 test files:
1. `app/src/test/java/com/example/ExampleUnitTest.kt`:
   ```kotlin
   class ExampleUnitTest {
     @Test
     fun addition_isCorrect() {
       assertEquals(4, 2 + 2)
     }
   }
   ```
   Pure JUnit 4 test. Does not reference any UI or app classes.

2. `app/src/test/java/com/example/ExampleRobolectricTest.kt`:
   ```kotlin
   @RunWith(RobolectricTestRunner::class)
   @Config(sdk = [36])
   class ExampleRobolectricTest {
     @Test
     fun `read string from context`() {
       val context = ApplicationProvider.getApplicationContext<Context>()
       val appName = context.getString(R.string.app_name)
       assertEquals("Lekker Charades", appName)
     }
   }
   ```
   Robolectric context test verifying `R.string.app_name` equals `"Lekker Charades"`. Does not test UI components, Composables, or `LobbyScreen`.

3. `app/src/test/java/com/example/base/FirestoreEmulatorTestBase.kt`:
   Abstract base class for Firestore and Auth emulator testing connecting to `127.0.0.1:8085` and `127.0.0.1:9099`.

4. `app/src/test/java/com/example/data/repository/MzansiRepositoryRuleTest.kt`:
   Tests repository layer rules (`UserRepository`, `GameSessionRepository`, `CustomCardRepository`, `PaymentRepository`).

- **Inspection of `app/src/androidTest/`**:
  `app/src/androidTest/java/com/example/ExampleInstrumentedTest.kt`:
  Validates package name `com.aistudio.mzansicharades.qkrty`.

- **Test Tag Codebase Audit**:
  Grep for `start_match_button` across all files:
  ```powershell
  Get-ChildItem -Path app\src -Recurse -File | Select-String -Pattern "start_match_button"
  # Result:
  # app\src\main\java\com\example\ui\LobbyScreen.kt:366:              .testTag("start_match_button"),
  ```
  `start_match_button` occurs **only** at `LobbyScreen.kt:366`. Zero test files query or assert it.

---

### 1.3 Test Dependencies & Gradle Configuration
Inspection of `app/build.gradle.kts`:
- Compose Testing dependencies are already declared:
  ```kotlin
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  ```
- Test options include resources:
  ```kotlin
  testOptions { unitTests { isIncludeAndroidResources = true } }
  ```
- Current `Test` task block (lines 84–92):
  ```kotlin
  tasks.withType<Test> {
    failFast = true
    timeout.set(Duration.ofMinutes(2))
    systemProperty("robolectric.dependency.dir", "/home/appuser/.m2/repository/org/robolectric/android-all-instrumented/16-robolectric-13921718-i7")
    systemProperty("robolectric.offline", "true")
    testLogging {
      events("passed", "failed", "skipped")
    }
  }
  ```
- Gradle wrapper scripts:
  `gradlew` and `gradlew.bat` are missing from the project root (scheduled for restoration in Milestone 4).
  A clean, intact Gradle wrapper distribution exists on the workstation at:
  `C:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Taxido_driver\android` (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`).

---

## 2. Logic Chain

1. **Zero Regression Risk on Current Test Suite**:
   Observations 1.2 confirm that zero existing tests in `app/src/test/` or `app/src/androidTest/` instantiate `LobbyScreen` or search for `start_match_button`. Therefore, a full rewrite and visual overhaul of `LobbyScreen.kt` in Milestone 2 cannot break any existing test suite assertions.

2. **Caller Parameter Contract Compatibility**:
   Observation 1.1 establishes that `MainFeatureScreen.kt` passes:
   - `userProfile`
   - `customCards`
   - `onStartGame`
   - `onNavigateToStore`
   - `onOpenPaymentSetup`
   If `LobbyScreen`'s top-level signature is:
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
   Both the named arguments from `MainFeatureScreen.kt` and callers omitting defaults will compile seamlessly without any signature mismatches.

3. **Critical Flow for `onStartGame` & `onNavigateToStore`**:
   In `MainFeatureScreen.kt`:
   - `onStartGame` updates state to mount `GamePlayScreen`.
   - In the M2 redesign, when a user selects an unlocked deck in the 2-column grid, `DeckQuickPlayDialog` opens.
   - Inside `DeckQuickPlayDialog`, the primary CTA button ("Start Playing") must bear `.testTag("start_match_button")` and invoke `onStartGame(selectedDeck, mode, duration)`.
   - When tapping the "Unlock All Decks" anchor card (Row 0, Column 1) or any locked expansion deck, `onNavigateToStore()` must be called to transition `currentTab = 1`.

4. **Test Command Tooling Availability**:
   Once Gradle wrapper is restored in Milestone 4, the standard test command will be:
   - Run all unit tests: `.\gradlew.bat testDebugUnitTest`
   - Run specific test: `.\gradlew.bat testDebugUnitTest --tests "com.example.ExampleUnitTest"`
   In the interim (Milestones 2 and 3), verification relies on static inspection, Kotlin compiler contract verification, and interface parity.

---

## 3. Caveats

1. **Robolectric Linux Path in `app/build.gradle.kts`**:
   Lines 87–88 in `app/build.gradle.kts` point `robolectric.dependency.dir` to `/home/appuser/.m2/...`. On a Windows machine, running Robolectric tests offline with this path will fail unless online or overridden with Windows path. Pure JUnit 4 tests (`ExampleUnitTest`) do not use Robolectric and will pass regardless.
2. **Wrapper Execution Deferred to Milestone 4**:
   Milestone 4 explicitly owns wrapper restoration and build execution (`./gradlew.bat assembleDebug`). Milestone 2 is strictly scoped to `LobbyScreen.kt` UI implementation.
3. **Emulator Requirement for `MzansiRepositoryRuleTest`**:
   `MzansiRepositoryRuleTest` requires running Firebase emulators on ports 8085 and 9099. General unit test runs should filter out or skip emulator tests unless emulators are started.

---

## 4. Conclusion

1. **Contract Verified**:
   `MainFeatureScreen.kt` invokes `LobbyScreen` with 5 named parameters (`userProfile`, `customCards`, `onStartGame`, `onNavigateToStore`, `onOpenPaymentSetup`). The redesigned `LobbyScreen.kt` must retain this exact signature to prevent compilation breakage.
2. **No Regression on Existing Tests**:
   Zero existing tests depend on `LobbyScreen` internals or existing test tags. Worker M2 can freely redesign the layout to match the reference screenshot.
3. **Mandatory Test Tag**:
   The primary start button in the new `DeckQuickPlayDialog` must be annotated with `.testTag("start_match_button")` to fulfill the project test tag contract.
4. **Navigation Triggers**:
   - Tapping "Unlock All Decks" gold card -> `onNavigateToStore()`
   - Tapping locked expansion deck -> `onNavigateToStore()`
   - Tapping unlocked deck -> opens `DeckQuickPlayDialog`
   - Tapping "Start Playing" (`start_match_button`) -> `onStartGame(deck, mode, duration)`

---

## 5. Verification Method

### 5.1 Verification Checklist for M2 Worker & Reviewers

1. **Verify Interface Matching**:
   Inspect `LobbyScreen.kt` declaration against `MainFeatureScreen.kt:393`:
   ```powershell
   Select-String -Path "app\src\main\java\com\example\ui\LobbyScreen.kt" -Pattern "fun LobbyScreen" -Context 0,6
   Select-String -Path "app\src\main\java\com\example\ui\MainFeatureScreen.kt" -Pattern "0 -> LobbyScreen" -Context 0,14
   ```
   *Expected outcome*: Parameter names, types, and defaults align identically.

2. **Verify `start_match_button` testTag**:
   ```powershell
   Get-ChildItem -Path app\src\main\java\com\example\ui\LobbyScreen.kt | Select-String -Pattern "start_match_button"
   ```
   *Expected outcome*: Exactly 1 match located on the "Start Playing" button of `DeckQuickPlayDialog`.

3. **Verify Recommended Robolectric Test Spec (for M4/M5)**:
   Below is the ready-to-use Compose Robolectric test implementation to validate `LobbyScreen` once wrapper tooling is restored in M4:

```kotlin
package com.example.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.model.Deck
import com.example.data.model.PredefinedDecks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LobbyScreenTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun lobbyScreen_rendersHeader_andUnlockAllDecksNavigatesToStore() {
    var storeNavigated = false

    composeTestRule.setContent {
      LobbyScreen(
        userProfile = null,
        onStartGame = { _, _, _ -> },
        onNavigateToStore = { storeNavigated = true }
      )
    }

    // 1. Verify "Decks" header title is displayed
    composeTestRule.onNodeWithText("Decks").assertIsDisplayed()

    // 2. Verify "Unlock All Decks" card is displayed and triggers store
    composeTestRule.onNodeWithText("Unlock All Decks").assertIsDisplayed().performClick()
    assertTrue("Clicking Unlock All Decks must trigger onNavigateToStore", storeNavigated)
  }

  @Test
  fun lobbyScreen_clickUnlockedDeck_opensQuickPlay_andStartsGame() {
    var startedDeck: Deck? = null
    var startedMode: String? = null
    var startedDuration: Long? = null

    composeTestRule.setContent {
      LobbyScreen(
        userProfile = null,
        onStartGame = { deck, mode, duration ->
          startedDeck = deck
          startedMode = mode
          startedDuration = duration
        },
        onNavigateToStore = {}
      )
    }

    // 1. Click on the first free deck in the grid
    val freeDeck = PredefinedDecks.DECKS.first { it.isFreeTier }
    composeTestRule.onNodeWithText(freeDeck.name).assertIsDisplayed().performClick()

    // 2. Verify start_match_button is displayed in Quick Play modal and click it
    composeTestRule.onNodeWithTag("start_match_button").assertIsDisplayed().performClick()

    // 3. Verify onStartGame callback was invoked with expected deck and duration
    assertNotNull(startedDeck)
    assertEquals(freeDeck.id, startedDeck?.id)
    assertEquals(60L, startedDuration)
  }
}
```

4. **Test Command (once wrapper restored in M4)**:
   ```powershell
   .\gradlew.bat testDebugUnitTest --tests "com.example.ExampleUnitTest"
   ```
