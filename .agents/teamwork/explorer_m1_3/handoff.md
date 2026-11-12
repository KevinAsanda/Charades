# Handoff Report — Explorer M1.3: Gradle Dependencies & Test Suite Investigation

## 1. Observation

### 1.1 Gradle Configuration & Firebase AI Dependency
- **File**: `app/build.gradle.kts`
  - Line 98: `implementation(platform(libs.firebase.bom))`
  - Line 121: `  implementation(libs.firebase.ai)`
  - Lines 122-128:
    ```kotlin
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)
    implementation(libs.firebase.appcheck.recaptcha)
    implementation(libs.firebase.appcheck.debug)
    ```
  - Lines 84-92:
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
- **File**: `gradle/libs.versions.toml`
  - Line 38: `firebaseBom = "34.17.0"`
  - Line 90: `firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }`
  - Line 91: `firebase-ai = { group = "com.google.firebase", name = "firebase-ai" }`

### 1.2 Codebase Search for `firebase.ai` and `AiRepository`
- Command: `git grep -i "firebase.ai"`
  - `app/build.gradle.kts:121:  implementation(libs.firebase.ai)`
  - `gradle/libs.versions.toml:91:firebase-ai = { group = "com.google.firebase", name = "firebase-ai" }`
  - `.env.example:2:# Firebase AI Logic uses App Check attestation for security instead of client-side API keys.`
  - `app/src/main/java/com/example/data/repository/AiRepository.kt`: lines 3-11 (imports: `com.google.firebase.ai.ai`, `ImageConfig`, `ImagePart`, `ImageSize`, `ResponseModality`, `Schema`, `ThinkingLevel`, `generationConfig`, `thinkingConfig`), lines 23, 39, 58, 86 (`Firebase.ai.generativeModel(...)`).
- Command: `git grep -i "AiRepository"`
  - Only referenced in `app/src/main/java/com/example/data/repository/AiRepository.kt`, `app/src/main/java/com/example/ui/AiCoachScreen.kt`, and `app/src/main/java/com/example/ui/DeckStudioScreen.kt`.
  - Zero references in any test file or any other production source file.

### 1.3 Robolectric Test App Name Assertion
- **File**: `app/src/main/res/values/strings.xml`
  - Line 2: `<string name="app_name">Lekker Charades</string>`
- **File**: `app/src/test/java/com/example/ExampleRobolectricTest.kt`
  - Lines 16-20:
    ```kotlin
    @Test
    fun `read string from context`() {
      val context = ApplicationProvider.getApplicationContext<Context>()
      val appName = context.getString(R.string.app_name)
      assertEquals("Mzansi Charades", appName)
    }
    ```
  - Verbatim line 19: `assertEquals("Mzansi Charades", appName)`
  - Current behavior: `context.getString(R.string.app_name)` returns `"Lekker Charades"`, triggering an assertion failure.

### 1.4 Test Suite Complete Inventory
All files under `app/src/test/` and `app/src/androidTest/`:
1. `app/src/test/java/com/example/ExampleRobolectricTest.kt`:
   - Runner: `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [36])`.
   - Tests: `read string from context`.
   - AI References: None.
   - Status: Needs line 19 assertion update to `"Lekker Charades"`.
2. `app/src/test/java/com/example/ExampleUnitTest.kt`:
   - Runner: Standard JUnit 4.
   - Tests: `addition_isCorrect()` with `assertEquals(4, 2 + 2)`.
   - AI References: None.
   - Status: Passing, untouched.
3. `app/src/test/java/com/example/base/FirestoreEmulatorTestBase.kt`:
   - Runner: `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [36], manifest = Config.NONE)`, `@LooperMode(LooperMode.Mode.INSTRUMENTATION_TEST)`.
   - Purpose: Base class configuring local Firestore emulator (`127.0.0.1:8085`) and Auth emulator (`127.0.0.1:9099`).
   - Uses `applicationId = "com.aistudio.mzansicharades.qkrty"`.
   - AI References: None.
   - Status: Clean, untouched.
4. `app/src/test/java/com/example/data/repository/MzansiRepositoryRuleTest.kt`:
   - Runner: Subclass of `FirestoreEmulatorTestBase`.
   - Tests: 6 integration tests for `UserRepository`, `GameSessionRepository`, `CustomCardRepository`, and `PaymentRepository`.
   - AI References: None.
   - Status: Clean, untouched.
5. `app/src/androidTest/java/com/example/ExampleInstrumentedTest.kt`:
   - Runner: `@RunWith(AndroidJUnit4::class)`.
   - Tests: `useAppContext()` checking package name against `BuildConfig.APPLICATION_ID`.
   - AI References: None.
   - Status: Clean, untouched.

---

## 2. Logic Chain

