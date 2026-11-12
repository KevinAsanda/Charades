# Handoff Report: UI and Navigation Cleanliness Investigation (Milestone 1)

## 1. Observation

### Observation 1: `MainFeatureScreen.kt` NavigationBar Configuration
- **File**: `app/src/main/java/com/example/ui/MainFeatureScreen.kt`
- **Lines**: 336–383
- **Verbatim Code**:
```kotlin
NavigationBar(
  containerColor = StripiCanvas,
  contentColor = StripiInk
) {
  NavigationBarItem(
    selected = currentTab == 0,
    onClick = { currentTab = 0 },
    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Play") },
    label = { Text("Play", style = StripiTypeTokens.micro) },
    colors = NavigationBarItemDefaults.colors(
      selectedIconColor = StripiPrimary,
      selectedTextColor = StripiPrimary,
      unselectedIconColor = StripiInkMute2,
      unselectedTextColor = StripiInkMute2,
      indicatorColor = StripiSubduedHover.copy(alpha = 0.45f)
    ),
    modifier = Modifier.testTag("nav_tab_play")
  )
  NavigationBarItem(
    selected = currentTab == 1,
    onClick = { currentTab = 1 },
    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Store") },
    label = { Text("Store", style = StripiTypeTokens.micro) },
    colors = NavigationBarItemDefaults.colors(
      selectedIconColor = StripiPrimary,
      selectedTextColor = StripiPrimary,
      unselectedIconColor = StripiInkMute2,
      unselectedTextColor = StripiInkMute2,
      indicatorColor = StripiSubduedHover.copy(alpha = 0.45f)
    ),
    modifier = Modifier.testTag("nav_tab_store")
  )
  NavigationBarItem(
    selected = currentTab == 2,
    onClick = { currentTab = 2 },
    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
    label = { Text("Profile", style = StripiTypeTokens.micro) },
    colors = NavigationBarItemDefaults.colors(
      selectedIconColor = StripiPrimary,
      selectedTextColor = StripiPrimary,
      unselectedIconColor = StripiInkMute2,
      unselectedTextColor = StripiInkMute2,
      indicatorColor = StripiSubduedHover.copy(alpha = 0.45f)
    ),
    modifier = Modifier.testTag("nav_tab_profile")
  )
}
```
- **Tab State Handling** (Lines 220–221, 392–492):
  - `var currentTab by remember { mutableIntStateOf(0) }`
  - `when (currentTab)` evaluates strictly to `0 -> LobbyScreen(...)`, `1 -> StoreScreen(...)`, and `2 -> ProfileScreen(...)`.
  - There are zero additional tabs, no Studio tab, and no AI Coach tab.

### Observation 2: `LobbyScreen.kt` Call Signature and Parameter Plumbing in `MainFeatureScreen.kt`
- **File**: `app/src/main/java/com/example/ui/MainFeatureScreen.kt`
- **Lines**: 393–406
- **Verbatim Code**:
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
- `MainFeatureScreen.kt` does not supply `onNavigateToStudio` or any AI-related parameters to `LobbyScreen`.

### Observation 3: Dead `onNavigateToStudio` Parameter in `LobbyScreen.kt`
- **File**: `app/src/main/java/com/example/ui/LobbyScreen.kt`
- **Lines**: 148–155
- **Verbatim Code**:
```kotlin
@Composable
fun LobbyScreen(
  userProfile: UserProfile?,
  customCards: List<CustomCard>,
  onStartGame: (Deck, String, Long) -> Unit,
  onNavigateToStore: () -> Unit,
  onNavigateToStudio: () -> Unit = {},
  onOpenPaymentSetup: () -> Unit = {}
)
```
- **Occurrence Count across `LobbyScreen.kt`**:
  Search for `onNavigateToStudio` within `app/src/main/java/com/example/ui/LobbyScreen.kt` yielded exactly 1 hit: Line 153.
  It is NEVER invoked, referenced, or passed down to any child composables or UI handlers in `LobbyScreen.kt`.

### Observation 4: Absence of AI UI Elements and Icons in `LobbyScreen.kt` and `MainFeatureScreen.kt`
- **Search command**: Case-insensitive regex `studio|coach|ai|gemini|generat` across `MainFeatureScreen.kt` and `LobbyScreen.kt`.
- **Result in `MainFeatureScreen.kt`**: Zero AI occurrences (matches were substrings in comments or standard words like `Hairline`, `email`, `claimable`).
- **Result in `LobbyScreen.kt`**: Only Line 153 (`onNavigateToStudio`). All other UI components are:
  - Atmospheric Hero with `R.drawable.logo_emblem`, "Lekker Charades" pill, ZAR billing pill (`Icons.Default.CreditCard`), Store Coins pill (`Icons.Default.MonetizationOn`).
  - Featured Deck Card with `StripiDeckIconBadge`, "Ready"/"Locked" indicator, Play/Store button.
  - Game Mode Carousel: 6 manual human party modes ("Act It Out", "Hit the Beat", "30-Seconds", "My Reaction", "Party Teams", "Heads-Up").
  - Category Pills: "All Decks", "Free Core", "Expansions", "Dezemba Groove" (`Icons.Default.Celebration`).
  - Deck cards list with lock/ownership badges.
  - Spaza Store promo card with `onNavigateToStore` button.
- **Unreferenced AI Asset**:
  - `app/src/main/res/drawable/ic_magic.xml` exists on disk.
  - Cross-repo grep confirms zero references in Kotlin or XML code.

---

## 2. Logic Chain

