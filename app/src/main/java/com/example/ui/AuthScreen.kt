package com.example.ui

import android.app.Activity
import android.content.Context
import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.data.local.LocalPlayerStore
import com.example.ui.components.StripiCreamCard
import com.example.ui.components.StripiDashboardMockupCard
import com.example.ui.components.StripiFeatureCardLight
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
import com.example.ui.theme.StripiRuby
import com.example.ui.theme.StripiSubduedHover
import com.example.ui.theme.StripiTypeTokens
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "AuthScreen"

@Composable
fun AuthScreen(
  onAuthSuccess: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }
  val localStore = remember { LocalPlayerStore.getInstance(context) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Silent Auto Sign-In on launch
  LaunchedEffect(Unit) {
    val existingUser = runCatching { Firebase.auth.currentUser }.getOrNull()
    if (existingUser != null || localStore.activeUserId.value != null) {
      onAuthSuccess()
      return@LaunchedEffect
    }
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = onAuthSuccess,
      scope = coroutineScope
    )
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = StripiCanvas
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
    ) {
      // 1. Stripi Atmospheric Gradient Mesh Hero (Strict 4-element Hero Stack Discipline)
      StripiGradientMeshHero(
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 28.dp)
      ) {
        // Element 1: Eyebrow pill tag (`pill-tag-soft`)
        StripiSoftPillTag(text = "South African Edition")

        Spacer(modifier = Modifier.height(16.dp))

        // Element 2: Thin Display Headline (weight 300, negative tracking)
        Text(
          text = "Mzansi Charades.",
          style = StripiTypeTokens.displayMobileHero.copy(color = StripiInk)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Element 3: Subtext (17 words, <= 20 words rule, body-lg weight 300)
        Text(
          text = "Act out early 2000s South African TV, Kwaito anthems, tuckshop classics, and township slang with your crew.",
          style = StripiTypeTokens.bodyLg.copy(color = StripiInkSecondary)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
          Text(
            text = errorMessage.orEmpty(),
            color = StripiRuby,
            style = StripiTypeTokens.caption,
            modifier = Modifier.padding(bottom = 12.dp)
          )
        }

        // Element 4: CTAs (1 Primary + 1 Secondary)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          StripiPrimaryPillButton(
            text = if (isLoading) "Signing in..." else "Sign in with Google",
            enabled = !isLoading,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("google_sign_in_button"),
            leadingIcon = {
              if (isLoading) {
                CircularProgressIndicator(
                  modifier = Modifier.size(16.dp),
                  color = StripiOnPrimary,
                  strokeWidth = 2.dp
                )
              } else {
                Icon(
                  imageVector = Icons.Default.Lock,
                  contentDescription = "Sign In",
                  modifier = Modifier.size(16.dp),
                  tint = StripiOnPrimary
                )
              }
            },
            trailingContent = {
              if (!isLoading) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = StripiOnPrimary
                )
              }
            },
            onClick = {
              isLoading = true
              errorMessage = null
              onGoogleSignInClicked(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                  isLoading = false
                  onAuthSuccess()
                },
                onAuthError = {
                  // When Google Sign-In isn't configured on this phone (missing google-services.json
                  // or SHA-1), seamlessly activate a local device profile so the user can play immediately.
                  isLoading = false
                  localStore.signInLocalUser(
                    displayName = "Mzansi Star",
                    email = "Signed in on Android"
                  )
                  onAuthSuccess()
                },
                onAuthCancelled = {
                  isLoading = false
                },
                scope = coroutineScope
              )
            }
          )

          StripiSecondaryPillButton(
            text = "Play Now on Phone",
            enabled = !isLoading,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("guest_sign_in_button"),
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = StripiPrimary
              )
            },
            onClick = {
              localStore.signInLocalUser(
                displayName = "Mzansi Star",
                email = "Local Mzansi Profile"
              )
              onAuthSuccess()
            }
          )
        }
      }

      // 2. Composited Dashboard Mockup (`card-dashboard-mockup` floating on canvas)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
      ) {
        StripiDashboardMockupCard(
          modifier = Modifier.fillMaxWidth(),
          contentPadding = PaddingValues(20.dp)
        ) {
          // Dark-app console panel composited inside the mockup card
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(StripiRounded.md)
              .background(StripiBrandDark900)
              .padding(16.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Starter Stash",
                  style = StripiTypeTokens.caption.copy(color = StripiSubduedHover)
                )
                Spacer(modifier = Modifier.height(2.dp))
                StripiTabularText(
                  text = "150 Coins",
                  color = StripiOnPrimary,
                  fontSize = 22,
                  fontWeight = FontWeight.Light
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "Expansion Packs",
                  style = StripiTypeTokens.caption.copy(color = StripiSubduedHover)
                )
                Spacer(modifier = Modifier.height(2.dp))
                StripiTabularText(
                  text = "R19.99",
                  color = StripiOnPrimary,
                  fontSize = 22,
                  fontWeight = FontWeight.Light
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Tabular Deck Telemetry Rows (`body-tabular` with `tnum`)
          MockupDeckRow(
            deckTitle = "Trending Apps & Digital SA",
            era = "2020-2026",
            cardCount = "12 cards",
            tierStatus = "Included"
          )
          HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp),
            color = StripiHairline
          )
          MockupDeckRow(
            deckTitle = "TikTok Trends & Lockdown Vault",
            era = "2020-2026",
            cardCount = "18 cards",
            tierStatus = "Included"
          )
          HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp),
            color = StripiHairline
          )
          MockupDeckRow(
            deckTitle = "Amapiano, 3-Step, Lekompo & Kwaito",
            era = "2000-2026",
            cardCount = "13 cards",
            tierStatus = "Included"
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // 3. Asymmetric Bento Feature Band on `canvas-soft` (#F6F9FC) with Cream Band Interlude
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(StripiCanvasSoft)
          .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text(
          text = "Built for South African game nights.",
          style = StripiTypeTokens.displayMd.copy(color = StripiInk)
        )

        // Bento Cell 1: Full-width light feature card
        StripiFeatureCardLight(
          modifier = Modifier.fillMaxWidth(),
          contentPadding = PaddingValues(20.dp)
        ) {
          FeatureTileContent(
            icon = Icons.Default.MusicNote,
            title = "4 Authentic Gameplay Modes",
            subtitle = "Act It Out, Hit the Beat, Mzansi 30-Seconds, and the Oct 2026 My Reaction facial ranking challenge."
          )
        }

        // Bento Cells 2 & 3: Asymmetric 2-column split (Light Card + Warm Cream Interlude Card)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          StripiFeatureCardLight(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(18.dp)
          ) {
            FeatureTileContent(
              icon = Icons.Default.SportsSoccer,
              title = "2000-2026 Culture",
              subtitle = "Dricus 8% oxygen, 3-Step, Lekompo, SASL Vibe signs, and 2020 Lockdown memes."
            )
          }

          StripiCreamCard(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(18.dp)
          ) {
            FeatureTileContent(
              icon = Icons.Default.CreditCard,
              title = "ZAR Settlement",
              subtitle = "8 free core decks plus instant unlocks via Card, PayShap, or SnapScan."
            )
          }
        }
      }

      // 4. Footer Light (`footer-light`: #FFFFFF canvas, #64748D ink-mute, caption 13sp tnum)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(StripiCanvas)
          .padding(horizontal = 24.dp, vertical = 32.dp)
      ) {
        HorizontalDivider(color = StripiHairline)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Mzansi Charades",
            style = StripiTypeTokens.caption.copy(color = StripiInkSecondary)
          )
          Text(
            text = "8 Free Core Decks · ZAR Billing",
            style = StripiTypeTokens.caption.copy(color = StripiInkMute)
          )
        }
      }
    }
  }
}

