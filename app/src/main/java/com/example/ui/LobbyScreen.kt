package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.components.StripiFeatureCardLight
import com.example.ui.components.StripiFeaturedDarkCard
import com.example.ui.components.StripiGradientMeshHero
import com.example.ui.components.StripiPrimaryPillButton
import com.example.ui.components.StripiSecondaryPillButton
import com.example.ui.components.StripiSoftPillTag
import com.example.ui.components.StripiTabularText
import com.example.ui.theme.StripiBrandDark900
import com.example.ui.theme.StripiCanvas
import com.example.ui.theme.StripiCanvasSoft
import com.example.ui.theme.StripiEmerald
import com.example.ui.theme.StripiHairline
import com.example.ui.theme.StripiInk
import com.example.ui.theme.StripiInkMute
import com.example.ui.theme.StripiInkSecondary
import com.example.ui.theme.StripiLemon
import com.example.ui.theme.StripiOnPrimary
import com.example.ui.theme.StripiPrimary
import com.example.ui.theme.StripiSubduedHover

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
        iconEmoji = "✨",
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

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(StripiCanvas),
    contentPadding = PaddingValues(bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Stripi Gradient Mesh Hero Header
    item {
      StripiGradientMeshHero(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          StripiSoftPillTag(text = "Mzansi Party Lobby")

          // Coin Balance & Payment Pill
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(percent = 50),
              color = StripiCanvas,
              border = androidx.compose.foundation.BorderStroke(1.dp, StripiHairline),
              modifier = Modifier
                .clickable { onOpenPaymentSetup() }
                .testTag("lobby_payment_methods_pill")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CreditCard,
                  contentDescription = "Payment Methods",
                  tint = StripiPrimary,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "ZAR Pay",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = StripiInk,
                    fontWeight = FontWeight.Medium
                  )
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(percent = 50),
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
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                StripiTabularText(
                  text = "$userCoins",
                  color = StripiOnPrimary,
                  fontSize = 14,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Sanibonani, ${userProfile?.displayName?.split(" ")?.firstOrNull() ?: "Mzansi"}.",
          style = MaterialTheme.typography.displayMedium.copy(
            color = StripiInk,
            fontWeight = FontWeight.Light
          )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Select your gameplay mode, set the round clock, and launch a South African charades session.",
          style = MaterialTheme.typography.bodyMedium.copy(color = StripiInkSecondary)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Featured Active Deck Summary Card inside Hero
        StripiFeaturedDarkCard(
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "SELECTED DECK",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = StripiSubduedHover,
                  letterSpacing = 0.8.sp
                )
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "${selectedDeck.iconEmoji} ${selectedDeck.name}",
                style = MaterialTheme.typography.titleLarge.copy(
                  color = StripiOnPrimary,
                  fontWeight = FontWeight.Normal
                )
              )
              Text(
                text = "${selectedDeck.cards.size} cards • ${roundDuration}s round • ${selectedGameMode.replace("_", " ").uppercase()}",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = StripiSubduedHover.copy(alpha = 0.85f)
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          StripiPrimaryPillButton(
            text = if (isSelectedReady) {
              "Start Mzansi Match (${selectedDeck.name})"
            } else {
              "Unlock ${selectedDeck.name} in Store"
            },
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

    // 2. Gameplay Mode & Timer Controls
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "1. Gameplay Mode",
          style = MaterialTheme.typography.titleMedium.copy(
            color = StripiInk,
            fontWeight = FontWeight.Medium
          )
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

        // Round Duration Selector
        StripiFeatureCardLight(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = StripiCanvasSoft,
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "2. Round Clock",
                style = MaterialTheme.typography.titleSmall.copy(
                  color = StripiInk,
                  fontWeight = FontWeight.Medium
                )
              )
              Text(
                text = "Tabular countdown timer",
                style = MaterialTheme.typography.bodySmall.copy(color = StripiInkMute)
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf(60L, 90L, 120L).forEach { seconds ->
                val isSelected = roundDuration == seconds
                Surface(
                  shape = RoundedCornerShape(percent = 50),
                  color = if (isSelected) StripiPrimary else StripiCanvas,
                  border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) StripiPrimary else StripiHairline
                  ),
                  modifier = Modifier.clickable { roundDuration = seconds }
                ) {
                  StripiTabularText(
                    text = "${seconds}s",
                    color = if (isSelected) StripiOnPrimary else StripiInk,
                    fontSize = 13,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
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
          .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "3. Choose Mzansi Deck",
          style = MaterialTheme.typography.titleMedium.copy(
            color = StripiInk,
            fontWeight = FontWeight.Medium
          )
        )
        StripiTabularText(
          text = "${allDecks.size} Decks",
          color = StripiInkMute,
          fontSize = 13
        )
      }
    }

    // 4. Deck Cards List
    items(allDecks) { deckItem ->
      val isUnlocked = deckItem.isFreeTier || unlockedPacks.contains(deckItem.id)
      val isSelected = selectedDeckId == deckItem.id

      StripiFeatureCardLight(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp),
        borderColor = if (isSelected) StripiPrimary else StripiHairline,
        borderWidth = if (isSelected) 1.5.dp else 1.dp,
        backgroundColor = if (isSelected) StripiCanvasSoft else StripiCanvas,
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
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                  if (isSelected) StripiSubduedHover.copy(alpha = 0.45f)
                  else StripiCanvasSoft
                )
                .border(1.dp, StripiHairline, RoundedCornerShape(10.dp)),
              contentAlignment = Alignment.Center
            ) {
              Text(text = deckItem.iconEmoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = deckItem.name,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) StripiPrimary else StripiInk
                  )
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (deckItem.isFreeTier) {
                  StripiSoftPillTag(
                    text = "Free Tier"
                  )
                } else if (isUnlocked) {
                  StripiSoftPillTag(
                    text = "Owned",
                    containerColor = StripiEmerald.copy(alpha = 0.15f),
                    contentColor = StripiEmerald
                  )
                }
              }

              Spacer(modifier = Modifier.height(2.dp))

              Text(
                text = deckItem.tagLine,
                style = MaterialTheme.typography.bodySmall.copy(color = StripiInkMute)
              )

              Spacer(modifier = Modifier.height(4.dp))

              StripiTabularText(
                text = "${deckItem.cards.size} cards",
                color = StripiInkSecondary,
                fontSize = 12,
                fontWeight = FontWeight.Medium
              )
            }
          }

          if (!isUnlocked) {
            Column(
              horizontalAlignment = Alignment.End,
              modifier = Modifier.padding(start = 8.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(percent = 50),
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
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  StripiTabularText(
                    text = StoreCatalog.formattedZarForDeck(deckItem),
                    color = StripiOnPrimary,
                    fontSize = 12,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              StripiTabularText(
                text = "or ${deckItem.priceCoins} coins",
                color = StripiInkMute,
                fontSize = 11
              )
            }
          }
        }
      }
    }

    // 5. Editorial Cream Callout for Custom Studio
    item {
      StripiCreamCard(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp),
        onClick = onNavigateToStudio
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Craft your own Mzansi deck with Gemini",
              style = MaterialTheme.typography.titleMedium.copy(
                color = StripiInk,
                fontWeight = FontWeight.Medium
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Open the Studio tab to generate custom cards and 1K, 2K, or 4K artwork.",
              style = MaterialTheme.typography.bodySmall.copy(color = StripiInkSecondary)
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
    contentPadding = PaddingValues(12.dp),
    onClick = onClick
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = if (isSelected) StripiPrimary else StripiInkMute,
      modifier = Modifier.size(22.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall.copy(
        fontWeight = FontWeight.Medium,
        color = if (isSelected) StripiPrimary else StripiInk
      )
    )
    Text(
      text = subtitle,
      style = MaterialTheme.typography.labelSmall.copy(color = StripiInkMute)
    )
  }
}
