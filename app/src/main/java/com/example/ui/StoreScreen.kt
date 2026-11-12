package com.example.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentTransaction
import com.example.data.model.StoreCatalog
import com.example.data.model.UserProfile
import com.example.ui.components.StripiCreamCard
import com.example.ui.components.StripiDarkPillButton
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
import com.example.ui.theme.StripiRuby
import com.example.ui.theme.StripiSubduedHover
import com.example.ui.theme.StripiTypeTokens
import kotlinx.coroutines.delay

@Composable
fun StoreScreen(
  userProfile: UserProfile?,
  savedPaymentMethods: List<PaymentMethod> = emptyList(),
  transactions: List<PaymentTransaction> = emptyList(),
  isDailyGiftClaimable: Boolean = true,
  onAddCoins: (Long) -> Unit,
  onUnlockPack: (String, Long) -> Unit,
  onRequestZarCheckout: (CheckoutItemSpec) -> Unit = {},
  onOpenPaymentMethodsModal: () -> Unit = {},
  onClaimDailyGift: () -> Boolean = { onAddCoins(100L); true },
  onWatchRewardedAd: (((coinsEarned: Long) -> Unit) -> Unit)? = null
) {
  val userCoins = userProfile?.coins ?: 150L
  val unlockedPacks = userProfile?.unlockedPacks ?: emptyList()
  val defaultPaymentMethod = savedPaymentMethods.firstOrNull { it.isDefault } ?: savedPaymentMethods.firstOrNull()

  var isSimulatingAd by remember { mutableStateOf(false) }
  var adCountdown by remember { mutableIntStateOf(5) }
  var currentAdRewardCoins by remember { mutableLongStateOf(50L) }
  var statusMessage by remember { mutableStateOf<String?>(null) }

  // Rewarded Ad Simulation Countdown
  LaunchedEffect(isSimulatingAd) {
    if (isSimulatingAd) {
      adCountdown = 5
      while (adCountdown > 0) {
        delay(1000)
        adCountdown--
      }
    }
  }

  // Rewarded Ad Modal (Stripi Dark Featured Styling)
  if (isSimulatingAd) {
    Dialog(onDismissRequest = { if (adCountdown == 0) isSimulatingAd = false }) {
      StripiFeaturedDarkCard(
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          StripiSoftPillTag(
            text = "Sponsored Mzansi Break",
            containerColor = StripiPrimary,
            contentColor = StripiOnPrimary
          )
          if (adCountdown == 0) {
            IconButton(onClick = { isSimulatingAd = false }) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = StripiOnPrimary)
            }
          } else {
            StripiTabularText(
              text = "${adCountdown}s",
              color = StripiSubduedHover,
              fontSize = 14,
              fontWeight = FontWeight.Normal
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(StripiRounded.md)
            .background(
              Brush.linearGradient(
                listOf(StripiPrimary, StripiRuby)
              )
            )
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Tv,
              contentDescription = null,
              tint = StripiOnPrimary,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Cremora: 'It's not inside, it's on top!'",
              style = StripiTypeTokens.headingSm.copy(color = StripiOnPrimary),
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Classic 2000s South African TV Commercial Archive",
              style = StripiTypeTokens.caption.copy(
                color = StripiOnPrimary.copy(alpha = 0.85f)
              ),
              textAlign = TextAlign.Center
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (adCountdown > 0) {
          Text(
            text = "Reward unlocks in $adCountdown seconds...",
            style = StripiTypeTokens.caption.copy(color = StripiSubduedHover),
            modifier = Modifier.align(Alignment.CenterHorizontally)
          )
        } else {
          StripiPrimaryPillButton(
            text = "Claim +$currentAdRewardCoins Coins",
            modifier = Modifier
              .fillMaxWidth()
              .testTag("claim_ad_reward_button"),
            onClick = {
              onAddCoins(currentAdRewardCoins)
              isSimulatingAd = false
              statusMessage = "Ke a leboga! +$currentAdRewardCoins Gold Coins credited to your balance."
            }
          )
        }
      }
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(StripiCanvas),
    contentPadding = PaddingValues(bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // 1. Stripi Gradient Mesh Hero & Balance Telemetry
    item {
      StripiGradientMeshHero(
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 28.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          StripiSoftPillTag(text = "Spaza Store & ZAR Billing")

          Surface(
            shape = StripiRounded.pill,
            color = StripiBrandDark900
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = null,
                tint = StripiSubduedHover,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              StripiTabularText(
                text = "$userCoins Coins",
                color = StripiOnPrimary,
                fontSize = 14,
                fontWeight = FontWeight.Normal
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Transparent Mzansi pricing.",
          style = StripiTypeTokens.displayLg.copy(color = StripiInk)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Enjoy our free tier, or unlock expansion packs in South African Rand via Card, PayShap, or SnapScan.",
          style = StripiTypeTokens.bodyMd.copy(color = StripiInkSecondary)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Active Payment Method Banner Card (`card-feature-light`)
        StripiFeatureCardLight(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("manage_payment_methods_card"),
          contentPadding = PaddingValues(18.dp),
          onClick = onOpenPaymentMethodsModal
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(StripiRounded.md)
                  .background(StripiBrandDark900),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CreditCard,
                  contentDescription = null,
                  tint = StripiOnPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = if (defaultPaymentMethod != null) {
                    defaultPaymentMethod.summaryLabel()
                  } else {
                    "No ZAR payment method saved"
                  },
                  style = StripiTypeTokens.headingSm.copy(color = StripiInk)
                )
                Text(
                  text = if (defaultPaymentMethod != null) {
                    "Holder: ${defaultPaymentMethod.holderName}"
                  } else {
                    "Add Visa, Mastercard, PayShap, or SnapScan"
                  },
                  style = StripiTypeTokens.caption.copy(color = StripiInkMute)
                )
              }
            }

            Spacer(modifier = Modifier.width(8.dp))

            StripiSecondaryPillButton(
              text = if (defaultPaymentMethod != null) "Manage" else "Set Up",
              onClick = onOpenPaymentMethodsModal
            )
          }
        }
      }
    }

    if (statusMessage != null) {
      item {
        StripiCreamCard(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
          contentPadding = PaddingValues(16.dp)
        ) {
          Text(
            text = statusMessage.orEmpty(),
            style = StripiTypeTokens.bodyMd.copy(color = StripiInk)
          )
        }
      }
    }

    // 2. Featured Pricing Band (`card-pricing-featured` + 2-up `card-pricing`)
    // Single-Indigo CTA Rule: Only the featured pass uses `button-primary-pill`;
    // the 2 coin bundles use `button-secondary`!
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "ZAR Bundles & Passes",
          style = StripiTypeTokens.headingMd.copy(color = StripiInk)
        )

        // Featured Dark Tier (`card-pricing-featured` #1C1E54)
        StripiFeaturedDarkCard(
          modifier = Modifier.fillMaxWidth(),
          contentPadding = PaddingValues(28.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            StripiSoftPillTag(
              text = "Most Popular",
              containerColor = StripiSubduedHover,
              contentColor = StripiBrandDark900
            )
            StripiTabularText(
              text = "R99.00",
              color = StripiOnPrimary,
              fontSize = 26,
              fontWeight = FontWeight.Light
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "All-Access Mzansi Pass",
            style = StripiTypeTokens.headingLg.copy(color = StripiOnPrimary)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Unlocks all 4 Expansion Packs permanently plus +1,500 Gold Coins for store decks.",
            style = StripiTypeTokens.bodyMd.copy(
              color = StripiSubduedHover.copy(alpha = 0.88f)
            )
          )

          Spacer(modifier = Modifier.height(20.dp))

          StripiPrimaryPillButton(
            text = "Checkout R99.00",
            modifier = Modifier
              .fillMaxWidth()
              .testTag("buy_all_access_pass_button"),
            onClick = {
              onRequestZarCheckout(
                CheckoutItemSpec(
                  itemId = StoreCatalog.ALL_ACCESS_PASS.id,
                  title = StoreCatalog.ALL_ACCESS_PASS.title,
                  subtitle = StoreCatalog.ALL_ACCESS_PASS.subtitle,
                  amountCents = StoreCatalog.ALL_ACCESS_PASS.amountCentsZar,
                  coinsGranted = StoreCatalog.ALL_ACCESS_PASS.rewardCoins,
                  packIdToUnlock = "ALL_EXPANSIONS"
                )
              )
            }
          )
        }

        // Two-Column Standard Pricing Cards (`card-pricing` on white canvas)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          CoinStashCard(
            title = "Township Stash",
            coins = 300,
            price = "R29.99",
            modifier = Modifier.weight(1f),
            onBuy = {
              onRequestZarCheckout(
                CheckoutItemSpec(
                  itemId = StoreCatalog.COIN_BUNDLE_SMALL.id,
                  title = StoreCatalog.COIN_BUNDLE_SMALL.title,
                  subtitle = "+300 Gold Coins",
                  amountCents = StoreCatalog.COIN_BUNDLE_SMALL.amountCentsZar,
                  coinsGranted = 300L
                )
              )
            }
          )
          CoinStashCard(
            title = "Grootman Chest",
            coins = 1000,
            price = "R79.99",
            modifier = Modifier.weight(1f),
            onBuy = {
              onRequestZarCheckout(
                CheckoutItemSpec(
                  itemId = StoreCatalog.COIN_BUNDLE_LARGE.id,
                  title = StoreCatalog.COIN_BUNDLE_LARGE.title,
                  subtitle = "+1,000 Gold Coins",
                  amountCents = StoreCatalog.COIN_BUNDLE_LARGE.amountCentsZar,
                  coinsGranted = 1000L
                )
              )
            }
          )
        }
      }
    }

    // 3. Free Tier Perks (`canvas-soft` band)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Free Tier Perks",
          style = StripiTypeTokens.headingMd.copy(color = StripiInk)
        )

        StripiFeatureCardLight(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = StripiCanvasSoft,
          contentPadding = PaddingValues(20.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Nostalgic Mzansi Ad",
                  style = StripiTypeTokens.headingSm.copy(color = StripiInk)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StripiSoftPillTag(text = "+50 Coins")
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Watch a 5-second classic 2000s SA commercial to earn free coins.",
                style = StripiTypeTokens.bodyMd.copy(color = StripiInkMute)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            StripiDarkPillButton(
              text = "Watch Ad",
              modifier = Modifier.testTag("watch_ad_button"),
              onClick = {
                currentAdRewardCoins = 50L
                if (onWatchRewardedAd != null) {
                  onWatchRewardedAd { earned ->
                    onAddCoins(earned)
                    statusMessage = "Ke a leboga! +$earned Gold Coins credited for watching."
                  }
                } else {
                  isSimulatingAd = true
                }
              }
            )
          }

          HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = StripiHairline
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Daily Spaza Gift",
                  style = StripiTypeTokens.headingSm.copy(color = StripiInk)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StripiSoftPillTag(text = "+100 Coins")
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = if (isDailyGiftClaimable) "Complimentary daily coin grant for casual players."
                       else "You have claimed today's gift! Resets tomorrow.",
                style = StripiTypeTokens.bodyMd.copy(color = StripiInkMute)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            StripiSecondaryPillButton(
              text = if (isDailyGiftClaimable) "Claim Free" else "Claimed",
              enabled = isDailyGiftClaimable,
              modifier = Modifier.testTag("claim_daily_button"),
              onClick = {
                if (isDailyGiftClaimable) {
                  val claimed = onClaimDailyGift()
                  if (claimed) {
                    statusMessage = "Sharp! You claimed +100 complimentary Daily Spaza coins."
                  } else {
                    statusMessage = "You already claimed your free gift today! Come back tomorrow."
                  }
                }
              }
            )
          }
        }
      }
    }

    // 4. Individual Expansion Packs (Unlock with Coins OR Direct ZAR Payment)
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Expansion Packs",
          style = StripiTypeTokens.headingMd.copy(color = StripiInk)
        )
        Text(
          text = "Coins or ZAR",
          style = StripiTypeTokens.caption.copy(color = StripiInkMute)
        )
      }
    }

    items(StoreCatalog.EXPANSION_PACKS) { pack ->
      val isUnlocked = unlockedPacks.contains(pack.id)
      val zarFormatted = StoreCatalog.formattedZarForDeck(pack)
      val zarCents = StoreCatalog.zarCentsForDeck(pack)

      StripiFeatureCardLight(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.Top,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.weight(1f)
          ) {
            StripiDeckIconBadge(deckId = pack.id)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Text(
                text = pack.name,
                style = StripiTypeTokens.headingSm.copy(color = StripiInk)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = pack.tagLine,
                style = StripiTypeTokens.bodyMd.copy(color = StripiInkMute)
              )
              Spacer(modifier = Modifier.height(6.dp))
              StripiTabularText(
                text = "${pack.cards.size} cards · $zarFormatted",
                color = StripiInkSecondary,
                fontSize = 14
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isUnlocked) {
          Surface(
            shape = StripiRounded.pill,
            color = StripiSubduedHover.copy(alpha = 0.4f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = StripiPrimaryDeep,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Unlocked in library",
                style = StripiTypeTokens.caption.copy(color = StripiPrimaryDeep)
              )
            }
          }
        } else {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            StripiSecondaryPillButton(
              text = "${pack.priceCoins} Coins",
              modifier = Modifier
                .weight(1f)
                .testTag("unlock_pack_${pack.id}"),
              onClick = {
                if (userCoins >= pack.priceCoins) {
                  onUnlockPack(pack.id, pack.priceCoins)
                  statusMessage = "Halala! Unlocked ${pack.name} using ${pack.priceCoins} coins."
                } else {
                  statusMessage = "You need ${pack.priceCoins - userCoins} more coins, or use direct ZAR checkout."
                }
              }
            )
            StripiPrimaryPillButton(
              text = "Buy $zarFormatted",
              modifier = Modifier
                .weight(1f)
                .testTag("buy_zar_pack_${pack.id}"),
              onClick = {
                onRequestZarCheckout(
                  CheckoutItemSpec(
                    itemId = pack.id,
                    title = pack.name,
                    subtitle = "Permanent Expansion Deck Unlock",
                    amountCents = zarCents,
                    coinsGranted = 0L,
                    packIdToUnlock = pack.id
                  )
                )
              }
            )
          }
        }
      }
    }

    // 5. Recent ZAR Transactions Ledger (Tabular Numerals `tnum`)
    if (transactions.isNotEmpty()) {
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "Recent ZAR Receipts",
            style = StripiTypeTokens.headingMd.copy(color = StripiInk)
          )

          StripiFeatureCardLight(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(18.dp)
          ) {
            transactions.take(5).forEachIndexed { index, tx ->
              if (index > 0) {
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
  }
}

/**
 * `card-pricing`:
 * Background `{colors.canvas}`, rounded `{rounded.lg}`, 1dp `{colors.hairline}` border.
 * Title `{typography.heading-lg}` (22sp weight 300), price `{typography.display-md}` (26sp weight 300).
 * Uses `button-secondary` to preserve single-indigo CTA hierarchy alongside the featured pass.
 */
@Composable
private fun CoinStashCard(
  title: String,
  coins: Int,
  price: String,
  modifier: Modifier = Modifier,
  onBuy: () -> Unit
) {
  StripiFeatureCardLight(
    modifier = modifier,
    contentPadding = PaddingValues(20.dp)
  ) {
    Text(
      text = title,
      style = StripiTypeTokens.headingLg.copy(color = StripiInk)
    )
    Spacer(modifier = Modifier.height(6.dp))
    StripiTabularText(
      text = price,
      color = StripiInk,
      fontSize = 26,
      fontWeight = FontWeight.Light
    )
    Spacer(modifier = Modifier.height(4.dp))
    StripiTabularText(
      text = "+$coins Gold Coins",
      color = StripiInkMute,
      fontSize = 14
    )
    Spacer(modifier = Modifier.height(16.dp))
    StripiSecondaryPillButton(
      text = "Buy $price",
      modifier = Modifier.fillMaxWidth(),
      onClick = onBuy
    )
  }
}
