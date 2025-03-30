package com.terfess.busetasyopal.admin.callback

import com.terfess.busetasyopal.modelos_dato.reports_system.DatoReport
import com.terfess.busetasyopal.enums.FirebaseEnums

interface OnGetReports {
    fun onSucces(data: List<DatoReport>)
    fun onErrorGetR(error : FirebaseEnums)
}