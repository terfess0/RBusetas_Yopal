package com.terfess.busetasyopal.admin.callback

import com.terfess.busetasyopal.enums.FirebaseEnums

interface UpdatePrice {
    fun onSuccesPrice()
    fun onErrorPriceUp(error:FirebaseEnums)
}