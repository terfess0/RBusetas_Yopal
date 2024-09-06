package com.terfess.busetasyopal.admin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.terfess.busetasyopal.admin.callback.OnGetAllData
import com.terfess.busetasyopal.admin.callback.updateRoute.ChangeStatusEnabled
import com.terfess.busetasyopal.admin.callback.updateRoute.UpdateFrequency
import com.terfess.busetasyopal.admin.callback.updateRoute.UpdateSchedule
import com.terfess.busetasyopal.admin.callback.updateRoute.UpdateSites
import com.terfess.busetasyopal.admin.model.AdminProvider
import com.terfess.busetasyopal.admin.model.DatoRuta
import com.terfess.busetasyopal.enums.FirebaseEnums

class RoutesViewModel : ViewModel(),
    OnGetAllData,
    UpdateSites,
    UpdateFrequency,
    UpdateSchedule,
    ChangeStatusEnabled{
    var allData = MutableLiveData<MutableList<DatoRuta>>()
    var onEditResult = MutableLiveData<Boolean>()
    var onEditStatusEnabled = MutableLiveData<Boolean>()

    //get all data routes
    fun getAllDataAdmin() {
        AdminProvider().getInfo(this)
    }

    override fun OnSuccessGet(dataList: MutableList<DatoRuta>) {
        //set data to view
        allData.postValue(dataList)
    }

    override fun onErrorGet(error: FirebaseEnums) {
        allData.postValue(mutableListOf())
    }
    //over get data routes

    //start updates route

    fun updateSitiosRuta(idRuta: Int, sitiosNew: String) {
        AdminProvider().updateSitesRoute(this, idRuta, sitiosNew)
    }

    override fun OnSucces() {
        onEditResult.postValue(true)
    }

    override fun OnErrorSites(error: FirebaseEnums) {
        onEditResult.postValue(false)
    }

    fun updateFrecuenciaRuta(idRuta: Int, frecNew: String, fieldName: String) {
        AdminProvider().updateFrequencyRuta(this, idRuta, frecNew, fieldName)
    }

    override fun onSuccess() {
        onEditResult.postValue(true)
    }

    override fun onErrorFrec(error: FirebaseEnums) {
        onEditResult.postValue(false)
    }

    fun updateHorarioRuta(idRuta: Int, horInicioNew: String, horFinNew: String, field: String) {
        AdminProvider().updateSchedule(
            this,
            idRuta,
            horInicioNew,
            horFinNew,
            field
        )
    }

    override fun onSuccessSH() {
        onEditResult.postValue(true)
    }

    override fun onErrorSH(error: FirebaseEnums) {
        onEditResult.postValue(false)
    }

    fun updateStatusEnabled(idRuta: Int, status: Boolean) {
        AdminProvider().setEnabledRoute(this, idRuta, status)
    }
    override fun onSuccessChange() {
        onEditStatusEnabled.postValue(true)
    }

    override fun onErrorChange(error: FirebaseEnums) {
        onEditStatusEnabled.postValue(false)
    }
}