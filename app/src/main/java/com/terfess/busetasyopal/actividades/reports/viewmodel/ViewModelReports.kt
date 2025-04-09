package com.terfess.busetasyopal.actividades.reports.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.terfess.busetasyopal.actividades.reports.model.ReportFunctions
import com.terfess.busetasyopal.modelos_dato.reports_system.DatoReport
import com.terfess.busetasyopal.callbacks.user_reports.OnGetMyReports
import com.terfess.busetasyopal.callbacks.user_reports.OnGetResponsesReport
import com.terfess.busetasyopal.callbacks.user_reports.OnMarkAsSeenResponses
import com.terfess.busetasyopal.enums.FirebaseEnums
import com.terfess.busetasyopal.modelos_dato.reports_system.ResponseReportDato

class ViewModelReports : ViewModel(),
    OnGetMyReports,
    OnGetResponsesReport,
    OnMarkAsSeenResponses {
    val myReports = MutableLiveData<List<DatoReport>>()
    val myResponses = MutableLiveData<List<ResponseReportDato>>()
    val hasBeenCkeckedResponses = MutableLiveData<Boolean>()
    val countEarringsNotis = MutableLiveData<Int>()

    //..
    fun getReports() {
        ReportFunctions().getUserReports(this)
    }

    override fun onMySuccess(data: List<DatoReport>) {
        myReports.postValue(data)
    }

    override fun onErrorGet(error: FirebaseEnums) {
        myReports.postValue(emptyList())
    }

    //..

    fun getResponsesByReportId(idReport: String) {
        ReportFunctions().getResponsesReport(this, idReport)
    }

    override fun onMyResponsesSuccess(data: List<ResponseReportDato>) {
        myResponses.postValue(data)
    }

    override fun onErrorGetResp(error: FirebaseEnums) {
        println("Error al obtener respuestas: $error")
    }
    //..

    fun byCycleChangeStatusViewNotis() {
        ReportFunctions().changeAllNotisUserViewCheckedStatus(this)
    }

    override fun onSuccessMarkAsSeen() {
        hasBeenCkeckedResponses.postValue(true)
    }
    //..

}