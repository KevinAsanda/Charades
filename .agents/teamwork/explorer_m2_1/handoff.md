# Technical Investigation Report: Milestone 2 (Main Screen 2-Column Deck Grid Redesign)

**Agent**: Explorer 1 (`explorer_m2_1`)  
**Target File**: `app/src/main/java/com/example/ui/LobbyScreen.kt`  
**Milestone**: M2 (Main Screen 2-Column Deck Grid Redesign)  
**Status**: Investigation Complete — Ready for Implementation  

---

## 1. Observation

### 1.1 Current State of `LobbyScreen.kt`
- **File Location**: `app/src/main/java/com/example/ui/LobbyScreen.kt` (730 lines).
- **Public Signature** (lines 148-154):
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
  *(Confirmed: `onNavigateToStudio` was successfully stripped in Milestone 1).*
- **Current Architecture & Composables**:
  - Root container (lines 204-210): Single-column `LazyColumn` on white `StripiCanvas` (`Arrangement.spacedBy(20.dp)`).
  - Item 1 (lines 212-393): `StripiGradientMeshHero` holding app emblem, "Lekker Charades" pill, wallet/currency pills (`lobby_payment_methods_pill`, coin balance), greeting text ("Sanibonani, Mzansi"), and large `StripiFeaturedDarkCard` with active deck details and "Start Match" button (`testTag("start_match_button")`).
  - Item 2 (lines 396-499): "Game Mode" section with duration selector pills (30s, 60s, 90s) and horizontal scrolling carousel of `StripiFeatureCardLight` cards for `BLUEPRINT_GAME_MODES` (`mode_card_${mode.id}`).
  - Item 3 (lines 502-574): "Mzansi Decks" category header and horizontal scrollable filter chips ("All", "Free", "Expansions", "Festive").
  - Items 4 (lines 577-694): Single-column list of horizontal rectangular cards (`StripiFeatureCardLight`) for each deck in `displayedDecks`.
  - Item 5 (lines 697-729): Cream card (`StripiCreamCard`) linking to the Spaza Store.

### 1.2 Calling Context in `MainFeatureScreen.kt`
- **Call Site**: `app/src/main/java/com/example/ui/MainFeatureScreen.kt:393-406`
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
  - When `onStartGame` is called, `activePlayingDeck` is set, which immediately mounts `GamePlayScreen`.
  - When `onNavigateToStore` is called, `currentTab = 1` switches to the Store screen.
  - When `onOpenPaymentSetup` is called, `showPaymentModal = true` displays the payment bottom sheet.

### 1.3 Reference Screenshot Direct Visual Observations
- **Reference Image**: `C:/Users/kevin/.gemini/antigravity/brain/d5830d68-8241-4496-a4c4-ec65c258cf7b/.user_uploaded/media_1794515064020_678066d7.jpg`
- **Canvas / Background**: Seamless deep sapphire / royal blue (`Color(0xFF142B68)`).
- **Header Bar**:
  - Height: Compact, ~52dp.
  - Background: Color matches canvas seamlessly (`Color(0xFF142B68)`).
  - Title: Bold, crisp white `"Decks"` centered.
  - Flanking: Left brand emblem / subtle action, right coin balance / store access.
- **Grid Layout**:
  - Full-bleed 2-column vertical grid (`LazyVerticalGrid(columns = GridCells.Fixed(2))`).
  - Spacing: 10dp horizontal between columns, 10dp vertical between rows.
  - Padding: 12dp horizontal start/end padding, 96dp bottom padding to avoid overlapping the bottom navigation bar.
- **Top-Right Anchor Card ("Unlock All Decks")**:
  - Fixed location: **Row 0, Column 1** (top-right of grid).
  - Background: Amber / Gold (`Color(0xFFF59E0B)`).
  - Art: Padlock icon (`Icons.Default.Lock` or `Check` if unlocked) inside circular white overlay.
  - Title: Bold white `"Unlock All Decks"` (or `"All Decks Unlocked"`).
  - Description: Concise white description.
  - Corner Badge: White angled tag in upper-right corner (`"PASS"` or `"ACTIVE"`).
  - Action: Directly navigates to store (`onNavigateToStore()`).
