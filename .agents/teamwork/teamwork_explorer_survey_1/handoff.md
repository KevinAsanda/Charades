# Handoff Report: R2 Main Screen Deck Grid Redesign Investigation

## 1. Observation

### 1.1 Reference Screenshot Analysis
**Source**: `C:/Users/kevin/.gemini/antigravity/brain/d5830d68-8241-4496-a4c4-ec65c258cf7b/.user_uploaded/media_1794515064020_678066d7.jpg`
- **Canvas / Background**: Rich royal blue / deep sapphire backdrop (`#142B68` / `#16357A`).
- **Top Header Bar**:
  - Compact height (~52.dp).
  - Background matches screen canvas (seamless royal blue).
  - Title text: Bold, clean, centered `"Decks"` in crisp white (`#FFFFFF`).
  - Subtle flanking icons (e.g., sound/settings or store).
- **Grid Layout**:
  - Full-bleed 2-column vertical grid (`columns = GridCells.Fixed(2)`).
  - Grid content padding: ~12.dp horizontal, ~10.dp vertical spacing between rows and columns.
- **Top-Right Anchor Card ("Unlock All Decks")**:
  - Positioned exactly at **Row 0, Column 1** (the top of the right column).
  - Distinctive bright golden-amber background (`#F59E0B` / `#EAB308`).
  - Artwork: Centered white padlock icon (`Icons.Default.Lock`, ~36.dp).
  - Title: Bold, centered `"Unlock All Decks"` in white (`#FFFFFF`).
  - Action: Dedicated one-tap CTA navigating to the store/All-Access unlock flow.
- **Deck Card Anatomy**:
  - **Container**: Card with rounded corners (~18.dp radius). Uniform height (~210.dp - 220.dp).
  - **Individual Color Pop**: Each deck has its own vibrant solid/gradient color (Warm Orange, Coral Red, Lime Green, Violet, Sky Blue, Pink, Teal, Mint, Emerald).
  - **Top-Right Corner Badge**: Small white tag / folded corner ribbon (~24.dp - 28.dp) in the upper-right corner.
  - **Centered Artwork / Icon**: Centered vector icon/illustration in a soft tinted circular container (~48.dp - 52.dp diameter, ~26.dp - 28.dp icon).
  - **Centered Title**: High-contrast white text (`StripiOnPrimary`), bold, ~16.sp - 18.sp, max 2 lines, centered alignment.
  - **Centered Description**: High-contrast white text with ~90% opacity, ~11.sp - 12.sp, 2-3 lines concise summary, centered alignment.

---

### 1.2 Existing `LobbyScreen.kt` Codebase State
**File Path**: `app/src/main/java/com/example/ui/LobbyScreen.kt` (Lines 1-731)
- **Current Layout Structure**:
  - Line 205-211: Single-column vertical `LazyColumn` with `Arrangement.spacedBy(20.dp)` on a pure white `StripiCanvas` background.
  - Line 213-394: Large `StripiGradientMeshHero` containing:
    - App emblem + "Lekker Charades" soft tag.
    - Currency / ZAR wallet pill + coin balance pill.
    - Large greeting text (`"Sanibonani, Mzansi"`).
    - Subtitle (`"Select a deck, choose your party mode, and start acting."`).
    - Bulky `StripiFeaturedDarkCard` with active deck details and a `"Start Match"` button (`testTag("start_match_button")`).
  - Line 397-500: Horizontal game mode carousel (`BLUEPRINT_GAME_MODES`: Act It Out, Hit the Beat, 30-Seconds, My Reaction, Party Teams, Heads-Up) + 30s/60s/90s duration selector pills.
  - Line 503-575: Horizontal segmented category chip filter ("All Decks", "Free Core", "Expansions", "Dezemba Groove").
  - Line 578-695: 1-column list of horizontal rectangular deck cards (`StripiFeatureCardLight`).
  - Line 698-730: `StripiCreamCard` for the "Unlock More Mzansi Decks" Spaza Store banner.
