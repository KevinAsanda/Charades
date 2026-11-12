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

private data class GameplayModeSpec(
  val id: String,
  val title: String,
  val subtitle: String,
  val ruleSummary: String,
  val defaultSeconds: Long,
  val icon: ImageVector
)

private fun deckCardBackgroundColor(deckId: String): Color {
  return when (deckId) {
    "deck_trending_apps" -> Color(0xFFFF6D3A)      // Warm Sunset Orange
    "deck_tiktok_lockdown" -> Color(0xFF3B82F6)    // Electric Blue
    "core_sports" -> Color(0xFF10B981)             // Stadium Emerald Green
    "core_kwaito" -> Color(0xFFF43F5E)             // Coral Red
    "core_2000s_tv" -> Color(0xFF0284C7)           // Vivid Sky Blue
    "deck_creators_podcasts" -> Color(0xFF06B6D4)  // Electric Cyan
    "core_slang" -> Color(0xFF8B5CF6)              // Vivid Purple
    "core_tuckshop" -> Color(0xFFD97706)           // Warm Honey Amber
    "exp_pantsula" -> Color(0xFF6366F1)            // Vibrant Indigo
    "exp_school_days" -> Color(0xFF65A30D)         // Lime Apple Green
    "exp_road_trip" -> Color(0xFFCA8A04)           // Golden Mustard
    "exp_memes" -> Color(0xFFBE185D)               // Rich Raspberry Pink
    "deck_festive_groove" -> Color(0xFFDC2626)     // Festive Red
    "custom_deck_user" -> Color(0xFF7E22CE)        // Royal Amethyst
    else -> Color(0xFF4F46E5)                      // Deep Indigo
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
      onNavigateToStore = onNavigateToStore,
      onOpenPaymentSetup = onOpenPaymentSetup
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
        // Row 0, Column 0: First Deck (Trending Apps)
        item(key = allDecks.first().id) {
          val firstDeck = allDecks.first()
          val isUnlocked = firstDeck.isFreeTier || unlockedPacks.contains(firstDeck.id)
          DeckGridCard(
            deck = firstDeck,
            isUnlocked = isUnlocked,
            backgroundColor = deckCardBackgroundColor(firstDeck.id),
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
            backgroundColor = deckCardBackgroundColor(deck.id),
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
  onOpenPaymentSetup: () -> Unit,
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
        .size(26.dp)
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
      modifier = Modifier
        .clickable { onNavigateToStore() }
        .testTag("lobby_payment_methods_pill")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
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
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
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
      // Centered Artwork / Icon Container (50dp circle)
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

      // Centered Title
      Text(
        text = deck.name,
        style = StripiTypeTokens.bodyLg.copy(
          color = StripiOnPrimary,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          lineHeight = 18.sp
        ),
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 4.dp)
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Centered Concise Description
      Text(
        text = deck.tagLine,
        style = StripiTypeTokens.caption.copy(
          color = StripiOnPrimary.copy(alpha = 0.90f),
          fontSize = 11.sp,
          lineHeight = 14.sp
        ),
        textAlign = TextAlign.Center,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 4.dp)
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
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
          contentDescription = "Unlock All Decks",
          tint = Color.White,
          modifier = Modifier.size(28.dp)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = if (isAllUnlocked) "All Decks Unlocked" else "Unlock All Decks",
        style = StripiTypeTokens.bodyLg.copy(
          color = StripiOnPrimary,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          lineHeight = 18.sp
        ),
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 4.dp)
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = if (isAllUnlocked)
          "You have full access to every current & future expansion pack!"
        else
          "Instant access to all expansion packs, bonus coins & upcoming decks!",
        style = StripiTypeTokens.caption.copy(
          color = StripiOnPrimary.copy(alpha = 0.90f),
          fontSize = 11.sp,
          lineHeight = 14.sp
        ),
        textAlign = TextAlign.Center,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 4.dp)
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
            modifier = Modifier.weight(1f, fill = false),
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

        // 2. Mode Selection (4 Modes in 2x2 grid)
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
