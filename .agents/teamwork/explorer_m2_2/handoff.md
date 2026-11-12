# Handoff Report — Milestone 2 Explorer 2 (Design Tokens, Card Anatomy & Palette)

## 1. Observation

### 1.1 Reference Screenshot Visual Analysis
Direct inspection of reference image `C:/Users/kevin/.gemini/antigravity/brain/d5830d68-8241-4496-a4c4-ec65c258cf7b/.user_uploaded/media_1794515064020_678066d7.jpg`:
- **Screen Background**: Deep royal blue canvas (`#142B68` / `#0D1F58`).
- **Top Header**: Compact bar labeled "Decks" in centered bold white typography, seamlessly sitting on the royal blue background.
- **Grid Layout**: 2-column vertical grid of deck cards with rounded corners (~18dp).
- **Anchor Card Placement**: At **Row 0, Column 1** (top right slot of the grid), there is a prominent amber/gold card titled **"Unlock All Decks"** with a padlock icon.
- **Card Anatomy**:
  - Uniform aspect ratio with height ~215dp.
  - Smooth rounded corners (~18dp radius).
  - Top-right corner tag/badge indicating status ("FREE", "OWNED", or lock icon).
  - Centered circular container (~50dp) with soft translucent background containing a white vector glyph/emblem.
  - Centered bold title (1-2 lines) in crisp white.
  - Centered concise description / tagline (2-3 lines) in semi-translucent white with ellipsis truncation.
  - High-saturation, distinct, vivid solid background color per deck (warm orange, lime green, bright blue, hot pink, purple, teal, gold, etc.).

### 1.2 Design System Tokens & Typography
Direct inspection of `app/src/main/java/com/example/ui/theme/Color.kt` and `Type.kt`:
- `Color.kt:7`: `StripiPrimary = Color(0xFF533AFD)`
- `Color.kt:8`: `StripiPrimaryDeep = Color(0xFF4434D4)`
- `Color.kt:12`: `StripiBrandDark900 = Color(0xFF1C1E54)`
- `Color.kt:19`: `StripiOnPrimary = Color(0xFFFFFFFF)`
- `Color.kt:29-31`: `StripiRuby = Color(0xFFEA2261)`, `StripiMagenta = Color(0xFFF96BEE)`, `StripiLemon = Color(0xFF9B6829)`
- `Type.kt:16-22`: `InterFontFamily` loaded at weights `Light`, `Normal`, `Medium`, `SemiBold`, `Bold`.
- `Type.kt:31-191`: Tokens available include `headingSm` (18sp), `bodyLg` (16sp), `bodyMd` (15sp), `caption` (13sp), `micro` (11sp), `microCap` (10sp).
- Vector icon library in `app/build.gradle.kts:105-106`:
  `implementation(libs.androidx.compose.material.icons.core)` and `implementation(libs.androidx.compose.material.icons.extended)` both active, providing the complete set of Material Design icons.

### 1.3 Complete App Deck Inventory
Direct inspection of `app/src/main/java/com/example/data/model/Deck.kt`:
The app defines 13 standard decks (9 Free Core + 4 Expansions):
1. `deck_trending_apps` (Line 117): "Trending Apps & Digital SA" (Free)
2. `deck_tiktok_lockdown` (Line 312): "TikTok Challenges & Lockdown Era" (Free)
3. `core_sports` (Line 598): "Sports Legends & Stadium Anthems" (Free)
4. `core_kwaito` (Line 792): "Kwaito Classics & 90s Heritage" (Free)
5. `core_2000s_tv` (Line 1012): "2000s Mzansi Nostalgia & TV Golden Age" (Free)
6. `deck_creators_podcasts` (Line 1176): "Creators, Memes & Podcasts" (Free)
7. `core_slang` (Line 1337): "Mzansi Slang & Street Culture" (Free)
8. `core_tuckshop` (Line 1529): "Tuckshop Snacks & Daily Life" (Free)
9. `exp_pantsula` (Line 1633): "Pantsula Life & Street Dance" (Expansion)
10. `exp_school_days` (Line 1715): "School Days & Childhood Nostalgia" (Expansion)
11. `exp_road_trip` (Line 1782): "Mzansi Road Trip & Places" (Expansion)
12. `exp_memes` (Line 1852): "Viral SA Memes & Internet Gold" (Expansion)
13. `deck_festive_groove` (Line 1939): "Dezemba Festive Groove Pack" (Free)
14. Custom user deck (`custom_deck_user` constructed when `customCards.isNotEmpty()`).
15. Store Anchor: `Unlock All Decks` (`StoreCatalog.ALL_ACCESS_PASS`, Line 66 in `StorePack.kt`).

