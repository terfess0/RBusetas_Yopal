package com.terfess.busetasyopal

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

interface DatosDeFirebaseCallback {
    fun onDatosRecibidos(listaCoorPrimParte: MutableList<LatLng>, listaCoorSegParte: MutableList<LatLng>)
}
interface allDatosRutas{
    fun todosDatosRecibidos(listaCoorPrimParte: MutableList<List<LatLng>>)
}

class DatosDeFirebase {

    private var baseDatosFirebaseDatabase = FirebaseDatabase.getInstance()

    fun recibirCoorPrimeraParte(idruta: Int, callback: DatosDeFirebaseCallback) {
        val listaCoorPrimParte = mutableListOf<LatLng>()

        baseDatosFirebaseDatabase.getReference("features/0/rutas/$idruta/salida")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (childSnapshot in dataSnapshot.children) {
                        val lat = childSnapshot.child("1").getValue(Double::class.java)
                        val lng = childSnapshot.child("0").getValue(Double::class.java)

                        val latValue = lat ?: 0.0
                        val lngValue = lng ?: 0.0

                        val ubicacion = LatLng(latValue, lngValue)
                        listaCoorPrimParte.add(ubicacion)
                    }
                    // Llamamos al método callback cuando los datos están listos
                    callback.onDatosRecibidos(listaCoorPrimParte, mutableListOf())//se reemplaza el faltante con lista vacia
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores si los hay
                    println("Algo salió mal en la creación de la llegada ruta $idruta.")
                }
            })

        val listaCoorSegParte = mutableListOf<LatLng>()

        baseDatosFirebaseDatabase.getReference("features/0/rutas/$idruta/llegada")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (childSnapshot in dataSnapshot.children) {
                        val lat = childSnapshot.child("1").getValue(Double::class.java)
                        val lng = childSnapshot.child("0").getValue(Double::class.java)

                        val latValue = lat ?: 0.0
                        val lngValue = lng ?: 0.0

                        val ubicacion = LatLng(latValue, lngValue)
                        listaCoorSegParte.add(ubicacion)
                    }
                    // Llamamos al método callback cuando los datos están listos
                    callback.onDatosRecibidos(mutableListOf(), listaCoorSegParte)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores si los hay
                    println("Algo salió mal en la creación de la llegada ruta $idruta.")
                }
            })
    }


    fun descargarInformacion(callback: allDatosRutas, index: Int = 0, listaCompleta: MutableList<List<LatLng>> = mutableListOf()) {
        val idRuta = intArrayOf(2, 3, 6, 7, 8, 9, 10, 11, 13)
        if (index < idRuta.size) {
            recibirCoorPrimeraParte(idRuta[index], object : DatosDeFirebaseCallback {
                override fun onDatosRecibidos(listaCoorPrimParte: MutableList<LatLng>, listaCoorSegParte: MutableList<LatLng>) {
                    val listaUnida = listaCoorPrimParte + listaCoorSegParte
                    println("Datos recibidos para ruta ${idRuta[index]}")
                    listaCompleta.add(listaUnida)
                    descargarInformacion(callback, index + 1, listaCompleta)
                }
            })
        } else {
            callback.todosDatosRecibidos(listaCompleta)
        }
    }
}
