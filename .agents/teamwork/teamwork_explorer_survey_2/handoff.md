# Handoff Report: R3 (Feature Simplification & AI Removal) & Navigation Architecture Investigation

**Date**: 2026-11-12T20:50:00Z  
**Agent**: Explorer 2 (`teamwork_explorer_survey_2`)  
**Mission**: Codebase investigation for Navigation Architecture, 3-Tab Structure, AI Feature Stripping, Branding Assets, and Inventory of Files to Modify.

---

## 1. Observation

### 1.1 Navigation Architecture & Structure
1. **Entry Point & High-Level Routing (`MainActivity.kt`)**:
   - `MainActivity.kt:71-93`: Navigation is entirely state-driven within Jetpack Compose (no `NavHost` / `rememberNavController` is used):
     ```kotlin
     val effectiveUid = firebaseUid ?: localUserId

     if (effectiveUid == null) {
       AuthScreen(onAuthSuccess = { ... })
     } else {
       key(effectiveUid) {
         MainFeatureScreen(currentUserId = effectiveUid)
       }
     }
     ```
2. **In-App Navigation & Destinations (`MainFeatureScreen.kt`)**:
   - `MainFeatureScreen.kt:311-329`: When `activePlayingDeck != null`, the full screen displays `GamePlayScreen(...)`.
   - `MainFeatureScreen.kt:330-494`: When `activePlayingDeck == null`, a `Scaffold` hosts the bottom `NavigationBar` with an internal state variable:
     - `MainFeatureScreen.kt:221`: `var currentTab by remember { mutableIntStateOf(0) }`
   - Bottom bar contains exactly 3 `NavigationBarItem` elements:
     - **Tab 0 (Play)** (`MainFeatureScreen.kt:340-353`):
       - Label: `Text("Play", style = StripiTypeTokens.micro)`
       - Icon: `Icon(Icons.Default.PlayArrow, contentDescription = "Play")`
       - Modifier: `Modifier.testTag("nav_tab_play")`
       - Target Composable (`MainFeatureScreen.kt:393-406`): `LobbyScreen(...)`
     - **Tab 1 (Store)** (`MainFeatureScreen.kt:354-367`):
       - Label: `Text("Store", style = StripiTypeTokens.micro)`
       - Icon: `Icon(Icons.Default.ShoppingBag, contentDescription = "Store")`
       - Modifier: `Modifier.testTag("nav_tab_store")`
       - Target Composable (`MainFeatureScreen.kt:407-478`): `StoreScreen(...)`
     - **Tab 2 (Profile)** (`MainFeatureScreen.kt:368-381`):
       - Label: `Text("Profile", style = StripiTypeTokens.micro)`
       - Icon: `Icon(Icons.Default.Person, contentDescription = "Profile")`
       - Modifier: `Modifier.testTag("nav_tab_profile")`
       - Target Composable (`MainFeatureScreen.kt:479-491`): `ProfileScreen(...)`
   - Modal Dialog Destination:
     - `PaymentCheckoutDialog` (`MainFeatureScreen.kt:223-309`), toggled via `showPaymentModal: Boolean`. Accessible from Lobby (via ZAR pill), Store, and Profile.

### 1.2 Historical Navigation & Removal of Extra Tabs
- Git history reveals that in commit `cc871e544ce58c0a06b45bbc35f0cd07dc851343` ("Integrate AdMob rewarded ads & Google Play Billing; strip down navigation to Play, Store, Profile; restrict daily gift to once per day"):
  - `MainFeatureScreen.kt` previously had 5 navigation items:
    - Tab 0: Play (`LobbyScreen`)
    - Tab 1: Store (`StoreScreen`)
    - Tab 2: Coach (`AiCoachScreen`, icon `Icons.Default.AutoAwesome`, testTag `nav_tab_coach`)
    - Tab 3: Studio (`DeckStudioScreen`, icon `Icons.Default.Palette`, testTag `nav_tab_studio`)
    - Tab 4: Profile (`ProfileScreen`, icon `Icons.Default.Person`, testTag `nav_tab_profile`)
  - Commit `cc871e5` removed tabs 2 (`AiCoachScreen`) and 3 (`DeckStudioScreen`) from `NavigationBar` and remapped Profile to index 2.