- **Deck Grid Card Anatomy**:
  - Height: Uniform ~215dp with `RoundedCornerShape(18.dp)`.
  - Vibrant unique solid color per deck (Orange, Blue, Lime, Red, Teal, Amber, Purple, Crimson, etc.).
  - Upper-Right Corner Badge: White tag (`RoundedCornerShape(bottomStart = 8.dp, topEnd = 18.dp)`) displaying `"FREE"` for free tier, `"OWNED"` for purchased expansions, or a padlock icon for locked decks.
  - Centered Artwork: Material vector icon mapped via `deckIconForId(deck.id)` inside a 50dp circular container with 22% white opacity.
  - Centered Title: 16sp bold white text (`maxLines = 2`, `TextOverflow.Ellipsis`).
  - Centered Description: 11.5sp white text at 90% opacity (`maxLines = 3`, `lineHeight = 15.sp`, `TextOverflow.Ellipsis`).

### 1.4 Deck Inventory and Catalog Data
- **File**: `app/src/main/java/com/example/data/model/Deck.kt`
  - Core Decks:
    - `deck_trending_apps` ("Trending Apps & Digital SA", Free, Orange)
    - `deck_tiktok_lockdown` ("TikTok Trends & Lockdown Vault", Free, Blue)
    - `core_sports` ("Bafana, UFC, Bokke & Proteas", Free, Lime Green)
    - `core_kwaito` ("Amapiano, 3-Step & Kwaito", Free, Coral Red)
    - `core_2000s_tv` ("2000s TV & Soapie Dramas", Free, Deep Teal)
    - `deck_creators_podcasts` ("Content Creators & Podcasts", Free, Warm Amber)
    - `core_slang` ("Mzansi Slang & Street Culture", Free, Vivid Purple)
    - `core_tuckshop` ("Food & Tuckshop Classics", Free, Crimson Watermelon)
  - Expansion Decks:
    - `exp_pantsula` ("Pantsula Life & Heritage", Expansion, Sky Blue)
    - `exp_school_days` ("Mzansi School Days", Expansion, Deep Violet)
    - `exp_road_trip` ("Road Trips & Taxi Ranks", Expansion, Golden Mustard)
    - `exp_memes` ("Meme Hall of Fame", Expansion, Mint Green)
  - Seasonal & Custom:
    - `deck_festive_groove` ("Dezemba Festive Groove Pack", Free, Festive Red)
    - `custom_deck_user` ("My Custom Mzansi Cards", Free, Indigo)
- **Store Pass Model** (`app/src/main/java/com/example/data/model/StorePack.kt`):
  - `StoreCatalog.ALL_ACCESS_PASS.id` = `"mzansi_all_access"`

---

## 2. Logic Chain

1. **Root Screen Transformation**:
   - The current `LobbyScreen.kt` embeds a single-column `LazyColumn` holding an atmospheric hero mesh, mode carousel, category chips, and horizontal deck rows.
   - To match Requirement R2 and the reference screenshot, we replace the `LazyColumn` with a `Column` composed of:
     1. `DecksTopBar`: 52dp height, `#142B68` background, centered "Decks" title.
     2. `LazyVerticalGrid(columns = GridCells.Fixed(2))`: Full-bleed 2-column layout filling the remaining height.

2. **Ensuring Exact Grid Layout & Position of "Unlock All Decks" Card**:
   - In a 2-column Compose `LazyVerticalGrid`, elements are positioned row by row:
     - Index 0 -> Row 0, Column 0 (top-left).
     - Index 1 -> Row 0, Column 1 (top-right).
     - Index 2 -> Row 1, Column 0.
     - Index 3 -> Row 1, Column 1.
   - By rendering:
     - `item(key = allDecks.first().id)` -> First deck (`deck_trending_apps`) sits at Row 0, Col 0.
     - `item(key = "unlock_all_decks_anchor")` -> `UnlockAllDecksCard` sits at Row 0, Col 1.
     - `items(items = allDecks.drop(1), key = { it.id })` -> All remaining decks flow down into Row 1+.
   - This layout mathematically guarantees that the golden "Unlock All Decks" card occupies Row 0, Column 1 without requiring complex span math or custom layout managers.

