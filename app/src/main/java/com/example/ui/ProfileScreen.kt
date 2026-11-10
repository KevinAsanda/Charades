package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.GameSession
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentTransaction
import com.example.data.model.UserProfile
import com.example.ui.components.StripiCreamCard
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
import com.example.ui.theme.StripiSubduedHover
import com.example.ui.theme.StripiTypeTokens
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProfileScreen(
  userProfile: UserProfile?,
  gameSessions: List<GameSession>,
  savedPaymentMethods: List<PaymentMethod> = emptyList(),
  transactions: List<PaymentTransaction> = emptyList(),
  onOpenPaymentMethodsModal: () -> Unit = {},
  onSignOut: () -> Unit
) {
  val displayName = userProfile?.displayName?.ifBlank { "Mzansi Star" } ?: "Mzansi Star"
  val email = userProfile?.email ?: "Signed in with Google"
  val gamesPlayed = userProfile?.gamesPlayed ?: 0L
  val totalCorrect = userProfile?.totalCorrect ?: 0L
  val highScore = userProfile?.highScore ?: 0L
  val coins = userProfile?.coins ?: 150L

  val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(StripiCanvas),
    contentPadding = PaddingValues(bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // 1. Stripi Gradient Mesh Hero Profile Header
    item {
      StripiGradientMeshHero(
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 28.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(StripiBrandDark900),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = StripiOnPrimary,
              modifier = Modifier.size(32.dp)
            )
          }

          Spacer(modifier = Modifier.width(16.dp))

          Column(modifier = Modifier.weight(1f)) {
            StripiSoftPillTag(text = "Verified Mzansi Account")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = displayName,
              style = StripiTypeTokens.displayMd.copy(color = StripiInk)
            )
            Text(
              text = email,
              style = StripiTypeTokens.caption.copy(color = StripiInkSecondary)
            )
          }
        }
      }
    }

    // 2. Career Telemetry Grid (Tabular Numerals `tnum`)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Career Telemetry",
          style = StripiTypeTokens.headingMd.copy(color = StripiInk)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          StatBox(
            label = "High Score",
            value = "$highScore",
            icon = Icons.Default.EmojiEvents,
            modifier = Modifier.weight(1f)
          )
          StatBox(
            label = "Words Guessed",
            value = "$totalCorrect",
            icon = Icons.Default.Star,
            modifier = Modifier.weight(1f)
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          StatBox(
            label = "Matches Played",
            value = "$gamesPlayed",
            icon = Icons.Default.SportsEsports,
            modifier = Modifier.weight(1f)
          )
          StatBox(
            label = "Gold Coins",
            value = "$coins",
            icon = Icons.Default.MonetizationOn,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // 3. Payment Methods & ZAR Billing Section
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
            text = "Saved Payment Methods",
            style = StripiTypeTokens.headingMd.copy(color = StripiInk)
          )
          StripiTabularText(
            text = "${savedPaymentMethods.size} Active",
            color = StripiInkMute,
            fontSize = 13
          )
        }

        StripiFeaturedDarkCard(
          modifier = Modifier.fillMaxWidth(),
          contentPadding = PaddingValues(24.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              if (savedPaymentMethods.isEmpty()) {
                Text(
                  text = "No saved cards or wallets yet",
                  style = StripiTypeTokens.headingSm.copy(color = StripiOnPrimary)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Add a Visa, Mastercard, PayShap ID, or SnapScan wallet.",
                  style = StripiTypeTokens.bodyMd.copy(
                    color = StripiSubduedHover.copy(alpha = 0.85f)
                  )
                )
              } else {
                savedPaymentMethods.take(2).forEach { pm ->
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 3.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.CreditCard,
                      contentDescription = null,
                      tint = StripiSubduedHover,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StripiTabularText(
                      text = pm.summaryLabel(),
                      color = StripiOnPrimary,
                      fontSize = 14,
                      fontWeight = FontWeight.Normal
                    )
                    if (pm.isDefault) {
                      Spacer(modifier = Modifier.width(8.dp))
                      StripiSoftPillTag(text = "Default")
                    }
                  }
                }
              }
            }
            Spacer(modifier = Modifier.width(12.dp))
            StripiPrimaryPillButton(
              text = if (savedPaymentMethods.isEmpty()) "Add Method" else "Manage",
              modifier = Modifier.testTag("profile_manage_payments_button"),
              onClick = onOpenPaymentMethodsModal
            )
          }
        }
      }
    }

    // 4. ZAR Transaction History Table (if any)
    if (transactions.isNotEmpty()) {
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "ZAR Purchase Ledger",
            style = StripiTypeTokens.headingMd.copy(color = StripiInk)
          )

          StripiFeatureCardLight(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(18.dp)
          ) {
            transactions.take(6).forEachIndexed { idx, tx ->
              if (idx > 0) {
                HorizontalDivider(
                  modifier = Modifier.padding(vertical = 10.dp),
                  color = StripiHairline
                )
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = tx.itemTitle,
                    style = StripiTypeTokens.bodyMd.copy(color = StripiInk)
                  )
                  Text(
                    text = tx.paymentMethodSummary,
                    style = StripiTypeTokens.caption.copy(color = StripiInkMute)
                  )
                }
                StripiTabularText(
                  text = tx.formattedAmountZar(),
                  color = StripiInk,
                  fontSize = 15,
                  fontWeight = FontWeight.Normal
                )
              }
            }
          }
        }
      }
    }

    // 5. Match History Section
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Match History",
          style = StripiTypeTokens.headingMd.copy(color = StripiInk)
        )
        StripiTabularText(
          text = "${gameSessions.size} Recorded",
          color = StripiInkMute,
          fontSize = 13
        )
      }
    }

    if (gameSessions.isEmpty()) {
      item {
        StripiCreamCard(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = null,
              tint = StripiInk,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "No matches recorded yet",
                style = StripiTypeTokens.headingSm.copy(color = StripiInk)
              )
              Text(
                text = "Complete a round of Heads-Up or Party Teams to log your match telemetry.",
                style = StripiTypeTokens.bodyMd.copy(color = StripiInkSecondary)
              )
            }
          }
        }
      }
    }

    items(gameSessions) { session ->
      StripiFeatureCardLight(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(18.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = session.deckName,
              style = StripiTypeTokens.headingSm.copy(color = StripiInk)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "${session.gameMode.replace("_", " ").uppercase()} · ${session.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "Recent"}",
              style = StripiTypeTokens.caption.copy(color = StripiInkMute)
            )
            if (session.wordsGuessed.isNotEmpty()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Guessed: ${session.wordsGuessed.take(3).joinToString(", ")}${if (session.wordsGuessed.size > 3) "..." else ""}",
                style = StripiTypeTokens.bodyMd.copy(color = StripiInkSecondary)
              )
            }
          }

          Column(horizontalAlignment = Alignment.End) {
            StripiTabularText(
              text = "${session.teamAScore}",
              color = StripiPrimary,
              fontSize = 22,
              fontWeight = FontWeight.Light
            )
            Text(
              text = "Points",
              style = StripiTypeTokens.micro.copy(color = StripiInkMute)
            )
          }
        }
      }
    }

    // 6. Sign Out Pill Button (`button-secondary`)
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 8.dp)
      ) {
        StripiSecondaryPillButton(
          text = "Sign Out",
          modifier = Modifier
            .fillMaxWidth()
            .testTag("sign_out_button"),
          leadingIcon = {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ExitToApp,
              contentDescription = null,
              tint = StripiPrimary,
              modifier = Modifier.size(18.dp)
            )
          },
          onClick = onSignOut
        )
      }
    }
  }
}

@Composable
private fun StatBox(
  label: String,
  value: String,
  icon: ImageVector,
  modifier: Modifier = Modifier
) {
  StripiFeatureCardLight(
    modifier = modifier,
    backgroundColor = StripiCanvasSoft,
    contentPadding = PaddingValues(16.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(StripiRounded.md)
          .background(StripiSubduedHover.copy(alpha = 0.35f))
          .border(1.dp, StripiHairline, StripiRounded.md),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = StripiPrimary,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        StripiTabularText(
          text = value,
          color = StripiInk,
          fontSize = 22,
          fontWeight = FontWeight.Light
        )
        Text(
          text = label,
          style = StripiTypeTokens.caption.copy(color = StripiInkMute)
        )
      }
    }
  }
}
