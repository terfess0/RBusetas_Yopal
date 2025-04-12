package com.terfess.busetasyopal.services.workers

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.random.Random

class UserResponsesNotificationsFun {
    fun checkNotificationsByFirebaseTokenUser(callback: OnGetCallback) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful || task.result.isNullOrEmpty()) {
                println("Error obteniendo el token de usuario: ${task.exception}")
                return@addOnCompleteListener
            }

            val tokenIdApp = task.result!!
            checkUserNotifications(tokenIdApp, callback)
        }
    }

    private fun checkUserNotifications(tokenIdApp: String, callback: OnGetCallback) {
        val userNotificationsRef = FirebaseDatabase.getInstance()
            .getReference("features/0/users/$tokenIdApp")

        userNotificationsRef.child("pendingResponseNotifications")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.getValue(Boolean::class.java) == true) {
                        println("No hay nuevos registros. pendingResponseNotifications ya está en true.")
                        return
                    }

                    checkResponseNotifications(tokenIdApp, callback)
                    updateNotisCheck(userNotificationsRef)
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error al leer pendingResponseNotifications: ${error.toException()}")
                }
            })
    }

    private fun checkResponseNotifications(
        tokenIdApp: String,
        callback: OnGetCallback
    ) {
        val responseRef = FirebaseDatabase.getInstance()
            .getReference("features/0/reportsModule/reportResponses")

        val firstQuery = responseRef
            .orderByChild("idUser")
            .equalTo(tokenIdApp)

        firstQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val elementos = snapshot.children.mapNotNull { child ->
                    val statusChecked =
                        child.child("statusCheckedReceivedNoti").getValue(Boolean::class.java)
                            ?: true
                    if (!statusChecked) {
                        val titulo = "Respuesta Reporte"
                        val mensaje = child.child("textResponse").getValue(String::class.java)
                        val key = child.key

                        if (mensaje != null && key != null) Triple(key, titulo, mensaje)
                        else null
                    } else {
                        null
                    }
                }

                if (elementos.isEmpty()) {
                    println("No hay elementos con statusCheckedReceivedNoti = false")
                    return
                }

                println("Elementos a actualizar: $elementos")
                callback.onCallback(elementos)
                updateResponseStatus(elementos, responseRef)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error al leer reports: ${error.toException()}")
            }
        })

    }

    private fun updateResponseStatus(
        elementos: List<Triple<String, String, String>>,
        responseRef: DatabaseReference
    ) {
        elementos.forEach { (key, _, _) ->
            responseRef.child(key).child("statusCheckedReceivedNoti")
                .setValue(true)
                .addOnSuccessListener {
                    println("statusCheckedView actualizado en: $key")
                }
                .addOnFailureListener {
                    println("Error al actualizar statusCheckedView en $key: $it")
                }
        }
    }

    private fun updateNotisCheck(userNotificationsRef: DatabaseReference) {
        userNotificationsRef.child("pendingResponseNotifications").setValue(true)
            .addOnSuccessListener {
                println("Se ha actualizado pendingResponseNotifications a true")
            }
            .addOnFailureListener {
                println("Error al actualizar pendingResponseNotifications: $it")
            }
    }
}