- **Deficiencies Against Requirement R2**:
  - No 2-column grid.
  - No compact "Decks" top bar.
  - No top-right "Unlock All Decks" card.
  - Cluttered with hero mesh, wallet pills, horizontal mode carousel, and filter chips on the main screen.
  - Card layout is wide and horizontal rather than vertical grid cards matching the reference image.
  - Contains unused AI callback parameter: line 153 `onNavigateToStudio: () -> Unit = {}`.

---

### 1.3 Stripi Design System Tokens
**File Paths**:
- `app/src/main/java/com/example/ui/theme/Color.kt`:
  - `StripiPrimary` = `Color(0xFF533AFD)` (Electric Indigo)
  - `StripiPrimaryDeep` = `Color(0xFF4434D4)`
  - `StripiPrimaryPress` = `Color(0xFF2E2B8C)`
  - `StripiSubduedHover` = `Color(0xFFB9B9F9)`
  - `StripiBrandDark900` = `Color(0xFF1C1E54)` (Deep Navy)
  - `StripiInk` = `Color(0xFF0D253D)`
  - `StripiOnPrimary` = `Color(0xFFFFFFFF)` (Crisp White)
  - `StripiCanvas` = `Color(0xFFFFFFFF)`
  - `StripiCanvasSoft` = `Color(0xFFF6F9FC)`
  - `StripiCanvasCream` = `Color(0xFFF5E9D4)`
  - `StripiHairline` = `Color(0xFFE3E8EE)`
  - `StripiRuby` = `Color(0xFFEA2261)`
  - `StripiMagenta` = `Color(0xFFF96BEE)`
- `app/src/main/java/com/example/ui/theme/Type.kt`:
  - `InterFontFamily` with weights 300 (Light), 400 (Normal), 500 (Medium), 600 (SemiBold), 700 (Bold).
  - `StripiTypeTokens.headingMd`: 20sp, weight 300/bold, line height 28sp.
  - `StripiTypeTokens.headingSm`: 18sp, weight 300/bold.
  - `StripiTypeTokens.bodyLg`: 16sp.
  - `StripiTypeTokens.bodyMd`: 15sp.
  - `StripiTypeTokens.caption`: 13sp.
  - `StripiTypeTokens.microCap`: 10sp, uppercase, letterSpacing = 0.1sp.
  - `StripiTabularStyle`: Tabular numeral (`tnum, ss01`).
- `app/src/main/java/com/example/ui/components/StripiComponents.kt`:
  - `StripiRounded.xl` = `RoundedCornerShape(16.dp)`, `StripiRounded.pill` = `RoundedCornerShape(percent = 50)`.
  - `deckIconForId(deckId: String): ImageVector`: Maps deck IDs to Material vector icons (`PhoneAndroid`, `Whatshot`, `SportsSoccer`, `MusicNote`, `Tv`, `Mic`, `Forum`, `Fastfood`, `DirectionsRun`, `School`, `DirectionsCar`, `Celebration`).
  - `StripiPrimaryPillButton`: Tactile scale, pill shape, `{colors.primary}` fill.

---

### 1.4 Deck Models and Inventory
**File Path**: `app/src/main/java/com/example/data/model/Deck.kt` & `StorePack.kt`
The application contains **13 pre-built decks** plus user custom card support:
1. `CORE_TRENDING_APPS` ("Trending Apps & Digital SA"): 12 cards, Free, Icon `PhoneAndroid`
2. `CORE_TIKTOK_LOCKDOWN` ("TikTok Trends & Lockdown Vault"): 18 cards, Free, Icon `Whatshot`
3. `CORE_SPORTS_LEGENDS` ("Bafana, UFC, Bokke & Proteas"): 12 cards, Free, Icon `SportsSoccer`
4. `CORE_KWAITO` ("Amapiano, 3-Step & Kwaito"): 12 cards, Free, Icon `MusicNote`
5. `CORE_2000S_TV` ("2000s TV & Soapie Dramas"): 12 cards, Free, Icon `Tv`
6. `CORE_CREATORS_PODCASTS` ("Content Creators & Podcasts"): 12 cards, Free, Icon `Mic`
7. `CORE_SLANG` ("Mzansi Slang & Street Culture"): 12 cards, Free, Icon `Forum`
8. `CORE_TUCKSHOP` ("Food & Tuckshop Classics"): 12 cards, Free, Icon `Fastfood`
9. `EXP_PANTSULA_LIFE` ("Pantsula Life & Heritage"): 10 cards, Expansion, Icon `DirectionsRun`
10. `EXP_SCHOOL_DAYS` ("Mzansi School Days"): 10 cards, Expansion, Icon `School`
11. `EXP_ROAD_TRIP` ("Road Trips & Taxi Ranks"): 10 cards, Expansion, Icon `DirectionsCar`
12. `EXP_MEMES` ("Meme Hall of Fame"): 10 cards, Expansion, Icon `Celebration`
13. `buildFestiveGrooveDeck()` ("Dezemba Festive Groove"): 15+ cards, Free, Icon `Celebration`
14. `customDeck` ("My Custom Mzansi Cards"): Optional custom user deck
15. `StoreCatalog.ALL_ACCESS_PASS` ("All-Access Mzansi Pass"): Unlocks all expansion packs (`R99.00`).