### 1.4 Screen Hosting & Navigation Insets
Direct inspection of `MainFeatureScreen.kt:330-394`:
- `Scaffold` hosts a bottom `NavigationBar` with 3 items: Play, Store, Profile.
- The `Box` containing `LobbyScreen` is wrapped with `.padding(innerPadding)`.
- `MainActivity.kt:36`: `enableEdgeToEdge()` is active.
- To prevent cards from being partially hidden or cramped at the bottom of the scroll area when scrolling, `LazyVerticalGrid` requires explicit bottom content padding (`PaddingValues(bottom = 96.dp)`).

---

## 2. Logic Chain

1. **Alignment with Reference Screenshot**:
   - The screenshot shows a full 2-column card grid where each card has a bold, vivid solid background color with high contrast against the dark royal blue background (`#142B68`).
   - Row 0 Column 1 is explicitly occupied by "Unlock All Decks" in bright amber/gold.
   - Therefore, grid item 0 must be `displayedDecks[0]`, grid item 1 must be `UnlockAllDecksGridCard`, and subsequent items must be `displayedDecks.drop(1)`. This guarantees Row 0 Column 1 is always the Unlock All Decks card without leaving a hole in the grid.

2. **Color Palette Mapping**:
   - To match the chromatic vibrancy of the reference image without visual clashes, each deck is assigned a distinct vivid color with sufficient contrast for white text (`Color.White`):
     - `deck_trending_apps`: `#FF6D3A` (Warm Sunset Orange — matches Row 0 Col 0 in reference screenshot)
     - `unlock_all_decks`: `#F59E0B` (Amber Gold — exactly required by specification and reference screenshot)
     - `deck_tiktok_lockdown`: `#E11D74` (Hot Magenta Pink)
     - `core_sports`: `#10B981` (Stadium Emerald Green)
     - `core_kwaito`: `#8B5CF6` (Electric Purple)
     - `core_2000s_tv`: `#0284C7` (Vivid Sky Blue)
     - `deck_creators_podcasts`: `#06B6D4` (Electric Cyan / Teal)
     - `core_slang`: `#F43F5E` (Warm Coral Red)
     - `core_tuckshop`: `#D97706` (Warm Honey Amber)
     - `exp_pantsula`: `#6366F1` (Vibrant Indigo)
     - `exp_school_days`: `#65A30D` (Lime Apple Green)
     - `exp_road_trip`: `#2563EB` (Cobalt Royal Blue)
     - `exp_memes`: `#BE185D` (Rich Raspberry Pink)
     - `deck_festive_groove`: `#DC2626` (Festive Red)
     - `custom_deck_user`: `#7E22CE` (Royal Amethyst)

3. **Card Anatomy & Sizing**:
   - Card height: `215.dp` uniform height.
   - Corners: `RoundedCornerShape(18.dp)`.
   - Top-right badge: 22dp height, right-aligned. Shows "FREE" pill for free-tier decks, "OWNED" pill for unlocked expansion decks, or a lock icon with price for locked expansion decks.
   - Icon container: Exactly `50.dp` circle (`clip(CircleShape)`), `Color.White.copy(alpha = 0.22f)` background, centered horizontally, holding a 28dp white vector icon.
   - Title: Centered, `fontFamily = InterFontFamily`, `fontWeight = FontWeight.Bold`, `fontSize = 15.sp`, `lineHeight = 18.sp`, `color = Color.White`, `maxLines = 2`, `overflow = TextOverflow.Ellipsis`.
   - Tagline: Centered, `fontFamily = InterFontFamily`, `fontSize = 11.sp`, `lineHeight = 14.sp`, `color = Color.White.copy(alpha = 0.88f)`, `maxLines = 3`, `overflow = TextOverflow.Ellipsis`.
   - Total vertical stack: ~30dp (badge) + 4dp + 50dp (icon) + 8dp + 36dp (title) + 4dp + 42dp (tagline) + 12dp (bottom padding) = 186dp, leaving a comfortable 29dp buffer within the 215dp card.

