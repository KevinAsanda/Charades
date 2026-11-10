package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.UserProfile
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository(private val db: FirebaseFirestore) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private val auth = FirebaseAuth.getInstance()

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  fun observeUserProfile(userId: String): Flow<UserProfile?> {
    val docRef = db.collection("users").document(userId)
    return docRef.snapshots()
      .map { snapshot ->
        if (snapshot.exists()) {
          snapshot.toObject(UserProfile::class.java)
        } else {
          null
        }
      }
      .catch { error ->
        if (error is CancellationException) throw error
        handleFirestoreError(error, OperationType.GET, docRef.path)
        throw error
      }
  }

  suspend fun getOrCreateProfile(
    userId: String,
    displayName: String,
    email: String?,
    photoUrl: String?
  ): UserProfile {
    require(userId == requireUserId()) { "Cannot access profile of another user" }
    val docRef = db.collection("users").document(userId)
    try {
      val snapshot = docRef.get().await()
      if (snapshot.exists()) {
        val existing = snapshot.toObject(UserProfile::class.java)
        if (existing != null) return existing
      }

      val newProfilePayload = mapOf(
        "userId" to userId,
        "displayName" to displayName.ifBlank { "Mzansi Player" },
        "email" to email,
        "photoUrl" to photoUrl,
        "coins" to 150L,
        "unlockedPacks" to listOf("core_kwaito", "core_slang", "core_2000s_tv", "core_tuckshop", "core_sports"),
        "gamesPlayed" to 0L,
        "totalCorrect" to 0L,
        "highScore" to 0L,
        "createdAt" to FieldValue.serverTimestamp()
      )
      docRef.set(newProfilePayload).await()
      return UserProfile(
        id = userId,
        userId = userId,
        displayName = displayName.ifBlank { "Mzansi Player" },
        email = email,
        photoUrl = photoUrl,
        coins = 150L
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun addCoins(userId: String, amount: Long) {
    require(userId == requireUserId())
    val docRef = db.collection("users").document(userId)
    try {
      docRef.update(
        mapOf(
          "coins" to FieldValue.increment(amount),
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun unlockPack(userId: String, packId: String, costCoins: Long) {
    require(userId == requireUserId())
    val docRef = db.collection("users").document(userId)
    try {
      val updates = mutableMapOf<String, Any>(
        "unlockedPacks" to FieldValue.arrayUnion(packId),
        "updatedAt" to FieldValue.serverTimestamp()
      )
      if (costCoins > 0) {
        updates["coins"] = FieldValue.increment(-costCoins)
      }
      docRef.update(updates).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun recordGameResult(userId: String, correctCount: Long) {
    require(userId == requireUserId())
    val docRef = db.collection("users").document(userId)
    try {
      val snapshot = docRef.get().await()
      val currentHighScore = snapshot.getLong("highScore") ?: 0L
      val newHighScore = maxOf(currentHighScore, correctCount)

      // Reward: 5 coins per correct guess + 10 completion coins
      val earnedCoins = 10L + (correctCount * 5L)

      docRef.update(
        mapOf(
          "gamesPlayed" to FieldValue.increment(1),
          "totalCorrect" to FieldValue.increment(correctCount),
          "highScore" to newHighScore,
          "coins" to FieldValue.increment(earnedCoins),
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }
}
