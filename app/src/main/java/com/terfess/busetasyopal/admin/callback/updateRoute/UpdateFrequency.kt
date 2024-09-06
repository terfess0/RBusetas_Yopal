package com.terfess.busetasyopal.admin.callback.updateRoute

import com.terfess.busetasyopal.enums.FirebaseEnums

interface UpdateFrequency {
    fun onSuccess()
    fun onErrorFrec(error: FirebaseEnums)
}