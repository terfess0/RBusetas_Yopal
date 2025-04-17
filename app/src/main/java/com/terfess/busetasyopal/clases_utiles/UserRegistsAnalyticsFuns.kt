package com.terfess.busetasyopal.clases_utiles

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.messaging.FirebaseMessaging

class UserRegistsAnalyticsFuns {
    private val instUtilidadesMenores = UtilidadesMenores()
    private val dataBaseFirebase = FirebaseDatabase.getInstance()

    fun registLastConectionUser() {
        var tokenIdApp: String
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                tokenIdApp = task.result

                val firebase: FirebaseDatabase = FirebaseDatabase.getInstance()

                val ref: DatabaseReference =
                    firebase.getReference(
                        "features/0/users/$tokenIdApp/lastConnection"
                    )

                val str = obtenerUltimaConexion()
                ref.setValue(str)
            }
        }
    }

    private fun obtenerUltimaConexion(): String {
        val hora = instUtilidadesMenores.getHoraActual()
        val fecha = instUtilidadesMenores.getDate()

        return "Último inicio de conexión a las $hora del $fecha"
    }

    fun registSelectRouteUser(idRoute:Int) {
        var tokenIdApp: String
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                tokenIdApp = task.result

                val firebase: FirebaseDatabase = FirebaseDatabase.getInstance()

                val ref: DatabaseReference =
                    firebase.getReference(
                        "features/0/"
                    )

                // Check if the route exists
                ref.child("rutas/$idRoute").get().addOnSuccessListener {

                    if (!it.exists()) {
                        // Route does not exist
                        return@addOnSuccessListener
                    }

                    val newRef = ref.child("analytics/clicksRouteData/$idRoute/")

                    val z = newRef.child("clicksRegister").push()

                    // Save the click regist
                    val str = mapOf(
                        "id" to z.key,
                        "date" to instUtilidadesMenores.getDate(),
                        "time" to instUtilidadesMenores.getHoraActual(),
                        "idUser" to tokenIdApp,
                        "idRouteClicked" to idRoute
                    )

                    z.setValue(str)

                    // Up the clicks count
                    newRef.child("clicksCount").get().addOnSuccessListener {
                        val y = it.getValue(Int::class.java) ?: 0
                        y.toString().toInt()
                        newRef.child("clicksCount").setValue(y + 1)
                    }
                }
            }
        }
    }

    fun registerDailyAppStart() {
        val today = instUtilidadesMenores.getSimpleDate()

        val refPath = "/features/0/analytics/dailyAppStarts/$today"
        val reference = dataBaseFirebase.getReference(refPath)

        reference.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentCount = currentData.getValue(Int::class.java) ?: 0
                currentData.value = currentCount + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    Log.e("AppStart", "Error al actualizar contador diario", error.toException())
                } else {
                    Log.d("AppStart", "Contador diario actualizado correctamente")
                }
            }
        })
    }

}