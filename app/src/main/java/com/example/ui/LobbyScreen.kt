package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CharadesCard
import com.example.data.model.CustomCard
import com.example.data.model.Deck
import com.example.data.model.PredefinedDecks
import com.example.data.model.StoreCatalog
import com.example.data.model.UserProfile
import com.example.ui.components.StripiCreamCard
import com.example.ui.components.StripiDeckIconBadge
import com.example.ui.components.StripiFeatureCardLight
import com.example.ui.components.StripiFeaturedDarkCard
import com.example.ui.components.StripiGradientMeshHero
import com.example.ui.components.StripiPrimaryPillButton
import com.example.ui.components.StripiRounded
import com.example.ui.components.StripiSecondaryPillButton
import com.example.ui.components.StripiSoftPillTag
import com.example.ui.components.StripiTabularText
import com.example.ui.theme.StripiBrandDark900
import com.example.ui.theme.StripiCanvas
import com.example.ui.theme.StripiCanvasSoft
import com.example.ui.theme.StripiHairline
import com.example.ui.theme.StripiInk
import com.example.ui.theme.StripiInkMute
import com.example.ui.theme.StripiInkSecondary
import com.example.ui.theme.StripiOnPrimary
import com.example.ui.theme.StripiPrimary
import com.example.ui.theme.StripiPrimaryDeep
import com.example.ui.theme.StripiSubduedHover
import com.example.ui.theme.StripiTypeTokens

data class GameplayModeSpec(
  val id: String,
  val title: String,
  val subtitle: String,
  val ruleSummary: String,
  val defaultSeconds: Long,
  val icon: ImageVector
)

private val BLUEPRINT_GAME_MODES = listOf(
  GameplayModeSpec(
    id = "act_it_out",
    title = "Act It Out",
    subtitle = "Silent Mime",
    ruleSummary = "Pure physical mime and body comedy. Zero spoken words or sounds.",
    defaultSeconds = 60L,
    icon = Icons.AutoMirrored.Filled.DirectionsRun
  ),
  GameplayModeSpec(
    id = "hit_the_beat",
    title = "Hit the Beat",
    subtitle = "Dance & Hum",
    ruleSummary = "Amapiano, 3-Step, or Bacardi moves. Hum the bassline, no lyrics.",
    defaultSeconds = 60L,
    icon = Icons.Default.MusicNote
  ),
  GameplayModeSpec(
    id = "mzansi_30_seconds",
    title = "30-Seconds",
    subtitle = "Verbal Blitz",
    ruleSummary = "Fast verbal clues! Never say the forbidden root words on the card.",
    defaultSeconds = 30L,
    icon = Icons.Default.RecordVoiceOver
  ),
  GameplayModeSpec(
    id = "my_reaction",
    title = "My Reaction",
    subtitle = "Facial Ranking",
    ruleSummary = "Point to ranks 1 to 5 in the air using only facial expressions.",
    defaultSeconds = 60L,
    icon = Icons.Default.Face
  ),
  GameplayModeSpec(
    id = "party_teams",
    title = "Party Teams",
    subtitle = "Team A vs B",
    ruleSummary = "Head-to-head team showdown with halftime switch for Team B.",
    defaultSeconds = 60L,
    icon = Icons.Default.Group
  ),
  GameplayModeSpec(
    id = "heads_up",
    title = "Heads-Up",
    subtitle = "Forehead Tilt",
    ruleSummary = "Phone on forehead. Tilt down for Correct, tilt up to Pass.",
    defaultSeconds = 60L,
    icon = Icons.Default.ScreenRotation
  )
)

