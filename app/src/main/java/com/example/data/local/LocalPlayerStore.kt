package com.example.data.local

import android.content.Context
import com.example.data.model.CustomCard
import com.example.data.model.GameSession
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentTransaction
import com.example.data.model.UserProfile
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import java.util.UUID

/**
 * Device-local persistence store backed by SharedPreferences and reactive StateFlows.
 * Ensures that Sign-In, Coins, Pack Unlocks, Saved Payment Methods, ZAR Receipts,
 * Custom Cards, and Match History work 100% reliably on a physical phone even when
 * Firebase / Firestore is offline or not yet configured with google-services.json.
 */
class LocalPlayerStore(context: Context) {

  private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  private val _activeUserId = MutableStateFlow(prefs.getString(KEY_ACTIVE_UID, null))
  val activeUserId: StateFlow<String?> = _activeUserId.asStateFlow()

  private val _userProfile = MutableStateFlow(loadProfile())
  val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

  private val _gameSessions = MutableStateFlow(loadSessions())
  val gameSessions: StateFlow<List<GameSession>> = _gameSessions.asStateFlow()

  private val _customCards = MutableStateFlow(loadCustomCards())
  val customCards: StateFlow<List<CustomCard>> = _customCards.asStateFlow()

  private val _paymentMethods = MutableStateFlow(loadPaymentMethods())
  val paymentMethods: StateFlow<List<PaymentMethod>> = _paymentMethods.asStateFlow()

  private val _transactions = MutableStateFlow(loadTransactions())
  val transactions: StateFlow<List<PaymentTransaction>> = _transactions.asStateFlow()

  fun signInLocalUser(
    uid: String = prefs.getString(KEY_SAVED_UID, null) ?: "mzansi_player_${UUID.randomUUID().toString().take(8)}",
    displayName: String = "Mzansi Star",
    email: String = "Player on Android"
  ): String {
    prefs.edit()
      .putString(KEY_ACTIVE_UID, uid)
      .putString(KEY_SAVED_UID, uid)
      .apply()
    _activeUserId.value = uid

    val current = _userProfile.value
    val updated = current.copy(
      id = uid,
      userId = uid,
      displayName = if (current.displayName.isNotBlank() && current.displayName != "Mzansi Star") current.displayName else displayName,
      email = current.email ?: email
    )
    saveProfile(updated)
    return uid
  }

  fun signOutLocalUser() {
    prefs.edit().remove(KEY_ACTIVE_UID).apply()
    _activeUserId.value = null
  }

  fun syncFromRemoteProfile(remote: UserProfile) {
    val current = _userProfile.value
    // Merge coins and unlocked packs so local progress is never lost
    val mergedPacks = (current.unlockedPacks + remote.unlockedPacks).distinct()
    val merged = remote.copy(
      coins = maxOf(current.coins, remote.coins),
      unlockedPacks = mergedPacks,
      gamesPlayed = maxOf(current.gamesPlayed, remote.gamesPlayed),
      totalCorrect = maxOf(current.totalCorrect, remote.totalCorrect),
      highScore = maxOf(current.highScore, remote.highScore)
    )
    saveProfile(merged)
  }

  fun addCoins(amount: Long) {
    val current = _userProfile.value
    val updated = current.copy(
      coins = (current.coins + amount).coerceAtLeast(0L),
      updatedAt = Timestamp.now()
    )
    saveProfile(updated)
  }

  fun unlockPack(packId: String, costCoins: Long) {
    val current = _userProfile.value
    val updatedPacks = (current.unlockedPacks + packId).distinct()
    val updatedCoins = (current.coins - costCoins).coerceAtLeast(0L)
    val updated = current.copy(
      coins = updatedCoins,
      unlockedPacks = updatedPacks,
      updatedAt = Timestamp.now()
    )
    saveProfile(updated)
  }

  fun recordGameResult(session: GameSession) {
    val correctCount = session.wordsGuessed.size.toLong()
    val earnedCoins = 10L + (correctCount * 5L)
    val current = _userProfile.value
    val updatedProfile = current.copy(
      gamesPlayed = current.gamesPlayed + 1L,
      totalCorrect = current.totalCorrect + correctCount,
      highScore = maxOf(current.highScore, maxOf(session.teamAScore, session.teamBScore)),
      coins = current.coins + earnedCoins,
      updatedAt = Timestamp.now()
    )
    saveProfile(updatedProfile)

    val newSession = session.copy(
      id = session.id.ifBlank { "session_${UUID.randomUUID().toString().take(8)}" },
      userId = current.userId.ifBlank { "local_user" },
      createdAt = session.createdAt ?: Timestamp.now()
    )
    val updatedSessions = (listOf(newSession) + _gameSessions.value).take(30)
    saveSessions(updatedSessions)
  }

