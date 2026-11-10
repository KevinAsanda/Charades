package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Palette
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
import com.example.data.model.Deck
import com.example.data.model.StoreCatalog
import com.example.data.repository.AiRepository
import com.example.data.repository.CustomCardRepository
import com.example.data.repository.GameSessionRepository
import com.example.data.repository.PaymentRepository
import com.example.data.repository.UserRepository
import com.example.ui.theme.StripiCanvas
import com.example.ui.theme.StripiHairline
import com.example.ui.theme.StripiInk
import com.example.ui.theme.StripiInkMute
import com.example.ui.theme.StripiPrimary
import com.example.ui.theme.StripiSubduedHover
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun MainFeatureScreen(
  currentUserId: String
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  val credentialManager = remember { CredentialManager.create(context) }

  val userRepository = remember { UserRepository(context) }
  val gameSessionRepository = remember { GameSessionRepository(context) }
  val customCardRepository = remember { CustomCardRepository(context) }
  val paymentRepository = remember { PaymentRepository(context) }
  val aiRepository = remember { AiRepository() }

  // Observe player profile
  val profileFlow = remember(currentUserId) { userRepository.observeUserProfile(currentUserId) }
  val userProfile by profileFlow.collectAsStateWithLifecycle(initialValue = null)

  // Initialize profile on first launch
  LaunchedEffect(currentUserId) {
    val authUser = Firebase.auth.currentUser
    if (authUser != null) {
      try {
        userRepository.getOrCreateProfile(
          userId = currentUserId,
          displayName = authUser.displayName.orEmpty().ifBlank { "Mzansi Star" },
          email = authUser.email,
          photoUrl = authUser.photoUrl?.toString()
        )
      } catch (_: Exception) {}
    }
  }

  // Observe match history
  val sessionFlow = remember(currentUserId) { gameSessionRepository.observeSessions(currentUserId) }
  val gameSessions by sessionFlow.collectAsStateWithLifecycle(initialValue = emptyList())

  // Observe custom cards
  val customCardFlow = remember(currentUserId) { customCardRepository.observeCards(currentUserId) }
  val customCards by customCardFlow.collectAsStateWithLifecycle(initialValue = emptyList())

  // Observe saved payment methods & ZAR transactions
  val paymentMethodsFlow = remember(currentUserId) { paymentRepository.observePaymentMethods(currentUserId) }
  val savedPaymentMethods by paymentMethodsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

  val transactionsFlow = remember(currentUserId) { paymentRepository.observeTransactions(currentUserId) }
  val transactions by transactionsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

  // Active game session state (when playing a round)
  var activePlayingDeck by remember { mutableStateOf<Deck?>(null) }
  var activeGameMode by remember { mutableStateOf("heads_up") }
  var activeRoundDuration by remember { mutableLongStateOf(60L) }

  // Payment Checkout / Setup Modal State
  var showPaymentModal by remember { mutableStateOf(false) }
  var pendingCheckoutItem by remember { mutableStateOf<CheckoutItemSpec?>(null) }
  var isProcessingPayment by remember { mutableStateOf(false) }

  // Navigation tab
  var currentTab by remember { mutableIntStateOf(0) } // 0: Play, 1: Store, 2: AI Coach, 3: Studio, 4: Profile

  if (showPaymentModal) {
    PaymentCheckoutDialog(
      checkoutItem = pendingCheckoutItem,
      savedMethods = savedPaymentMethods,
      isProcessing = isProcessingPayment,
      onAddPaymentMethod = { newMethod ->
        coroutineScope.launch {
          try {
            paymentRepository.addPaymentMethod(newMethod)
            snackbarHostState.showSnackbar("Saved ${newMethod.summaryLabel()} as default payment method.")
          } catch (e: Exception) {
            snackbarHostState.showSnackbar("Could not save payment method: ${e.localizedMessage}")
          }
        }
      },
      onSetDefaultMethod = { methodId ->
        coroutineScope.launch {
          try {
            paymentRepository.setDefaultPaymentMethod(methodId)
          } catch (_: Exception) {}
        }
      },
      onDeleteMethod = { methodId ->
        coroutineScope.launch {
          try {
            paymentRepository.deletePaymentMethod(methodId)
            snackbarHostState.showSnackbar("Payment method removed.")
          } catch (e: Exception) {
            snackbarHostState.showSnackbar("Failed to remove method: ${e.localizedMessage}")
          }
        }
      },
      onCompleteCheckout = { item, method ->
        isProcessingPayment = true
        coroutineScope.launch {
          try {
            paymentRepository.recordPurchaseTransaction(
              itemId = item.itemId,
              itemTitle = item.title,
              amountCents = item.amountCents,
              paymentMethod = method,
              coinsGranted = item.coinsGranted,
              packUnlocked = item.packIdToUnlock
            )
            if (item.coinsGranted > 0L) {
              userRepository.addCoins(currentUserId, item.coinsGranted)
            }
            if (item.packIdToUnlock == "ALL_EXPANSIONS") {
              StoreCatalog.EXPANSION_PACKS.forEach { pack ->
                userRepository.unlockPack(currentUserId, pack.id, 0L)
              }
            } else if (!item.packIdToUnlock.isNullOrBlank()) {
              userRepository.unlockPack(currentUserId, item.packIdToUnlock, 0L)
            }
            showPaymentModal = false
            pendingCheckoutItem = null
            snackbarHostState.showSnackbar("Halala! Paid ${item.formattedZar()} for ${item.title}.")
          } catch (e: Exception) {
            snackbarHostState.showSnackbar("Payment failed: ${e.localizedMessage}")
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
          try {
            gameSessionRepository.recordSession(session)
            userRepository.recordGameResult(currentUserId, session.wordsGuessed.size.toLong())
          } catch (e: Exception) {
            snackbarHostState.showSnackbar("Failed to sync match: ${e.localizedMessage}")
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
              label = { Text("Play") },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = StripiPrimary,
                selectedTextColor = StripiPrimary,
                unselectedIconColor = StripiInkMute,
                unselectedTextColor = StripiInkMute,
                indicatorColor = StripiSubduedHover.copy(alpha = 0.45f)
              ),
              modifier = Modifier.testTag("nav_tab_play")
            )
            NavigationBarItem(
              selected = currentTab == 1,
              onClick = { currentTab = 1 },
              icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Store") },
              label = { Text("Store") },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = StripiPrimary,
                selectedTextColor = StripiPrimary,
                unselectedIconColor = StripiInkMute,
                unselectedTextColor = StripiInkMute,
                indicatorColor = StripiSubduedHover.copy(alpha = 0.45f)
              ),
              modifier = Modifier.testTag("nav_tab_store")
            )
            NavigationBarItem(
              selected = currentTab == 2,
              onClick = { currentTab = 2 },
              icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Coach") },
              label = { Text("AI Coach") },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = StripiPrimary,
                selectedTextColor = StripiPrimary,
                unselectedIconColor = StripiInkMute,
                unselectedTextColor = StripiInkMute,
                indicatorColor = StripiSubduedHover.copy(alpha = 0.45f)
              ),
              modifier = Modifier.testTag("nav_tab_ai")
            )
            NavigationBarItem(
              selected = currentTab == 3,
              onClick = { currentTab = 3 },
              icon = { Icon(Icons.Default.Palette, contentDescription = "Studio") },
              label = { Text("Studio") },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = StripiPrimary,
                selectedTextColor = StripiPrimary,
                unselectedIconColor = StripiInkMute,
                unselectedTextColor = StripiInkMute,
                indicatorColor = StripiSubduedHover.copy(alpha = 0.45f)
              ),
              modifier = Modifier.testTag("nav_tab_studio")
            )
            NavigationBarItem(
              selected = currentTab == 4,
              onClick = { currentTab = 4 },
              icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
              label = { Text("Profile") },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = StripiPrimary,
                selectedTextColor = StripiPrimary,
                unselectedIconColor = StripiInkMute,
                unselectedTextColor = StripiInkMute,
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
            onNavigateToStudio = { currentTab = 3 },
            onOpenPaymentSetup = {
              pendingCheckoutItem = null
              showPaymentModal = true
            }
          )
          1 -> StoreScreen(
            userProfile = userProfile,
            savedPaymentMethods = savedPaymentMethods,
            transactions = transactions,
            onAddCoins = { amount ->
              coroutineScope.launch {
                try {
                  userRepository.addCoins(currentUserId, amount)
                } catch (e: Exception) {
                  snackbarHostState.showSnackbar("Coin addition failed: ${e.localizedMessage}")
                }
              }
            },
            onUnlockPack = { packId, cost ->
              coroutineScope.launch {
                try {
                  userRepository.unlockPack(currentUserId, packId, cost)
                } catch (e: Exception) {
                  snackbarHostState.showSnackbar("Unlock failed: ${e.localizedMessage}")
                }
              }
            },
            onRequestZarCheckout = { checkoutSpec ->
              pendingCheckoutItem = checkoutSpec
              showPaymentModal = true
            },
            onOpenPaymentMethodsModal = {
              pendingCheckoutItem = null
              showPaymentModal = true
            }
          )
          2 -> AiCoachScreen(
            aiRepository = aiRepository
          )
          3 -> DeckStudioScreen(
            aiRepository = aiRepository,
            customCards = customCards,
            onAddCard = { card ->
              coroutineScope.launch {
                try {
                  customCardRepository.addCustomCard(card)
                  snackbarHostState.showSnackbar("Card '${card.term}' created!")
                } catch (e: Exception) {
                  snackbarHostState.showSnackbar("Failed to create card: ${e.localizedMessage}")
                }
              }
            },
            onDeleteCard = { cardId ->
              coroutineScope.launch {
                try {
                  customCardRepository.deleteCustomCard(cardId)
                  snackbarHostState.showSnackbar("Card deleted.")
                } catch (e: Exception) {
                  snackbarHostState.showSnackbar("Delete failed: ${e.localizedMessage}")
                }
              }
            }
          )
          4 -> ProfileScreen(
            userProfile = userProfile,
            gameSessions = gameSessions,
            savedPaymentMethods = savedPaymentMethods,
            transactions = transactions,
            onOpenPaymentMethodsModal = {
              pendingCheckoutItem = null
              showPaymentModal = true
            },
            onSignOut = {
              appSignOut(credentialManager, coroutineScope)
            }
          )
        }
      }
    }
  }
}