@Composable
private fun MockupDeckRow(
  deckTitle: String,
  era: String,
  cardCount: String,
  tierStatus: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = deckTitle,
        style = StripiTypeTokens.bodyMd.copy(color = StripiInk)
      )
      Text(
        text = era,
        style = StripiTypeTokens.bodyTabular.copy(color = StripiInkMute)
      )
    }
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      StripiTabularText(
        text = cardCount,
        color = StripiInkSecondary,
        fontSize = 14
      )
      StripiSoftPillTag(text = tierStatus)
    }
  }
}

@Composable
private fun FeatureTileContent(
  icon: ImageVector,
  title: String,
  subtitle: String
) {
  Column {
    Box(
      modifier = Modifier
        .size(36.dp)
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
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = title,
      style = StripiTypeTokens.headingSm.copy(color = StripiInk)
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = subtitle,
      style = StripiTypeTokens.bodyMd.copy(color = StripiInkMute)
    )
  }
}

private fun resolveWebClientId(context: Context): String? {
  return runCatching { context.getString(com.example.R.string.default_web_client_id) }
    .getOrNull()
    ?.takeIf { it.isNotBlank() }
}

private fun attemptAutoSignIn(
  context: Context,
  credentialManager: CredentialManager,
  onAuthSuccess: () -> Unit,
  scope: CoroutineScope
) {
  val clientId = resolveWebClientId(context) ?: return

  val googleIdOption = GetGoogleIdOption.Builder()
    .setFilterByAuthorizedAccounts(true)
    .setServerClientId(clientId)
    .setAutoSelectEnabled(true)
    .build()

  val request = GetCredentialRequest.Builder()
    .addCredentialOption(googleIdOption)
    .build()

  scope.launch {
    try {
      val result = credentialManager.getCredential(context, request)
      val credential = result.credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        Firebase.auth.signInWithCredential(authCredential).await()
        onAuthSuccess()
      }
    } catch (e: Exception) {
      Log.d(TAG, "Silent auto-sign in not applicable: ${e.message}")
    }
  }
}

