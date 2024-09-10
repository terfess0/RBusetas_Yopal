package com.terfess.busetasyopal.admin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.terfess.busetasyopal.admin.callback.UpdatePrice
import com.terfess.busetasyopal.admin.callback.updateInfo.UpVersionInfo
import com.terfess.busetasyopal.admin.model.AdminProvider
import com.terfess.busetasyopal.enums.FirebaseEnums

class AdminViewModel : ViewModel(),
    UpdatePrice,
    UpVersionInfo{
    var onEditPrice = MutableLiveData<Boolean>()
    var onUpVersion = MutableLiveData<Boolean>()

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
}