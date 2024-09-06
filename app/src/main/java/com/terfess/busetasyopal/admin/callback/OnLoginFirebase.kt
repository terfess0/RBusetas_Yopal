package com.terfess.busetasyopal.admin.callback

import com.terfess.busetasyopal.enums.FirebaseEnums

interface OnLoginFirebase {
    fun onSucces()
    fun onErrorLogin(error: FirebaseEnums)
}