1. **Firebase AI Removal Safety**:
   - Observation 1.2 establishes that `com.google.firebase.ai` is exclusively imported and called within `AiRepository.kt`.
   - Observation 1.2 establishes that `AiRepository` is exclusively used by `AiCoachScreen.kt` and `DeckStudioScreen.kt`.
   - As defined in `ORIGINAL_REQUEST.md` (R3) and `SCOPE.md` (Feature 1), `AiCoachScreen.kt`, `DeckStudioScreen.kt`, and `AiRepository.kt` are being completely deleted in Milestone 1.
   - Consequently, removing `implementation(libs.firebase.ai)` from `app/build.gradle.kts` (line 121) and `firebase-ai` from `gradle/libs.versions.toml` (line 91) leaves zero orphaned callers or broken references.
   - Observation 1.1 shows that all other Firebase libraries (`firestore`, `auth`, `appcheck.*`) are declared as separate artifacts under `firebase.bom`. Removing `firebase-ai` has zero side-effects on other Firebase features.
   - Therefore, removing `libs.firebase.ai` is 100% safe.

2. **Robolectric Test Assertion Fix**:
   - Observation 1.3 shows `app/src/main/res/values/strings.xml` defines `<string name="app_name">Lekker Charades</string>`.
   - Observation 1.3 shows `ExampleRobolectricTest.kt` line 19 asserts `assertEquals("Mzansi Charades", appName)`.
   - Because `appName` evaluates to `"Lekker Charades"` at runtime, the assertion fails with `expected:<[Mzansi] Charades> but was:<[Lekker] Charades>`.
   - Updating line 19 to `assertEquals("Lekker Charades", appName)` resolves the failure and reflects the current app name.

3. **Test Suite Scope & AI References**:
   - Observation 1.4 catalogs all test files across `app/src/test/` and `app/src/androidTest/`.
   - `git grep -i "ai" app/src/test/` and `git grep -i "coach" app/src/test/` confirm there are zero unit tests, integration tests, or mock fixtures for `AiCoachScreen`, `DeckStudioScreen`, or `AiRepository`.
   - Therefore, no test files need deletion or modification regarding AI features.

---

## 3. Caveats

1. **Robolectric Offline Configuration in Local Windows Environment**:
   - In `app/build.gradle.kts` lines 87-88:
     ```kotlin
     systemProperty("robolectric.dependency.dir", "/home/appuser/.m2/repository/org/robolectric/android-all-instrumented/16-robolectric-13921718-i7")
     systemProperty("robolectric.offline", "true")
     ```
     This property points to a Linux path configured for the Google AI Studio container. If running Robolectric tests locally on Windows without this directory, Robolectric may attempt to find jar files at that path. Standard JVM unit tests (`ExampleUnitTest.kt`) and APK compilation (`assembleDebug`) are not affected. For Milestones 4 and 5, the build team should be aware of this configuration.
2. **Firestore Emulator Dependency for `MzansiRepositoryRuleTest`**:
   - `MzansiRepositoryRuleTest` connects to local ports 8085 and 9099. If Firebase emulators are not running during `./gradlew test`, those specific emulator integration tests will timeout after 6000ms.
3. **Root Project Name**:
   - `settings.gradle.kts` line 25 defines `rootProject.name = "Mzansi Charades"`. While `strings.xml` defines `app_name` as `"Lekker Charades"`, `rootProject.name` is purely internal Gradle project naming and does not affect app runtime or APK branding.

---

## 4. Conclusion

The exact, actionable recommendations for the Milestone 1 Worker (`worker_m1`) are:

1. **`app/build.gradle.kts`**:
   - Delete Line 121:
     ```kotlin
     implementation(libs.firebase.ai)
     ```
2. **`gradle/libs.versions.toml`**:
   - Delete Line 91:
     ```toml
     firebase-ai = { group = "com.google.firebase", name = "firebase-ai" }
     ```
3. **`app/src/test/java/com/example/ExampleRobolectricTest.kt`**:
   - Edit Line 19:
     - Replace:
       ```kotlin
       assertEquals("Mzansi Charades", appName)
       ```
     - With:
       ```kotlin
       assertEquals("Lekker Charades", appName)
       ```
4. **All other test files**:
   - No modifications required. No tests reference AI features or deleted classes.

---

## 5. Verification Method

To independently verify this investigation:

1. **Verify `firebase-ai` references in source code**:
   ```powershell
   git grep -i "firebase.ai"
   git grep -i "com.google.firebase.ai"
   ```
   *Expected outcome*: Confirms references exist only in `app/build.gradle.kts:121`, `gradle/libs.versions.toml:91`, and `AiRepository.kt` (which is deleted).

2. **Verify app name string resource**:
   ```powershell
   git grep "app_name" app/src/main/res/values/strings.xml
   ```
   *Expected outcome*: `<string name="app_name">Lekker Charades</string>`

3. **Verify `ExampleRobolectricTest.kt` string mismatch**:
   ```powershell
   git grep "Mzansi Charades" app/src/test/java/com/example/ExampleRobolectricTest.kt
   ```
   *Expected outcome*: Line 19 contains `assertEquals("Mzansi Charades", appName)`

4. **Verify absence of AI references in test files**:
   ```powershell
   git grep -i "AiRepository" app/src/test/
   git grep -i "AiCoach" app/src/test/
   git grep -i "DeckStudio" app/src/test/
   ```
   *Expected outcome*: Exit code 1 (zero matches).
