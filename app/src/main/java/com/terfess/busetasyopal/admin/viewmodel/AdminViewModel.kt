package com.terfess.busetasyopal.admin.viewmodel

import androidx.lifecycle.ViewModel
import com.terfess.busetasyopal.admin.model.AdminProvider

class AdminViewModel : ViewModel() {
    fun updatePrice(newPrice:String) {
        AdminProvider().setPricePassage(newPrice)
    }

}