package com.terfess.busetasyopal.admin.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.terfess.busetasyopal.admin.callback.OnLoginFirebase
import com.terfess.busetasyopal.enums.FirebaseErrors
import com.terfess.busetasyopal.services.AuthFirebase

class LoginAdminVM : ViewModel(), OnLoginFirebase {
    val loggedSucces = MutableLiveData<Boolean>()

    fun loginAdmin(email: String, password: String) {
        AuthFirebase().loginEmailPassword(this, email, password)
    }

    override fun onSucces() {
        loggedSucces.postValue(true)
    }

    override fun onErrorLogin(error: FirebaseErrors) {
        TODO("Not yet implemented")
    }
}