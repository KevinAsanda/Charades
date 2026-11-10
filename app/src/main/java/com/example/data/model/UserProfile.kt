package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "",
  val email: String? = null,
  val photoUrl: String? = null,
  val coins: Long = 150L,
  val unlockedPacks: List<String> = listOf("core_kwaito", "core_slang", "core_2000s_tv", "core_tuckshop", "core_sports"),
  val gamesPlayed: Long = 0L,
  val totalCorrect: Long = 0L,
  val highScore: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)
