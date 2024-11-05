package com.terfess.busetasyopal.actividades.reports.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.terfess.busetasyopal.actividades.reports.model.ReportFunctions
import com.terfess.busetasyopal.admin.model.DatoReport
import com.terfess.busetasyopal.callbacks.user_reports.OnGetMyReports
import com.terfess.busetasyopal.enums.FirebaseEnums

class ViewModelReports:ViewModel(),
    OnGetMyReports{
    val myReports = MutableLiveData<List<DatoReport>>()

    fun getReports(){
        ReportFunctions().getUserReports(this)
    }
    override fun onMySuccess(data: List<DatoReport>) {
        myReports.postValue(data)
    }

    override fun onErrorGet(error: FirebaseEnums) {
        myReports.postValue(emptyList())
    }


}