  fun addCustomCard(card: CustomCard): CustomCard {
    val newCard = card.copy(
      id = card.id.ifBlank { "card_${UUID.randomUUID().toString().take(8)}" },
      userId = _userProfile.value.userId.ifBlank { "local_user" },
      term = card.term.trim(),
      category = card.category.trim().ifBlank { "Custom Mzansi" },
      culturalEra = card.culturalEra.trim().ifBlank { "Early 2000s" },
      createdAt = Timestamp.now()
    )
    val updated = listOf(newCard) + _customCards.value.filterNot { it.id == newCard.id }
    saveCustomCards(updated)
    return newCard
  }

  fun deleteCustomCard(cardId: String) {
    val updated = _customCards.value.filterNot { it.id == cardId }
    saveCustomCards(updated)
  }

  fun addPaymentMethod(method: PaymentMethod): PaymentMethod {
    val newMethod = method.copy(
      id = method.id.ifBlank { "pm_${UUID.randomUUID().toString().take(8)}" },
      userId = _userProfile.value.userId.ifBlank { "local_user" },
      brand = method.brand.ifBlank { "Visa" },
      last4 = method.last4.ifBlank { "4242" },
      holderName = method.holderName.ifBlank { "Mzansi Player" },
      isDefault = true,
      createdAt = Timestamp.now()
    )
    val existing = _paymentMethods.value.map { it.copy(isDefault = false) }
    val updated = listOf(newMethod) + existing
    savePaymentMethods(updated)
    return newMethod
  }

  fun setDefaultPaymentMethod(methodId: String) {
    val updated = _paymentMethods.value.map { it.copy(isDefault = it.id == methodId) }
    savePaymentMethods(updated)
  }

  fun deletePaymentMethod(methodId: String) {
    val remaining = _paymentMethods.value.filterNot { it.id == methodId }
    val updated = if (remaining.isNotEmpty() && remaining.none { it.isDefault }) {
      remaining.mapIndexed { idx, pm -> pm.copy(isDefault = idx == 0) }
    } else {
      remaining
    }
    savePaymentMethods(updated)
  }

  fun recordTransaction(
    itemId: String,
    itemTitle: String,
    amountCents: Long,
    paymentMethod: PaymentMethod,
    coinsGranted: Long,
    packUnlocked: String?
  ): PaymentTransaction {
    val tx = PaymentTransaction(
      id = "tx_${UUID.randomUUID().toString().take(8)}",
      userId = _userProfile.value.userId.ifBlank { "local_user" },
      itemId = itemId,
      itemTitle = itemTitle,
      amountCents = amountCents,
      currency = "ZAR",
      paymentMethodId = paymentMethod.id.ifBlank { "local_pm" },
      paymentMethodSummary = paymentMethod.summaryLabel(),
      coinsGranted = coinsGranted,
      packUnlocked = packUnlocked,
      status = "succeeded",
      createdAt = Timestamp.now()
    )
    val updated = (listOf(tx) + _transactions.value).take(30)
    saveTransactions(updated)
    return tx
  }

  private fun loadProfile(): UserProfile {
    val raw = prefs.getString(KEY_PROFILE_JSON, null)
    val defaultPacks = listOf("core_kwaito", "core_slang", "core_2000s_tv", "core_tuckshop", "core_sports")
    if (raw.isNullOrBlank()) {
      return UserProfile(
        id = "local_user",
        userId = "local_user",
        displayName = "Mzansi Star",
        email = "Signed in on device",
        coins = 150L,
        unlockedPacks = defaultPacks
      )
    }
    return try {
      val obj = JSONObject(raw)
      val packsArr = obj.optJSONArray("unlockedPacks")
      val packs = if (packsArr != null) {
        (0 until packsArr.length()).map { packsArr.getString(it) }
      } else defaultPacks
      UserProfile(
        id = obj.optString("id", "local_user"),
        userId = obj.optString("userId", "local_user"),
        displayName = obj.optString("displayName", "Mzansi Star"),
        email = obj.optString("email", "Signed in on device"),
        coins = obj.optLong("coins", 150L),
        unlockedPacks = (defaultPacks + packs).distinct(),
        gamesPlayed = obj.optLong("gamesPlayed", 0L),
        totalCorrect = obj.optLong("totalCorrect", 0L),
        highScore = obj.optLong("highScore", 0L)
      )
    } catch (_: Exception) {
      UserProfile(
        id = "local_user",
        userId = "local_user",
        displayName = "Mzansi Star",
        email = "Signed in on device",
        coins = 150L,
        unlockedPacks = defaultPacks
      )
    }
  }

