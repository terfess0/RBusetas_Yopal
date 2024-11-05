package com.terfess.busetasyopal.actividades.reports.model

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.terfess.busetasyopal.admin.callback.OnGetReports
import com.terfess.busetasyopal.admin.model.DatoReport
import com.terfess.busetasyopal.callbacks.user_reports.OnGetMyReports
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.enums.FirebaseEnums

class ReportFunctions {
    private var firebase = FirebaseDatabase.getInstance()

    fun getUserReports(callback: OnGetMyReports) {
        var userUid = ""

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userUid = task.result

                firebase.getReference("features/0/reportesNew")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val data = mutableListOf<DatoReport>()
                            for (childSnapshot in dataSnapshot.children) {
                                val report = childSnapshot.getValue(DatoReport::class.java)
                                if (report != null && report.origin == userUid) {
                                    report.id =
                                        childSnapshot.key ?: ""  // Asigna el ID del documento
                                    data.add(report)
                                }
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
}