package com.terfess.busetasyopal.clases_utiles

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.terfess.busetasyopal.modelos_dato.DatoFrecuencia
import com.terfess.busetasyopal.modelos_dato.DatoHorario
import com.terfess.busetasyopal.modelos_dato.EstructuraDatosBaseDatos

interface DatosDeFirebaseCallback { //callback para detectar que los datos de una ruta han sido recibidos
    //se divide en tres callbacks para mejor funcionamiento / evitar bucles

    fun onHorFrecRecibidos(
        listaHor: DatoHorario, listaFrec: DatoFrecuencia
    )

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


    private fun recibirCoordenadasRuta(idruta: Int, callback: DatosDeFirebaseCallback) {
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

        //HORARIOS Y FRECUENCIAS DE LA RUTA
        baseDatosFirebaseDatabase.getReference("features/0/rutas/$idruta")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    //recuperar los datos de interes
                    val horLunVie1 =
                        dataSnapshot.child("horarioLunVie").child("0").value.toString()
                    val horLunVie2 =
                        dataSnapshot.child("horarioLunVie").child("1").value.toString()
                    val horSab1 =
                        dataSnapshot.child("horarioSab").child("0").value
                            .toString()
                    val horSab2 =
                        dataSnapshot.child("horarioSab").child("1").value
                            .toString()
                    val horDomFest1 =
                        dataSnapshot.child("horarioDomFest").child("0").value
                            .toString()
                    val horDomFest2 =
                        dataSnapshot.child("horarioDomFest").child("1").value
                            .toString()
                    val frecLunVie = dataSnapshot.child("frecuenciaLunVie").value.toString()
                    val frecSab = dataSnapshot.child("frecuenciaSab").value.toString()
                    val frecDomFest = dataSnapshot.child("frecuenciaDomFest").value.toString()

                    val objetoHorario = DatoHorario(
                        idruta,
                        horLunVie1,
                        horLunVie2,
                        horSab1,
                        horSab2,
                        horDomFest1,
                        horDomFest2
                    )

                    val objetoFrecuencia = DatoFrecuencia(idruta, frecLunVie, frecSab, frecDomFest)

                    if (objetoHorario.horaFinalDom.isNotEmpty() && objetoFrecuencia.frecDomFest.isNotEmpty()) {
                        callback.onHorFrecRecibidos(objetoHorario, objetoFrecuencia)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores si los hay
                    println("Algo salió mal en horarios y frecuencis de la llegada ruta $idruta.")
                }
            })
    }


    fun descargarInformacion(
        contexto:Context,
        callback: allDatosRutas,
        listaCompleta: MutableList<EstructuraDatosBaseDatos> = mutableListOf()
    ) {

            val idRuta = intArrayOf(2, 3, 6, 7, 8, 9, 10, 13)
            var primeraLista = mutableListOf<LatLng>()

            for (i in 0..idRuta.size - 1) {
                val rutaId = idRuta[i]
                recibirCoordenadasRuta(rutaId, object : DatosDeFirebaseCallback {
                    override fun onHorFrecRecibidos(
                        listaHor: DatoHorario,
                        listaFrec: DatoFrecuencia
                    ) {
                        val sqlDB = DatosASqliteLocal(contexto)
                        sqlDB.insertarHorario(rutaId, listaHor)
                        sqlDB.insertarFrecuencia(rutaId, listaFrec)
                    }

                    override fun onDatosRecibidos1(
                        listaCoorPrimParte: MutableList<LatLng>
                    ) {
                        primeraLista = listaCoorPrimParte
                    }

                    override fun onDatosRecibidos2(listaCoorSegParte: MutableList<LatLng>) {
                        //Se aumenta variable guia para entrar en el if teniendo las dos listas recibidas
                        if ((primeraLista.size > 1)) { //verifica que primera lista tenga varios elementos
                            //se agrega la ruta y sus coordenadas a la lista que se enviara por callback a descargar_informacion()
                            listaCompleta.add(
                                EstructuraDatosBaseDatos(
                                    rutaId,
                                    primeraLista,
                                    listaCoorSegParte,
                                )
                            )
                            if (rutaId == idRuta[idRuta.size - 1]) {//cuando se llega a la ultima ruta (y se descarga)
                                // Se ejecuta cuando se han procesado todas las rutas
                                println("Se recibió toda la información")
                                callback.todosDatosRecibidos(listaCompleta) //callback con la lista de  todas las rutas
                                baseDatosFirebaseDatabase.goOffline()
                            }
                        }else{
                            println("ERROR EN RUTA $rutaId")
                        }
                    }
                })
            }

    }


}
