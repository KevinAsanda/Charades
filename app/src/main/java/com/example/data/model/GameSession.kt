package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class GameSession(
  @DocumentId val id: String = "",
  val userId: String = "",
  val deckId: String = "",
  val deckName: String = "",
  val gameMode: String = "heads_up", // heads_up, party_teams, quick_play
  val teamAScore: Long = 0L,
  val teamBScore: Long = 0L,
  val wordsGuessed: List<String> = emptyList(),
  val wordsPassed: List<String> = emptyList(),
  val durationSeconds: Long = 60L,
  @ServerTimestamp val createdAt: Timestamp? = null
)
