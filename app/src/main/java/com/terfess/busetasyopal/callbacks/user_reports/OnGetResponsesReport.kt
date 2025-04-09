package com.terfess.busetasyopal.callbacks.user_reports

import com.terfess.busetasyopal.enums.FirebaseEnums
import com.terfess.busetasyopal.modelos_dato.reports_system.ResponseReportDato

interface OnGetResponsesReport {
    fun onMyResponsesSuccess(data: List<ResponseReportDato>)
    fun onErrorGetResp(error: FirebaseEnums)
}