1. **Premise 1**: The user requirements (R2, R3) and Milestone 1 scope specify that navigation must be restricted strictly to Play, Store, and Profile, with all visible AI features and dead AI plumbing removed.
2. **Premise 2**: Direct inspection of `MainFeatureScreen.kt` (lines 336–383) confirms that the bottom `NavigationBar` defines exactly 3 `NavigationBarItem`s:
   - Index 0: Play (`testTag("nav_tab_play")`)
   - Index 1: Store (`nav_tab_store")`)
   - Index 2: Profile (`testTag("nav_tab_profile")`)
   Navigation switching (lines 392–492) handles strictly tabs 0, 1, and 2. Thus, the navigation bar already strictly complies with the 3-tab requirement.
3. **Premise 3**: Inspection of `MainFeatureScreen.kt` call site to `LobbyScreen` (lines 393–406) confirms that `MainFeatureScreen.kt` does not pass `onNavigateToStudio`, nor does it maintain any state or imports related to AI Studio or AI Coach.
4. **Premise 4**: Inspection of `LobbyScreen.kt` (line 153) reveals a leftover unused parameter: `onNavigateToStudio: () -> Unit = {}`. Because it has an empty default lambda and is never invoked anywhere inside `LobbyScreen.kt`, removing this parameter will have zero runtime impact and will eliminate dead AI plumbing.
5. **Premise 5**: Because `MainFeatureScreen.kt` is the sole caller of `LobbyScreen` and does not pass `onNavigateToStudio`, removing `onNavigateToStudio` from `LobbyScreen.kt` requires no changes in `MainFeatureScreen.kt` to maintain compilation.
6. **Premise 6**: Neither `MainFeatureScreen.kt` nor `LobbyScreen.kt` contain any AI-related buttons, icons, or navigation routes. The unused drawable `ic_magic.xml` is unreferenced and can be safely deleted in M1.

---

## 3. Caveats

- **Milestone 2 Redesign of LobbyScreen**:
  `LobbyScreen.kt` currently implements a vertical list with a featured hero card rather than the 2-column grid requested in R2. Milestone 2 is tasked with redesigning the interior body of `LobbyScreen.kt` into the 2-column grid. The focus of Milestone 1 is strictly cleaning up the parameter signature (`onNavigateToStudio`) and verifying UI cleanliness.
- **Local Gradle Wrapper Absence**:
  `gradlew` and `gradlew.bat` are not currently in the project root directory; restoring them is designated for Milestone 4 (Worker M4). Consequently, verification for Milestone 1 relies on AST/code analysis, file search, and regex verification.

---

## 4. Conclusion

1. **Navigation Restriction**: Verified. `MainFeatureScreen.kt` `NavigationBar` strictly contains 3 tabs: Play (`nav_tab_play`), Store (`nav_tab_store`), Profile (`nav_tab_profile`).
2. **AI Plumbing Cleanup**: In `LobbyScreen.kt`, line 153 contains the sole remaining dead AI parameter:
   `onNavigateToStudio: () -> Unit = {},`.
   This parameter must be removed by Worker M1.
3. **Caller Alignment**: `MainFeatureScreen.kt` does not pass `onNavigateToStudio`, so removing `onNavigateToStudio` from `LobbyScreen.kt` will not break `MainFeatureScreen.kt`.
4. **UI Cleanliness**: Zero AI icons, buttons, banners, or routes exist in `MainFeatureScreen.kt` or `LobbyScreen.kt`.

### Proposed Code Modification for Worker M1

**Target File**: `app/src/main/java/com/example/ui/LobbyScreen.kt` (Lines 148–156)

```kotlin
// BEFORE:
@Composable
fun LobbyScreen(
  userProfile: UserProfile?,
  customCards: List<CustomCard>,
  onStartGame: (Deck, String, Long) -> Unit,
  onNavigateToStore: () -> Unit,
  onNavigateToStudio: () -> Unit = {},
  onOpenPaymentSetup: () -> Unit = {}
)

// AFTER:
@Composable
fun LobbyScreen(
  userProfile: UserProfile?,
  customCards: List<CustomCard> = emptyList(),
  onStartGame: (Deck, String, Long) -> Unit,
  onNavigateToStore: () -> Unit,
  onOpenPaymentSetup: () -> Unit = {}
)
```

---

## 5. Verification Method

To independently verify the observations and conclusions:

1. **Verify NavigationBar 3-tab layout**:
   Inspect `app/src/main/java/com/example/ui/MainFeatureScreen.kt` lines 336–383:
   - Check that exactly 3 `NavigationBarItem` elements exist.
   - Confirm test tags: `nav_tab_play`, `nav_tab_store`, `nav_tab_profile`.

2. **Verify `onNavigateToStudio` single occurrence**:
   Execute PowerShell in project root:
   ```powershell
   Select-String -Path "app\src\main\java\com\example\ui\LobbyScreen.kt" -Pattern "onNavigateToStudio"
   ```
   Expect exactly 1 result at line 153.

3. **Verify zero AI references in `MainFeatureScreen.kt` and `LobbyScreen.kt`**:
   Execute PowerShell in project root:
   ```powershell
   Get-ChildItem -Path "app\src\main\java\com\example\ui\MainFeatureScreen.kt", "app\src\main\java\com\example\ui\LobbyScreen.kt" | Select-String -Pattern "studio|coach|AiRepository|ic_magic" -CaseSensitive:$false
   ```
   Confirm that after removing line 153 in `LobbyScreen.kt`, 0 results are returned.

4. **Verify unreferenced `ic_magic.xml`**:
   Execute PowerShell in project root:
   ```powershell
   Get-ChildItem -Path "app\src" -Recurse -File | Select-String -Pattern "ic_magic"
   ```
   Confirm 0 references in code.