### 1.3 Audit of AI Features & Remnants in Codebase
1. **Unlinked AI Screens (Dead Code)**:
   - `app/src/main/java/com/example/ui/AiCoachScreen.kt` (519 lines):
     - Line 84: `text = "Slang Coach & Referee."`
     - Line 89: `text = "Instant acting clues via gemini-3.1-flash-lite and deep cultural dispute adjudication via gemini-4-argon."`
     - Sub-composables: `FastSlangCoachSection` and `HighThinkingRefereeSection`.
     - Zero references in active UI; completely unlinked from navigation.
   - `app/src/main/java/com/example/ui/DeckStudioScreen.kt` (851 lines):
     - Line 96: `StripiSoftPillTag(text = "Mzansi Creative Studio")`
     - Line 104: `text = "Generate structured decks with gemini-3.8-flash and 1K, 2K, or 4K artwork with gemini-3-pro-image-preview."`
     - Line 120: `title = "AI Deck Maker"`
     - Sub-composables: `AiDeckGeneratorTab`, `AiCardArtGeneratorTab`, `MyCustomCardsTab`.
     - Zero references in active UI; completely unlinked from navigation.
2. **AI Repository**:
   - `app/src/main/java/com/example/data/repository/AiRepository.kt` (282 lines):
     - Line 14: `import com.google.firebase.ai.ai`
     - Calls `gemini-3.1-flash-lite`, `gemini-3.8-flash`, `gemini-4-argon` with `ThinkingLevel.HIGH`, and `gemini-3-pro-image-preview`.
     - Has no active instantiations or callers in `MainActivity.kt` or `MainFeatureScreen.kt`.
3. **Dead Parameter in `LobbyScreen.kt`**:
   - Line 153: `onNavigateToStudio: () -> Unit = {}` in `fun LobbyScreen(...)` signature. Not called or rendered anywhere in the body of `LobbyScreen.kt`.
4. **Custom Cards Remnant**:
   - `MainFeatureScreen.kt:110, 116, 173, 200, 395`: `customCards` are collected from `CustomCardRepository` and `LocalPlayerStore`, and passed into `LobbyScreen`.
   - `LobbyScreen.kt:164-183`: Combines `customCards` into `customDeck` ("My Custom Mzansi Cards") if `customCards.isNotEmpty()`. Since AI Studio is unlinked, no new custom cards are generated.
5. **Gradle Dependency**:
   - `app/build.gradle.kts:121`: `implementation(libs.firebase.ai)`
   - `gradle/libs.versions.toml:91`: `firebase-ai = { group = "com.google.firebase", name = "firebase-ai" }`
6. **Icons & Drawables**:
   - `app/src/main/res/drawable/ic_magic.xml`: Vector drawable for a magic/sparkle icon. Verified: 0 usages across the entire codebase.
7. **Visible UI on Active Tabs (Play, Store, Profile)**:
   - `LobbyScreen.kt`: Zero visible AI buttons, AI floating action buttons (FABs), AI banners, AI prompts, or coach hints.
   - `StoreScreen.kt`: Zero AI items, AI tokens, or AI features. Only Game packs, Coin bundles, All-Access Pass, Daily Gift, and Rewarded Ad.
   - `ProfileScreen.kt`: Zero AI items or telemetry. Only Career stats, Saved Payment Methods, ZAR Purchase Ledger, and Match History.

### 1.4 Branding Assets & App Visual Identity
1. **App Identity**:
   - `app/src/main/res/values/strings.xml`: `<string name="app_name">Lekker Charades</string>`
   - `app/src/main/AndroidManifest.xml`: `android:label="@string/app_name"`
2. **Branding Artwork & Logos** (`app/src/main/res/drawable-nodpi/`):
   - `logo_emblem.png`: Lekker Charades emblem icon (displayed in `LobbyScreen.kt:228` top bar and `AuthScreen.kt:140` hero).
   - `logo_full.png`: Full horizontal brand lockup.
   - `logo_wordmark.png`: Typography wordmark logo.
   - `splash_icon.png`: App splash screen asset.
   - `ic_launcher-playstore.png` (in root): 512x512 Store launcher icon.
