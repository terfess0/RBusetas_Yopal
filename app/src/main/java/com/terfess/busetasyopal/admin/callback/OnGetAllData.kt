package com.terfess.busetasyopal.admin.callback

import com.terfess.busetasyopal.admin.model.DatoRuta
import com.terfess.busetasyopal.enums.FirebaseEnums

interface OnGetAllData {
    fun OnSuccessGet(dataList: MutableList<DatoRuta>)
    fun onErrorGet(error: FirebaseEnums)
}