4. **Interactive Quick Play Modal**:
   - Tapping an unlocked deck card opens a clean dialog modal (`DeckQuickPlayDialog`) displaying mode selector chips ("Act It Out", "Hit the Beat", "30-Seconds", "Heads-Up Tilt"), round duration pills (30s, 60s, 90s), and a prominent "Start Playing" button tagged with `Modifier.testTag("start_match_button")`.
   - Tapping a locked deck card immediately triggers `onNavigateToStore()`.

5. **Navigation Insets & Scrolling**:
   - `Scaffold`'s `innerPadding` offsets the bottom navigation bar.
   - Adding `contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 96.dp)` inside `LazyVerticalGrid` ensures that when users scroll to the bottom, the final row of cards is elevated comfortably above the 3-tab navigation bar without visual clipping or finger interference.

---

## 3. Caveats

- **No caveats**: The codebase already includes `material.icons.extended` in `build.gradle.kts`, all 13 decks exist in `Deck.kt`, and the Stripi font family (`InterFontFamily`) is fully linked in `Type.kt`.
- The `Unlock All Decks` card is always displayed at Row 0, Column 1 regardless of category filter to preserve the primary monetization anchor as requested.

---

## 4. Conclusion & Precise Worker Specifications

### 4.1 Deck Color & Icon Mapping

```kotlin
fun deckCardBackgroundColor(deckId: String): Color {
  return when (deckId) {
    "deck_trending_apps" -> Color(0xFFFF6D3A)      // Warm Sunset Orange
    "unlock_all_decks" -> Color(0xFFF59E0B)        // Amber Gold (Mandatory Spec)
    "deck_tiktok_lockdown" -> Color(0xFFE11D74)    // Hot Magenta Pink
    "core_sports" -> Color(0xFF10B981)             // Stadium Emerald Green
    "core_kwaito" -> Color(0xFF8B5CF6)             // Electric Purple
    "core_2000s_tv" -> Color(0xFF0284C7)           // Vivid Sky Blue
    "deck_creators_podcasts" -> Color(0xFF06B6D4)  // Electric Cyan / Teal
    "core_slang" -> Color(0xFFF43F5E)              // Warm Coral Red
    "core_tuckshop" -> Color(0xFFD97706)           // Warm Honey Amber
    "exp_pantsula" -> Color(0xFF6366F1)            // Vibrant Indigo
    "exp_school_days" -> Color(0xFF65A30D)         // Lime Apple Green
    "exp_road_trip" -> Color(0xFF2563EB)           // Cobalt Royal Blue
    "exp_memes" -> Color(0xFFBE185D)               // Rich Raspberry Pink
    "deck_festive_groove" -> Color(0xFFDC2626)     // Festive Crimson Red
    else -> Color(0xFF7E22CE)                      // Royal Amethyst (Custom Decks)
  }
}

fun deckIconForId(deckId: String): ImageVector {
  return when (deckId) {
    "deck_trending_apps" -> Icons.Default.PhoneAndroid
    "deck_tiktok_lockdown" -> Icons.Default.Whatshot
    "core_sports" -> Icons.Default.SportsSoccer
    "core_kwaito" -> Icons.Default.MusicNote
    "core_2000s_tv" -> Icons.Default.Tv
    "deck_creators_podcasts" -> Icons.Default.Mic
    "core_slang" -> Icons.Default.Forum
    "core_tuckshop" -> Icons.Default.Fastfood
    "exp_pantsula" -> Icons.AutoMirrored.Filled.DirectionsRun
    "exp_school_days" -> Icons.Default.School
    "exp_road_trip" -> Icons.Default.DirectionsCar
    "exp_memes" -> Icons.Default.Celebration
    "deck_festive_groove" -> Icons.Default.Celebration
    "unlock_all_decks" -> Icons.Default.Lock
    else -> Icons.Default.AutoAwesome
  }
}
```