@Composable
fun LobbyScreen(
  userProfile: UserProfile?,
  customCards: List<CustomCard>,
  onStartGame: (Deck, String, Long) -> Unit,
  onNavigateToStore: () -> Unit,
  onNavigateToStudio: () -> Unit,
  onOpenPaymentSetup: () -> Unit = {}
) {
  val unlockedPacks = userProfile?.unlockedPacks ?: PredefinedDecks.FREE_DECK_IDS
  val userCoins = userProfile?.coins ?: 150L

  var selectedGameMode by remember { mutableStateOf("act_it_out") }
  var selectedDeckId by remember { mutableStateOf("deck_trending_apps") }
  var roundDuration by remember { mutableLongStateOf(60L) }
  var selectedDeckCategory by remember { mutableStateOf("All") } // All, Free, Expansions, Festive

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
    val base = listOf(festiveDeck) + PredefinedDecks.ALL_DECKS
    if (customDeck != null) listOf(customDeck) + base else base
  }

  val displayedDecks = remember(allDecks, selectedDeckCategory) {
    when (selectedDeckCategory) {
      "Free" -> allDecks.filter { it.isFreeTier && it.id != "deck_festive_groove" }
      "Expansions" -> allDecks.filter { !it.isFreeTier }
      "Festive" -> allDecks.filter { it.id == "deck_festive_groove" }
      else -> allDecks
    }
  }

  val activeDeck = allDecks.find { it.id == selectedDeckId } ?: PredefinedDecks.CORE_TRENDING_APPS
  val isSelectedReady = activeDeck.isFreeTier || unlockedPacks.contains(activeDeck.id)
  val activeModeSpec = BLUEPRINT_GAME_MODES.find { it.id == selectedGameMode } ?: BLUEPRINT_GAME_MODES.first()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(StripiCanvas),
    contentPadding = PaddingValues(bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // 1. Clean Atmospheric Hero Section
    item {
      StripiGradientMeshHero(
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 24.dp)
      ) {
        // Minimalist Top Utility Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          StripiSoftPillTag(text = "Mzansi Charades")

          // Wallet & Billing Pill
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = StripiRounded.pill,
              color = StripiCanvas,
              border = BorderStroke(1.dp, StripiHairline),
              modifier = Modifier
                .clickable { onOpenPaymentSetup() }
                .testTag("lobby_payment_methods_pill")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CreditCard,
                  contentDescription = "Payment Methods",
                  tint = StripiPrimary,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "ZAR",
                  style = StripiTypeTokens.caption.copy(color = StripiInk, fontWeight = FontWeight.Medium)
                )
              }
            }

            Surface(
              shape = StripiRounded.pill,
              color = StripiBrandDark900,
              modifier = Modifier.clickable { onNavigateToStore() }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.MonetizationOn,
                  contentDescription = "Coins",
                  tint = StripiSubduedHover,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                StripiTabularText(
                  text = "$userCoins",
                  color = StripiOnPrimary,
                  fontSize = 13,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Sanibonani, ${userProfile?.displayName?.split(" ")?.firstOrNull() ?: "Mzansi"}.",
          style = StripiTypeTokens.displayLg.copy(color = StripiInk)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "Select a deck, choose your party mode, and start acting.",
          style = StripiTypeTokens.bodyMd.copy(color = StripiInkSecondary)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Featured Active Deck Showcase Card
        StripiFeaturedDarkCard(
          modifier = Modifier.fillMaxWidth(),
          contentPadding = PaddingValues(20.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            StripiDeckIconBadge(
              deckId = activeDeck.id,
              isSelected = true,
              modifier = Modifier.size(46.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = activeDeck.name,
                  style = StripiTypeTokens.headingMd.copy(color = StripiOnPrimary),
                  maxLines = 1
                )
              }
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = "${activeDeck.cards.size} cards · ${activeModeSpec.title} (${roundDuration}s)",
                style = StripiTypeTokens.caption.copy(color = StripiSubduedHover)
              )
            }

            StripiSoftPillTag(
              text = if (isSelectedReady) "Ready" else "Locked",
              containerColor = if (isSelectedReady) StripiPrimary else StripiSubduedHover,
              contentColor = if (isSelectedReady) StripiOnPrimary else StripiBrandDark900
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = activeModeSpec.ruleSummary,
            style = StripiTypeTokens.caption.copy(color = StripiOnPrimary.copy(alpha = 0.78f))
          )

          Spacer(modifier = Modifier.height(16.dp))

          StripiPrimaryPillButton(
            text = if (isSelectedReady) "Start ${activeModeSpec.title}" else "Unlock in Store",
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("start_match_button"),
            leadingIcon = {
              Icon(
                imageVector = if (isSelectedReady) Icons.Default.PlayArrow else Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = StripiOnPrimary,
                modifier = Modifier.size(18.dp)
              )
            },
            trailingContent = {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = StripiOnPrimary,
                modifier = Modifier.size(16.dp)
              )
            },
            onClick = {
              if (isSelectedReady) {
                onStartGame(activeDeck, selectedGameMode, roundDuration)
              } else {
                onNavigateToStore()
              }
            }
          )
        }
      }
    }

    // 2. Intentional Game Modes Section
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Game Mode",
            style = StripiTypeTokens.headingMd.copy(color = StripiInk)
          )

          // Sleek Round Duration Selector
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Timer,
              contentDescription = null,
              tint = StripiInkMute,
              modifier = Modifier.size(14.dp)
            )
            listOf(30L, 60L, 90L).forEach { secs ->
              val isSecSelected = roundDuration == secs
              Surface(
                shape = StripiRounded.pill,
                color = if (isSecSelected) StripiBrandDark900 else StripiCanvasSoft,
                modifier = Modifier.clickable { roundDuration = secs }
              ) {
                Text(
                  text = "${secs}s",
                  style = StripiTypeTokens.micro.copy(
                    color = if (isSecSelected) StripiOnPrimary else StripiInkMute,
                    fontWeight = if (isSecSelected) FontWeight.Medium else FontWeight.Normal
                  ),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }
        }

        // Horizontal Mode Carousel: Spacious, clean cards that never squish text
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          BLUEPRINT_GAME_MODES.forEach { mode ->
            val isSelected = selectedGameMode == mode.id
            StripiFeatureCardLight(
              modifier = Modifier
                .width(132.dp)
                .testTag("mode_card_${mode.id}"),
              borderColor = if (isSelected) StripiPrimary else StripiHairline,
              borderWidth = if (isSelected) 1.5.dp else 1.dp,
              backgroundColor = if (isSelected) StripiSubduedHover.copy(alpha = 0.20f) else StripiCanvas,
              contentPadding = PaddingValues(12.dp),
              onClick = {
                selectedGameMode = mode.id
                roundDuration = mode.defaultSeconds
              }
            ) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(StripiRounded.sm)
                  .background(if (isSelected) StripiPrimary else StripiCanvasSoft),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = mode.icon,
                  contentDescription = null,
                  tint = if (isSelected) StripiOnPrimary else StripiInk,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = mode.title,
                style = StripiTypeTokens.bodyMd.copy(
                  color = if (isSelected) StripiPrimary else StripiInk,
                  fontWeight = FontWeight.Medium
                ),
                maxLines = 1
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = mode.subtitle,
                style = StripiTypeTokens.caption.copy(color = StripiInkMute),
                maxLines = 1
              )
            }
          }
        }
      }
    }

    // 3. Clean Deck Category Navigation
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Mzansi Decks",
            style = StripiTypeTokens.headingMd.copy(color = StripiInk)
          )
          StripiTabularText(
            text = "${displayedDecks.size} available",
            color = StripiInkMute,
            fontSize = 13
          )
        }

        // Clean Segmented Category Tabs
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("All", "Free", "Expansions", "Festive").forEach { cat ->
            val isCatSelected = selectedDeckCategory == cat
            Surface(
              shape = StripiRounded.pill,
              color = if (isCatSelected) StripiBrandDark900 else StripiCanvasSoft,
              border = BorderStroke(1.dp, if (isCatSelected) StripiBrandDark900 else StripiHairline),
              modifier = Modifier
                .clickable { selectedDeckCategory = cat }
                .then(
                  if (cat == "Festive") Modifier.testTag("festive_groove_filter_chip") else Modifier
                )
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                if (cat == "Festive") {
                  Icon(
                    imageVector = Icons.Default.Celebration,
                    contentDescription = null,
                    tint = if (isCatSelected) StripiOnPrimary else StripiPrimary,
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                  text = when (cat) {
                    "Free" -> "Free Core"
                    "Expansions" -> "Expansions"
                    "Festive" -> "Dezemba Groove"
                    else -> "All Decks"
                  },
                  style = StripiTypeTokens.caption.copy(
                    color = if (isCatSelected) StripiOnPrimary else StripiInkSecondary,
                    fontWeight = if (isCatSelected) FontWeight.Medium else FontWeight.Normal
                  )
                )
              }
            }
          }
        }
      }
    }

    // 4. Polished Deck Cards List
    items(displayedDecks) { deckItem ->
      val isUnlocked = deckItem.isFreeTier || unlockedPacks.contains(deckItem.id)
      val isSelected = selectedDeckId == deckItem.id

      StripiFeatureCardLight(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        borderColor = if (isSelected) StripiPrimary else StripiHairline,
        borderWidth = if (isSelected) 1.5.dp else 1.dp,
        backgroundColor = if (isSelected) StripiSubduedHover.copy(alpha = 0.16f) else StripiCanvas,
        contentPadding = PaddingValues(16.dp),
        onClick = {
          if (isUnlocked) {
            selectedDeckId = deckItem.id
          } else {
            onNavigateToStore()
          }
        }
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          StripiDeckIconBadge(
            deckId = deckItem.id,
            isSelected = isSelected,
            modifier = Modifier.size(42.dp)
          )

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = deckItem.name,
                style = StripiTypeTokens.bodyLg.copy(
                  color = if (isSelected) StripiPrimary else StripiInk,
                  fontWeight = FontWeight.Medium
                ),
                maxLines = 1
              )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
              text = deckItem.tagLine,
              style = StripiTypeTokens.caption.copy(color = StripiInkMute),
              maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              StripiTabularText(
                text = "${deckItem.cards.size} cards",
                color = StripiInkSecondary,
                fontSize = 12
              )
              if (deckItem.isFreeTier) {
                StripiSoftPillTag(text = "Included")
              } else if (isUnlocked) {
                StripiSoftPillTag(
                  text = "Owned",
                  containerColor = StripiSubduedHover,
                  contentColor = StripiPrimaryDeep
                )
              }
            }
          }

          if (!isUnlocked) {
            Surface(
              shape = StripiRounded.pill,
              color = StripiCanvasSoft,
              border = BorderStroke(1.dp, StripiHairline)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Lock,
                  contentDescription = null,
                  tint = StripiInkMute,
                  modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                StripiTabularText(
                  text = StoreCatalog.formattedZarForDeck(deckItem),
                  color = StripiInk,
                  fontSize = 12,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          } else if (isSelected) {
            Surface(
              shape = StripiRounded.pill,
              color = StripiPrimary
            ) {
              Text(
                text = "Selected",
                style = StripiTypeTokens.micro.copy(color = StripiOnPrimary, fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }

    // 5. Minimalist Creative Studio Link Card
    item {
      StripiCreamCard(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(18.dp),
        onClick = onNavigateToStudio
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Custom Mzansi Decks",
              style = StripiTypeTokens.headingSm.copy(color = StripiInk)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Build personal cards or generate AI artwork in Studio.",
              style = StripiTypeTokens.bodyMd.copy(color = StripiInkSecondary)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          StripiSecondaryPillButton(
            text = "Studio",
            onClick = onNavigateToStudio
          )
        }
      }
    }
  }
}
