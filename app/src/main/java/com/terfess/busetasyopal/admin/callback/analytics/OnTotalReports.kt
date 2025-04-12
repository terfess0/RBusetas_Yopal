package com.terfess.busetasyopal.admin.callback.analytics

import com.terfess.busetasyopal.enums.FirebaseEnums

interface OnTotalReports {
    fun OnSuccessTotalReports(numReports:Int)
    fun OnErrorTotalReports(errorType: FirebaseEnums)
}