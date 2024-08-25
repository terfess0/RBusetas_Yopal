package com.terfess.busetasyopal.admin.callback

import com.terfess.busetasyopal.admin.model.DatoReport
import com.terfess.busetasyopal.enums.FirebaseErrors

interface OnGetReports {
    fun onSucces(data: List<DatoReport>)
    fun onErrorGetR(error : FirebaseErrors)
}