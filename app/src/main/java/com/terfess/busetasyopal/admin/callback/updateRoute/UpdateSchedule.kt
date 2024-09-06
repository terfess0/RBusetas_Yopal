package com.terfess.busetasyopal.admin.callback.updateRoute

import com.terfess.busetasyopal.enums.FirebaseEnums

interface UpdateSchedule {
    fun onSuccessSH()
    fun onErrorSH(error: FirebaseEnums)
}