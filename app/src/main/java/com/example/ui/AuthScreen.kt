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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.components.StripiCreamCard
import com.example.ui.components.StripiFeatureCardLight
import com.example.ui.components.StripiFeaturedDarkCard
import com.example.ui.components.StripiGradientMeshHero
import com.example.ui.components.StripiPrimaryPillButton
import com.example.ui.components.StripiSoftPillTag
import com.example.ui.components.StripiTabularText
import com.example.ui.theme.StripiBrandDark900
import com.example.ui.theme.StripiCanvas
import com.example.ui.theme.StripiHairline
import com.example.ui.theme.StripiInk
import com.example.ui.theme.StripiInkMute
import com.example.ui.theme.StripiInkSecondary
import com.example.ui.theme.StripiOnPrimary
import com.example.ui.theme.StripiPrimary
import com.example.ui.theme.StripiRuby
import com.example.ui.theme.StripiSubduedHover
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
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Silent Auto Sign-In on launch
  LaunchedEffect(Unit) {
    if (Firebase.auth.currentUser != null) {
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
      // Stripi Atmospheric Gradient Mesh Hero Section
      StripiGradientMeshHero(
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 32.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          StripiSoftPillTag(text = "South African Edition")
          Text(
            text = "2000s Culture • Kwaito • Slang",
            style = MaterialTheme.typography.labelMedium.copy(color = StripiInkSecondary)
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
          text = "Mzansi Charades.",
          style = MaterialTheme.typography.displayLarge.copy(
            color = StripiInk,
            fontWeight = FontWeight.Light
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Act out iconic early 2000s South African television, Kwaito anthems, tuckshop classics, and local township slang with your crew.",
          style = MaterialTheme.typography.bodyLarge.copy(
            color = StripiInkSecondary
          )
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
          Text(
            text = errorMessage.orEmpty(),
            color = StripiRuby,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 12.dp)
          )
        }

        StripiPrimaryPillButton(
          text = if (isLoading) "Signing in with Google..." else "Sign in with Google",
          enabled = !isLoading,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("google_sign_in_button"),
          leadingIcon = {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = StripiOnPrimary,
                strokeWidth = 2.dp
              )
            } else {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Sign In Lock Icon",
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
              onAuthError = { err ->
                isLoading = false
                errorMessage = err
              },
              onAuthCancelled = {
                isLoading = false
              },
              scope = coroutineScope
            )
          }
        )
      }

      // Main Architectural Content Band
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Featured Dark Dashboard Preview Card
        StripiFeaturedDarkCard(
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "LIVE DECK TELEMETRY",
              style = MaterialTheme.typography.labelSmall.copy(
                color = StripiSubduedHover,
                letterSpacing = 0.8.sp
              )
            )
            StripiSoftPillTag(
              text = "5 Free Core Decks",
              containerColor = StripiPrimary,
              contentColor = StripiOnPrimary
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "Starter Stash",
                style = MaterialTheme.typography.bodySmall.copy(color = StripiSubduedHover)
              )
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
                style = MaterialTheme.typography.bodySmall.copy(color = StripiSubduedHover)
              )
              StripiTabularText(
                text = "From R19.99",
                color = StripiOnPrimary,
                fontSize = 22,
                fontWeight = FontWeight.Light
              )
            }
          }
        }

        // Hairline Feature Grid Card
        StripiFeatureCardLight(
          modifier = Modifier.fillMaxWidth()
        ) {
          FeatureRow(
            icon = Icons.Default.MusicNote,
            title = "Kwaito & 2000s TV Anthems",
            subtitle = "Mandoza Nkalakatha, TKZee Shibobo, Queen Moroka & Tata Ma Millions."
          )
          HorizontalDivider(
            modifier = Modifier.padding(vertical = 14.dp),
            color = StripiHairline
          )
          FeatureRow(
            icon = Icons.Default.SportsSoccer,
            title = "Authentic Local Slang & Moments",
            subtitle = "2010 World Cup Vuvuzelas, Eish, Sharp Fede, Voetstoots & Spatlo Kotas."
          )
          HorizontalDivider(
            modifier = Modifier.padding(vertical = 14.dp),
            color = StripiHairline
          )
          FeatureRow(
            icon = Icons.Default.CreditCard,
            title = "Generous Free Tier & ZAR Checkout",
            subtitle = "Play 5 core decks free, watch nostalgically themed ads, or unlock packs via Card, PayShap, or SnapScan."
          )
        }

        // Warm Cream Editorial Callout Card
        StripiCreamCard(
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Built for Mzansi game nights",
                style = MaterialTheme.typography.titleMedium.copy(
                  color = StripiInk,
                  fontWeight = FontWeight.Medium
                )
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Cloud syncs your custom AI cards, match history, and saved ZAR payment methods across devices.",
                style = MaterialTheme.typography.bodySmall.copy(color = StripiInkSecondary)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
private fun FeatureRow(
  icon: ImageVector,
  title: String,
  subtitle: String
) {
  Row(
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(StripiSubduedHover.copy(alpha = 0.35f))
        .border(1.dp, StripiHairline, RoundedCornerShape(8.dp)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = StripiPrimary,
        modifier = Modifier.size(18.dp)
      )
    }
    Spacer(modifier = Modifier.width(14.dp))
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Medium,
          color = StripiInk
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall.copy(color = StripiInkMute)
      )
    }
  }
}

private fun attemptAutoSignIn(
  context: Context,
  credentialManager: CredentialManager,
  onAuthSuccess: () -> Unit,
  scope: CoroutineScope
) {
  val clientId = try {
    context.getString(R.string.default_web_client_id)
  } catch (e: Exception) {
    Log.w(TAG, "default_web_client_id not yet resolved")
    return
  }

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
  val clientId = try {
    context.getString(R.string.default_web_client_id)
  } catch (e: Exception) {
    onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
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
      onAuthError(e.localizedMessage ?: "Sign in failed. Please try again.")
    }
  }
}

fun appSignOut(
  credentialManager: CredentialManager,
  scope: CoroutineScope,
  onSignOutComplete: () -> Unit = {}
) {
  scope.launch {
    withContext(NonCancellable) {
      try {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
      } catch (e: Exception) {
        Log.w(TAG, "Failed to clear credential state: ${e.message}")
      } finally {
        Firebase.auth.signOut()
        onSignOutComplete()
      }
    }
  }
}