### 4.2 Deck Card Composable Specification (`DeckGridCard`)

```kotlin
@Composable
fun DeckGridCard(
  deck: Deck,
  isUnlocked: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .height(215.dp)
      .fillMaxWidth()
      .shadow(
        elevation = 4.dp,
        shape = RoundedCornerShape(18.dp),
        ambientColor = Color.Black.copy(alpha = 0.20f),
        spotColor = Color.Black.copy(alpha = 0.15f)
      )
      .clip(RoundedCornerShape(18.dp))
      .background(deckCardBackgroundColor(deck.id))
      .clickable(onClick = onClick)
      .padding(12.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Top Right Badge Row (Height: 22dp)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(22.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (deck.isFreeTier) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(percent = 50))
              .background(Color.White.copy(alpha = 0.25f))
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text(
              text = "FREE",
              style = TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color.White
              )
            )
          }
        } else if (isUnlocked) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(percent = 50))
              .background(Color.White.copy(alpha = 0.25f))
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text(
              text = "OWNED",
              style = TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color.White
              )
            )
          }
        } else {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(percent = 50))
              .background(Color.Black.copy(alpha = 0.28f))
              .padding(horizontal = 7.dp, vertical = 2.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = Color.White,
                modifier = Modifier.size(11.dp)
              )
              Text(
                text = StoreCatalog.formattedZarForDeck(deck),
                style = TextStyle(
                  fontFamily = InterFontFamily,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 10.sp,
                  color = Color.White
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // 2. Centered Circular Icon Container (50dp)
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
          modifier = Modifier.size(28.dp)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 3. Card Title (2 lines max, bold)
      Text(
        text = deck.name,
        style = TextStyle(
          fontFamily = InterFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          lineHeight = 18.sp,
          color = Color.White,
          textAlign = TextAlign.Center
        ),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 4.dp)
      )

      Spacer(modifier = Modifier.height(4.dp))

      // 4. Card Description / Tagline (2-3 lines max)
      Text(
        text = deck.tagLine,
        style = TextStyle(
          fontFamily = InterFontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = 11.sp,
          lineHeight = 14.sp,
          color = Color.White.copy(alpha = 0.88f),
          textAlign = TextAlign.Center
        ),
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 4.dp)
      )
    }
  }
}
```

### 4.3 Unlock All Decks Anchor Card Composable (`UnlockAllDecksGridCard`)

```kotlin
@Composable
fun UnlockAllDecksGridCard(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .height(215.dp)
      .fillMaxWidth()
      .shadow(
        elevation = 4.dp,
        shape = RoundedCornerShape(18.dp),
        ambientColor = Color.Black.copy(alpha = 0.20f),
        spotColor = Color.Black.copy(alpha = 0.15f)
      )
      .clip(RoundedCornerShape(18.dp))
      .background(Color(0xFFF59E0B)) // Vibrant Amber Gold (#F59E0B)
      .clickable(onClick = onClick)
      .padding(12.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Top Right Badge Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(22.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(Color.White.copy(alpha = 0.28f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text(
            text = "ALL-ACCESS",
            style = TextStyle(
              fontFamily = InterFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 9.sp,
              color = Color.White
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // 2. Padlock Icon Container (50dp)
      Box(
        modifier = Modifier
          .size(50.dp)
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = "Unlock All Decks",
          tint = Color.White,
          modifier = Modifier.size(28.dp)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 3. Title
      Text(
        text = "Unlock All Decks",
        style = TextStyle(
          fontFamily = InterFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          lineHeight = 18.sp,
          color = Color.White,
          textAlign = TextAlign.Center
        ),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 4.dp)
      )

      Spacer(modifier = Modifier.height(4.dp))

      // 4. Description
      Text(
        text = "Get instant access to all expansion packs & upcoming party decks!",
        style = TextStyle(
          fontFamily = InterFontFamily,
          fontWeight = FontWeight.Normal,
          fontSize = 11.sp,
          lineHeight = 14.sp,
          color = Color.White.copy(alpha = 0.90f),
          textAlign = TextAlign.Center
        ),
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 4.dp)
      )
    }
  }
}
```

