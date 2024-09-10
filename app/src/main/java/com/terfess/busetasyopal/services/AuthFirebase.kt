package com.terfess.busetasyopal.services

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import com.terfess.busetasyopal.admin.callback.OnLoginFirebase
import com.terfess.busetasyopal.enums.FirebaseEnums

class AuthFirebase {
    private var auth: FirebaseAuth = Firebase.auth
    fun loginEmailPassword(callback: OnLoginFirebase, email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //sign in success
                    callback.onSucces()
                    Log.i("OnLoginFirebase", "Inicio correctamente")
                } else {
                    //callback fail
                    when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            // Credenciales de autenticación inválidas, como una contraseña incorrecta
                            callback.onErrorLogin(FirebaseEnums.ERROR_CREDENTIAL)
                            Log.e("OnLoginFirebase", "Error de credenciales")

                        }

                        is FirebaseAuthInvalidUserException -> {
                            // El usuario no existe o fue deshabilitado
                            callback.onErrorLogin(FirebaseEnums.NO_EXIST_OR_DISABLED)
                            Log.e("OnLoginFirebase", "El usuario no existe o esta deshabilitado")
                        }

                        else -> {
                            callback.onErrorLogin(FirebaseEnums.ERROR_AUTH)
                            Log.e("OnLoginFirebase", "Algun error desconocido")
                        }
                    }
                }
            }
    }

    fun isUserLoggedIn(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser != null
    }

    fun logOutSessionUser(){
        val currentUser = auth.currentUser
        if (currentUser != null) {
            auth.signOut()
            println("Session cerrada")
        }
    }
}