3. **User Flow & Modal Quick Play Interaction**:
   - In the reference screenshot, cards are clean without embedded start buttons.
   - Tapping an **unlocked deck**:
     - Opens `DeckQuickPlayDialog` showing deck identity, 4 core party modes (`Act It Out`, `Hit the Beat`, `30-Seconds`, `Heads-Up Tilt`), duration selector pills (`30s`, `60s`, `90s`), and a full-width "Start Playing" button with `testTag("start_match_button")`.
     - Tapping "Start Playing" calls `onStartGame(deck, selectedMode, selectedDuration)` and dismisses the dialog.
   - Tapping a **locked expansion deck**:
     - Directly calls `onNavigateToStore()`.
   - Tapping the **"Unlock All Decks" card**:
     - Directly calls `onNavigateToStore()`.

4. **Public Signature Integrity**:
   - Must be strictly:
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
   - No `onNavigateToStudio` parameter. Matches `MainFeatureScreen.kt:393-406` identically.

---

## 3. Caveats

1. **Bottom Navigation Bar Inset**:
   - `MainFeatureScreen.kt` renders a `NavigationBar` (~80dp height) at the bottom.
   - The grid must specify `PaddingValues(bottom = 96.dp)` so that users can scroll the bottom-most deck cards completely into view without being obscured by the navigation bar.
2. **Text Ellipsis on Small Screens**:
   - On narrow devices (320-360dp width), grid cells are ~150dp wide.
   - Card titles must enforce `maxLines = 2` and `overflow = TextOverflow.Ellipsis`.
   - Card descriptions must enforce `maxLines = 3`, `lineHeight = 15.sp`, and `overflow = TextOverflow.Ellipsis`.
3. **All Decks Unlocked State**:
   - If the user owns the `mzansi_all_access` pass or all expansion packs, `UnlockAllDecksCard` displays `"ACTIVE"` badge, `"All Decks Unlocked"` title, and a checkmark icon, while remaining in place to preserve grid visual balance.

---

## 4. Conclusion & Complete Proposed Implementation

Here is the exact code structure to be implemented in `app/src/main/java/com/example/ui/LobbyScreen.kt`:

