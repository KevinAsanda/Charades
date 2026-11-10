package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class PaymentMethod(
  @DocumentId val id: String = "",
  val userId: String = "",
  val type: String = "card", // "card", "payshap", "snapscan"
  val brand: String = "Visa",
  val last4: String = "4242",
  val expiryMonth: Long = 12L,
  val expiryYear: Long = 2028L,
  val holderName: String = "",
  val isDefault: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null
) {
  fun summaryLabel(): String {
    return when (type) {
      "payshap" -> "PayShap Instant ••$last4"
      "snapscan" -> "SnapScan Wallet ••$last4"
      else -> "$brand •••• $last4"
    }
  }
}

@IgnoreExtraProperties
data class PaymentTransaction(
  @DocumentId val id: String = "",
  val userId: String = "",
  val itemId: String = "",
  val itemTitle: String = "",
  val amountCents: Long = 0L,
  val currency: String = "ZAR",
  val paymentMethodId: String = "",
  val paymentMethodSummary: String = "",
  val coinsGranted: Long = 0L,
  val packUnlocked: String? = null,
  val status: String = "succeeded",
  @ServerTimestamp val createdAt: Timestamp? = null
) {
  fun formattedAmountZar(): String {
    val rands = amountCents / 100
    val cents = amountCents % 100
    return String.format("R%d.%02d", rands, cents)
  }
}
