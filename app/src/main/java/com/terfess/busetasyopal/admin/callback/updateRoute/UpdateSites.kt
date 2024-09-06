package com.terfess.busetasyopal.admin.callback.updateRoute

import com.terfess.busetasyopal.enums.FirebaseEnums

interface UpdateSites {
    fun OnSucces()
    fun OnErrorSites(error: FirebaseEnums)
}