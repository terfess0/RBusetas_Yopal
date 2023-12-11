package com.terfess.busetasyopal.clases_utiles

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.terfess.busetasyopal.modelos_dato.EstructuraDatosBaseDatos


interface DatosDeFirebaseCallback { //callback para detectar que los datos de una ruta han sido recibidos
    fun onDatosRecibidos(
        listaCoorPrimParte: MutableList<LatLng>,
        listaCoorSegParte: MutableList<LatLng>
    )
}

interface allDatosRutas { //callback para detectar que los datos de todas las rutas han sido recibidos
    fun todosDatosRecibidos(listaCompleta: MutableList<EstructuraDatosBaseDatos>)
}

class DatosDeFirebase {

    private var baseDatosFirebaseDatabase = FirebaseDatabase.getInstance()

    fun recibirCoordenadasRuta(idruta: Int, callback: DatosDeFirebaseCallback) {
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
                    callback.onDatosRecibidos(
                        listaCoorPrimParte,
                        mutableListOf()
                    )//se reemplaza el faltante con lista vacia
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


    fun descargarInformacion(
        callback: allDatosRutas,
        listaCompleta: MutableList<EstructuraDatosBaseDatos> = mutableListOf()
    ) {
        val idRuta = intArrayOf(2, 3, 6, 7, 8, 9, 10, 11, 13)

        for (i in 0 until idRuta.size) {
            recibirCoordenadasRuta(idRuta[i], object : DatosDeFirebaseCallback {
                override fun onDatosRecibidos(
                    listaCoorPrimParte: MutableList<LatLng>,
                    listaCoorSegParte: MutableList<LatLng>
                ) {
                    listaCompleta.add(
                        EstructuraDatosBaseDatos(
                            idRuta[i],
                            listaCoorPrimParte,
                            listaCoorSegParte
                        )
                    )
                    println("id ruta ==== ${idRuta[i]}, y $i")

                    if (idRuta[i] == idRuta[idRuta.size-1]) {
                        // Se ejecuta cuando se han procesado todas las rutas
                        Log.i("Informe", "Se recibió toda la información")
                        callback.todosDatosRecibidos(listaCompleta)

                    }
                }
            })
        }
    }

}
