package com.terfess.busetasyopal.actividades.reports.model

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.terfess.busetasyopal.modelos_dato.reports_system.DatoReport
import com.terfess.busetasyopal.callbacks.user_reports.OnGetMyReports
import com.terfess.busetasyopal.callbacks.user_reports.OnGetResponsesReport
import com.terfess.busetasyopal.callbacks.user_reports.OnMarkAsSeenResponses
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.enums.FirebaseEnums
import com.terfess.busetasyopal.modelos_dato.reports_system.ResponseReportDato

class ReportFunctions {
    private var firebase = FirebaseDatabase.getInstance()

    fun getUserReports(callback: OnGetMyReports) {
        var userUid = ""

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userUid = task.result

                firebase.getReference("features/0/reportsModule/reportsUsers")
                    .orderByChild("origin")
                    .equalTo(userUid)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val data = mutableListOf<DatoReport>()

                            for (childSnapshot in dataSnapshot.children) {
                                val report = childSnapshot.getValue(DatoReport::class.java)
                                report?.id =
                                    childSnapshot.key ?: ""  // Asigna el ID del documento

                                data.add(report!!)

                            }

                            val sortedData = data.sortedByDescending { it.dateReport }
                            callback.onMySuccess(sortedData)
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Manejar errores si los hay
                            val errorType =
                                UtilidadesMenores().handleFirebaseError(databaseError.toException())
                            callback.onErrorGet(errorType)

                            println("Algo salió mal: ${databaseError.message}")
                            callback.onErrorGet(FirebaseEnums.ERROR_NOT_EXISTS)
                        }
                    })
            }
        }
    }

    fun getResponsesReport(callback: OnGetResponsesReport, idReport: String) {
        var userUid = ""

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userUid = task.result

                firebase.getReference("features/0/users/$userUid/reportResponses")
                    .orderByChild("idReport")
                    .equalTo(idReport)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val data = mutableListOf<ResponseReportDato>()

                            for (childSnapshot in dataSnapshot.children) {
                                val response =
                                    childSnapshot.getValue(ResponseReportDato::class.java)
                                response?.idResponse =
                                    childSnapshot.key ?: ""  // Asigna el ID del documento

                                data.add(response!!)

                            }

                            val sortedData = data.sortedByDescending { it.dateResponse }
                            callback.onMyResponsesSuccess(sortedData)
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Manejar errores si los hay
                            val errorType =
                                UtilidadesMenores().handleFirebaseError(databaseError.toException())
                            callback.onErrorGetResp(errorType)
                        }
                    })
            }
        }
    }

    // START SETTERS ------------------
    fun changeAllNotisUserViewCheckedStatus(callback: OnMarkAsSeenResponses) {

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result

                val reportResponsesRef =
                    firebase.getReference("features/0/users/${task.result}/reportResponses")


                val query = reportResponsesRef
                    .orderByChild("statusCheckedSeenNoti")
                    .equalTo(false)


                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Mapa para actualización masiva (mejor rendimiento)
                        val updates = HashMap<String, Any>()

                        for (responseSnapshot in snapshot.children) {
                            updates["${responseSnapshot.key}/statusCheckedSeenNoti"] = true
                        }

                        // 3. Actualización atómica (una sola operación de red)
                        if (updates.isNotEmpty()) {
                            reportResponsesRef.updateChildren(updates)
                                .addOnSuccessListener {
                                    callback.onSuccessMarkAsSeen()
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseUpdate", "Error de lectura: ${error.message}")
                    }
                })
            }
        }
    }
}