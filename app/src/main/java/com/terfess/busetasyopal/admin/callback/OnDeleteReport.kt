package com.terfess.busetasyopal.admin.callback

import com.terfess.busetasyopal.enums.FirebaseErrors

interface OnDeleteReport {
    fun OnSuccesTask()
    fun OnErrorDelete(error: FirebaseErrors)
}