3. **App Launcher Mipmaps** (`app/src/main/res/mipmap-*/`):
   - `ic_launcher.png`, `ic_launcher_round.png`, `ic_launcher_foreground.png`, `ic_launcher_monochrome.png`.
   - Configured in `mipmap-anydpi-v26/ic_launcher.xml` with `@drawable/ic_launcher_background` and foreground layers.
4. **Design System & Typography**:
   - `app/src/main/java/com/example/ui/theme/Type.kt`: Configures `HankenGroteskFontFamily` (300 to 700) and `InterFontFamily` with tabular numbers (`tnum`).
   - `app/src/main/java/com/example/ui/theme/Color.kt`: Stripi token palette (`StripiPrimary = #533AFD`, `StripiBrandDark900 = #1C1E54`, `StripiCanvas = #FFFFFF`, `StripiCanvasSoft = #F6F9FC`, `StripiCanvasCream = #F5E9D4`, etc.).
   - `app/src/main/java/com/example/ui/components/StripiComponents.kt`: Standardized design components.
5. **Unit Test Assertion Discrepancy**:
   - `app/src/test/java/com/example/ExampleRobolectricTest.kt:19`:
     ```kotlin
     val appName = context.getString(R.string.app_name)
     assertEquals("Mzansi Charades", appName)
     ```
     `strings.xml` defines `"Lekker Charades"`. This test fails if executed unless updated to `"Lekker Charades"`.

---

## 2. Logic Chain

1. **Observation 1.1 + 1.2**: In `MainFeatureScreen.kt`, the `NavigationBar` contains exactly 3 tabs (Play, Store, Profile), and the prior Coach and Studio tabs were removed in commit `cc871e5`.
   - **Inference**: The runtime UI already enforces the required 3-tab navigation structure. No additional tab removal is required in `NavigationBar`.
2. **Observation 1.3**: The source files `AiCoachScreen.kt`, `DeckStudioScreen.kt`, and `AiRepository.kt` remain present in the project but are completely detached from `MainFeatureScreen.kt` and `MainActivity.kt`.
   - **Inference**: While they do not appear in the runtime UI, they remain compiled artifacts and dead code. Removing or cleanly isolating them will guarantee zero inadvertent entry points and avoid unnecessary compilation overhead.
3. **Observation 1.3.3**: `LobbyScreen.kt` has an uninvoked `onNavigateToStudio: () -> Unit = {}` parameter.
   - **Inference**: This was a leftover callback from the former Studio tab. Removing it cleans the public API of `LobbyScreen`.
4. **Observation 1.3.7**: An exhaustive search across `LobbyScreen.kt`, `StoreScreen.kt`, `ProfileScreen.kt`, and `AuthScreen.kt` found zero AI banners, zero AI cards, zero floating buttons, and zero references to AI models.
   - **Inference**: The visible user-facing screens in the Play, Store, and Profile tabs are already free of visible AI features.
5. **Observation 1.4**: Branding assets (`logo_emblem.png`, `logo_full.png`, `logo_wordmark.png`, `splash_icon.png`, `strings.xml`) are already unified under the "Lekker Charades" identity and Stripi design system tokens.
   - **Inference**: The branding foundation is solid. The upcoming R2 Lobby redesign into the 2-column grid will complete the transformation of the Play tab.
6. **Observation 1.4.5**: `ExampleRobolectricTest.kt:19` asserts the obsolete string `"Mzansi Charades"`.
   - **Inference**: To ensure clean build verification (R5) and test execution, this assertion must be updated to `"Lekker Charades"`.

---

## 3. Caveats

- **Active Gameplay Landscape Rotation (R4)**: The sensor and landscape lifecycle logic was examined in `GamePlayScreen.kt:197-222`, but full validation and detailed testing of the orientation handling is scoped to Explorer 3.
- **Lobby Deck Grid Redesign (R2)**: The 2-column deck card layout specifications and visual mapping against the reference screenshot (`media_1794515064020_678066d7.jpg`) are scoped to Explorer 1.
- **Firebase AI Dependency**: In `app/build.gradle.kts`, `libs.firebase.ai` can be removed once `AiRepository.kt` is removed or decoupled; removing it before adjusting `AiRepository.kt` would break compilation.

