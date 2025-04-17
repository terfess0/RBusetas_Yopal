package com.terfess.busetasyopal.services

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.clases_utiles.UserRegistsAnalyticsFuns
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores

class MyApplication : Application(), Application.ActivityLifecycleCallbacks {
    private lateinit var appOpenAdManager: AppOpenAdManager
    private var currentActivity: Activity? = null
    private var hasShownAdThisSession = false


    override fun onCreate() {
        super.onCreate()
        UserRegistsAnalyticsFuns().registLastConectionUser()
        UserRegistsAnalyticsFuns().registerDailyAppStart()

        // Inicializar Google Ads
        MobileAds.initialize(this) {}

        // Registrar ciclo de vida
        registerActivityLifecycleCallbacks(this)

        // Crear y cargar anuncio
        appOpenAdManager = AppOpenAdManager()
    }

    inner class AppOpenAdManager {
        private var appOpenAd: AppOpenAd? = null
        private var isLoadingAd = false
        var isShowingAd = false

        fun loadAd(context: Context, onAdLoaded: (() -> Unit)? = null) {
            if (isLoadingAd || isAdAvailable()) {
                return
            }

            isLoadingAd = true
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                context, AD_UNIT_ID, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {

                    override fun onAdLoaded(ad: AppOpenAd) {
                        Log.d(LOG_TAG, "Ad was loaded.")
                        appOpenAd = ad
                        isLoadingAd = false
                        onAdLoaded?.invoke() // Llama al callback cuando el anuncio esté listo
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d(LOG_TAG, loadAdError.message)
                        isLoadingAd = false
                    }
                })
        }


        private fun isAdAvailable(): Boolean = appOpenAd != null

        fun showAdIfAvailable(
            activity: Activity,
            onShowAdCompleteListener: OnShowAdCompleteListener
        ) {
            if (isShowingAd) {
                Log.d(LOG_TAG, "Ad is already showing.")
                return
            }

            if (!isAdAvailable()) {
                Log.d(LOG_TAG, "Ad not available yet.")
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity)
                return
            }

            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(LOG_TAG, "Ad dismissed.")
                    appOpenAd = null
                    isShowingAd = false
                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d(LOG_TAG, "Ad failed to show: ${adError.message}")
                    appOpenAd = null
                    isShowingAd = false
                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(LOG_TAG, "Ad showed fullscreen content.")
                }
            }

            isShowingAd = true
            appOpenAd?.show(activity)
        }
    }

    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

    companion object {
        // Todo: Change for real id
        private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
        private const val LOG_TAG = "AppOpenAdManager"
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity

        val str = activity.getString(R.string.name_shared_ads_restriction)
        val state = UtilidadesMenores().readSharedBooleanPref(activity, str)

        if (!state && !hasShownAdThisSession) {
            appOpenAdManager.loadAd(activity) {
                appOpenAdManager.showAdIfAvailable(activity, object : OnShowAdCompleteListener {
                    override fun onShowAdComplete() {
                        hasShownAdThisSession = true
                        Log.d(LOG_TAG, "Ad finished or not available")
                    }
                })
            }
        }
    }

    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}