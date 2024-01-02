package com.terfess.busetasyopal.clases_utiles

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.terfess.busetasyopal.modelos_dato.EstructuraDatosBaseDatos

interface DatosDeFirebaseCallback { //callback para detectar que los datos de una ruta han sido recibidos
    //se divide en dos callbacks para mejor funcionamiento / evitar bucles
    fun onDatosRecibidos1(
        listaCoorPrimParte: MutableList<LatLng>
    )

    fun onDatosRecibidos2(
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
                    callback.onDatosRecibidos1(listaCoorPrimParte) //primera parte de ruta X recibida
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
                    callback.onDatosRecibidos2(listaCoorSegParte) //segunda parte de ruta X recibida
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
        var sentenciador = 0
        var primeraLista = mutableListOf<LatLng>()
        for (i in 0..idRuta.size - 1) {
            val rutaId = idRuta[i]
            recibirCoordenadasRuta(rutaId, object : DatosDeFirebaseCallback {
                override fun onDatosRecibidos1(
                    listaCoorPrimParte: MutableList<LatLng>
                ) {
                    //primera parte recibida, se asigna a primeraLista y se aumenta variable guia
                    sentenciador += 1
                    primeraLista = listaCoorPrimParte
                }
                override fun onDatosRecibidos2(listaCoorSegParte: MutableList<LatLng>) {
                    //Se aumenta variable guia para entrar en el if teniendo las dos listas recibidas
                    sentenciador += 1
                    if (sentenciador >= 2 && primeraLista.size > 1) { //verifica que primera lista tenga varios elementos
                        //se agrega la ruta y sus coordenadas a la lista que se enviara por callback a descargar_informacion()
                        listaCompleta.add(
                            EstructuraDatosBaseDatos(
                                rutaId,
                                primeraLista,
                                listaCoorSegParte
                            )
                        )
                        sentenciador == 0 // se reinicia variable guia
                        if (rutaId == idRuta[idRuta.size-1]) {//cuando se llega a la ultima ruta (y se descarga)
                            // Se ejecuta cuando se han procesado todas las rutas
                            Log.i("Informe", "Se recibió toda la información")
                            callback.todosDatosRecibidos(listaCompleta) //callback con la lista de  todas las rutas
                        }
                    }
                }
            })
        }
    }


}
