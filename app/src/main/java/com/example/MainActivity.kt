package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.LocalPlayerStore
import com.example.ui.AuthScreen
import com.example.ui.MainFeatureScreen
import com.example.ui.theme.MzansiCharadesTheme
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Configure App Check safely before UI or Firebase operations
    configureAppCheck(this, intent)

    enableEdgeToEdge()

    setContent {
      MzansiCharadesTheme {
        Surface {
          AppContent()
        }
      }
    }
  }
}

@Composable
fun AppContent() {
  val context = LocalContext.current
  val localStore = remember { LocalPlayerStore.getInstance(context) }
  val localUserId by localStore.activeUserId.collectAsStateWithLifecycle()

  var firebaseUid by remember {
    mutableStateOf(runCatching { Firebase.auth.currentUser?.uid }.getOrNull())
  }

  DisposableEffect(Unit) {
    val authInstance = runCatching { Firebase.auth }.getOrNull()
    val listener = FirebaseAuth.AuthStateListener { auth ->
      firebaseUid = auth.currentUser?.uid
    }
    authInstance?.addAuthStateListener(listener)
    onDispose {
      authInstance?.removeAuthStateListener(listener)
    }
  }

  val effectiveUid = firebaseUid ?: localUserId

  if (effectiveUid == null) {
    AuthScreen(
      onAuthSuccess = {
        val currentFirebaseUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (currentFirebaseUser != null) {
          firebaseUid = currentFirebaseUser.uid
          localStore.signInLocalUser(
            uid = currentFirebaseUser.uid,
            displayName = currentFirebaseUser.displayName.orEmpty().ifBlank { "Mzansi Star" },
            email = currentFirebaseUser.email ?: "Signed in with Google"
          )
        } else {
          localStore.signInLocalUser()
        }
      }
    )
  } else {
    key(effectiveUid) {
      MainFeatureScreen(
        currentUserId = effectiveUid
      )
    }
  }
}
