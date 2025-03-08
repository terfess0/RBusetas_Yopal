//package com.terfess.busetasyopal.services
//
//import android.app.Activity
//import android.app.Application
//import android.content.Context
//import android.os.Bundle
//import android.util.Log
//import com.google.android.gms.ads.AdError
//import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.FullScreenContentCallback
//import com.google.android.gms.ads.LoadAdError
//import com.google.android.gms.ads.MobileAds
//import com.google.android.gms.ads.appopen.AppOpenAd
//import com.terfess.busetasyopal.R
//import com.terfess.busetasyopal.actividades.Splash
//
//class MyApplication : Application(), Application.ActivityLifecycleCallbacks {
//
//    lateinit var appOpenAdManager: AppOpenAdManager
//    private var isAppStartedFromBackground = false // Rastrea si la app se inició desde el fondo
//
//    override fun onCreate() {
//        super.onCreate()
//        MobileAds.initialize(this) {}
//        appOpenAdManager = AppOpenAdManager(this)
//        appOpenAdManager.loadAd() // Cargar el anuncio al iniciar la aplicación
//        registerActivityLifecycleCallbacks(this)
//    }
//
//    inner class AppOpenAdManager(private val context: Context) {
//        private var appOpenAd: AppOpenAd? = null
//        private var isLoadingAd = false
//        private var isShowingAd = false
//
//        fun isAdAvailable(): Boolean {
//            return appOpenAd != null
//        }
//
//        fun loadAd(activity: Activity? = null) {
//            if (isLoadingAd || isAdAvailable()) return
//
//            isLoadingAd = true
//            val request = AdRequest.Builder().build()
//            AppOpenAd.load(
//                context, AD_UNIT_ID, request,
//                object : AppOpenAd.AppOpenAdLoadCallback() {
//                    override fun onAdLoaded(ad: AppOpenAd) {
//                        appOpenAd = ad
//                        isLoadingAd = false
//                        Log.d(LOG_TAG, "Ad Loaded")
//
//                        // Mostrar el anuncio inmediatamente después de cargarse
//                        activity?.let {
//                            showAdIfAvailable(it)
//                        }
//                    }
//
//                    override fun onAdFailedToLoad(error: LoadAdError) {
//                        Log.e(LOG_TAG, "Ad Failed to Load: ${error.message}")
//                        isLoadingAd = false
//                    }
//                }
//            )
//        }
//
//        fun showAdIfAvailable(activity: Activity) {
//            if (!isAdAvailable() || isShowingAd || activity.isFinishing) {
//                return
//            }
//
//            appOpenAd?.let { ad ->
//                isShowingAd = true
//                ad.show(activity)
//                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
//
//
//                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
//                        Log.e(LOG_TAG, "Ad Failed to Show: ${adError.message}")
//                        isShowingAd = false
//                        appOpenAd = null
//                    }
//                }
//            }
//        }
//    }
//
//    companion object {
//        private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921" // Prueba
//        private const val LOG_TAG = "MyApplication"
//    }
//
//    override fun onActivityStarted(activity: Activity) {
//        Log.d(LOG_TAG, "Activity Started: ${activity.localClassName}")
//
//        // No mostrar el anuncio en el SplashScreen
//        if (activity !is Splash && !isAppStartedFromBackground) {
//            appOpenAdManager.loadAd(activity)
//            isAppStartedFromBackground = true
//        }
//    }
//
//    override fun onActivityStopped(activity: Activity) {
//        // Cuando la app va al fondo, marcamos que no se ha iniciado desde el fondo
//        isAppStartedFromBackground = false
//    }
//
//    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
//    override fun onActivityResumed(activity: Activity) {}
//    override fun onActivityPaused(activity: Activity) {}
//    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
//    override fun onActivityDestroyed(activity: Activity) {}
//}