  private fun saveProfile(profile: UserProfile) {
    _userProfile.value = profile
    val obj = JSONObject().apply {
      put("id", profile.id)
      put("userId", profile.userId)
      put("displayName", profile.displayName)
      put("email", profile.email ?: "")
      put("coins", profile.coins)
      put("unlockedPacks", JSONArray(profile.unlockedPacks))
      put("gamesPlayed", profile.gamesPlayed)
      put("totalCorrect", profile.totalCorrect)
      put("highScore", profile.highScore)
    }
    prefs.edit().putString(KEY_PROFILE_JSON, obj.toString()).apply()
  }

  private fun loadSessions(): List<GameSession> {
    val raw = prefs.getString(KEY_SESSIONS_JSON, null) ?: return emptyList()
    return try {
      val arr = JSONArray(raw)
      (0 until arr.length()).map { idx ->
        val obj = arr.getJSONObject(idx)
        val guessedArr = obj.optJSONArray("wordsGuessed") ?: JSONArray()
        val passedArr = obj.optJSONArray("wordsPassed") ?: JSONArray()
        val epochMs = obj.optLong("createdAtMs", System.currentTimeMillis())
        GameSession(
          id = obj.optString("id"),
          userId = obj.optString("userId"),
          deckId = obj.optString("deckId"),
          deckName = obj.optString("deckName"),
          gameMode = obj.optString("gameMode", "heads_up"),
          teamAScore = obj.optLong("teamAScore", 0L),
          teamBScore = obj.optLong("teamBScore", 0L),
          wordsGuessed = (0 until guessedArr.length()).map { guessedArr.getString(it) },
          wordsPassed = (0 until passedArr.length()).map { passedArr.getString(it) },
          durationSeconds = obj.optLong("durationSeconds", 60L),
          createdAt = Timestamp(Date(epochMs))
        )
      }
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun saveSessions(sessions: List<GameSession>) {
    _gameSessions.value = sessions
    val arr = JSONArray()
    sessions.forEach { s ->
      arr.put(
        JSONObject().apply {
          put("id", s.id)
          put("userId", s.userId)
          put("deckId", s.deckId)
          put("deckName", s.deckName)
          put("gameMode", s.gameMode)
          put("teamAScore", s.teamAScore)
          put("teamBScore", s.teamBScore)
          put("wordsGuessed", JSONArray(s.wordsGuessed))
          put("wordsPassed", JSONArray(s.wordsPassed))
          put("durationSeconds", s.durationSeconds)
          put("createdAtMs", s.createdAt?.toDate()?.time ?: System.currentTimeMillis())
        }
      )
    }
    prefs.edit().putString(KEY_SESSIONS_JSON, arr.toString()).apply()
  }

  private fun loadCustomCards(): List<CustomCard> {
    val raw = prefs.getString(KEY_CARDS_JSON, null) ?: return emptyList()
    return try {
      val arr = JSONArray(raw)
      (0 until arr.length()).map { idx ->
        val obj = arr.getJSONObject(idx)
        val hintsArr = obj.optJSONArray("hints") ?: JSONArray()
        CustomCard(
          id = obj.optString("id"),
          userId = obj.optString("userId"),
          term = obj.optString("term"),
          category = obj.optString("category"),
          hints = (0 until hintsArr.length()).map { hintsArr.getString(it) },
          culturalEra = obj.optString("culturalEra", "Early 2000s")
        )
      }
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun saveCustomCards(cards: List<CustomCard>) {
    _customCards.value = cards
    val arr = JSONArray()
    cards.forEach { c ->
      arr.put(
        JSONObject().apply {
          put("id", c.id)
          put("userId", c.userId)
          put("term", c.term)
          put("category", c.category)
          put("hints", JSONArray(c.hints))
          put("culturalEra", c.culturalEra)
        }
      )
    }
    prefs.edit().putString(KEY_CARDS_JSON, arr.toString()).apply()
  }

  private fun loadPaymentMethods(): List<PaymentMethod> {
    val raw = prefs.getString(KEY_METHODS_JSON, null) ?: return emptyList()
    return try {
      val arr = JSONArray(raw)
      (0 until arr.length()).map { idx ->
        val obj = arr.getJSONObject(idx)
        PaymentMethod(
          id = obj.optString("id"),
          userId = obj.optString("userId"),
          type = obj.optString("type", "card"),
          brand = obj.optString("brand", "Visa"),
          last4 = obj.optString("last4", "4242"),
          expiryMonth = obj.optLong("expiryMonth", 8L),
          expiryYear = obj.optLong("expiryYear", 2029L),
          holderName = obj.optString("holderName", "Mzansi Player"),
          isDefault = obj.optBoolean("isDefault", false)
        )
      }
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun savePaymentMethods(methods: List<PaymentMethod>) {
    _paymentMethods.value = methods
    val arr = JSONArray()
    methods.forEach { m ->
      arr.put(
        JSONObject().apply {
          put("id", m.id)
          put("userId", m.userId)
          put("type", m.type)
          put("brand", m.brand)
          put("last4", m.last4)
          put("expiryMonth", m.expiryMonth)
          put("expiryYear", m.expiryYear)
          put("holderName", m.holderName)
          put("isDefault", m.isDefault)
        }
      )
    }
    prefs.edit().putString(KEY_METHODS_JSON, arr.toString()).apply()
  }

  private fun loadTransactions(): List<PaymentTransaction> {
    val raw = prefs.getString(KEY_TX_JSON, null) ?: return emptyList()
    return try {
      val arr = JSONArray(raw)
      (0 until arr.length()).map { idx ->
        val obj = arr.getJSONObject(idx)
        PaymentTransaction(
          id = obj.optString("id"),
          userId = obj.optString("userId"),
          itemId = obj.optString("itemId"),
          itemTitle = obj.optString("itemTitle"),
          amountCents = obj.optLong("amountCents", 0L),
          currency = obj.optString("currency", "ZAR"),
          paymentMethodId = obj.optString("paymentMethodId"),
          paymentMethodSummary = obj.optString("paymentMethodSummary"),
          coinsGranted = obj.optLong("coinsGranted", 0L),
          packUnlocked = obj.optString("packUnlocked").ifBlank { null },
          status = obj.optString("status", "succeeded")
        )
      }
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun saveTransactions(txs: List<PaymentTransaction>) {
    _transactions.value = txs
    val arr = JSONArray()
    txs.forEach { tx ->
      arr.put(
        JSONObject().apply {
          put("id", tx.id)
          put("userId", tx.userId)
          put("itemId", tx.itemId)
          put("itemTitle", tx.itemTitle)
          put("amountCents", tx.amountCents)
          put("currency", tx.currency)
          put("paymentMethodId", tx.paymentMethodId)
          put("paymentMethodSummary", tx.paymentMethodSummary)
          put("coinsGranted", tx.coinsGranted)
          put("packUnlocked", tx.packUnlocked ?: "")
          put("status", tx.status)
        }
      )
    }
    prefs.edit().putString(KEY_TX_JSON, arr.toString()).apply()
  }

  companion object {
    private const val PREFS_NAME = "mzansi_charades_local_store"
    private const val KEY_ACTIVE_UID = "active_uid"
    private const val KEY_SAVED_UID = "saved_uid"
    private const val KEY_PROFILE_JSON = "profile_json"
    private const val KEY_SESSIONS_JSON = "sessions_json"
    private const val KEY_CARDS_JSON = "cards_json"
    private const val KEY_METHODS_JSON = "methods_json"
    private const val KEY_TX_JSON = "transactions_json"

    @Volatile
    private var instance: LocalPlayerStore? = null

    fun getInstance(context: Context): LocalPlayerStore {
      return instance ?: synchronized(this) {
        instance ?: LocalPlayerStore(context.applicationContext).also { instance = it }
      }
    }
  }
}
