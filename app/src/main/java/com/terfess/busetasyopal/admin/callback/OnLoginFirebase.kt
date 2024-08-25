package com.terfess.busetasyopal.admin.callback

import com.terfess.busetasyopal.enums.FirebaseErrors

interface OnLoginFirebase {
    fun onSucces()
    fun onErrorLogin(error: FirebaseErrors)
}