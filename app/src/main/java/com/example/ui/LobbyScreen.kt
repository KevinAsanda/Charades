package com.example.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.ShoppingBag
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
import com.example.ui.components.deckIconForId
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

@Composable
fun LobbyScreen(
  userProfile: UserProfile?,
  customCards: List<CustomCard>,
  onStartGame: (Deck, String, Long) -> Unit,
  onNavigateToStore: () -> Unit,
  onNavigateToStudio: () -> Unit,
  onOpenPaymentSetup: () -> Unit = {}
) {
  val unlockedPacks = userProfile?.unlockedPacks
    ?: listOf("core_kwaito", "core_slang", "core_2000s_tv", "core_tuckshop", "core_sports")
  val userCoins = userProfile?.coins ?: 150L

  var selectedGameMode by remember { mutableStateOf("heads_up") }
  var selectedDeckId by remember { mutableStateOf("core_kwaito") }
  var roundDuration by remember { mutableLongStateOf(60L) }

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

  val allDecks = remember(customDeck) {
    if (customDeck != null) listOf(customDeck) + PredefinedDecks.ALL_DECKS else PredefinedDecks.ALL_DECKS
  }

  val selectedDeck = allDecks.find { it.id == selectedDeckId } ?: PredefinedDecks.CORE_KWAITO
  val isSelectedReady = selectedDeck.isFreeTier || unlockedPacks.contains(selectedDeck.id)

  val modeLabel = when (selectedGameMode) {
    "party_teams" -> "Party Teams"
    "quick_play" -> "Quick Play"
    else -> "Heads-Up"
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(StripiCanvas),
    contentPadding = PaddingValues(bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // 1. Stripi Gradient Mesh Hero Header (`nav-bar-on-mesh` + Hero + Featured Dark Deck Console)
    item {
      StripiGradientMeshHero(
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 28.dp)
      ) {
        // Top Nav Bar on Mesh (`nav-bar-on-mesh`)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          StripiSoftPillTag(text = "Mzansi Party Lobby")

          // Coin Balance & Payment Pill (`tnum` tabular figures)
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
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CreditCard,
                  contentDescription = "Payment Methods",
                  tint = StripiPrimary,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "ZAR Pay",
                  style = StripiTypeTokens.caption.copy(color = StripiInk)
                )
              }
            }

            Surface(
              shape = StripiRounded.pill,
              color = StripiBrandDark900,
              modifier = Modifier.clickable { onNavigateToStore() }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.MonetizationOn,
                  contentDescription = "Coins",
                  tint = StripiSubduedHover,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                StripiTabularText(
                  text = "$userCoins",
                  color = StripiOnPrimary,
                  fontSize = 14,
                  fontWeight = FontWeight.Normal
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

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Pick your mode, set the round timer, and start a South African charades match.",
          style = StripiTypeTokens.bodyMd.copy(color = StripiInkSecondary)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Featured Dark Active Deck Console (`card-pricing-featured` #1C1E54)
        StripiFeaturedDarkCard(
          modifier = Modifier.fillMaxWidth(),
          contentPadding = PaddingValues(24.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(StripiRounded.md)
                  .background(StripiOnPrimary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = deckIconForId(selectedDeck.id),
                  contentDescription = null,
                  tint = StripiSubduedHover,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = selectedDeck.name,
                  style = StripiTypeTokens.headingLg.copy(color = StripiOnPrimary)
                )
                Spacer(modifier = Modifier.height(4.dp))
                StripiTabularText(
                  text = "${selectedDeck.cards.size} cards · ${roundDuration}s round",
                  color = StripiSubduedHover,
                  fontSize = 14
                )
              }
            }

            Spacer(modifier = Modifier.width(8.dp))

            StripiSoftPillTag(
              text = modeLabel,
              containerColor = StripiSubduedHover,
              contentColor = StripiBrandDark900
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          StripiPrimaryPillButton(
            text = if (isSelectedReady) "Start Match" else "Unlock in Store",
            modifier = Modifier
              .fillMaxWidth()
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
                onStartGame(selectedDeck, selectedGameMode, roundDuration)
              } else {
                onNavigateToStore()
              }
            }
          )
        }
      }
    }

    // 2. Gameplay Mode & Timer Controls (No section-number prefixes per Section 9.F)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "Gameplay Mode",
          style = StripiTypeTokens.headingMd.copy(color = StripiInk)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          ModeCard(
            title = "Heads-Up",
            subtitle = "Forehead Tilt",
            icon = Icons.Default.ScreenRotation,
            isSelected = selectedGameMode == "heads_up",
            modifier = Modifier.weight(1f),
            onClick = { selectedGameMode = "heads_up" }
          )
          ModeCard(
            title = "Party Teams",
            subtitle = "Team A vs B",
            icon = Icons.Default.Group,
            isSelected = selectedGameMode == "party_teams",
            modifier = Modifier.weight(1f),
            onClick = { selectedGameMode = "party_teams" }
          )
          ModeCard(
            title = "Quick Play",
            subtitle = "Casual Table",
            icon = Icons.Default.Bolt,
            isSelected = selectedGameMode == "quick_play",
            modifier = Modifier.weight(1f),
            onClick = { selectedGameMode = "quick_play" }
          )
        }

        // Round Duration Selector on `canvas-soft`
        StripiFeatureCardLight(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = StripiCanvasSoft,
          contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Round Clock",
                style = StripiTypeTokens.headingSm.copy(color = StripiInk)
              )
              Text(
                text = "Tabular countdown",
                style = StripiTypeTokens.caption.copy(color = StripiInkMute)
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf(60L, 90L, 120L).forEach { seconds ->
                val isSelected = roundDuration == seconds
                Surface(
                  shape = StripiRounded.pill,
                  color = if (isSelected) StripiPrimary else StripiCanvas,
                  border = BorderStroke(
                    1.dp,
                    if (isSelected) StripiPrimary else StripiHairline
                  ),
                  modifier = Modifier.clickable { roundDuration = seconds }
                ) {
                  StripiTabularText(
                    text = "${seconds}s",
                    color = if (isSelected) StripiOnPrimary else StripiInk,
                    fontSize = 14,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                  )
                }
              }
            }
          }
        }
      }
    }

    // 3. Deck Catalog Section Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Mzansi Decks",
          style = StripiTypeTokens.headingMd.copy(color = StripiInk)
        )
        StripiTabularText(
          text = "${allDecks.size} Decks",
          color = StripiInkMute,
          fontSize = 14
        )
      }
    }

    // 4. Deck Cards List (Vector Icons instead of raw emojis)
    items(allDecks) { deckItem ->
      val isUnlocked = deckItem.isFreeTier || unlockedPacks.contains(deckItem.id)
      val isSelected = selectedDeckId == deckItem.id

      StripiFeatureCardLight(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        borderColor = if (isSelected) StripiPrimary else StripiHairline,
        borderWidth = if (isSelected) 1.5.dp else 1.dp,
        backgroundColor = if (isSelected) StripiCanvasSoft else StripiCanvas,
        contentPadding = PaddingValues(20.dp),
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
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            StripiDeckIconBadge(
              deckId = deckItem.id,
              isSelected = isSelected
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = deckItem.name,
                  style = StripiTypeTokens.headingSm.copy(
                    color = if (isSelected) StripiPrimary else StripiInk
                  )
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (deckItem.isFreeTier) {
                  StripiSoftPillTag(text = "Free")
                } else if (isUnlocked) {
                  StripiSoftPillTag(
                    text = "Owned",
                    containerColor = StripiSubduedHover,
                    contentColor = StripiPrimaryDeep
                  )
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = deckItem.tagLine,
                style = StripiTypeTokens.bodyMd.copy(color = StripiInkMute)
              )

              Spacer(modifier = Modifier.height(6.dp))

              StripiTabularText(
                text = "${deckItem.cards.size} cards",
                color = StripiInkSecondary,
                fontSize = 14
              )
            }
          }

          if (!isUnlocked) {
            Column(
              horizontalAlignment = Alignment.End,
              modifier = Modifier.padding(start = 10.dp)
            ) {
              Surface(
                shape = StripiRounded.pill,
                color = StripiBrandDark900
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = StripiSubduedHover,
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  StripiTabularText(
                    text = StoreCatalog.formattedZarForDeck(deckItem),
                    color = StripiOnPrimary,
                    fontSize = 13,
                    fontWeight = FontWeight.Normal
                  )
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
              StripiTabularText(
                text = "${deckItem.priceCoins} coins",
                color = StripiInkMute,
                fontSize = 12
              )
            }
          }
        }
      }
    }

    // 5. Warm Cream Band Callout (`card-cream-band` #F5E9D4)
    item {
      StripiCreamCard(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(24.dp),
        onClick = onNavigateToStudio
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Craft your own Mzansi deck",
              style = StripiTypeTokens.headingMd.copy(color = StripiInk)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Generate custom cards and 1K, 2K, or 4K card art in Studio.",
              style = StripiTypeTokens.bodyMd.copy(color = StripiInkSecondary)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          StripiSecondaryPillButton(
            text = "Open Studio",
            onClick = onNavigateToStudio
          )
        }
      }
    }
  }
}

@Composable
private fun ModeCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  StripiFeatureCardLight(
    modifier = modifier,
    borderColor = if (isSelected) StripiPrimary else StripiHairline,
    borderWidth = if (isSelected) 1.5.dp else 1.dp,
    backgroundColor = if (isSelected) StripiSubduedHover.copy(alpha = 0.22f) else StripiCanvas,
    contentPadding = PaddingValues(14.dp),
    onClick = onClick
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = if (isSelected) StripiPrimary else StripiInkMute,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.height(10.dp))
    Text(
      text = title,
      style = StripiTypeTokens.headingSm.copy(
        color = if (isSelected) StripiPrimary else StripiInk,
        fontSize = 16.sp
      )
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = subtitle,
      style = StripiTypeTokens.micro.copy(color = StripiInkMute)
    )
  }
}
