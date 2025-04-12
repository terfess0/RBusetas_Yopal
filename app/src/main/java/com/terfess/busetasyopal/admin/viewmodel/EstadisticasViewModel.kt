package com.terfess.busetasyopal.admin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.terfess.busetasyopal.admin.callback.analytics.OnDailyStarts
import com.terfess.busetasyopal.admin.callback.analytics.OnGetCountsClicks
import com.terfess.busetasyopal.admin.callback.analytics.OnTotalReports
import com.terfess.busetasyopal.admin.model.AdminProvider
import com.terfess.busetasyopal.enums.FirebaseEnums

class EstadisticasViewModel:ViewModel(),
    OnTotalReports,
    OnGetCountsClicks.OnGetRouteMaxClicks,
    OnGetCountsClicks.OnGetRouteMinClicks,
    OnGetCountsClicks.OnGetTotalClicks,
    OnDailyStarts,
    OnDailyStarts.OnTotalStarts {

    var totalClicks = MutableLiveData<Int>()
    var routeMaxClicks = MutableLiveData<String>()
    var routeMinClicks = MutableLiveData<String>()
    var totalReports = MutableLiveData<Int>()

    var todayStarts = MutableLiveData<String>()
    var totalStarts = MutableLiveData<String>()

    // Total reports ..
    fun requestTotalReports(){
        AdminProvider().getTotalReportsCount(this)
    }

    override fun OnSuccessTotalReports(numReports: Int) {
        totalReports.postValue(numReports)
    }

    override fun OnErrorTotalReports(errorType: FirebaseEnums) {
        totalReports.postValue(-1)
    }

    //..
    // Route max clicks ..
    fun requestRouteMaxClicks(){
        AdminProvider().getClicksRoute(this)
    }

    override fun OnSuccessMax(route: String) {
        routeMaxClicks.postValue(route)
    }

    override fun OnErrorGetStat(errorType: String) {
        routeMaxClicks.postValue("No se pudo obtener")
    }

    // Route min clicks
    fun requestRouteMinClicks(){
        AdminProvider().getRouteMinClicks(this)
    }

    override fun OnSuccessMin(route: String) {
        routeMinClicks.postValue(route)
    }

    override fun OnErrorGetStatMin(errorType: String) {
        routeMaxClicks.postValue("No se pudo obtener")
    }
    //..
    // Total clicks
    fun requestTotalClicks(){
        AdminProvider().getTotalClicksCount(this)
    }

    override fun OnSuccessTotal(clicks: Int) {
        totalClicks.postValue(clicks)
    }

    override fun OnErrorGetTotalStat(errorType: String) {
        println("Error al obtener total clicks: $errorType")
    }

    //..
    // Daily starts
    fun requestDailyStarts(){
        AdminProvider().getTodayAppStartCount(this)
    }

    override fun OnGetDailyStarts(dailyStarts: String) {
        todayStarts.postValue(dailyStarts)
    }

    // Total starts
    fun requestTotalStarts(){
        AdminProvider().getTotalAppStarts(this)
    }

    override fun OnGetTotalStarts(total: String) {
        totalStarts.postValue(total)
    }
    //..

}