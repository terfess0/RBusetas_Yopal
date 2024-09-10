package com.terfess.busetasyopal.admin.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.terfess.busetasyopal.admin.callback.AddRoute
import com.terfess.busetasyopal.admin.callback.GetRecords
import com.terfess.busetasyopal.admin.callback.OnDeleteReport
import com.terfess.busetasyopal.admin.callback.OnGetAllData
import com.terfess.busetasyopal.admin.callback.OnGetReports
import com.terfess.busetasyopal.admin.callback.UpdatePrice
import com.terfess.busetasyopal.admin.callback.updateInfo.UpVersionInfo
import com.terfess.busetasyopal.admin.callback.updateRoute.ChangeStatusEnabled
import com.terfess.busetasyopal.admin.callback.updateRoute.UpdateFrequency
import com.terfess.busetasyopal.admin.callback.updateRoute.UpdateSchedule
import com.terfess.busetasyopal.admin.callback.updateRoute.UpdateSites
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.enums.FirebaseEnums
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdminProvider : ViewModel() {
    private var dataBaseFirebase = FirebaseDatabase.getInstance()
    private var firebaseAuth = FirebaseAuth.getInstance()

    //GETTERS-------------------------
    fun getInfo(callback: OnGetAllData) {
        CoroutineScope(Dispatchers.IO).launch {
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
                        val errorType =
                            UtilidadesMenores().handleFirebaseError(databaseError.toException())
                        callback.onErrorGet(errorType)

                        println("Algo salió mal: ${databaseError.message}")
                        callback.onErrorGet(FirebaseEnums.ERROR_NOT_EXISTS)
                    }
                })

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

                    val sortedData = data.sortedByDescending { it.dateReport }
                    callback.onSucces(sortedData)

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores si los hay
                    val errorType =
                        UtilidadesMenores().handleFirebaseError(databaseError.toException())
                    callback.onErrorGetR(errorType)

                    println("Algo salió mal: ${databaseError.message}")
                    callback.onErrorGetR(FirebaseEnums.ERROR_NOT_EXISTS)
                }
            })
    }

    fun getRecords(callback: GetRecords) {
        dataBaseFirebase.getReference("features/0/administradores/registros")
            .orderByChild("fecha")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val data = mutableListOf<DatoRecord>()
                    for (childSnapshot in dataSnapshot.children) {

                        // Obtener los datos del documento y asignar el ID
                        val record = childSnapshot.getValue(DatoRecord::class.java)
                        if (record != null) {
                            // save to list
                            data.add(record)
                        }
                    }

                    val sortedData = data.sortedByDescending { it.fecha }
                    callback.OnSuccesGetRecords(sortedData)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores si los hay
                    val errorType =
                        UtilidadesMenores().handleFirebaseError(databaseError.toException())
                    callback.OnErrorRecord(errorType)

                    println("Algo salió mal: ${databaseError.message}")
                    callback.OnErrorRecord(FirebaseEnums.ERROR_NOT_EXISTS)
                }
            })
    }
    //END GETTERS----------------

    //SETTERS------------------
    fun setPricePassage(callback: UpdatePrice, newPrice: String) {

        // Reference to price in database
        val refPrice = "/features/0/precio"
        val referencia = dataBaseFirebase.getReference(refPrice)

        // update price
        referencia.setValue(newPrice)
            .addOnSuccessListener {
                // Éxito en la actualización
                Log.d("Firebase", "Precio actualizado exitosamente:")
                val txtAction = "Actualizó el precio del pasaje a [$newPrice] :: AdminProvider"
                saveActionRegist(txtAction)
            }
            .addOnFailureListener { error ->
                // Manejar errores
                val errorType = UtilidadesMenores().handleFirebaseError(error)
                callback.onErrorPriceUp(errorType)

                Log.e("Firebase", "Error al actualizar el precio pasaje:", error)
            }
    }

    fun setEnabledRoute(callback: ChangeStatusEnabled, idRuta: Int, enabled: Boolean) {

        // ref to specific route data
        val refEnabled = "/features/0/rutas/$idRuta/enabled"
        val referencia = dataBaseFirebase.getReference(refEnabled)

        // Actualizar el valor en la base de datos
        referencia.setValue(enabled)
            .addOnSuccessListener {
                // Éxito en la actualización
                callback.onSuccessChange()

                Log.d("Firebase", "Estado actualizado exitosamente en la ruta $idRuta a $enabled:")
                val txtAction =
                    "Actualizó el estado de la ruta $idRuta a [$enabled] :: AdminProvider"
                saveActionRegist(txtAction)
            }
            .addOnFailureListener { error ->
                // Manejar errores
                val errorType = UtilidadesMenores().handleFirebaseError(error)
                callback.onErrorChange(errorType)

                Log.e("Firebase", "Error al actualizar el estado en la ruta $idRuta:", error)
            }
    }


    private fun saveActionRegist(action: String) {
        // Obtener la referencia a la base de datos
        val database = dataBaseFirebase.getReference("features/0/administradores")
        val emailAdmin = firebaseAuth.currentUser?.email

        // Referencia al nodo donde se almacenarán los registros
        val registrosRef = database.child("registros")

        // Obtener una nueva clave para cada registro
        val nuevaClave = registrosRef.push().key ?: return

        val currentDate: Date = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaFormateada: String = dateFormat.format(currentDate)

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val horaFormateada: String = timeFormat.format(currentDate)

        val regist = "$emailAdmin -> $action"
        // Crear un mapa para almacenar los datos en Firebase
        val datosMap = mapOf(
            "registro" to regist,
            "fecha" to fechaFormateada,
            "hora" to horaFormateada
        )

        // Guardar los datos en Firebase usando la nueva clave
        registrosRef.child(nuevaClave).setValue(datosMap)
            .addOnSuccessListener {
                println("Registro guardado.")
            }
            .addOnFailureListener { exception ->
                println("Error al agregar datos: ${exception.message}")
            }
    }


    fun setNewRoute(callback: AddRoute, newRoute: DatoRuta) {
        val pointRef = dataBaseFirebase.getReference("features/0/rutas/${newRoute.numRuta[0]}")

        // Verificar si ya existe la ruta
        pointRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                // La ruta ya existe, manejar el error
                callback.onAddRouteError(FirebaseEnums.ROUTE_ALREADY_EXISTS)
            } else {
                // La ruta no existe, proceder a agregarla
                pointRef.setValue(newRoute)
                    .addOnSuccessListener {
                        // Éxito al agregar la ruta
                        callback.onAddSuccess()
                    }
                    .addOnFailureListener { error ->
                        // Manejo de error al guardar los datos
                        val errorType = UtilidadesMenores().handleFirebaseError(error)
                        callback.onAddRouteError(errorType)

                        Log.e("FirebaseError", "Error al guardar los datos", error)
                    }
            }
        }.addOnFailureListener { error ->
            // Manejo de error en la consulta
            val errorType = UtilidadesMenores().handleFirebaseError(error)
            callback.onAddRouteError(errorType)

            Log.e("FirebaseError", "Error al verificar la existencia de la ruta", error)
        }
    }


    //END SETTERS------------

    //DELETES------------------
    fun deleteReport(callback: OnDeleteReport, idFieldReport: String) {
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
                Log.d("Firebase", "Reporte eliminado exitosamente.")

                val txtAction = "Borró un reporte :: AdminProvider"
                saveActionRegist(txtAction)
            }
            .addOnFailureListener { e ->
                // Error al eliminar el campo
                val errorType = UtilidadesMenores().handleFirebaseError(e)
                callback.OnErrorDelete(errorType)

                Log.e("Firebase", "Error al eliminar el campo.", e)
            }
    }

    //END DELETES------------------

    //UPDATES-----------------

    fun updateSitesRoute(callback: UpdateSites, idRuta: Int, sitiosNew: String) {
        // referencia al documento en Firebase
        val databaseReference = dataBaseFirebase.getReference("features/0/rutas/$idRuta")

        val updates = hashMapOf<String, Any?>(
            "sitios" to sitiosNew
        )

        // actualizar el documento eliminando el campo
        databaseReference.updateChildren(updates)
            .addOnSuccessListener {
                // Operación exitosa
                callback.OnSucces()
                Log.d("Firebase", "Campo sitios actualizado exitosamente.")

                val txtAction = "Actualizó los sitios de la ruta #$idRuta :: AdminProvider"
                saveActionRegist(txtAction)
            }
            .addOnFailureListener { e ->
                // Error al eliminar el campo
                val errorType = UtilidadesMenores().handleFirebaseError(e)
                callback.OnErrorSites(errorType)

                Log.e("Firebase", "Error al actualizar sitios ruta $idRuta.", e)
            }
    }

    fun updateFrequencyRuta(
        callback: UpdateFrequency,
        idRuta: Int,
        frecNew: String,
        fieldName: String
    ) {
        // referencia al documento en Firebase
        val databaseReference = dataBaseFirebase.getReference("features/0/rutas/$idRuta")

        val updates = hashMapOf<String, Any?>(
            fieldName to frecNew
        )

        // actualizar el documento eliminando el campo
        databaseReference.updateChildren(updates)
            .addOnSuccessListener {
                // Operación exitosa
                callback.onSuccess()
                Log.d("Firebase", "Campo $fieldName actualizado exitosamente.")

                val txtAction =
                    "Actualizó $fieldName de la ruta #$idRuta [$frecNew] :: AdminProvider"
                saveActionRegist(txtAction)
            }
            .addOnFailureListener { e ->
                // Error al eliminar el campo
                val errorType = UtilidadesMenores().handleFirebaseError(e)
                callback.onErrorFrec(errorType)
                Log.e("Firebase", "Error al actualizar $fieldName ruta $idRuta.", e)
            }
    }

    fun updateSchedule(
        callback: UpdateSchedule,
        idRuta: Int,
        horInicioNew: String,
        horFinNew: String,
        field: String
    ) {
        // referencia al documento en Firebase
        val databaseReference =
            dataBaseFirebase.getReference("features/0/rutas/$idRuta/$field")

        val updates = hashMapOf<String, Any?>(
            "0" to horInicioNew,
            "1" to horFinNew
        )

        // actualizar el documento eliminando el campo
        databaseReference.updateChildren(updates)
            .addOnSuccessListener {
                // Operación exitosa
                callback.onSuccessSH()
                Log.d("Firebase", "Campo $field actualizado exitosamente.")

                val txtAction =
                    "Actualizó $field de la ruta #$idRuta por [$horInicioNew - $horFinNew] :: AdminProvider"
                saveActionRegist(txtAction)
            }
            .addOnFailureListener { e ->
                // Error al eliminar el campo
                val errorType = UtilidadesMenores().handleFirebaseError(e)
                callback.onErrorSH(errorType)

                Log.e("Firebase", "Error al actualizar $field ruta $idRuta.", e)
            }
    }

    fun updateVersionData(callback: UpVersionInfo) {
        val pointRef = dataBaseFirebase.getReference("features/0/version")

        // get the actual num version
        pointRef.get().addOnSuccessListener { dataSnapshot ->

            val currentVersion = dataSnapshot.getValue(Int::class.java) ?: 1
            val newVersion = currentVersion + 1

            pointRef.setValue(newVersion)
                .addOnSuccessListener {
                    // Succes on update
                    callback.onSuccessUp()
                    val txtAction =
                        "Actualizó la versión de los datos de Version Old = [$currentVersion] a New Version = [$newVersion] :: AdminProvider"
                    saveActionRegist(txtAction)

                }
                .addOnFailureListener { error ->
                    // error
                    val errorType = UtilidadesMenores().handleFirebaseError(error)
                    callback.onErrorUp(errorType)

                    Log.e("FirebaseError", "Error al guardar los datos", error)
                }

        }.addOnFailureListener { error ->
            // Manejo de error en la consulta
            val errorType = UtilidadesMenores().handleFirebaseError(error)
            callback.onErrorUp(errorType)

            Log.e("FirebaseError", "Error al verificar la existencia de la ruta", error)
        }
    }
    //END UPDATES

}