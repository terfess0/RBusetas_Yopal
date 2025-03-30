package com.terfess.busetasyopal.callbacks.user_reports

import com.terfess.busetasyopal.modelos_dato.reports_system.DatoReport
import com.terfess.busetasyopal.enums.FirebaseEnums

interface OnGetMyReports {
    fun onMySuccess(data: List<DatoReport>)
    fun onErrorGet(error: FirebaseEnums)
}