package com.terfess.busetasyopal.admin.callback

import com.terfess.busetasyopal.enums.FirebaseEnums

interface AddRoute {
    fun onAddSuccess()
    fun onAddRouteError(error:FirebaseEnums)

    interface LastRoute {
        fun onGetSuccess(idLast: String?)
        fun onGetLastRouteError(error: FirebaseEnums)
    }
}