---

## 2. Logic Chain

1. **Mapping Requirement R2 to Architecture**:
   - The user requires a compact top header bar labeled `"Decks"` and a full-bleed 2-column grid.
   - Replacing the root `LazyColumn` with a `Column` containing `DecksHeaderBar` + `LazyVerticalGrid(columns = GridCells.Fixed(2))` directly fulfills the layout requirement.

2. **Mathematical Grid Positioning of the "Unlock All Decks" Card**:
   - In a 2-column `LazyVerticalGrid`:
     - Index 0 maps to Row 0, Col 0 (Top Left).
     - Index 1 maps to Row 0, Col 1 (Top Right).
     - Index 2 maps to Row 1, Col 0.
     - Index 3 maps to Row 1, Col 1.
   - By structuring the item list as `[FirstDeck, UnlockAllCard, Deck2, Deck3, ...]`:
     - `FirstDeck` occupies Top Left.
     - `UnlockAllCard` occupies Top Right (exactly matching the reference screenshot!).
     - All subsequent decks flow neatly down the two columns.

3. **Card Anatomy & Aesthetics**:
   - Setting a uniform card height of `210.dp` with `RoundedCornerShape(18.dp)` ensures strict horizontal alignment across both columns.
   - Using a distinct, curated color per deck (e.g., Orange, Red, Lime, Purple, Sky Blue, Teal, Amber) matches the playful, vibrant aesthetic of the screenshot.
   - Positioning a white badge in the top-right corner of each card provides instant visual feedback (`"FREE"`, `"OWNED"`, or a lock icon for unpurchased expansions).
   - Centering a circular icon badge (`size = 50.dp`, `icon = 26.dp`), followed by a bold centered title (16-17sp, max 2 lines), followed by a 2-3 line concise description (11.5sp, 90% white opacity) precisely replicates the reference card layout.

4. **Preserving Game Mode Selection & Smooth User Experience**:
   - In the existing screen, the game mode was selected via a bulky carousel before tapping "Start".
   - In the reference layout, the main screen is clean and dedicated to decks.
   - When an unlocked deck card is tapped, opening a compact `DeckQuickPlayDialog` (or bottom sheet) allows players to:
     - View the deck name, icon, and card count.
     - Choose their game mode (`Act It Out`, `Hit the Beat`, `30-Seconds`, `Heads-Up Tilt`, etc.).
     - Choose round duration (`30s`, `60s`, `90s`).
     - Tap a prominent `StripiPrimaryPillButton` labeled `"Start Playing"` (with `testTag("start_match_button")`).
   - If a locked expansion deck is tapped, it smoothly routes to `onNavigateToStore()`.
   - If the "Unlock All Decks" card is tapped, it calls `onNavigateToStore()` for instant pass purchase.

5. **Stripi Design System Consistency**:
   - The deep canvas background (`Color(0xFF142B68)`) creates intense contrast with white text and vibrant deck cards.
   - All typography uses `StripiTypeTokens` (Inter/Hanken with `ss01` and `tnum`).
   - Buttons use `StripiPrimaryPillButton` with tactile physics and 50% rounded pills.

