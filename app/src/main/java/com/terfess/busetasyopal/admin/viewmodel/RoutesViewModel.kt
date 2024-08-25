package com.terfess.busetasyopal.admin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.terfess.busetasyopal.admin.callback.OnGetAllData
import com.terfess.busetasyopal.admin.model.AdminProvider
import com.terfess.busetasyopal.admin.model.DatoRuta
import com.terfess.busetasyopal.enums.FirebaseErrors

class RoutesViewModel : ViewModel(),
    OnGetAllData {
    var allData = MutableLiveData<MutableList<DatoRuta>>()

    //get all data routes
    fun getAllDataAdmin() {
        AdminProvider().getInfo(this)
    }

    override fun OnSuccessGet(dataList: MutableList<DatoRuta>) {
        //set data to view
        allData.postValue(dataList)
        println("Data en viewmodel")
    }

    override fun onErrorGet(error: FirebaseErrors) {
        TODO("Not yet implemented")
    }

    //over get data routes
}