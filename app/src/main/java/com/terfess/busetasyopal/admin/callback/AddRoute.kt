package com.terfess.busetasyopal.admin.callback

import com.terfess.busetasyopal.enums.FirebaseEnums

interface AddRoute {
    fun onAddSuccess()
    fun onAddRouteError(error:FirebaseEnums)
}