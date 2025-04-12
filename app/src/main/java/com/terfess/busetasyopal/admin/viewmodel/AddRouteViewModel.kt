package com.terfess.busetasyopal.admin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.terfess.busetasyopal.admin.callback.AddRoute
import com.terfess.busetasyopal.admin.model.AdminProvider
import com.terfess.busetasyopal.admin.model.DatoRuta
import com.terfess.busetasyopal.enums.FirebaseEnums

class AddRouteViewModel:ViewModel(),
    AddRoute,
AddRoute.LastRoute{
    val resultAdd = MutableLiveData<Boolean>()
    val lastiIdRoute = MutableLiveData<Int>()

    fun requestAddRoute(data:DatoRuta){
        AdminProvider().setNewRoute(this, data)
    }

    override fun onAddSuccess() {
        resultAdd.postValue(true)
    }

    override fun onAddRouteError(error: FirebaseEnums) {
        resultAdd.postValue(false)
    }

    // Get Last id Routes
    fun getLastIdRoute() {
        AdminProvider().getLastIdRoute(this)
    }

    override fun onGetSuccess(idLast: String?) {
        lastiIdRoute.postValue(idLast!!.toString().toInt())
    }

    override fun onGetLastRouteError(error: FirebaseEnums) {
        println("error")
    }
}