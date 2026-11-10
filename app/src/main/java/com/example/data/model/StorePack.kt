package com.example.data.model

data class StoreItem(
  val id: String,
  val title: String,
  val subtitle: String,
  val emoji: String,
  val costCoins: Long,
  val realPriceFormatted: String,
  val amountCentsZar: Long = 0L,
  val rewardCoins: Long = 0L,
  val isAdReward: Boolean = false,
  val isCoinBundle: Boolean = false,
  val deckId: String? = null
)

object StoreCatalog {
  val AD_REWARD = StoreItem(
    id = "ad_reward_50",
    title = "Watch Mzansi Ad",
    subtitle = "Support the free tier and earn +50 gold coins instantly",
    emoji = "🎬",
    costCoins = 0,
    realPriceFormatted = "R0.00",
    amountCentsZar = 0L,
    rewardCoins = 50,
    isAdReward = true
  )

  val DAILY_TUCKSHOP_BONUS = StoreItem(
    id = "daily_bonus",
    title = "Daily Tuckshop Treat",
    subtitle = "Complimentary daily gift from the corner Spaza shop",
    emoji = "🎁",
    costCoins = 0,
    realPriceFormatted = "R0.00",
    amountCentsZar = 0L,
    rewardCoins = 100,
    isAdReward = false
  )

  val COIN_BUNDLE_SMALL = StoreItem(
    id = "coins_300",
    title = "Township Stash",
    subtitle = "300 Gold Coins to unlock 2 to 3 Expansion Decks",
    emoji = "🪙",
    costCoins = 0,
    realPriceFormatted = "R29.99",
    amountCentsZar = 2999L,
    rewardCoins = 300,
    isCoinBundle = true
  )

  val COIN_BUNDLE_LARGE = StoreItem(
    id = "coins_1000",
    title = "Grootman Chest",
    subtitle = "1,000 Gold Coins plus priority AI Deck Studio credits",
    emoji = "💰",
    costCoins = 0,
    realPriceFormatted = "R79.99",
    amountCentsZar = 7999L,
    rewardCoins = 1000,
    isCoinBundle = true
  )

  val ALL_ACCESS_PASS = StoreItem(
    id = "mzansi_all_access",
    title = "All-Access Mzansi Pass",
    subtitle = "Unlocks all 4 Expansion Packs + 1,500 bonus Gold Coins",
    emoji = "👑",
    costCoins = 0,
    realPriceFormatted = "R99.00",
    amountCentsZar = 9900L,
    rewardCoins = 1500,
    isCoinBundle = true
  )

  val EXPANSION_PACKS = listOf(
    PredefinedDecks.EXP_PANTSULA_LIFE,
    PredefinedDecks.EXP_SCHOOL_DAYS,
    PredefinedDecks.EXP_ROAD_TRIP,
    PredefinedDecks.EXP_MEMES
  )

  fun zarCentsForDeck(deck: Deck): Long {
    return if (deck.priceCoins <= 100L) 1999L else 2499L
  }

  fun formattedZarForDeck(deck: Deck): String {
    val cents = zarCentsForDeck(deck)
    return String.format("R%d.%02d", cents / 100, cents % 100)
  }
}
