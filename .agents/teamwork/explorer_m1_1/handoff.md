# Milestone 1: Dead AI Files & Drawables Investigation Report (M1.1)

## 1. Observation

Direct observations and evidence gathered across the codebase:

### Target 1: `app/src/main/java/com/example/ui/AiCoachScreen.kt`
- **Definition**: Lines 68–70:
  ```kotlin
  @Composable
  fun AiCoachScreen(
    aiRepository: AiRepository
  ) {
  ```
- **Pattern Match Search**: Executed `git grep -n "AiCoachScreen"` across the repository:
  ```
  app/src/main/java/com/example/ui/AiCoachScreen.kt:68:fun AiCoachScreen(
  ```
  Result: Exactly 1 occurrence (the function definition itself). No other file in the project imports, references, or calls `AiCoachScreen`.
- **Sub-components**: Contains private composables `SegmentPillTab`, `FastSlangCoachSection`, `HighThinkingRefereeSection`. None are exported or referenced outside this file.
- **Dependencies**: Depends on `com.example.data.repository.AiRepository` and common UI components (`com.example.ui.components.*`, `com.example.ui.theme.*`).

### Target 2: `app/src/main/java/com/example/ui/DeckStudioScreen.kt`
- **Definition**: Lines 80–85:
  ```kotlin
  @Composable
  fun DeckStudioScreen(
    aiRepository: AiRepository,
    customCards: List<CustomCard>,
    onAddCard: (CustomCard) -> Unit,
    onDeleteCard: (String) -> Unit
  ) {
  ```
- **Pattern Match Search**: Executed `git grep -n "DeckStudioScreen"` across the repository:
  ```
  app/src/main/java/com/example/ui/DeckStudioScreen.kt:80:fun DeckStudioScreen(
  ```
  Result: Exactly 1 occurrence (the function definition itself). No other file imports or references `DeckStudioScreen`.
- **Sub-components**: Contains private composables `StudioTabPill`, `AiDeckGeneratorTab`, `AiCardArtGeneratorTab`, `MyCustomCardsTab`. None are exported or referenced outside this file.
- **Dependencies**: Depends on `AiRepository`, `CustomCard`, `CardImageSize`, `GeneratedDeck`, and common UI components.

### Target 3: `app/src/main/java/com/example/data/repository/AiRepository.kt`
- **Definition**:
  - Line 33: `enum class CardImageSize(val label: String, val imageSize: ImageSize)`
  - Line 39: `data class GeneratedDeck(val title: String, val description: String, val cards: List<CharadesCard>)`
  - Line 45: `class AiRepository`
- **Pattern Match Search for `AiRepository`**: Executed `git grep -n "AiRepository"`:
  ```
  app/src/main/java/com/example/data/repository/AiRepository.kt:31:private const val TAG = "AiRepository"
  app/src/main/java/com/example/data/repository/AiRepository.kt:45:class AiRepository {
  app/src/main/java/com/example/ui/AiCoachScreen.kt:46:import com.example.data.repository.AiRepository
  app/src/main/java/com/example/ui/AiCoachScreen.kt:69:  aiRepository: AiRepository
  app/src/main/java/com/example/ui/AiCoachScreen.kt:160:private fun FastSlangCoachSection(aiRepository: AiRepository) {
  app/src/main/java/com/example/ui/AiCoachScreen.kt:341:private fun HighThinkingRefereeSection(aiRepository: AiRepository) {
  app/src/main/java/com/example/ui/DeckStudioScreen.kt:53:import com.example.data.repository.AiRepository
  app/src/main/java/com/example/ui/DeckStudioScreen.kt:81:  aiRepository: AiRepository,
  app/src/main/java/com/example/ui/DeckStudioScreen.kt:181:  aiRepository: AiRepository,
  app/src/main/java/com/example/ui/DeckStudioScreen.kt:416:  aiRepository: AiRepository,
  ```
  Result: Referenced solely within `AiCoachScreen.kt` and `DeckStudioScreen.kt`. Zero callers outside these two screens.
- **Pattern Match Search for `CardImageSize`**: Executed `git grep -n "CardImageSize"`:
  Found exclusively in `AiRepository.kt` (lines 33, 231, 498, 501–503) and `DeckStudioScreen.kt` (lines 54, 421, 507).
- **Pattern Match Search for `GeneratedDeck`**: Executed `git grep -n "GeneratedDeck"`:
  Found exclusively in `AiRepository.kt` (lines 39, 150, 185, 314, 317, 371, 425) and `DeckStudioScreen.kt` (lines 55, 187).