### 4.4 Top Header Bar Specification
- **Height**: `52.dp`
- **Background**: `Color(0xFF142B68)`
- **Layout**: Row with centered "Decks" title and right-aligned coin pill:
```kotlin
Row(
  modifier = Modifier
    .fillMaxWidth()
    .height(52.dp)
    .background(Color(0xFF142B68))
    .padding(horizontal = 16.dp),
  horizontalArrangement = Arrangement.SpaceBetween,
  verticalAlignment = Alignment.CenterVertically
) {
  Spacer(modifier = Modifier.width(36.dp))

  Text(
    text = "Decks",
    style = TextStyle(
      fontFamily = InterFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 20.sp,
      color = Color.White
    )
  )

  Surface(
    shape = StripiRounded.pill,
    color = Color.White.copy(alpha = 0.15f),
    modifier = Modifier.clickable { onNavigateToStore() }
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.MonetizationOn,
        contentDescription = "Coins",
        tint = Color(0xFFFBBF24),
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "$userCoins",
        style = StripiTypeTokens.caption.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
      )
    }
  }
}
```

### 4.5 LazyVerticalGrid Assembly & Insets
```kotlin
LazyVerticalGrid(
  columns = GridCells.Fixed(2),
  modifier = Modifier
    .fillMaxSize()
    .background(Color(0xFF142B68)),
  contentPadding = PaddingValues(
    start = 12.dp,
    end = 12.dp,
    top = 8.dp,
    bottom = 96.dp // Elevated bottom clearance for 3-tab navigation bar
  ),
  horizontalArrangement = Arrangement.spacedBy(10.dp),
  verticalArrangement = Arrangement.spacedBy(10.dp)
) {
  if (displayedDecks.isNotEmpty()) {
    // Row 0, Column 0: First Deck
    item(key = displayedDecks[0].id) {
      val deck = displayedDecks[0]
      val isUnlocked = deck.isFreeTier || unlockedPacks.contains(deck.id)
      DeckGridCard(
        deck = deck,
        isUnlocked = isUnlocked,
        onClick = {
          if (isUnlocked) selectedDeckForPlay = deck else onNavigateToStore()
        }
      )
    }

    // Row 0, Column 1: Unlock All Decks Anchor Card
    item(key = "unlock_all_decks_anchor") {
      UnlockAllDecksGridCard(onClick = onNavigateToStore)
    }

    // Remaining Decks: Row 1..N
    items(
      items = displayedDecks.drop(1),
      key = { it.id }
    ) { deck ->
      val isUnlocked = deck.isFreeTier || unlockedPacks.contains(deck.id)
      DeckGridCard(
        deck = deck,
        isUnlocked = isUnlocked,
        onClick = {
          if (isUnlocked) selectedDeckForPlay = deck else onNavigateToStore()
        }
      )
    }
  }
}
```

---

## 5. Verification Method

1. **Static / Source Inspection**:
   - Inspect `app/src/main/java/com/example/ui/LobbyScreen.kt` after implementation to verify that `DeckGridCard` uses `height(215.dp)`, `RoundedCornerShape(18.dp)`, `50.dp` circular container, and the defined palette.
   - Verify `UnlockAllDecksGridCard` uses `Color(0xFFF59E0B)` and sits at Row 0, Col 1.
   - Verify `contentPadding` has `bottom = 96.dp`.

2. **Test Command Verification**:
   Execute the local unit test suite:
   ```powershell
   ./gradlew.bat testDebugUnitTest
   ```
   Confirm all tests pass without errors.

3. **Invalidation Conditions**:
   - If any deck card height is not 215dp.
   - If the "Unlock All Decks" card is not placed at Row 0, Col 1 or is missing `#F59E0B`.
   - If bottom cards are clipped or obstructed by the navigation bar due to inadequate bottom padding.
