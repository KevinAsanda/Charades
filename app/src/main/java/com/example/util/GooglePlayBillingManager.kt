package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages Google Play In-App Billing for Mzansi Charades.
 * Handles connecting to Google Play, querying in-app products, launching the
 * 1-tap Google Play payment sheet, acknowledging purchases, and delivering items.
 */
class GooglePlayBillingManager(
  private val context: Context,
  private val coroutineScope: CoroutineScope,
  private val onPurchaseCompleted: (productId: String, orderId: String) -> Unit
) : PurchasesUpdatedListener {

  private val TAG = "BillingManager"

  private val _isConnected = MutableStateFlow(false)
  val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

  private val _availableProducts = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
  val availableProducts: StateFlow<Map<String, ProductDetails>> = _availableProducts.asStateFlow()

  private var activePurchaseCallback: ((success: Boolean, message: String?) -> Unit)? = null

  private val billingClient: BillingClient = BillingClient.newBuilder(context)
    .setListener(this)
    .enablePendingPurchases(
      PendingPurchasesParams.newBuilder()
        .enableOneTimeProducts()
        .build()
    )
    .build()

  init {
    startBillingConnection()
  }

  fun startBillingConnection() {
    billingClient.startConnection(object : BillingClientStateListener {
      override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
          Log.d(TAG, "Connected to Google Play Billing.")
          _isConnected.value = true
          queryCatalogProducts()
        } else {
          Log.w(TAG, "Google Play Billing setup response: ${billingResult.debugMessage}")
          _isConnected.value = false
        }
      }

      override fun onBillingServiceDisconnected() {
        Log.w(TAG, "Google Play Billing service disconnected. Will reconnect on next purchase attempt.")
        _isConnected.value = false
      }
    })
  }

  fun queryCatalogProducts(
    productIds: List<String> = listOf(
      "all_access_pass",
      "coin_bundle_small",
      "coin_bundle_large",
      "exp_pantsula",
      "exp_school_days",
      "exp_road_trip",
      "exp_memes"
    )
  ) {
    if (!billingClient.isReady) return

    val productList = productIds.map { id ->
      QueryProductDetailsParams.Product.newBuilder()
        .setProductId(id)
        .setProductType(BillingClient.ProductType.INAPP)
        .build()
    }

    val params = QueryProductDetailsParams.newBuilder()
      .setProductList(productList)
      .build()

    coroutineScope.launch(Dispatchers.IO) {
      try {
        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
          val productMap = result.productDetailsList?.associateBy { it.productId }.orEmpty()
          _availableProducts.value = productMap
          Log.d(TAG, "Found ${productMap.size} in-app products in Google Play Console.")
        } else {
          Log.w(TAG, "Query product details code: ${result.billingResult.responseCode}")
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error querying product details", e)
      }
    }
  }

  /**
   * Launches Google Play's native bottom-sheet purchase flow.
   * If Google Play is not available (e.g. running unsigned debug build without Play Store setup),
   * calls [onFallbackSimulation] so testing on device is never blocked.
   */
  fun launchPurchase(
    activity: Activity,
    productId: String,
    onResult: (success: Boolean, message: String?) -> Unit,
    onFallbackSimulation: () -> Unit
  ) {
    activePurchaseCallback = onResult

    val productDetails = _availableProducts.value[productId]
    if (productDetails == null || !billingClient.isReady) {
      Log.i(TAG, "Product '$productId' not loaded from Play Store or BillingClient not ready. Falling back to local simulation.")
      onFallbackSimulation()
      return
    }

    val productDetailsParamsList = listOf(
      BillingFlowParams.ProductDetailsParams.newBuilder()
        .setProductDetails(productDetails)
        .build()
    )

    val billingFlowParams = BillingFlowParams.newBuilder()
      .setProductDetailsParamsList(productDetailsParamsList)
      .build()

    val response = billingClient.launchBillingFlow(activity, billingFlowParams)
    if (response.responseCode != BillingClient.BillingResponseCode.OK) {
      Log.w(TAG, "Failed to launch billing flow: ${response.debugMessage}")
      onFallbackSimulation()
    }
  }

  override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
      for (purchase in purchases) {
        handlePurchase(purchase)
      }
    } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
      Log.d(TAG, "User cancelled purchase.")
      activePurchaseCallback?.invoke(false, "Purchase cancelled")
      activePurchaseCallback = null
    } else {
      Log.w(TAG, "Purchase failed: ${billingResult.debugMessage}")
      activePurchaseCallback?.invoke(false, billingResult.debugMessage)
      activePurchaseCallback = null
    }
  }

  private fun handlePurchase(purchase: Purchase) {
    coroutineScope.launch(Dispatchers.IO) {
      if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
        if (!purchase.isAcknowledged) {
          val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
          val ackResult = billingClient.acknowledgePurchase(acknowledgeParams)
          if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "Purchase acknowledged successfully.")
          }
        }

        val productId = purchase.products.firstOrNull() ?: ""
        onPurchaseCompleted(productId, purchase.orderId ?: purchase.purchaseToken)
        activePurchaseCallback?.invoke(true, null)
        activePurchaseCallback = null
      }
    }
  }

  fun destroy() {
    if (billingClient.isReady) {
      billingClient.endConnection()
    }
  }
}