- **Gradle Dependencies**:
  - `app/build.gradle.kts`: Line 121: `implementation(libs.firebase.ai)`
  - `gradle/libs.versions.toml`: Line 91: `firebase-ai = { group = "com.google.firebase", name = "firebase-ai" }`
  - Pattern search `git grep -n "firebase.*ai"` confirms no other file in the project uses Firebase AI / GenAI SDK.

### Target 4: `app/src/main/res/drawable/ic_magic.xml`
- **Definition**: 24dp vector drawable (lines 1–11).
- **Pattern Match Search**: Executed `git grep -n "ic_magic"`:
  Result: 0 occurrences across the entire project. Not referenced in any layout, drawable, theme, or Kotlin composable.

### Adjacent Observations:
1. **`app/src/main/java/com/example/ui/LobbyScreen.kt`**:
   - Line 153 declares parameter: `onNavigateToStudio: () -> Unit = {},`
   - Searching `git grep -n "onNavigateToStudio"` across the entire project returns only line 153 of `LobbyScreen.kt`.
   - The parameter is NEVER invoked in `LobbyScreen.kt`.
   - In `MainFeatureScreen.kt` line 393–406, `LobbyScreen` is instantiated with explicit named arguments without passing `onNavigateToStudio`.
2. **`app/src/main/java/com/example/ui/MainFeatureScreen.kt`**:
   - Lines 360–383 define bottom navigation items: strictly 3 items:
     - Index 0: "Play" (icon: `Icons.Default.SportsEsports`)
     - Index 1: "Store" (icon: `Icons.Default.ShoppingBag`)
     - Index 2: "Profile" (icon: `Icons.Default.Person`)
   - Lines 392–492 define `when (currentTab)` with cases `0` (LobbyScreen), `1` (StoreScreen), and `2` (ProfileScreen).
   - Zero tabs or entry points exist for AI Coach or Deck Studio in navigation.
3. **`app/src/test/java/com/example/ExampleRobolectricTest.kt`**:
   - Line 18–19:
     ```kotlin
     val appName = context.getString(R.string.app_name)
     assertEquals("Mzansi Charades", appName)
     ```
   - In `app/src/main/res/values/strings.xml`, line 2:
     ```xml
     <string name="app_name">Lekker Charades</string>
     ```
   - The test currently asserts the obsolete app title `"Mzansi Charades"` rather than `"Lekker Charades"`.
4. **`CustomCard` Model**:
   - `CustomCard` is declared in `app/src/main/java/com/example/data/model/CustomCard.kt`.
   - Used by `CustomCardRepository.kt`, `LocalPlayerStore.kt`, `LobbyScreen.kt`, `MainFeatureScreen.kt`, and `MzansiRepositoryRuleTest.kt`.
   - `CustomCard` is entirely separate from `AiRepository` and must be retained.

---

## 2. Logic Chain

1. **Premise 1 (`AiCoachScreen.kt`)**: Observation shows `AiCoachScreen` is never invoked, imported, or referenced anywhere in `MainActivity.kt`, `MainFeatureScreen.kt`, `LobbyScreen.kt`, or tests.
   - *Inference*: Removing `AiCoachScreen.kt` breaks zero call sites, zero imports, and zero dependencies.

2. **Premise 2 (`DeckStudioScreen.kt`)**: Observation shows `DeckStudioScreen` is never invoked, imported, or referenced anywhere in `MainActivity.kt`, `MainFeatureScreen.kt`, `LobbyScreen.kt`, or tests.
   - *Inference*: Removing `DeckStudioScreen.kt` breaks zero call sites, zero imports, and zero dependencies.

3. **Premise 3 (`AiRepository.kt`)**: Observation shows `AiRepository`, `CardImageSize`, and `GeneratedDeck` are imported and referenced solely within `AiCoachScreen.kt` and `DeckStudioScreen.kt`.
   - *Inference*: Once Premise 1 and Premise 2 hold (the two screens are deleted), `AiRepository.kt` has zero callers across the codebase. Deleting `AiRepository.kt` will not leave any unresolved symbols.

4. **Premise 4 (`libs.firebase.ai`)**: Observation shows `com.google.firebase:firebase-ai` is imported only in `AiRepository.kt`.
   - *Inference*: After deleting `AiRepository.kt`, the dependency `libs.firebase.ai` in `app/build.gradle.kts` and `gradle/libs.versions.toml` becomes obsolete and can be removed without affecting Firebase Auth, Firestore, or App Check.

5. **Premise 5 (`ic_magic.xml`)**: Observation shows `ic_magic.xml` is unreferenced in any XML file, drawable, or Kotlin file.
   - *Inference*: Deleting `ic_magic.xml` has zero impact on resources or runtime UI.

