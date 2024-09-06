package com.terfess.busetasyopal.admin.callback

import com.terfess.busetasyopal.enums.FirebaseEnums

interface OnDeleteReport {
    fun OnSuccesTask()
    fun OnErrorDelete(error: FirebaseEnums)
}