package com.terfess.busetasyopal.admin.callback.pricePassage

import com.terfess.busetasyopal.enums.FirebaseEnums

interface UpdatePrice {
    fun onSuccesPrice()
    fun onErrorPriceUp(error:FirebaseEnums)
}