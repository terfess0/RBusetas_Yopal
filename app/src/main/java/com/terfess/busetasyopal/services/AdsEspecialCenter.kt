package com.terfess.busetasyopal.services

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores

class AdsEspecialCenter {

    var mInterstitialAd: InterstitialAd? = null
    fun intersticialAdRequest(
        context: Context,
        instUtilidadesMenores: UtilidadesMenores,
        tag: String,
        activityOrigin: Activity,
        keyAddString: String?
    ) {
        val adRequest = AdRequest.Builder().build()

        val keyAd = keyAddString ?: context.getString(R.string.fake_key_intersticial)

        val str = context.getString(R.string.name_shared_ads_restriction)
        val state = instUtilidadesMenores.readSharedBooleanPref(context, str)


        if (!state) {

            InterstitialAd.load(context, keyAd, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(tag, adError.toString())
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(tag, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                    mInterstitialAd?.show(activityOrigin)
                }
            })

            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                    Log.d(tag, "Ad was clicked.")
                }

                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    Log.d(tag, "Ad dismissed fullscreen content.")
                    mInterstitialAd = null
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    // Called when ad fails to show.
                    Log.e(tag, "Ad failed to show fullscreen content.")
                    mInterstitialAd = null
                }

                override fun onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    Log.d(tag, "Ad recorded an impression.")
                }

                override fun onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    Log.d(tag, "Ad showed fullscreen content.")
                }
            }
        }
    }
}