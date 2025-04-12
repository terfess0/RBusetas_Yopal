package com.terfess.busetasyopal.admin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.terfess.busetasyopal.admin.callback.GhostRouteDelete
import com.terfess.busetasyopal.admin.callback.pricePassage.GetPricePassage
import com.terfess.busetasyopal.admin.callback.pricePassage.UpdatePrice
import com.terfess.busetasyopal.admin.callback.updateInfo.UpVersionInfo
import com.terfess.busetasyopal.admin.model.AdminProvider
import com.terfess.busetasyopal.enums.FirebaseEnums

class AdminViewModel : ViewModel(),
    UpdatePrice,
    GetPricePassage,
    UpVersionInfo,
    GhostRouteDelete {
    var onEditPrice = MutableLiveData<Boolean>()
    var onUpVersion = MutableLiveData<Boolean>()
    var currentPricePassage = MutableLiveData<String>()
    var strResultDelGhostRoutes = MutableLiveData<String>()

    // price
    fun updatePrice(newPrice: String) {
        AdminProvider().setPricePassage(this, newPrice)
    }

    override fun onSuccesPrice() {
        onEditPrice.postValue(true)
    }

    override fun onErrorPriceUp(error: FirebaseEnums) {
        onEditPrice.postValue(false)
        println("Error at update price :: -> $error")
    }

    fun requestGetCurrentPrice(){
        AdminProvider().getPricePassage(this)
    }

    override fun onGetPricePassage(price: String) {
        currentPricePassage.postValue("($price)")
    }
    //..

    fun reqUpdateVersionInfo(){
        AdminProvider().updateVersionData(this)
    }

    override fun onSuccessUp() {
        onUpVersion.postValue(true)
    }

    override fun onErrorUp(error: FirebaseEnums) {
        onUpVersion.postValue(true)
        println("Error at update version info :: -> $error")
    }
    //..

    // ..
    fun checkGhostRoutesData(){
        AdminProvider().checkAndDeleteGhostRoutesByDataClicksData(this)
    }

    override fun OnDeleteGhosts(result: String) {
        strResultDelGhostRoutes.postValue(result)
    }

    //..
}