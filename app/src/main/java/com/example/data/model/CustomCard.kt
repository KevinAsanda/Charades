package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class CustomCard(
  @DocumentId val id: String = "",
  val userId: String = "",
  val term: String = "",
  val category: String = "",
  val hints: List<String> = emptyList(),
  val culturalEra: String = "Early 2000s",
  val imageUrl: String? = null,
  @ServerTimestamp val createdAt: Timestamp? = null
)
