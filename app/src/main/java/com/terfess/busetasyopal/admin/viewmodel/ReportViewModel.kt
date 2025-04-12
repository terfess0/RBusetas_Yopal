package com.terfess.busetasyopal.admin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.terfess.busetasyopal.actividades.reports.model.ReportFunctions
import com.terfess.busetasyopal.admin.callback.OnDeleteReport
import com.terfess.busetasyopal.admin.callback.OnDeleteResponse
import com.terfess.busetasyopal.admin.callback.OnGetReports
import com.terfess.busetasyopal.admin.callback.OnReplyReport
import com.terfess.busetasyopal.admin.model.AdminProvider
import com.terfess.busetasyopal.callbacks.user_reports.OnGetResponsesReport
import com.terfess.busetasyopal.modelos_dato.reports_system.DatoReport
import com.terfess.busetasyopal.enums.FirebaseEnums
import com.terfess.busetasyopal.modelos_dato.reports_system.ResponseReportDato

class ReportViewModel : ViewModel(),
    OnGetReports,
    OnDeleteReport,
    OnReplyReport,
    OnGetResponsesReport,
    OnDeleteResponse {
    var reports = MutableLiveData<List<DatoReport>>()
    var errorReports = MutableLiveData<FirebaseEnums>()
    var resultDeleteReport = MutableLiveData<Boolean>()
    var resultDeleteResponse = MutableLiveData<Boolean>()
    var resultReplyReport = MutableLiveData<Boolean>()
    var errorReplyReport: String = ""
    var alertOnScreen = MutableLiveData<String>()

    val myResponses = MutableLiveData<List<ResponseReportDato>>()

    // start get reports
    fun requestReports() {
        AdminProvider().getReports(this)
    }

    override fun onSucces(data: List<DatoReport>) {
        //reports obtained
        reports.postValue(data)
    }

    override fun onErrorGetR(error: FirebaseEnums) {
        errorReports.postValue(error)
    }

    //end get reports

    //start delete Report
    fun requestDeleteReport(idReport: String, hasResponse: Boolean) {
        AdminProvider().deleteReport(this, idReport, hasResponse)
    }

    override fun OnSuccesTask() {
        resultDeleteReport.postValue(true)
    }

    override fun OnErrorDelete(error: FirebaseEnums) {
        resultDeleteReport.postValue(false)
    }

    //start delete Report
    fun requestDeleteResponse(idResponse: String, txtResponse: String) {
        AdminProvider().deleteResponse(this, idResponse, txtResponse)
    }

    override fun onDeleteResponse(result: Boolean) {
        resultDeleteResponse.postValue(result)
    }
    //..

    //Reply reports
    fun requestSendReply(idReport: String, reply: String, origin: String) {
        AdminProvider().replyReport(this, idReport, reply, origin)
    }

    override fun OnSuccessReply() {
        resultReplyReport.postValue(true)
    }

    override fun OnErrorReply(error: FirebaseEnums) {
        resultReplyReport.postValue(false)
        errorReplyReport = error.toString()
    }
    //..

    // set alert (snackbar) to screen
    fun setAlert(message: String) {
        alertOnScreen.postValue(message)
    }
    //..

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
}