private fun onGoogleSignInClicked(
  context: Context,
  credentialManager: CredentialManager,
  onAuthSuccess: () -> Unit,
  onAuthError: (String) -> Unit,
  onAuthCancelled: () -> Unit,
  scope: CoroutineScope
) {
  val clientId = resolveWebClientId(context)
  if (clientId == null) {
    onAuthError("Google Sign-In client ID not configured; activating local device profile.")
    return
  }

  val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
  val request = GetCredentialRequest.Builder()
    .addCredentialOption(signInOption)
    .build()

  scope.launch {
    try {
      val activity = context as? Activity
        ?: throw IllegalStateException("Context must be an Activity to prompt sign in.")
      val result = credentialManager.getCredential(activity, request)
      val credential = result.credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        Firebase.auth.signInWithCredential(authCredential).await()
        onAuthSuccess()
      } else {
        onAuthError("Unexpected credential received")
      }
    } catch (e: GetCredentialCancellationException) {
      Log.w(TAG, "Google Sign-In cancelled or dismissed: ${e.message}", e)
      onAuthCancelled()
    } catch (e: Exception) {
      Log.e(TAG, "Google Sign-In failed", e)
      onAuthError(e.localizedMessage ?: "Sign in failed.")
    }
  }
}

fun appSignOut(
  context: Context,
  credentialManager: CredentialManager,
  scope: CoroutineScope,
  onSignOutComplete: () -> Unit = {}
) {
  scope.launch {
    withContext(NonCancellable) {
      try {
        LocalPlayerStore.getInstance(context).signOutLocalUser()
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
      } catch (e: Exception) {
        Log.w(TAG, "Failed to clear credential state: ${e.message}")
      } finally {
        runCatching { Firebase.auth.signOut() }
        onSignOutComplete()
      }
    }
  }
}
