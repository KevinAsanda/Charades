package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CustomCard
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CustomCardRepository(private val db: FirebaseFirestore) {

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

  fun observeCards(userId: String): Flow<List<CustomCard>> {
    val path = "users/$userId/custom_cards"
    return db.collection("users")
      .document(userId)
      .collection("custom_cards")
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(CustomCard::class.java)
      }
      .catch { error ->
        if (error is CancellationException) throw error
        handleFirestoreError(error, OperationType.LIST, path)
        throw error
      }
  }

  suspend fun addCustomCard(card: CustomCard): String {
    val uid = requireUserId()
    val docRef = db.collection("users")
      .document(uid)
      .collection("custom_cards")
      .document()

    val payload = mapOf(
      "cardId" to docRef.id,
      "userId" to uid,
      "term" to card.term.trim(),
      "category" to card.category.trim().ifBlank { "Custom Mzansi" },
      "hints" to card.hints.filter { it.isNotBlank() },
      "culturalEra" to card.culturalEra.ifBlank { "Early 2000s" },
      "imageUrl" to card.imageUrl,
      "createdAt" to FieldValue.serverTimestamp()
    )

    try {
      docRef.set(payload).await()
      return docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  suspend fun deleteCustomCard(cardId: String) {
    val uid = requireUserId()
    val docRef = db.collection("users")
      .document(uid)
      .collection("custom_cards")
      .document(cardId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }
}
