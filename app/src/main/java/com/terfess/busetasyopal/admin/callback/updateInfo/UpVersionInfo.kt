package com.terfess.busetasyopal.admin.callback.updateInfo

import com.terfess.busetasyopal.enums.FirebaseEnums

interface UpVersionInfo {
    fun onSuccessUp()
    fun onErrorUp(error: FirebaseEnums)
}