6. **AI Stripping (R3 Compliance)**:
   - Remove `onNavigateToStudio: () -> Unit = {}` from `LobbyScreen.kt` parameter list.
   - Ensure zero AI buttons or references are rendered in the Play tab.

---

## 3. Caveats

1. **Screen Aspect Ratio & Density**:
   - On smaller devices (< 360dp width), 2-column cards must wrap text gracefully. Using `maxLines = 2` for title and `maxLines = 3` for description with `TextOverflow.Ellipsis` prevents any layout overflow.
2. **Navigation Bar Padding**:
   - The grid must include bottom content padding (`PaddingValues(bottom = 96.dp)`) so the lowest deck cards are fully scrollable above the bottom navigation bar.
3. **Owned State for "Unlock All Decks"**:
   - If the player has already unlocked all decks, the "Unlock All Decks" card should gracefully indicate `"All Decks Unlocked"` with an open lock or celebratory icon, while remaining in place to preserve the visual grid balance.

---

## 4. Conclusion & Technical Implementation Plan

### 4.1 Proposed Implementation Specification for `LobbyScreen.kt`

#### A. Data Models and Palette Helper
```kotlin
sealed interface LobbyGridItem {
  data class DeckItem(val deck: Deck, val isUnlocked: Boolean) : LobbyGridItem
  data class UnlockAllCard(val isAllUnlocked: Boolean) : LobbyGridItem
}

private fun colorForDeck(deckId: String): Color {
  return when (deckId) {
    "deck_trending_apps" -> Color(0xFFF97316)   // Vibrant Orange
    "deck_tiktok_lockdown" -> Color(0xFFF43F5E) // Hot Coral / Red
    "core_sports" -> Color(0xFF65A30D)          // Lime Green
    "core_kwaito" -> Color(0xFF8B5CF6)          // Vivid Purple
    "core_2000s_tv" -> Color(0xFF0284C7)        // Sky Blue
    "core_creators_podcasts" -> Color(0xFFD97706)// Warm Amber
    "core_slang" -> Color(0xFF0D9488)           // Teal / Emerald
    "core_tuckshop" -> Color(0xFFE11D48)        // Crimson / Watermelon
    "exp_pantsula" -> Color(0xFF0284C7)         // Electric Cyan
    "exp_school_days" -> Color(0xFF9333EA)      // Deep Violet
    "exp_road_trip" -> Color(0xFFCA8A04)        // Golden Mustard
    "exp_memes" -> Color(0xFF059669)            // Mint Green
    "deck_festive_groove" -> Color(0xFFDC2626)  // Festive Red
    else -> Color(0xFF4F46E5)                   // Indigo for Custom Decks
  }
}
```

#### B. Component Hierarchy
```
LobbyScreen (Root Column)
├── DecksHeaderBar
│   ├── Left: Optional Navigation / Back / Info Icon
│   ├── Center: Text("Decks", style = StripiTypeTokens.headingMd, color = White, fontWeight = Bold)
│   └── Right: Store / Sound Icon Button
├── LazyVerticalGrid (columns = Fixed(2), spacing = 10.dp, padding = 12.dp)
│   ├── Index 0: DeckGridCard (First Deck: e.g. Trending Apps)
│   ├── Index 1: UnlockAllDecksCard (Padlock icon + "Unlock All Decks" in Gold)
│   └── Index 2..N: DeckGridCard (Remaining decks: TikTok, Sports, Kwaito, 2000s TV, etc.)
└── DeckQuickPlayDialog (when selectedDeckForPlay != null)
    ├── Deck Header (Icon + Name + Card Count)
    ├── Mode Selector Pills (Act It Out, Hit the Beat, Heads-Up Tilt, 30-Seconds)
    ├── Duration Selector Pills (30s, 60s, 90s)
    └── StripiPrimaryPillButton ("Start Playing", testTag = "start_match_button")
```

#### C. Exact Signature for `LobbyScreen.kt`
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
*(Removed `onNavigateToStudio` completely).*

