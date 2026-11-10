package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.GameSession
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

class GameSessionRepository(private val db: FirebaseFirestore) {

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

  fun observeSessions(userId: String): Flow<List<GameSession>> {
    val path = "users/$userId/game_sessions"
    return db.collection("users")
      .document(userId)
      .collection("game_sessions")
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .limit(30)
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(GameSession::class.java)
      }
      .catch { error ->
        if (error is CancellationException) throw error
        handleFirestoreError(error, OperationType.LIST, path)
        throw error
      }
  }

  suspend fun recordSession(session: GameSession): String {
    val uid = requireUserId()
    val docRef = db.collection("users")
      .document(uid)
      .collection("game_sessions")
      .document()

    val payload = mapOf(
      "sessionId" to docRef.id,
      "userId" to uid,
      "deckId" to session.deckId,
      "deckName" to session.deckName,
      "gameMode" to session.gameMode,
      "teamAScore" to session.teamAScore,
      "teamBScore" to session.teamBScore,
      "wordsGuessed" to session.wordsGuessed,
      "wordsPassed" to session.wordsPassed,
      "durationSeconds" to session.durationSeconds,
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
}
