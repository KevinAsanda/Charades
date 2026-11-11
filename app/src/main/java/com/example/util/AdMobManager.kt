package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Manages Google Mobile Ads (AdMob) integration for Mzansi Charades.
 * Uses official Google AdMob Test Ad Units by default to ensure safe testing
 * without triggering account invalid traffic suspensions.
 */
object AdMobManager {

  private const val TAG = "AdMobManager"

  // Google's official sample/test ad units for Android
  // Replace with your real AdMob Ad Unit IDs from https://admob.google.com when ready for production!
  const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

  private var isInitialized = false
  private var rewardedAd: RewardedAd? = null
  private var isLoadingRewarded = false

  fun initialize(context: Context) {
    if (isInitialized) return
    try {
      MobileAds.initialize(context.applicationContext) { initStatus ->
        isInitialized = true
        Log.d(TAG, "Google Mobile Ads initialized successfully: $initStatus")
        // Preload first rewarded ad in the background
        loadRewardedAd(context.applicationContext)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to initialize Google Mobile Ads", e)
    }
  }

  fun loadRewardedAd(
    context: Context,
    adUnitId: String = TEST_REWARDED_AD_UNIT_ID,
    onLoaded: (() -> Unit)? = null
  ) {
    if (rewardedAd != null || isLoadingRewarded) return
    isLoadingRewarded = true

    val adRequest = AdRequest.Builder().build()
    RewardedAd.load(
      context,
      adUnitId,
      adRequest,
      object : RewardedAdLoadCallback() {
        override fun onAdLoaded(ad: RewardedAd) {
          rewardedAd = ad
          isLoadingRewarded = false
          Log.d(TAG, "Rewarded ad loaded successfully.")
          onLoaded?.invoke()
        }

        override fun onAdFailedToLoad(error: LoadAdError) {
          rewardedAd = null
          isLoadingRewarded = false
          Log.w(TAG, "Rewarded ad failed to load: ${error.message} (Code: ${error.code})")
        }
      }
    )
  }

  fun isRewardedAdReady(): Boolean = rewardedAd != null

  /**
   * Shows a rewarded video ad. When the user finishes watching,
   * [onUserEarnedReward] is triggered with the reward coins (default 50L).
   * If no ad is currently loaded, [onFallback] is called so testing is never blocked.
   */
  fun showRewardedAd(
    activity: Activity,
    rewardCoins: Long = 50L,
    onUserEarnedReward: (coins: Long) -> Unit,
    onAdDismissed: () -> Unit,
    onFallback: (() -> Unit)? = null
  ) {
    val currentAd = rewardedAd
    if (currentAd == null) {
      Log.i(TAG, "No AdMob rewarded ad preloaded. Triggering fallback.")
      loadRewardedAd(activity.applicationContext)
      onFallback?.invoke() ?: onAdDismissed()
      return
    }

    currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
      override fun onAdDismissedFullScreenContent() {
        Log.d(TAG, "Rewarded ad dismissed.")
        rewardedAd = null
        loadRewardedAd(activity.applicationContext)
        onAdDismissed()
      }

      override fun onAdFailedToShowFullScreenContent(error: AdError) {
        Log.w(TAG, "Rewarded ad failed to show: ${error.message}")
        rewardedAd = null
        loadRewardedAd(activity.applicationContext)
        onFallback?.invoke() ?: onAdDismissed()
      }
    }

    currentAd.show(activity) { rewardItem ->
      val amount = if (rewardItem.amount > 0) rewardItem.amount.toLong() else rewardCoins
      Log.d(TAG, "User earned reward: $amount coins")
      onUserEarnedReward(amount)
    }
  }
}
