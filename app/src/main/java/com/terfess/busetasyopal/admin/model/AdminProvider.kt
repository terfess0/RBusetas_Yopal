package com.terfess.busetasyopal.admin.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.terfess.busetasyopal.admin.callback.OnDeleteReport
import com.terfess.busetasyopal.admin.callback.OnGetAllData
import com.terfess.busetasyopal.admin.callback.OnGetReports
import com.terfess.busetasyopal.enums.FirebaseErrors

class AdminProvider : ViewModel() {
    private var dataBaseFirebase = FirebaseDatabase.getInstance()

    fun getInfo(callback: OnGetAllData) {
        dataBaseFirebase.getReference("features/0/rutas")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val data = mutableListOf<DatoRuta>()
                    for (childSnapshot in dataSnapshot.children) {
                        val itRoute = childSnapshot.getValue(DatoRuta::class.java)
                        data.add(itRoute!!)
                    }

                    callback.OnSuccessGet(data)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores si los hay
                    println("Algo salió mal: ${databaseError.message}")
                    callback.onErrorGet(FirebaseErrors.ERROR_NOT_EXISTS)
                }
            })

    }

    fun setPricePassage(newPrice: String) {

        // Referencia a la ruta específica en la base de datos
        val refPrice = "/features/0/precio"
        println("ruta precio: $refPrice")
        val referencia = dataBaseFirebase.getReference(refPrice)

        // Actualizar el valor en la base de datos
        referencia.setValue(newPrice)
            .addOnSuccessListener {
                // Éxito en la actualización
                Log.d("Firebase", "Valor actualizado exitosamente en la ruta:")
            }
            .addOnFailureListener { error ->
                // Manejar errores
                Log.e("Firebase", "Error al actualizar el valor en la ruta:", error)
            }
    }

    fun getReports(callback: OnGetReports) {
        dataBaseFirebase.getReference("features/0/reportesNew")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val data = mutableListOf<DatoReport>()
                    for (childSnapshot in dataSnapshot.children) {
                        // Obtener el ID del documento (nodo hijo)
                        val id = childSnapshot.key

                        // Obtener los datos del documento y asignar el ID
                        val report = childSnapshot.getValue(DatoReport::class.java)
                        if (report != null) {
                            // save the 'idDoc'
                            report.id = id!!
                            data.add(report)
                        }
                    }

                    callback.onSucces(data)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores si los hay
                    println("Algo salió mal: ${databaseError.message}")
                    callback.onErrorGetR(FirebaseErrors.ERROR_NOT_EXISTS)
                }
            })
    }

    fun deleteReport(callback: OnDeleteReport, idFieldReport:String){
        // referencia al documento en Firebase
        val databaseReference = dataBaseFirebase.getReference("features/0/reportesNew")

        val updates = hashMapOf<String, Any?>(
            idFieldReport to null
        )

        // actualizar el documento eliminando el campo
        databaseReference.updateChildren(updates)
            .addOnSuccessListener {
                // Operación exitosa
                callback.OnSuccesTask()
                Log.d("Firebase", "Campo eliminado exitosamente.")
            }
            .addOnFailureListener { e ->
                // Error al eliminar el campo
                Log.e("Firebase", "Error al eliminar el campo.", e)
            }
    }
}