#### D. Deck Card Component Blueprint (`DeckGridCard`)
```kotlin
@Composable
fun DeckGridCard(
  deck: Deck,
  isUnlocked: Boolean,
  backgroundColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(215.dp)
      .clip(RoundedCornerShape(18.dp))
      .background(backgroundColor)
      .clickable { onClick() }
  ) {
    // Top-Right Corner Badge
    Surface(
      shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 18.dp),
      color = Color.White.copy(alpha = 0.95f),
      modifier = Modifier.align(Alignment.TopEnd)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (!isUnlocked) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked",
            tint = backgroundColor,
            modifier = Modifier.size(11.dp)
          )
        } else {
          Text(
            text = if (deck.isFreeTier) "FREE" else "OWNED",
            style = StripiTypeTokens.microCap.copy(
              color = backgroundColor,
              fontWeight = FontWeight.Bold
            )
          )
        }
      }
    }

    // Centered Content Stack
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp, vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Centered Artwork / Icon
      Box(
        modifier = Modifier
          .size(50.dp)
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.22f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = deckIconForId(deck.id),
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(26.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Centered Title
      Text(
        text = deck.name,
        style = StripiTypeTokens.bodyLg.copy(
          color = StripiOnPrimary,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        ),
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Centered Concise Description
      Text(
        text = deck.tagLine,
        style = StripiTypeTokens.caption.copy(
          color = StripiOnPrimary.copy(alpha = 0.90f),
          fontSize = 11.sp,
          lineHeight = 14.5.sp
        ),
        textAlign = TextAlign.Center,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}
```

#### E. "Unlock All Decks" Card Blueprint (`UnlockAllDecksCard`)
```kotlin
@Composable
fun UnlockAllDecksCard(
  isAllUnlocked: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val goldColor = Color(0xFFF59E0B)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(215.dp)
      .clip(RoundedCornerShape(18.dp))
      .background(goldColor)
      .clickable { onClick() }
  ) {
    // Corner Badge
    Surface(
      shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 18.dp),
      color = Color.White.copy(alpha = 0.95f),
      modifier = Modifier.align(Alignment.TopEnd)
    ) {
      Text(
        text = if (isAllUnlocked) "ACTIVE" else "PASS",
        style = StripiTypeTokens.microCap.copy(
          color = goldColor,
          fontWeight = FontWeight.Bold
        ),
        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
      )
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp, vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(50.dp)
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isAllUnlocked) Icons.Default.Check else Icons.Default.Lock,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(28.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = if (isAllUnlocked) "All Decks Unlocked" else "Unlock All Decks",
        style = StripiTypeTokens.bodyLg.copy(
          color = StripiOnPrimary,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        ),
        textAlign = TextAlign.Center,
        maxLines = 2
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = if (isAllUnlocked)
          "You have unlimited access to every current & future expansion pack!"
        else
          "Instant access to all expansion packs, bonus coins & upcoming decks!",
        style = StripiTypeTokens.caption.copy(
          color = StripiOnPrimary.copy(alpha = 0.90f),
          fontSize = 11.sp,
          lineHeight = 14.5.sp
        ),
        textAlign = TextAlign.Center,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}
```

---

## 5. Verification Method

1. **Layout & Visual Verification**:
   - Inspect the compiled UI in the Android emulator / device:
     - Verify top header bar displays `"Decks"` in compact height without unnecessary hero banners.
     - Verify 2-column vertical grid is displayed.
     - Verify Top Left item is Deck 0, and Top Right item is the golden `"Unlock All Decks"` card.
     - Verify each deck card displays its corner badge, centered artwork icon, centered title, and 2-3 line description.
     - Verify clicking an unlocked deck card displays the quick play dialog allowing game mode and timer selection before launching into gameplay.
     - Verify clicking `"Unlock All Decks"` navigates to Store tab.
2. **AI Removal Verification**:
   - Verify no AI Coach, AI Studio, or generator icons/buttons exist anywhere on `LobbyScreen`.
   - Verify `onNavigateToStudio` is removed from `LobbyScreen.kt` and `MainFeatureScreen.kt`.
3. **Build & Test Verification**:
   - Run the Gradle build command:
     ```powershell
     .\gradlew assembleDebug
     ```
     Ensure the build succeeds with exit code 0.
   - Run unit tests:
     ```powershell
     .\gradlew testDebugUnitTest
     ```
     Ensure all repository and unit tests pass without regression.
