package com.terfess.busetasyopal.services

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores

class MyApplication : Application(), Application.ActivityLifecycleCallbacks {

    override fun onCreate() {
        super.onCreate()
        UtilidadesMenores().registLastConectionUser()
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}