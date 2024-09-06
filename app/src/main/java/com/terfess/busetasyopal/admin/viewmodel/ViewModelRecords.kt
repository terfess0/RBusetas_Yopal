package com.terfess.busetasyopal.admin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.terfess.busetasyopal.admin.callback.GetRecords
import com.terfess.busetasyopal.admin.model.AdminProvider
import com.terfess.busetasyopal.admin.model.DatoRecord
import com.terfess.busetasyopal.enums.FirebaseEnums

class ViewModelRecords : ViewModel(),
    GetRecords {
    val listRecord = MutableLiveData<List<DatoRecord>>()

    fun requestRecords() {
        AdminProvider().getRecords(this)
    }

    override fun OnSuccesGetRecords(list: List<DatoRecord>) {
        listRecord.postValue(list)
    }

    override fun OnErrorRecord(error: FirebaseEnums) {
        listRecord.postValue(emptyList())
    }
}