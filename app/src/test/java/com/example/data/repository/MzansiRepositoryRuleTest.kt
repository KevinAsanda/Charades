package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.CustomCard
import com.example.data.model.GameSession
import com.example.data.model.PaymentMethod
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class MzansiRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun getOrCreateProfile_authenticatedUser_createsAndReturnsProfile() {
    runBlocking {
      val uid = signInTestUser(ALICE_EMAIL)
      val userRepo = UserRepository(firestore)

      val profile = withTimeout(DEFAULT_TIMEOUT_MS) {
        userRepo.getOrCreateProfile(uid, "Sipho Mzansi", ALICE_EMAIL, null)
      }

      assertEquals(uid, profile.userId)
      assertEquals(150L, profile.coins)
    }
  }

  @Test
  fun recordSession_andObserve_authenticatedUser_succeeds() {
    runBlocking {
      val uid = signInTestUser(ALICE_EMAIL)
      val sessionRepo = GameSessionRepository(firestore)

      val session = GameSession(
        userId = uid,
        deckId = "core_kwaito",
        deckName = "Kwaito & 2000s Grooves",
        gameMode = "party_teams",
        teamAScore = 8,
        teamBScore = 6,
        wordsGuessed = listOf("Nkalakatha", "Vul'indlela"),
        wordsPassed = listOf("Izinja"),
        durationSeconds = 90
      )

      val sessionId = withTimeout(DEFAULT_TIMEOUT_MS) {
        sessionRepo.recordSession(session)
      }
      assertTrue(sessionId.isNotEmpty())

      val sessions = withTimeout(FLOW_TIMEOUT_MS) {
        sessionRepo.observeSessions(uid).first { list -> list.any { it.id == sessionId } }
      }
      assertTrue(sessions.any { it.id == sessionId })
    }
  }

  @Test
  fun addCustomCard_authenticatedUser_createsAndEmitsCard() {
    runBlocking {
      val uid = signInTestUser(ALICE_EMAIL)
      val cardRepo = CustomCardRepository(firestore)

      val card = CustomCard(
        userId = uid,
        term = "Tata Ma Millions",
        category = "2000s TV",
        hints = listOf("Saturday draw", "Lotto ticket", "Tata ma chance"),
        culturalEra = "Early 2000s"
      )

      val cardId = withTimeout(DEFAULT_TIMEOUT_MS) {
        cardRepo.addCustomCard(card)
      }
      assertTrue(cardId.isNotEmpty())

      val cards = withTimeout(FLOW_TIMEOUT_MS) {
        cardRepo.observeCards(uid).first { list -> list.any { it.id == cardId } }
      }
      assertTrue(cards.any { it.id == cardId })
    }
  }

  @Test
  fun paymentRepository_addMethodAndRecordTransaction_succeeds() {
    runBlocking {
      val uid = signInTestUser(ALICE_EMAIL)
      val paymentRepo = PaymentRepository(firestore)

      val method = PaymentMethod(
        userId = uid,
        type = "card",
        brand = "Visa",
        last4 = "4242",
        expiryMonth = 9L,
        expiryYear = 2029L,
        holderName = "Sipho Khumalo",
        isDefault = true
      )

      val methodId = withTimeout(DEFAULT_TIMEOUT_MS) {
        paymentRepo.addPaymentMethod(method)
      }
      assertTrue(methodId.isNotEmpty())

      val savedMethods = withTimeout(FLOW_TIMEOUT_MS) {
        paymentRepo.observePaymentMethods(uid).first { list -> list.any { it.id == methodId } }
      }
      val savedMethod = savedMethods.first { it.id == methodId }
      assertEquals("4242", savedMethod.last4)

      val txId = withTimeout(DEFAULT_TIMEOUT_MS) {
        paymentRepo.recordPurchaseTransaction(
          itemId = "coins_300",
          itemTitle = "Township Stash",
          amountCents = 2999L,
          paymentMethod = savedMethod,
          coinsGranted = 300L,
          packUnlocked = null
        )
      }
      assertTrue(txId.isNotEmpty())

      val transactions = withTimeout(FLOW_TIMEOUT_MS) {
        paymentRepo.observeTransactions(uid).first { list -> list.any { it.id == txId } }
      }
      assertTrue(transactions.any { it.id == txId && it.amountCents == 2999L })
    }
  }

  @Test
  fun crossUserAccess_bobCannotModifyAliceProfile_failsWithException() {
    runBlocking {
      val aliceUid = signInTestUser(ALICE_EMAIL)
      val aliceRepo = UserRepository(firestore)
      withTimeout(DEFAULT_TIMEOUT_MS) {
        aliceRepo.getOrCreateProfile(aliceUid, "Alice", ALICE_EMAIL, null)
      }

      signInTestUser(BOB_EMAIL)
      val bobRepo = UserRepository(firestore)

      try {
        bobRepo.addCoins(aliceUid, 500)
        fail("Expected IllegalArgumentException when modifying another user profile")
      } catch (e: IllegalArgumentException) {
        assertNotNull(e.message)
      }
    }
  }

  @Test
  fun observeSessions_unauthenticatedUser_failsWithPermissionDenied() {
    runBlocking {
      val uid = signInTestUser(ALICE_EMAIL)
      val sessionRepo = GameSessionRepository(firestore)

      auth.signOut()

      try {
        withTimeout(FLOW_TIMEOUT_MS) {
          sessionRepo.observeSessions(uid).first()
        }
        fail("Expected exception when unauthenticated")
      } catch (e: Exception) {
        val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
          "Expected nested FirebaseFirestoreException, got $e"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
      }
    }
  }

  private companion object {
    const val ALICE_EMAIL = "sipho_alice@test.com"
    const val BOB_EMAIL = "bafana_bob@test.com"
    const val DEFAULT_TIMEOUT_MS = 6000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