6. **Premise 6 (`LobbyScreen.kt` signature)**: Observation shows `onNavigateToStudio` in `LobbyScreen.kt` has a default value `{}`, is unused in its body, and is omitted at the sole call site in `MainFeatureScreen.kt`.
   - *Inference*: Removing `onNavigateToStudio` from `LobbyScreen.kt`'s parameter list cleans the interface contract and introduces zero compilation errors.

7. **Premise 7 (`ExampleRobolectricTest.kt`)**: Observation shows `strings.xml` defines `app_name` as `"Lekker Charades"`, but line 19 of `ExampleRobolectricTest.kt` asserts `"Mzansi Charades"`.
   - *Inference*: Updating the assertion string to `"Lekker Charades"` reconciles the test with the brand identity.

---

## 3. Caveats

- **No caveats regarding code breakages**: Exhaustive `git grep` audits confirmed 0 inbound references for `AiCoachScreen`, `DeckStudioScreen`, `AiRepository`, and `ic_magic`.
- **Firestore resource strings**: `app/src/main/res/values/firebase_applet_config.xml` contains `<string name="firestore_database_id">ai-studio-android-mzansich-...</string>`. This is the remote cloud project ID for Firestore, NOT a local AI feature, and MUST NOT be altered.
- **`CustomCard` retention**: `CustomCard.kt` and `CustomCardRepository.kt` must remain intact; they provide local and cloud custom card persistence used by `LobbyScreen` and verified by `MzansiRepositoryRuleTest.kt`.
- **Gradle wrapper state**: As noted in PROJECT.md, Gradle wrapper binaries (`gradlew`, `gradlew.bat`, `gradle-wrapper.jar`) are currently missing from the repo root and will be restored in Milestone 4. Full gradle build execution is verified during M4/M5.

---

## 4. Conclusion

Deleting the 4 targeted files:
1. `app/src/main/java/com/example/ui/AiCoachScreen.kt`
2. `app/src/main/java/com/example/ui/DeckStudioScreen.kt`
3. `app/src/main/java/com/example/data/repository/AiRepository.kt`
4. `app/src/main/res/drawable/ic_magic.xml`

is **100% safe, non-breaking, and complete**.

### Recommended Action Plan for Worker M1.2:
1. **File Deletions**:
   - Delete `app/src/main/java/com/example/ui/AiCoachScreen.kt`
   - Delete `app/src/main/java/com/example/ui/DeckStudioScreen.kt`
   - Delete `app/src/main/java/com/example/data/repository/AiRepository.kt`
   - Delete `app/src/main/res/drawable/ic_magic.xml`
2. **Build Dependency Cleanliness**:
   - In `app/build.gradle.kts`: remove line 121 (`implementation(libs.firebase.ai)`)
   - In `gradle/libs.versions.toml`: remove line 91 (`firebase-ai = ...`)
3. **UI Contract Cleanliness**:
   - In `app/src/main/java/com/example/ui/LobbyScreen.kt`: remove line 153 (`onNavigateToStudio: () -> Unit = {},`)
4. **Test Assertion Correction**:
   - In `app/src/test/java/com/example/ExampleRobolectricTest.kt`: update line 19 from `"Mzansi Charades"` to `"Lekker Charades"`

---

## 5. Verification Method

To independently verify the investigation findings and safe deletion:

1. **Verify No Remaining References to Targets**:
   Run the following commands in the workspace root:
   ```powershell
   git grep -n "AiCoachScreen"
   git grep -n "DeckStudioScreen"
   git grep -n "AiRepository"
   git grep -n "ic_magic"
   git grep -n "CardImageSize"
   git grep -n "GeneratedDeck"
   ```
   *Expected result*: Only matches within the 4 target files themselves, with zero matches in any other file.

2. **Verify Navigation Isolation**:
   Inspect `app/src/main/java/com/example/ui/MainFeatureScreen.kt` lines 360–492:
   Confirm tabs are strictly Play (`0`), Store (`1`), and Profile (`2`).

3. **Verify `LobbyScreen` Caller Compatibility**:
   Inspect `app/src/main/java/com/example/ui/MainFeatureScreen.kt` lines 393–406:
   Confirm `LobbyScreen` is called with named parameters and does not pass `onNavigateToStudio`.

4. **Verify Test Assertion Mismatch**:
   Inspect `app/src/main/res/values/strings.xml` line 2 vs `app/src/test/java/com/example/ExampleRobolectricTest.kt` line 19:
   Confirm strings.xml has `"Lekker Charades"` while test asserts `"Mzansi Charades"`.