```kotlin
package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.CharadesCard
import com.example.data.model.CustomCard
import com.example.data.model.Deck
import com.example.data.model.PredefinedDecks
import com.example.data.model.StoreCatalog
import com.example.data.model.UserProfile
import com.example.ui.components.StripiDeckIconBadge
import com.example.ui.components.StripiPrimaryPillButton
import com.example.ui.components.StripiRounded
import com.example.ui.components.StripiTabularText
import com.example.ui.components.deckIconForId
import com.example.ui.theme.StripiBrandDark900
import com.example.ui.theme.StripiCanvasSoft
import com.example.ui.theme.StripiHairline
import com.example.ui.theme.StripiInk
import com.example.ui.theme.StripiInkMute
import com.example.ui.theme.StripiInkSecondary
import com.example.ui.theme.StripiOnPrimary
import com.example.ui.theme.StripiPrimary
import com.example.ui.theme.StripiTypeTokens

data class GameplayModeSpec(
  val id: String,
  val title: String,
  val subtitle: String,
  val ruleSummary: String,
  val defaultSeconds: Long,
  val icon: ImageVector
)

private fun colorForDeck(deckId: String): Color {
  return when (deckId) {
    "deck_trending_apps" -> Color(0xFFF97316)      // Vibrant Orange
    "deck_tiktok_lockdown" -> Color(0xFF3B82F6)    // Electric Blue
    "core_sports" -> Color(0xFF65A30D)             // Lime Green
    "core_kwaito" -> Color(0xFFF43F5E)             // Coral Red
    "core_2000s_tv" -> Color(0xFF0D9488)           // Deep Teal
    "deck_creators_podcasts" -> Color(0xFFD97706)  // Warm Amber
    "core_slang" -> Color(0xFF8B5CF6)              // Vivid Purple
    "core_tuckshop" -> Color(0xFFE11D48)           // Crimson Watermelon
    "exp_pantsula" -> Color(0xFF0284C7)            // Sky Blue
    "exp_school_days" -> Color(0xFF9333EA)         // Deep Violet
    "exp_road_trip" -> Color(0xFFCA8A04)           // Golden Mustard
    "exp_memes" -> Color(0xFF059669)               // Mint Green
    "deck_festive_groove" -> Color(0xFFDC2626)     // Festive Red
    else -> Color(0xFF4F46E5)                      // Indigo for Custom/Other Decks
  }
}

@Composable
fun LobbyScreen(
  userProfile: UserProfile?,
  customCards: List<CustomCard> = emptyList(),
  onStartGame: (Deck, String, Long) -> Unit,
  onNavigateToStore: () -> Unit,
  onOpenPaymentSetup: () -> Unit = {}
) {
  val unlockedPacks = userProfile?.unlockedPacks ?: PredefinedDecks.FREE_DECK_IDS
  val userCoins = userProfile?.coins ?: 150L
  val isAllUnlocked = unlockedPacks.contains(StoreCatalog.ALL_ACCESS_PASS.id) ||
      PredefinedDecks.ALL_DECKS.all { it.isFreeTier || unlockedPacks.contains(it.id) }

  var deckForQuickPlay by remember { mutableStateOf<Deck?>(null) }

  val customDeck = remember(customCards) {
    if (customCards.isNotEmpty()) {
      Deck(
        id = "custom_deck_user",
        name = "My Custom Mzansi Cards",
        tagLine = "Your personal community-created cards",
        iconEmoji = "",
        isFreeTier = true,
        cards = customCards.map {
          CharadesCard(
            id = it.id,
            term = it.term,
            category = it.category,
            hints = it.hints,
            culturalEra = it.culturalEra
          )
        }
      )
    } else null
  }

  val festiveDeck = remember { PredefinedDecks.buildFestiveGrooveDeck() }

  val allDecks = remember(customDeck, festiveDeck) {
    val base = PredefinedDecks.ALL_DECKS + listOf(festiveDeck)
    if (customDeck != null) base + listOf(customDeck) else base
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF142B68))
  ) {
    // 1. Compact "Decks" Header Bar (~52dp)
    DecksTopBar(
      userCoins = userCoins,
      onNavigateToStore = onNavigateToStore
    )

    // 2. Full-Bleed 2-Column Vertical Grid
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF142B68)),
      contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 96.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      if (allDecks.isNotEmpty()) {
        // Row 0, Column 0: First Deck
        item(key = allDecks.first().id) {
          val firstDeck = allDecks.first()
          val isUnlocked = firstDeck.isFreeTier || unlockedPacks.contains(firstDeck.id)
          DeckGridCard(
            deck = firstDeck,
            isUnlocked = isUnlocked,
            backgroundColor = colorForDeck(firstDeck.id),
            onClick = {
              if (isUnlocked) {
                deckForQuickPlay = firstDeck
              } else {
                onNavigateToStore()
              }
            }
          )
        }

        // Row 0, Column 1: "Unlock All Decks" Anchor Card
        item(key = "unlock_all_decks_anchor") {
          UnlockAllDecksCard(
            isAllUnlocked = isAllUnlocked,
            onClick = onNavigateToStore
          )
        }

        // Row 1+: Remaining Decks
        items(
          items = allDecks.drop(1),
          key = { it.id }
        ) { deck ->
          val isUnlocked = deck.isFreeTier || unlockedPacks.contains(deck.id)
          DeckGridCard(
            deck = deck,
            isUnlocked = isUnlocked,
            backgroundColor = colorForDeck(deck.id),
            onClick = {
              if (isUnlocked) {
                deckForQuickPlay = deck
              } else {
                onNavigateToStore()
              }
            }
          )
        }
      }
    }
  }

  // 3. Quick Play Modal on Unlocked Deck Tap
  deckForQuickPlay?.let { deck ->
    DeckQuickPlayDialog(
      deck = deck,
      onDismiss = { deckForQuickPlay = null },
      onStartGame = { targetDeck, mode, duration ->
        deckForQuickPlay = null
        onStartGame(targetDeck, mode, duration)
      }
    )
  }
}

@Composable
private fun DecksTopBar(
  userCoins: Long,
  onNavigateToStore: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(52.dp)
      .background(Color(0xFF142B68))
      .padding(horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Left: Brand Emblem
    Image(
      painter = painterResource(id = R.drawable.logo_emblem),
      contentDescription = "Lekker Charades Emblem",
      modifier = Modifier
        .size(24.dp)
        .clip(RoundedCornerShape(6.dp))
    )

    // Center: "Decks" Header Title
    Text(
      text = "Decks",
      style = StripiTypeTokens.headingMd.copy(
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
      ),
      textAlign = TextAlign.Center
    )

    // Right: Coins / Store Shortcut Pill
    Surface(
      shape = StripiRounded.pill,
      color = Color.White.copy(alpha = 0.15f),
      modifier = Modifier.clickable { onNavigateToStore() }
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Icon(
          imageVector = Icons.Default.MonetizationOn,
          contentDescription = "Coins",
          tint = Color(0xFFFDE047),
          modifier = Modifier.size(14.dp)
        )
        StripiTabularText(
          text = "$userCoins",
          color = Color.White,
          fontSize = 12,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}

@Composable
private fun DeckGridCard(
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
      .testTag("deck_grid_card_${deck.id}")
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
              fontWeight = FontWeight.Bold,
              fontSize = 9.5.sp
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
          fontSize = 11.5.sp,
          lineHeight = 15.sp
        ),
        textAlign = TextAlign.Center,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun UnlockAllDecksCard(
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
      .testTag("unlock_all_decks_card")
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
          fontWeight = FontWeight.Bold,
          fontSize = 9.5.sp
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
          "You have full access to every current & future expansion pack!"
        else
          "Instant access to all expansion packs, bonus coins & upcoming decks!",
        style = StripiTypeTokens.caption.copy(
          color = StripiOnPrimary.copy(alpha = 0.90f),
          fontSize = 11.5.sp,
          lineHeight = 15.sp
        ),
        textAlign = TextAlign.Center,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun DeckQuickPlayDialog(
  deck: Deck,
  initialGameMode: String = "act_it_out",
  initialDuration: Long = 60L,
  onDismiss: () -> Unit,
  onStartGame: (Deck, String, Long) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedMode by remember { mutableStateOf(initialGameMode) }
  var selectedDuration by remember { mutableLongStateOf(initialDuration) }

  val quickPlayModes = remember {
    listOf(
      GameplayModeSpec(
        id = "act_it_out",
        title = "Act It Out",
        subtitle = "Silent Mime",
        ruleSummary = "Pure physical mime and comedy.",
        defaultSeconds = 60L,
        icon = Icons.AutoMirrored.Filled.DirectionsRun
      ),
      GameplayModeSpec(
        id = "hit_the_beat",
        title = "Hit the Beat",
        subtitle = "Dance & Hum",
        ruleSummary = "Amapiano, 3-Step or Bacardi.",
        defaultSeconds = 60L,
        icon = Icons.Default.MusicNote
      ),
      GameplayModeSpec(
        id = "mzansi_30_seconds",
        title = "30-Seconds",
        subtitle = "Verbal Blitz",
        ruleSummary = "Fast verbal clues without root words.",
        defaultSeconds = 30L,
        icon = Icons.Default.RecordVoiceOver
      ),
      GameplayModeSpec(
        id = "heads_up",
        title = "Heads-Up Tilt",
        subtitle = "Forehead Tilt",
        ruleSummary = "Phone on forehead with tilt controls.",
        defaultSeconds = 60L,
        icon = Icons.Default.ScreenRotation
      )
    )
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color.White,
      tonalElevation = 8.dp,
      modifier = modifier
        .fillMaxWidth(0.92f)
        .clip(RoundedCornerShape(24.dp))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        // 1. Header with Deck Info & Close button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            StripiDeckIconBadge(
              deckId = deck.id,
              isSelected = true,
              modifier = Modifier.size(44.dp)
            )
            Column {
              Text(
                text = deck.name,
                style = StripiTypeTokens.headingMd.copy(color = StripiInk, fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${deck.cards.size} Cards · Ready to Play",
                style = StripiTypeTokens.caption.copy(color = StripiInkSecondary)
              )
            }
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = StripiInkMute,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Mode Selection (4 Modes)
        Text(
          text = "Choose Game Mode",
          style = StripiTypeTokens.bodyMd.copy(color = StripiInk, fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          quickPlayModes.chunked(2).forEach { rowModes ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              rowModes.forEach { mode ->
                val isSelected = selectedMode == mode.id
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = if (isSelected) StripiPrimary.copy(alpha = 0.10f) else StripiCanvasSoft,
                  border = BorderStroke(1.5.dp, if (isSelected) StripiPrimary else StripiHairline),
                  modifier = Modifier
                    .weight(1f)
                    .clickable {
                      selectedMode = mode.id
                      selectedDuration = mode.defaultSeconds
                    }
                    .testTag("mode_card_${mode.id}")
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) StripiPrimary else Color.White),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = mode.icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else StripiInk,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                    Column {
                      Text(
                        text = mode.title,
                        style = StripiTypeTokens.bodyMd.copy(
                          color = if (isSelected) StripiPrimary else StripiInk,
                          fontWeight = FontWeight.SemiBold,
                          fontSize = 13.sp
                        ),
                        maxLines = 1
                      )
                      Text(
                        text = mode.subtitle,
                        style = StripiTypeTokens.micro.copy(color = StripiInkMute, fontSize = 11.sp),
                        maxLines = 1
                      )
                    }
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Duration Selector
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Round Duration",
            style = StripiTypeTokens.bodyMd.copy(color = StripiInk, fontWeight = FontWeight.SemiBold)
          )

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(30L, 60L, 90L).forEach { secs ->
              val isSecSelected = selectedDuration == secs
              Surface(
                shape = StripiRounded.pill,
                color = if (isSecSelected) StripiBrandDark900 else StripiCanvasSoft,
                border = BorderStroke(1.dp, if (isSecSelected) StripiBrandDark900 else StripiHairline),
                modifier = Modifier.clickable { selectedDuration = secs }
              ) {
                Text(
                  text = "${secs}s",
                  style = StripiTypeTokens.caption.copy(
                    color = if (isSecSelected) StripiOnPrimary else StripiInk,
                    fontWeight = if (isSecSelected) FontWeight.Bold else FontWeight.Normal
                  ),
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. "Start Playing" Button
        StripiPrimaryPillButton(
          text = "Start Playing",
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("start_match_button"),
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = null,
              tint = StripiOnPrimary,
              modifier = Modifier.size(20.dp)
            )
          },
          onClick = {
            onStartGame(deck, selectedMode, selectedDuration)
            onDismiss()
          }
        )
      }
    }
  }
}
```

