package com.example.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.LocalPlayerStore
import com.example.data.model.Deck
import com.example.data.model.StoreCatalog
import com.example.data.repository.CustomCardRepository
import com.example.data.repository.GameSessionRepository
import com.example.data.repository.PaymentRepository
import com.example.data.repository.UserRepository
import com.example.ui.theme.StripiCanvas
import com.example.ui.theme.StripiHairline
import com.example.ui.theme.StripiInk
import com.example.ui.theme.StripiInkMute2
import com.example.ui.theme.StripiPrimary
import com.example.ui.theme.StripiSubduedHover
import com.example.ui.theme.StripiTypeTokens
import com.example.util.AdMobManager
import com.example.util.GooglePlayBillingManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@Composable
fun MainFeatureScreen(
  currentUserId: String
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  val credentialManager = remember { CredentialManager.create(context) }

  val localStore = remember { LocalPlayerStore.getInstance(context) }
  val activity = context as? Activity

  // Google Play Billing Manager (1-tap payments, airtime billing, zero monthly fees)
  val billingManager = remember {
    GooglePlayBillingManager(
      context = context,
      coroutineScope = coroutineScope,
      onPurchaseCompleted = { productId, _ ->
        coroutineScope.launch {
          when (productId) {
            StoreCatalog.ALL_ACCESS_PASS.id -> {
              localStore.addCoins(StoreCatalog.ALL_ACCESS_PASS.rewardCoins)
              StoreCatalog.EXPANSION_PACKS.forEach { pack ->
                localStore.unlockPack(pack.id, 0L)
              }
              snackbarHostState.showSnackbar("Halala! All-Access Mzansi Pass unlocked via Google Play!")
            }
            StoreCatalog.COIN_BUNDLE_SMALL.id -> {
              localStore.addCoins(StoreCatalog.COIN_BUNDLE_SMALL.rewardCoins)
              snackbarHostState.showSnackbar("+300 Coins added via Google Play!")
            }
            StoreCatalog.COIN_BUNDLE_LARGE.id -> {
              localStore.addCoins(StoreCatalog.COIN_BUNDLE_LARGE.rewardCoins)
              snackbarHostState.showSnackbar("+1,000 Coins added via Google Play!")
            }
            else -> {
              localStore.unlockPack(productId, 0L)
              snackbarHostState.showSnackbar("Pack unlocked via Google Play!")
            }
          }
        }
      }
    )
  }
  DisposableEffect(Unit) {
    onDispose { billingManager.destroy() }
  }

  // Lazily and safely create cloud repositories so missing google-services.json never crashes the app
  val userRepository = remember { runCatching { UserRepository(context) }.getOrNull() }
  val gameSessionRepository = remember { runCatching { GameSessionRepository(context) }.getOrNull() }
  val customCardRepository = remember { runCatching { CustomCardRepository(context) }.getOrNull() }
  val paymentRepository = remember { runCatching { PaymentRepository(context) }.getOrNull() }

  // Observe local device state first for instant responsiveness offline & online
  val localProfile by localStore.userProfile.collectAsStateWithLifecycle()
  val localSessions by localStore.gameSessions.collectAsStateWithLifecycle()
  val localCustomCards by localStore.customCards.collectAsStateWithLifecycle()
  val localPaymentMethods by localStore.paymentMethods.collectAsStateWithLifecycle()
  val localTransactions by localStore.transactions.collectAsStateWithLifecycle()
  val lastDailyClaim by localStore.lastDailyClaimTimestamp.collectAsStateWithLifecycle()
  val isDailyGiftClaimable = remember(lastDailyClaim) { localStore.isDailyGiftClaimable() }

  // Also keep track of any remote items if Firestore is active
  var remoteSessions by remember { mutableStateOf<List<com.example.data.model.GameSession>>(emptyList()) }
  var remoteCustomCards by remember { mutableStateOf<List<com.example.data.model.CustomCard>>(emptyList()) }
  var remotePaymentMethods by remember { mutableStateOf<List<com.example.data.model.PaymentMethod>>(emptyList()) }
  var remoteTransactions by remember { mutableStateOf<List<com.example.data.model.PaymentTransaction>>(emptyList()) }

  // Initialize local + cloud profile and subscribe safely to Firestore streams when available
  LaunchedEffect(currentUserId) {
    val authUser = runCatching { Firebase.auth.currentUser }.getOrNull()
    val displayName = authUser?.displayName.orEmpty().ifBlank { localProfile.displayName.ifBlank { "Mzansi Star" } }
    val email = authUser?.email ?: localProfile.email ?: "Player on Android"

    localStore.signInLocalUser(
      uid = currentUserId,
      displayName = displayName,
      email = email
    )

    if (authUser != null && userRepository != null) {
      runCatching {
        userRepository.getOrCreateProfile(
          userId = currentUserId,
          displayName = displayName,
          email = email,
          photoUrl = authUser.photoUrl?.toString()
        )
      }
    }

    launch {
      runCatching {
        userRepository?.observeUserProfile(currentUserId)
          ?.catch { }
          ?.collect { remote ->
            if (remote != null) {
              localStore.syncFromRemoteProfile(remote)
            }
          }
      }
    }

    launch {
      runCatching {
        gameSessionRepository?.observeSessions(currentUserId)
          ?.catch { }
          ?.collect { sessions -> remoteSessions = sessions }
      }
    }

    launch {
      runCatching {
        customCardRepository?.observeCards(currentUserId)
          ?.catch { }
          ?.collect { cards -> remoteCustomCards = cards }
      }
    }

    launch {
      runCatching {
        paymentRepository?.observePaymentMethods(currentUserId)
          ?.catch { }
          ?.collect { methods -> remotePaymentMethods = methods }
      }
    }

    launch {
      runCatching {
        paymentRepository?.observeTransactions(currentUserId)
          ?.catch { }
          ?.collect { txs -> remoteTransactions = txs }
      }
    }
  }

  val userProfile = localProfile
  val gameSessions = remember(localSessions, remoteSessions) {
    (localSessions + remoteSessions).distinctBy { it.id.ifBlank { "${it.deckId}_${it.teamAScore}_${it.teamBScore}" } }
  }
  val customCards = remember(localCustomCards, remoteCustomCards) {
    (localCustomCards + remoteCustomCards).distinctBy { it.id.ifBlank { it.term.lowercase() } }
  }
  val savedPaymentMethods = remember(localPaymentMethods, remotePaymentMethods) {
    (localPaymentMethods + remotePaymentMethods).distinctBy { "${it.brand}_${it.last4}_${it.holderName}" }
  }
  val transactions = remember(localTransactions, remoteTransactions) {
    (localTransactions + remoteTransactions).distinctBy { it.id.ifBlank { "${it.itemId}_${it.amountCents}" } }
  }

  // Active game session state (when playing a round)
  var activePlayingDeck by remember { mutableStateOf<Deck?>(null) }
  var activeGameMode by remember { mutableStateOf("heads_up") }
  var activeRoundDuration by remember { mutableLongStateOf(60L) }

  // Payment Checkout / Setup Modal State
  var showPaymentModal by remember { mutableStateOf(false) }
  var pendingCheckoutItem by remember { mutableStateOf<CheckoutItemSpec?>(null) }
  var isProcessingPayment by remember { mutableStateOf(false) }

  // Navigation tab: 0 = Play, 1 = Store, 2 = Profile
  var currentTab by remember { mutableIntStateOf(0) }

  if (showPaymentModal) {
    PaymentCheckoutDialog(
      checkoutItem = pendingCheckoutItem,
      savedMethods = savedPaymentMethods,
      isProcessing = isProcessingPayment,
      onAddPaymentMethod = { newMethod ->
        coroutineScope.launch {
          val savedLocal = localStore.addPaymentMethod(newMethod)
          runCatching { paymentRepository?.addPaymentMethod(savedLocal) }
          snackbarHostState.showSnackbar("Saved ${savedLocal.summaryLabel()} as default payment method.")
        }
      },
      onSetDefaultMethod = { methodId ->
        coroutineScope.launch {
          localStore.setDefaultPaymentMethod(methodId)
          runCatching { paymentRepository?.setDefaultPaymentMethod(methodId) }
        }
      },
      onDeleteMethod = { methodId ->
        coroutineScope.launch {
          localStore.deletePaymentMethod(methodId)
          runCatching { paymentRepository?.deletePaymentMethod(methodId) }
          snackbarHostState.showSnackbar("Payment method deleted.")
        }
      },
      onCompleteCheckout = { item, method ->
        isProcessingPayment = true
        coroutineScope.launch {
          try {
            // 1. Persist locally immediately so phone users get instant unlock & receipt
            localStore.recordTransaction(
              itemId = item.itemId,
              itemTitle = item.title,
              amountCents = item.amountCents,
              paymentMethod = method,
              coinsGranted = item.coinsGranted,
              packUnlocked = item.packIdToUnlock
            )
            if (item.coinsGranted > 0L) {
              localStore.addCoins(item.coinsGranted)
            }
            if (item.packIdToUnlock == "ALL_EXPANSIONS") {
              StoreCatalog.EXPANSION_PACKS.forEach { pack ->
                localStore.unlockPack(pack.id, 0L)
              }
            } else if (!item.packIdToUnlock.isNullOrBlank()) {
              localStore.unlockPack(item.packIdToUnlock, 0L)
            }

            // 2. Sync to Firestore in background if available
            runCatching {
              paymentRepository?.recordPurchaseTransaction(
                itemId = item.itemId,
                itemTitle = item.title,
                amountCents = item.amountCents,
                paymentMethod = method,
                coinsGranted = item.coinsGranted,
                packUnlocked = item.packIdToUnlock
              )
              if (item.coinsGranted > 0L) {
                userRepository?.addCoins(currentUserId, item.coinsGranted)
              }
              if (item.packIdToUnlock == "ALL_EXPANSIONS") {
                StoreCatalog.EXPANSION_PACKS.forEach { pack ->
                  userRepository?.unlockPack(currentUserId, pack.id, 0L)
                }
              } else if (!item.packIdToUnlock.isNullOrBlank()) {
                userRepository?.unlockPack(currentUserId, item.packIdToUnlock, 0L)
              }
            }

            showPaymentModal = false
            pendingCheckoutItem = null
            snackbarHostState.showSnackbar("Halala! Paid ${item.formattedZar()} for ${item.title}.")
          } finally {
            isProcessingPayment = false
          }
        }
      },
      onDismiss = {
        if (!isProcessingPayment) {
          showPaymentModal = false
          pendingCheckoutItem = null
        }
      }
    )
  }

  if (activePlayingDeck != null) {
    GamePlayScreen(
      deck = activePlayingDeck!!,
      gameMode = activeGameMode,
      roundDurationSeconds = activeRoundDuration,
      onFinishGame = { session ->
        coroutineScope.launch {
          localStore.recordGameResult(session)
          runCatching {
            gameSessionRepository?.recordSession(session)
            userRepository?.recordGameResult(currentUserId, session.wordsGuessed.size.toLong())
          }
        }
      },
      onExit = {
        activePlayingDeck = null
      }
    )
  } else {
    Scaffold(
      snackbarHost = { SnackbarHost(snackbarHostState) },
      containerColor = StripiCanvas,
      bottomBar = {
        Column(modifier = Modifier.fillMaxWidth()) {
          HorizontalDivider(color = StripiHairline)
          NavigationBar(
            containerColor = StripiCanvas,
            contentColor = StripiInk
          ) {
            NavigationBarItem(
              selected = currentTab == 0,
              onClick = { currentTab = 0 },
              icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Play") },
              label = { Text("Play", style = StripiTypeTokens.micro) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = StripiPrimary,
                selectedTextColor = StripiPrimary,
                unselectedIconColor = StripiInkMute2,
                unselectedTextColor = StripiInkMute2,
                indicatorColor = StripiSubduedHover.copy(alpha = 0.45f)
              ),
              modifier = Modifier.testTag("nav_tab_play")
            )
            NavigationBarItem(
              selected = currentTab == 1,
              onClick = { currentTab = 1 },
              icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Store") },
              label = { Text("Store", style = StripiTypeTokens.micro) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = StripiPrimary,
                selectedTextColor = StripiPrimary,
                unselectedIconColor = StripiInkMute2,
                unselectedTextColor = StripiInkMute2,
                indicatorColor = StripiSubduedHover.copy(alpha = 0.45f)
              ),
              modifier = Modifier.testTag("nav_tab_store")
            )
            NavigationBarItem(
              selected = currentTab == 2,
              onClick = { currentTab = 2 },
              icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
              label = { Text("Profile", style = StripiTypeTokens.micro) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = StripiPrimary,
                selectedTextColor = StripiPrimary,
                unselectedIconColor = StripiInkMute2,
                unselectedTextColor = StripiInkMute2,
                indicatorColor = StripiSubduedHover.copy(alpha = 0.45f)
              ),
              modifier = Modifier.testTag("nav_tab_profile")
            )
          }
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .background(StripiCanvas)
      ) {
        when (currentTab) {
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
          1 -> StoreScreen(
            userProfile = userProfile,
            savedPaymentMethods = savedPaymentMethods,
            transactions = transactions,
            isDailyGiftClaimable = isDailyGiftClaimable,
            onAddCoins = { amount ->
              coroutineScope.launch {
                localStore.addCoins(amount)
                runCatching { userRepository?.addCoins(currentUserId, amount) }
                snackbarHostState.showSnackbar("Added +$amount Mzansi Coins!")
              }
            },
            onUnlockPack = { packId, cost ->
              coroutineScope.launch {
                localStore.unlockPack(packId, cost)
                runCatching { userRepository?.unlockPack(currentUserId, packId, cost) }
                snackbarHostState.showSnackbar("Pack unlocked and ready to play!")
              }
            },
            onRequestZarCheckout = { checkoutSpec ->
              val act = activity
              if (act != null && billingManager.isConnected.value && billingManager.availableProducts.value.containsKey(checkoutSpec.itemId)) {
                billingManager.launchPurchase(
                  activity = act,
                  productId = checkoutSpec.itemId,
                  onResult = { success, msg ->
                    if (!success && msg != null && msg != "Purchase cancelled") {
                      snackbarHostState.showSnackbar(msg)
                    }
                  },
                  onFallbackSimulation = {
                    pendingCheckoutItem = checkoutSpec
                    showPaymentModal = true
                  }
                )
              } else {
                pendingCheckoutItem = checkoutSpec
                showPaymentModal = true
              }
            },
            onOpenPaymentMethodsModal = {
              pendingCheckoutItem = null
              showPaymentModal = true
            },
            onClaimDailyGift = {
              val success = localStore.claimDailyGift(100L)
              if (success) {
                coroutineScope.launch {
                  runCatching { userRepository?.addCoins(currentUserId, 100L) }
                }
              }
              success
            },
            onWatchRewardedAd = { onRewardEarned ->
              val act = activity
              if (act != null) {
                AdMobManager.showRewardedAd(
                  activity = act,
                  rewardCoins = 50L,
                  onUserEarnedReward = { coins ->
                    onRewardEarned(coins)
                  },
                  onAdDismissed = {},
                  onFallback = {
                    onRewardEarned(50L)
                  }
                )
              } else {
                onRewardEarned(50L)
              }
            }
          )
          2 -> ProfileScreen(
            userProfile = userProfile,
            gameSessions = gameSessions,
            savedPaymentMethods = savedPaymentMethods,
            transactions = transactions,
            onOpenPaymentMethodsModal = {
              pendingCheckoutItem = null
              showPaymentModal = true
            },
            onSignOut = {
              appSignOut(context, credentialManager, coroutineScope)
            }
          )
        }
      }
    }
  }
}
