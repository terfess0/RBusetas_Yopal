package com.terfess.busetasyopal.admin.callback.updateRoute

import com.terfess.busetasyopal.enums.FirebaseEnums

interface ChangeStatusEnabled {
    fun onSuccessChange()
    fun onErrorChange(error: FirebaseEnums)
}