package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentTransaction
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

class PaymentRepository(private val db: FirebaseFirestore) {

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

  fun observePaymentMethods(userId: String): Flow<List<PaymentMethod>> {
    val collectionRef = db.collection("users").document(userId).collection("payment_methods")
    return collectionRef
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .snapshots()
      .map { snapshot ->
        snapshot.documents.mapNotNull { doc ->
          doc.toObject(PaymentMethod::class.java)
        }
      }
      .catch { error ->
        if (error is CancellationException) throw error
        handleFirestoreError(error, OperationType.LIST, collectionRef.path)
        throw error
      }
  }

  suspend fun addPaymentMethod(method: PaymentMethod): String {
    val uid = requireUserId()
    val collectionRef = db.collection("users").document(uid).collection("payment_methods")
    val docRef = collectionRef.document()
    try {
      // If this new method is marked default, unset existing default methods first
      if (method.isDefault) {
        val existingDocs = collectionRef.get().await()
        for (doc in existingDocs.documents) {
          if (doc.getBoolean("isDefault") == true) {
            doc.reference.update("isDefault", false).await()
          }
        }
      }

      val payload = mapOf(
        "userId" to uid,
        "type" to method.type,
        "brand" to method.brand.take(40).ifBlank { "Card" },
        "last4" to method.last4.take(20).ifBlank { "0000" },
        "expiryMonth" to method.expiryMonth.coerceIn(1L, 12L),
        "expiryYear" to method.expiryYear.coerceIn(2024L, 2060L),
        "holderName" to method.holderName.take(80).ifBlank { "Mzansi Player" },
        "isDefault" to method.isDefault,
        "createdAt" to FieldValue.serverTimestamp()
      )
      docRef.set(payload).await()
      return docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  suspend fun setDefaultPaymentMethod(methodId: String) {
    val uid = requireUserId()
    val collectionRef = db.collection("users").document(uid).collection("payment_methods")
    try {
      val snapshot = collectionRef.get().await()
      for (doc in snapshot.documents) {
        val shouldBeDefault = doc.id == methodId
        if (doc.getBoolean("isDefault") != shouldBeDefault) {
          doc.reference.update("isDefault", shouldBeDefault).await()
        }
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, collectionRef.path)
      throw e
    }
  }

  suspend fun deletePaymentMethod(methodId: String) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("payment_methods").document(methodId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  fun observeTransactions(userId: String): Flow<List<PaymentTransaction>> {
    val collectionRef = db.collection("users").document(userId).collection("transactions")
    return collectionRef
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .limit(30)
      .snapshots()
      .map { snapshot ->
        snapshot.documents.mapNotNull { doc ->
          doc.toObject(PaymentTransaction::class.java)
        }
      }
      .catch { error ->
        if (error is CancellationException) throw error
        handleFirestoreError(error, OperationType.LIST, collectionRef.path)
        throw error
      }
  }

  suspend fun recordPurchaseTransaction(
    itemId: String,
    itemTitle: String,
    amountCents: Long,
    paymentMethod: PaymentMethod,
    coinsGranted: Long = 0L,
    packUnlocked: String? = null
  ): String {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("transactions").document()
    try {
      val payload = mutableMapOf<String, Any>(
        "userId" to uid,
        "itemId" to itemId.take(64),
        "itemTitle" to itemTitle.take(100),
        "amountCents" to amountCents.coerceIn(0L, 1000000L),
        "currency" to "ZAR",
        "paymentMethodId" to paymentMethod.id.ifBlank { "instant_method" }.take(128),
        "paymentMethodSummary" to paymentMethod.summaryLabel().take(100),
        "coinsGranted" to coinsGranted.coerceAtLeast(0L),
        "status" to "succeeded",
        "createdAt" to FieldValue.serverTimestamp()
      )
      if (!packUnlocked.isNullOrBlank()) {
        payload["packUnlocked"] = packUnlocked.take(64)
      }
      docRef.set(payload).await()
      return docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }
}
