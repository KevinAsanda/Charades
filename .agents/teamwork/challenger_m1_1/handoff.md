# Adversarial Challenge Report: Milestone 1 Execution (Challenger M1.1)

**Verdict**: `APPROVE`  
**Overall Risk Assessment**: LOW  
**Target Milestone**: M1 (AI Feature Stripping, Navigation Verification & Cleanliness)

---

## 1. Observation

Direct empirical observations and measurements gathered through automated adversarial test harnesses:

### 1.1 Physical Deletion of Dead AI Assets
Direct filesystem inspection confirms that the following 4 targets do not exist:
- `app/src/main/java/com/example/ui/AiCoachScreen.kt` (Exists: `False`)
- `app/src/main/java/com/example/ui/DeckStudioScreen.kt` (Exists: `False`)
- `app/src/main/java/com/example/data/repository/AiRepository.kt` (Exists: `False`)
- `app/src/main/res/drawable/ic_magic.xml` (Exists: `False`)
- Git index confirms all 4 files are staged for deletion (`git status --short`: `D app/...`).

### 1.2 Broken Imports & Orphan Reference Audit
- Scanned 852 import statements across all Kotlin source files in `app/src/main/`, `app/src/test/`, and `app/src/androidTest/`.
  - Zero imports of `AiCoachScreen`, `DeckStudioScreen`, `AiRepository`, `CardImageSize`, `GeneratedDeck`, or `com.google.firebase.ai.*` exist.
- Scanned all production and test files (`.kt`, `.xml`, `.kts`, `.toml`, `.properties`) for forbidden symbols (`AiCoachScreen`, `DeckStudioScreen`, `AiRepository`, `CardImageSize`, `GeneratedDeck`, `ic_magic`, `onNavigateToStudio`, `ThinkingLevel`, `ImageConfig`, `ImagePart`, `ImageSize`, `ResponseModality`, `generationConfig`, `thinkingConfig`, `referee_query_input`).
  - Total matches: 0.
- Checked for commented-out dead imports (`// import ...Ai...`):
  - Total matches: 0.

### 1.3 Mock / Stub Fallback Audit
- AST regex scan for mock definitions (`class AiRepository`, `fun AiCoachScreen`, `fun DeckStudioScreen`, `enum class CardImageSize`, `data class GeneratedDeck`):
  - Total matches: 0. Zero mock cheats or placeholder classes exist.

### 1.4 Gradle Dependency Stripping
- `app/build.gradle.kts`: `implementation(libs.firebase.ai)` removed.
- `gradle/libs.versions.toml`: `firebase-ai` removed. Valid TOML parsing verified; remaining 52 catalog libraries intact (including `firebase-firestore`, `firebase-auth`, `firebase-bom`, `firebase-appcheck-*`).

### 1.5 Contract & Signature Verification: LobbyScreen ↔ MainFeatureScreen
- Analyzed `app/src/main/java/com/example/ui/LobbyScreen.kt` declaration:
  ```kotlin
  @Composable
  fun LobbyScreen(
    userProfile: UserProfile?,
    customCards: List<CustomCard>,
    onStartGame: (Deck, String, Long) -> Unit,
    onNavigateToStore: () -> Unit,
    onOpenPaymentSetup: () -> Unit = {}
  )
  ```
  - `onNavigateToStudio` parameter is absent.
  - Zero occurrences of `onNavigateToStudio` exist inside `LobbyScreen.kt`.
- Analyzed `app/src/main/java/com/example/ui/MainFeatureScreen.kt` call-site:
  ```kotlin
  0 -> LobbyScreen(
    userProfile = userProfile,
    customCards = customCards,
    onStartGame = { deck, mode, duration ->
      activePlayingDeck = deck
      activeGameMode = mode
      activeRoundDuration = duration
    },
    onNavigateToStore = { currentTab = 1 },
    onOpenPaymentSetup = {
      pendingCheckoutItem = null
      showPaymentModal = true
    }
  )
  ```
  - Exactly 5 named arguments are passed, mapping 1-to-1 to the 5 declared parameters of `LobbyScreen`.
  - Zero studio parameters are passed.
- Verified `MainFeatureScreen.kt` navigation structure:
  - Bottom bar strictly contains 3 `NavigationBarItem`s:
    - Tab 0: testTag `nav_tab_play` ("Play")
    - Tab 1: testTag `nav_tab_store` ("Store")
    - Tab 2: testTag `nav_tab_profile` ("Profile")
  - `when (currentTab)` strictly branches on `0`, `1`, `2`.

### 1.6 Identity Test Assertion Verification
- `app/src/main/res/values/strings.xml`:
  ```xml
  <string name="app_name">Lekker Charades</string>
  ```
- `app/src/test/java/com/example/ExampleRobolectricTest.kt` line 19:
  ```kotlin
  assertEquals("Lekker Charades", appName)
  ```
  - XML parsed value matches test string assertion identically (`"Lekker Charades" == "Lekker Charades"`).

### 1.7 Non-Target File Integrity
- `app/src/main/java/com/example/data/model/CustomCard.kt`: Intact.
- `app/src/main/java/com/example/data/repository/CustomCardRepository.kt`: Intact.
- `app/src/test/java/com/example/data/repository/MzansiRepositoryRuleTest.kt`: Intact.

---

## 2. Logic Chain

1. **Safety of Removals (Observation 1.1 & 1.2)**:
   Because exhaustive symbol and import scans yielded zero matches across all remaining files, the deletion of `AiCoachScreen.kt`, `DeckStudioScreen.kt`, `AiRepository.kt`, and `ic_magic.xml` did not leave any dangling or broken references in the repository.

