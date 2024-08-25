package com.terfess.busetasyopal.admin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.terfess.busetasyopal.admin.callback.OnDeleteReport
import com.terfess.busetasyopal.admin.callback.OnGetReports
import com.terfess.busetasyopal.admin.model.AdminProvider
import com.terfess.busetasyopal.admin.model.DatoReport
import com.terfess.busetasyopal.enums.FirebaseErrors

class ReportViewModel : ViewModel(),
    OnGetReports,
    OnDeleteReport {
    var reports = MutableLiveData<List<DatoReport>>()
    var errorReports = MutableLiveData<FirebaseErrors>()
    var resultDeleteReport = MutableLiveData<Boolean>()

    // start get reports
    fun requestReports() {
        AdminProvider().getReports(this)
    }

    override fun onSucces(data: List<DatoReport>) {
        //reports obtained
        reports.postValue(data)
    }

    override fun onErrorGetR(error: FirebaseErrors) {
        errorReports.postValue(error)
    }

    //end get reports

    //start delete Report

    fun requestDeleteReport(idReport: String) {
        AdminProvider().deleteReport(this, idReport)
    }

    override fun OnSuccesTask() {
        resultDeleteReport.postValue(true)
    }

    override fun OnErrorDelete(error: FirebaseErrors) {
        resultDeleteReport.postValue(false)
    }
}