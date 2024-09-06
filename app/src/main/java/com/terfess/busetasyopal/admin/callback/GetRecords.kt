package com.terfess.busetasyopal.admin.callback

import com.terfess.busetasyopal.admin.model.DatoRecord
import com.terfess.busetasyopal.enums.FirebaseEnums

interface GetRecords {
    fun OnSuccesGetRecords(list: List<DatoRecord>)
    fun OnErrorRecord(error: FirebaseEnums)
}