---

## 4. Conclusion

1. **Navigation Structure**: The app's bottom bar correctly maintains the required 3-tab structure: **Play (0), Store (1), Profile (2)**. Zero extra tabs exist in the active UI.
2. **AI Feature Visibility**: There are **zero visible AI entry points** on the main screens (no AI coach button, no AI studio tab, no AI generation cards, no AI floating buttons, no AI banners).
3. **Dead Code & AI Remnants Identified for Cleanup**:
   - `app/src/main/java/com/example/ui/AiCoachScreen.kt` (Unused)
   - `app/src/main/java/com/example/ui/DeckStudioScreen.kt` (Unused)
   - `app/src/main/java/com/example/data/repository/AiRepository.kt` (Unused)
   - `LobbyScreen.kt:153`: `onNavigateToStudio` parameter (Unused)
   - `app/src/main/res/drawable/ic_magic.xml` (Unused)
4. **Branding**: App name is `"Lekker Charades"` in `strings.xml`. Logos and launcher icons are in place. Design tokens in `Color.kt` and `Type.kt` adhere to Stripi design system.
5. **Inventory of Files to Modify/Clean for R3**:

| File Path | Action | Description |
|---|---|---|
| `app/src/main/java/com/example/ui/LobbyScreen.kt` | Modify | Remove `onNavigateToStudio` parameter; prepare for R2 2-column deck grid. |
| `app/src/main/java/com/example/ui/MainFeatureScreen.kt` | Modify | Keep 3-tab structure intact; remove `customCards` / Studio plumbing if no longer needed. |
| `app/src/main/java/com/example/ui/AiCoachScreen.kt` | Delete / Exclude | Remove dead AI Coach screen. |
| `app/src/main/java/com/example/ui/DeckStudioScreen.kt` | Delete / Exclude | Remove dead AI Studio screen. |
| `app/src/main/java/com/example/data/repository/AiRepository.kt` | Delete / Exclude | Remove dead Gemini / Firebase AI repository. |
| `app/src/main/res/drawable/ic_magic.xml` | Delete (Optional) | Remove unused magic icon. |
| `app/build.gradle.kts` | Modify (Optional) | Remove `implementation(libs.firebase.ai)` after `AiRepository.kt` is deleted to shrink APK. |
| `app/src/test/java/com/example/ExampleRobolectricTest.kt` | Modify | Change expected string from `"Mzansi Charades"` to `"Lekker Charades"` at line 19. |

---

## 5. Verification Method

To independently verify these findings:

1. **Verify Navigation Bar Tabs**:
   - Inspect `app/src/main/java/com/example/ui/MainFeatureScreen.kt` lines 340–382.
   - Confirm only three `NavigationBarItem` items exist with test tags `nav_tab_play`, `nav_tab_store`, and `nav_tab_profile`.
2. **Verify Zero Visible AI Features on Active Tabs**:
   - Run grep across `LobbyScreen.kt`, `StoreScreen.kt`, and `ProfileScreen.kt` for `AiCoachScreen`, `DeckStudioScreen`, `AiRepository`, `gemini`, or `coach`:
     ```powershell
     Select-String -Path app\src\main\java\com\example\ui\LobbyScreen.kt,app\src\main\java\com\example\ui\StoreScreen.kt,app\src\main\java\com\example\ui\ProfileScreen.kt -Pattern "AiCoach|DeckStudio|AiRepository|gemini-3|gemini-4"
     ```
     (Returns zero matches).
3. **Verify Branding Strings & Assets**:
   - Check `strings.xml`:
     ```powershell
     Get-Content app\src\main\res\values\strings.xml
     ```
     (Shows `Lekker Charades`).
   - Check presence of brand logos:
     ```powershell
     Get-ChildItem app\src\main\res\drawable-nodpi
     ```
     (Shows `logo_emblem.png`, `logo_full.png`, `logo_wordmark.png`, `splash_icon.png`).
4. **Verify Outdated Test Assertion**:
   - Inspect `app/src/test/java/com/example/ExampleRobolectricTest.kt` line 19.
