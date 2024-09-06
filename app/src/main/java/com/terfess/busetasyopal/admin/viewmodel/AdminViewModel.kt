package com.terfess.busetasyopal.admin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.terfess.busetasyopal.admin.callback.UpdatePrice
import com.terfess.busetasyopal.admin.model.AdminProvider
import com.terfess.busetasyopal.enums.FirebaseEnums

class AdminViewModel : ViewModel(),
    UpdatePrice {
    var onEditPrice = MutableLiveData<Boolean>()

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

}