package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PaymentMethod
import com.example.ui.components.StripiDarkPillButton
import com.example.ui.components.StripiFeatureCardLight
import com.example.ui.components.StripiFeaturedDarkCard
import com.example.ui.components.StripiPrimaryPillButton
import com.example.ui.components.StripiRounded
import com.example.ui.components.StripiSecondaryPillButton
import com.example.ui.components.StripiSoftPillTag
import com.example.ui.components.StripiTabularText
import com.example.ui.components.StripiTextInput
import com.example.ui.theme.StripiBrandDark900
import com.example.ui.theme.StripiCanvas
import com.example.ui.theme.StripiCanvasSoft
import com.example.ui.theme.StripiHairline
import com.example.ui.theme.StripiInk
import com.example.ui.theme.StripiInkMute
import com.example.ui.theme.StripiOnPrimary
import com.example.ui.theme.StripiPrimary
import com.example.ui.theme.StripiPrimaryDeep
import com.example.ui.theme.StripiRuby
import com.example.ui.theme.StripiSubduedHover
import com.example.ui.theme.StripiTypeTokens

data class CheckoutItemSpec(
  val itemId: String,
  val title: String,
  val subtitle: String,
  val amountCents: Long,
  val coinsGranted: Long = 0L,
  val packIdToUnlock: String? = null
) {
  fun formattedZar(): String {
    val rands = amountCents / 100
    val cents = amountCents % 100
    return String.format("R%d.%02d", rands, cents)
  }
}

/**
 * Stripi-Inspired Payment Checkout & Payment Method Setup Modal.
 * Supports South African payment rails:
 * - Credit / Debit Card (Visa / Mastercard)
 * - PayShap Instant EFT (ShapID / Mobile)
 * - SnapScan QR Wallet
 */