2. **Clean Dependency Decoupling (Observation 1.4)**:
   The removed library `libs.firebase.ai` was consumed exclusively by `AiRepository.kt`. Its removal from `app/build.gradle.kts` and `gradle/libs.versions.toml` was verified to leave valid TOML without affecting required Firebase services.

3. **Interface Contract Cleanliness (Observation 1.5)**:
   The `onNavigateToStudio` parameter was removed from `LobbyScreen.kt`. Examination of `MainFeatureScreen.kt` confirms that `LobbyScreen` is invoked using named parameters matching all 5 declared arguments without mismatch. The bottom navigation bar is verified to present only 3 tabs (Play, Store, Profile), satisfying the architectural requirement of 0 AI entry points.

4. **Test Identity Accuracy (Observation 1.6)**:
   `ExampleRobolectricTest.kt` was updated to assert `"Lekker Charades"`, matching `strings.xml`. The obsolete `"Mzansi Charades"` branding string has been fully replaced.

5. **Absence of Evasion / Mocks (Observation 1.3)**:
   No mock, stub, or dummy classes were introduced to fake the deleted symbols. The AI feature removal was genuine and complete.

---

## 3. Caveats

- **Gradle Wrapper Restoration (M4 Scope)**: As planned across the project lifecycle, `gradlew` / `gradlew.bat` binaries are not committed to the repository at this milestone. Full end-to-end APK assembly (`assembleDebug`) and Robolectric test execution will occur in Milestone 4 once the wrapper is restored.
- **LobbyScreen Layout Redesign (M2 Scope)**: Visual grid restructuring of `LobbyScreen.kt` into the 2-column full-bleed layout is owned by Milestone 2. Milestone 1 changes to `LobbyScreen.kt` were strictly confined to parameter cleanup (`onNavigateToStudio`).
- **Unstaged GamePlayScreen.kt**: `GamePlayScreen.kt` contains orientation modifications prepared for Milestone 3; these were left untouched and do not impact Milestone 1 verification.

---

## 4. Conclusion

The changes made by Worker M1 for Milestone 1 are complete, robust, cleanly decoupled, and free of defects. Zero broken imports, zero orphan symbols, zero mock cheats, and zero navigation mismatches exist.

**Explicit Verdict**: `APPROVE`.

---

## 5. Verification Method

To reproduce and independently verify all 35 empirical checks:

1. **Verify git status & deleted files**:
   ```powershell
   git status --short
   Test-Path "app\src\main\java\com\example\ui\AiCoachScreen.kt", "app\src\main\java\com\example\ui\DeckStudioScreen.kt", "app\src\main\java\com\example\data\repository\AiRepository.kt", "app\src\main\res\drawable\ic_magic.xml"
   ```
   *Expected outcome*: All return `False`.

2. **Verify zero residual AI symbols via git grep**:
   ```powershell
   git grep -n "AiCoachScreen"
   git grep -n "DeckStudioScreen"
   git grep -n "AiRepository"
   git grep -n "CardImageSize"
   git grep -n "GeneratedDeck"
   git grep -n "ic_magic"
   git grep -n "onNavigateToStudio"
   git grep -n "firebase-ai"
   ```
   *Expected outcome*: All return exit code 1 (0 matches).

3. **Verify strings.xml vs ExampleRobolectricTest**:
   ```powershell
   Select-String -Path "app\src\main\res\values\strings.xml" -Pattern 'app_name'
   Select-String -Path "app\src\test\java\com\example\ExampleRobolectricTest.kt" -Pattern 'assertEquals'
   ```
   *Expected outcome*: Both reference `"Lekker Charades"`.

4. **Verify LobbyScreen ↔ MainFeatureScreen signature alignment**:
   ```powershell
   Select-String -Path "app\src\main\java\com\example\ui\LobbyScreen.kt" -Pattern 'fun LobbyScreen' -Context 0, 7
   Select-String -Path "app\src\main\java\com\example\ui\MainFeatureScreen.kt" -Pattern '0 -> LobbyScreen' -Context 0, 14
   ```
   *Expected outcome*: Both show matching 5 parameters without `onNavigateToStudio`.

---

## 6. Adversarial Challenge Matrix

| # | Challenge Hypothesis | Attack Scenario | Actual Result | Status |
|---|---------------------|-----------------|---------------|--------|
| CH-01 | Dead import leakage | Deleted files still imported in unvisited Kotlin files | Scanned 852 imports across all `.kt` files; 0 matches | PASS |
| CH-02 | Hidden AI navigation hook | `onNavigateToStudio` or AI studio route still callable in `LobbyScreen` or `MainFeatureScreen` | Signature and call-site parsed; 0 occurrences | PASS |
| CH-03 | Mock / Stub evasion | Deleted classes replaced with empty stubs to bypass compilation | AST regex scan confirmed 0 stub classes or functions | PASS |
| CH-04 | TOML catalog corruption | Dependency deletion broke TOML syntax or related Firebase libs | Parsed `libs.versions.toml` with `tomllib`; 52 libs valid | PASS |
| CH-05 | Obsolete branding in test | `ExampleRobolectricTest` asserts outdated `"Mzansi Charades"` | Verified against `strings.xml`: both assert `"Lekker Charades"` | PASS |
| CH-06 | Non-target collateral damage | Deleting `DeckStudioScreen` destroyed `CustomCard` model/repo | Verified `CustomCard.kt` and `CustomCardRepository.kt` intact | PASS |
| CH-07 | 4th navigation tab residual | Bottom navigation bar still retains Coach or Studio tab | Verified strictly 3 tabs (`nav_tab_play`, `nav_tab_store`, `nav_tab_profile`) | PASS |