---

## 5. Verification Method

1. **Static Analysis & Compilation Check**:
   - Check Kotlin compilation of `LobbyScreen.kt` using standard Gradle tooling once restored in M4:
     ```powershell
     .\gradlew compileDebugKotlin
     ```
   - Verify that all composable signatures match callers in `MainFeatureScreen.kt`.
2. **UI & Layout Verification**:
   - **Header Bar**: Displays "Decks" in bold white centered text at 52dp height with deep royal blue background `#142B68`.
   - **Grid Alignment**: 2-column grid rendered via `LazyVerticalGrid(columns = GridCells.Fixed(2))`.
   - **Anchor Card Placement**: Row 0, Column 0 is Deck 0 (`deck_trending_apps`); Row 0, Column 1 is the golden "Unlock All Decks" card (`testTag("unlock_all_decks_card")`).
   - **Card Anatomy**: Uniform 215dp height, rounded 18dp corners, top-right badge ("FREE", "OWNED", or lock), centered 50dp circular icon container, centered bold title, and centered 2-3 line description.
   - **Quick Play Dialog**:
     - Tapping an unlocked deck displays `DeckQuickPlayDialog`.
     - Dialog displays 4 party modes (`mode_card_act_it_out`, `mode_card_hit_the_beat`, `mode_card_mzansi_30_seconds`, `mode_card_heads_up`).
     - Dialog displays duration selector (30s, 60s, 90s).
     - Clicking "Start Playing" (`testTag("start_match_button")`) triggers `onStartGame` and navigates into active gameplay.
   - **Store Navigation**:
     - Clicking "Unlock All Decks" gold card or any locked deck routes directly to `onNavigateToStore()`.
3. **Automated Unit Testing**:
   - Run unit tests to ensure no regressions:
     ```powershell
     .\gradlew testDebugUnitTest
     ```