@Composable
fun PaymentCheckoutDialog(
  checkoutItem: CheckoutItemSpec?,
  savedMethods: List<PaymentMethod>,
  isProcessing: Boolean,
  onAddPaymentMethod: (PaymentMethod) -> Unit,
  onSetDefaultMethod: (String) -> Unit,
  onDeleteMethod: (String) -> Unit,
  onCompleteCheckout: (CheckoutItemSpec, PaymentMethod) -> Unit,
  onDismiss: () -> Unit
) {
  val defaultSelectedId = remember(savedMethods) {
    savedMethods.firstOrNull { it.isDefault }?.id ?: savedMethods.firstOrNull()?.id
  }
  var selectedMethodId by remember(defaultSelectedId) { mutableStateOf(defaultSelectedId) }
  var showAddForm by remember(savedMethods.isEmpty()) { mutableStateOf(savedMethods.isEmpty()) }

  // New payment method form state
  var selectedMethodType by remember { mutableStateOf("card") } // "card", "payshap", "snapscan"
  var holderName by remember { mutableStateOf("") }
  var cardNumberRaw by remember { mutableStateOf("") }
  var expiryMonthText by remember { mutableStateOf("08") }
  var expiryYearText by remember { mutableStateOf("2029") }
  var cvcText by remember { mutableStateOf("") }
  var payshapId by remember { mutableStateOf("") }
  var formError by remember { mutableStateOf<String?>(null) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 24.dp),
      shape = StripiRounded.xl,
      color = StripiCanvas
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Top Header Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            StripiSoftPillTag(
              text = if (checkoutItem != null) "ZAR Checkout" else "Payment Methods"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (checkoutItem != null) "Complete your purchase" else "Manage Mzansi payment rails",
              style = StripiTypeTokens.headingLg.copy(color = StripiInk)
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_payment_sheet_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close payment sheet",
              tint = StripiInkMute
            )
          }
        }

        // Order Summary Card (Featured Dark #1C1E54) when checking out an item
        if (checkoutItem != null) {
          StripiFeaturedDarkCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(20.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = checkoutItem.title,
                  style = StripiTypeTokens.headingMd.copy(color = StripiOnPrimary)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = checkoutItem.subtitle,
                  style = StripiTypeTokens.caption.copy(
                    color = StripiSubduedHover.copy(alpha = 0.85f)
                  )
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(horizontalAlignment = Alignment.End) {
                StripiTabularText(
                  text = checkoutItem.formattedZar(),
                  color = StripiOnPrimary,
                  fontSize = 24,
                  fontWeight = FontWeight.Light
                )
                Text(
                  text = "incl. 15% VAT",
                  style = StripiTypeTokens.micro.copy(
                    color = StripiSubduedHover.copy(alpha = 0.7f)
                  )
                )
              }
            }
          }
        }

        // Saved Payment Methods Section
        if (savedMethods.isNotEmpty()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Saved Payment Methods",
              style = StripiTypeTokens.headingSm.copy(color = StripiInk)
            )
            if (!showAddForm) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(StripiRounded.pill)
                  .clickable { showAddForm = true }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
                  .testTag("show_add_payment_method_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "Add new method",
                  tint = StripiPrimary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Add Method",
                  style = StripiTypeTokens.buttonSm.copy(color = StripiPrimary)
                )
              }
            }
          }

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            savedMethods.forEach { method ->
              val isSelected = selectedMethodId == method.id
              StripiFeatureCardLight(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("saved_payment_method_${method.last4}"),
                borderColor = if (isSelected) StripiPrimary else StripiHairline,
                borderWidth = if (isSelected) 1.5.dp else 1.dp,
                backgroundColor = if (isSelected) StripiCanvasSoft else StripiCanvas,
                contentPadding = PaddingValues(14.dp),
                onClick = {
                  selectedMethodId = method.id
                  onSetDefaultMethod(method.id)
                }
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(36.dp)
                        .clip(StripiRounded.md)
                        .background(if (isSelected) StripiPrimary else StripiBrandDark900),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = when (method.type) {
                          "payshap" -> Icons.Default.AccountBalance
                          "snapscan" -> Icons.Default.QrCodeScanner
                          else -> Icons.Default.CreditCard
                        },
                        contentDescription = null,
                        tint = StripiOnPrimary,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        StripiTabularText(
                          text = method.summaryLabel(),
                          color = StripiInk,
                          fontSize = 14,
                          fontWeight = FontWeight.Normal
                        )
                        if (method.isDefault) {
                          Spacer(modifier = Modifier.width(8.dp))
                          StripiSoftPillTag(text = "Default")
                        }
                      }
                      Text(
                        text = if (method.type == "card") {
                          "${method.holderName} · Exp ${String.format("%02d/%d", method.expiryMonth, method.expiryYear)}"
                        } else {
                          "Holder: ${method.holderName}"
                        },
                        style = StripiTypeTokens.caption.copy(color = StripiInkMute)
                      )
                    }
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                      Box(
                        modifier = Modifier
                          .size(24.dp)
                          .clip(CircleShape)
                          .background(StripiPrimary),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = "Selected",
                          tint = StripiOnPrimary,
                          modifier = Modifier.size(14.dp)
                        )
                      }
                    }
                    IconButton(
                      onClick = { onDeleteMethod(method.id) },
                      modifier = Modifier.size(32.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = StripiInkMute,
                        modifier = Modifier.size(16.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // Add New Payment Method Form
        if (showAddForm) {
          StripiFeatureCardLight(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = StripiCanvasSoft,
            contentPadding = PaddingValues(18.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Add South African payment method",
                style = StripiTypeTokens.headingSm.copy(color = StripiInk)
              )
              if (savedMethods.isNotEmpty()) {
                Text(
                  text = "Cancel",
                  style = StripiTypeTokens.buttonSm.copy(color = StripiPrimary),
                  modifier = Modifier.clickable { showAddForm = false }
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payment Rail Selector (Card, PayShap, SnapScan)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              PaymentRailOption(
                title = "Bank Card",
                subtitle = "Visa / Mastercard",
                isSelected = selectedMethodType == "card",
                modifier = Modifier.weight(1f).testTag("rail_option_card"),
                onClick = { selectedMethodType = "card" }
              )
              PaymentRailOption(
                title = "PayShap",
                subtitle = "Instant EFT",
                isSelected = selectedMethodType == "payshap",
                modifier = Modifier.weight(1f).testTag("rail_option_payshap"),
                onClick = { selectedMethodType = "payshap" }
              )
              PaymentRailOption(
                title = "SnapScan",
                subtitle = "ZA QR Wallet",
                isSelected = selectedMethodType == "snapscan",
                modifier = Modifier.weight(1f).testTag("rail_option_snapscan"),
                onClick = { selectedMethodType = "snapscan" }
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            StripiTextInput(
              value = holderName,
              onValueChange = {
                holderName = it
                formError = null
              },
              label = "Account / Cardholder Name",
              placeholder = "e.g. Sipho Khumalo",
              modifier = Modifier.testTag("payment_holder_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedMethodType == "card") {
              StripiTextInput(
                value = cardNumberRaw,
                onValueChange = { input ->
                  val digitsOnly = input.filter { it.isDigit() }.take(16)
                  cardNumberRaw = digitsOnly
                  formError = null
                },
                label = "Card Number (16 digits)",
                placeholder = "4242 4242 4242 4242",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.testTag("payment_card_number_input")
              )

              Spacer(modifier = Modifier.height(12.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                StripiTextInput(
                  value = expiryMonthText,
                  onValueChange = { expiryMonthText = it.filter { ch -> ch.isDigit() }.take(2) },
                  label = "Month",
                  placeholder = "08",
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  modifier = Modifier.weight(1f)
                )
                StripiTextInput(
                  value = expiryYearText,
                  onValueChange = { expiryYearText = it.filter { ch -> ch.isDigit() }.take(4) },
                  label = "Year",
                  placeholder = "2029",
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  modifier = Modifier.weight(1f)
                )
                StripiTextInput(
                  value = cvcText,
                  onValueChange = { cvcText = it.filter { ch -> ch.isDigit() }.take(4) },
                  label = "CVC",
                  placeholder = "842",
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  modifier = Modifier.weight(1f)
                )
              }
            } else {
              StripiTextInput(
                value = payshapId,
                onValueChange = {
                  payshapId = it
                  formError = null
                },
                label = if (selectedMethodType == "payshap") {
                  "PayShap ShapID or SA Mobile Number"
                } else {
                  "SnapScan Linked Mobile Number"
                },
                placeholder = "082 555 4289",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.testTag("payment_wallet_id_input")
              )
            }

            if (formError != null) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = formError.orEmpty(),
                style = StripiTypeTokens.caption.copy(color = StripiRuby)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val saveAction = {
              val cleanName = holderName.trim().ifBlank { "Mzansi Player" }
              if (selectedMethodType == "card") {
                val digits = cardNumberRaw.filter { it.isDigit() }
                if (digits.length < 4) {
                  formError = "Please enter at least the last 4 digits of your card."
                } else {
                  val brand = when {
                    digits.startsWith("4") -> "Visa"
                    digits.startsWith("5") || digits.startsWith("2") -> "Mastercard"
                    else -> "SA Bank Card"
                  }
                  val last4 = digits.takeLast(4)
                  val month = expiryMonthText.toLongOrNull()?.coerceIn(1L, 12L) ?: 8L
                  val year = expiryYearText.toLongOrNull()?.coerceIn(2025L, 2045L) ?: 2029L
                  val newMethod = PaymentMethod(
                    type = "card",
                    brand = brand,
                    last4 = last4,
                    expiryMonth = month,
                    expiryYear = year,
                    holderName = cleanName,
                    isDefault = true
                  )
                  onAddPaymentMethod(newMethod)
                  showAddForm = false
                  cardNumberRaw = ""
                  cvcText = ""
                }
              } else {
                val cleanPhone = payshapId.filter { it.isDigit() }
                if (cleanPhone.length < 4) {
                  formError = "Please enter a valid SA mobile number or ShapID."
                } else {
                  val brand = if (selectedMethodType == "payshap") "PayShap" else "SnapScan"
                  val newMethod = PaymentMethod(
                    type = selectedMethodType,
                    brand = brand,
                    last4 = cleanPhone.takeLast(4),
                    expiryMonth = 12L,
                    expiryYear = 2030L,
                    holderName = cleanName,
                    isDefault = true
                  )
                  onAddPaymentMethod(newMethod)
                  showAddForm = false
                  payshapId = ""
                }
              }
            }

            if (checkoutItem != null) {
              StripiDarkPillButton(
                text = "Save Payment Method",
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("save_payment_method_button"),
                onClick = saveAction
              )
            } else {
              StripiPrimaryPillButton(
                text = "Save Payment Method",
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("save_payment_method_button"),
                onClick = saveAction
              )
            }
          }
        }

        HorizontalDivider(color = StripiHairline)

        // Security Badge Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.VerifiedUser,
              contentDescription = null,
              tint = StripiPrimaryDeep,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Encrypted South African ZAR settlement · 3D Secure 2.0",
              style = StripiTypeTokens.micro.copy(color = StripiInkMute)
            )
          }
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = StripiInkMute,
            modifier = Modifier.size(14.dp)
          )
        }

        // Primary Pay CTA when checking out (`button-primary-pill` #533AFD)
        if (checkoutItem != null) {
          val activeMethod = savedMethods.find { it.id == selectedMethodId } ?: savedMethods.firstOrNull()
          StripiPrimaryPillButton(
            text = if (isProcessing) {
              "Processing ZAR..."
            } else if (activeMethod != null) {
              "Pay ${checkoutItem.formattedZar()}"
            } else {
              "Save a Method to Pay ${checkoutItem.formattedZar()}"
            },
            enabled = activeMethod != null && !isProcessing,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("confirm_zar_payment_button"),
            leadingIcon = {
              if (isProcessing) {
                CircularProgressIndicator(
                  modifier = Modifier.size(16.dp),
                  color = StripiOnPrimary,
                  strokeWidth = 2.dp
                )
              }
            },
            onClick = {
              if (activeMethod != null) {
                onCompleteCheckout(checkoutItem, activeMethod)
              }
            }
          )
        } else {
          StripiSecondaryPillButton(
            text = "Done",
            modifier = Modifier.fillMaxWidth(),
            onClick = onDismiss
          )
        }
      }
    }
  }
}

@Composable
private fun PaymentRailOption(
  title: String,
  subtitle: String,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Box(
    modifier = modifier
      .clip(StripiRounded.md)
      .background(if (isSelected) StripiSubduedHover.copy(alpha = 0.35f) else StripiCanvas)
      .border(
        width = if (isSelected) 1.5.dp else 1.dp,
        color = if (isSelected) StripiPrimary else StripiHairline,
        shape = StripiRounded.md
      )
      .clickable { onClick() }
      .padding(horizontal = 10.dp, vertical = 8.dp)
  ) {
    Column {
      Text(
        text = title,
        style = StripiTypeTokens.buttonSm.copy(
          color = if (isSelected) StripiPrimary else StripiInk
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        style = StripiTypeTokens.micro.copy(color = StripiInkMute)
      )
    }
  }
}
