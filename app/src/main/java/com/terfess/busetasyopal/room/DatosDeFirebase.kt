package com.terfess.busetasyopal.room

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.terfess.busetasyopal.actividades.Splash
import com.terfess.busetasyopal.enums.RoomPeriod
import com.terfess.busetasyopal.enums.RoomTypePath
import com.terfess.busetasyopal.modelos_dato.DatoFrecuencia
import com.terfess.busetasyopal.modelos_dato.DatoHorario
import com.terfess.busetasyopal.modelos_dato.EstructuraDatosBaseDatos
import com.terfess.busetasyopal.room.model.Route
import com.terfess.busetasyopal.room.model.Schedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface DatosDeFirebaseCallback { //callback para detectar que los datos de una ruta han sido recibidos

    fun onRouteReceived(
        route: Route,
        listaHor: DatoHorario,
        listaCoorPrimParte: MutableList<LatLng>,
        listaCoorSegParte: MutableList<LatLng>
    )


}

interface allDatosRutas { //callback para detectar que los datos de todas las rutas han sido recibidos
    fun todosDatosRecibidos()
}

class DatosDeFirebase {

    private fun recibirCoordenadasRuta(
        baseDatosFirebaseDatabase: FirebaseDatabase,
        callAll: allDatosRutas,
        callback: DatosDeFirebaseCallback
    ) {
        if (!Splash.downloading){
            Splash.downloading = true

            // Recoge todas las rutas
            baseDatosFirebaseDatabase.getReference("features/0/rutas/")
                .get().addOnSuccessListener { dataSnapshot ->

                    for (rutaSnapshot in dataSnapshot.children) {
                        val idruta = rutaSnapshot.key?.toInt()
                            ?: continue // Asegúrate de que el ID sea un entero

                        val listaCoorPrimParte = mutableListOf<LatLng>()
                        val listaCoorSegParte = mutableListOf<LatLng>()

                        // Extraer coordenadas de salida
                        val salidaSnapshot = rutaSnapshot.child("salida")
                        for (childSnapshot in salidaSnapshot.children) {
                            val lat = childSnapshot.child("1").getValue(Double::class.java)
                            val lng = childSnapshot.child("0").getValue(Double::class.java)
                            val latValue = lat ?: 0.0
                            val lngValue = lng ?: 0.0
                            listaCoorPrimParte.add(LatLng(latValue, lngValue))
                        }

                        // Extraer coordenadas de llegada
                        val llegadaSnapshot = rutaSnapshot.child("llegada")
                        for (childSnapshot in llegadaSnapshot.children) {
                            val lat = childSnapshot.child("1").getValue(Double::class.java)
                            val lng = childSnapshot.child("0").getValue(Double::class.java)
                            val latValue = lat ?: 0.0
                            val lngValue = lng ?: 0.0
                            listaCoorSegParte.add(LatLng(latValue, lngValue))
                        }

                        // Extraer horarios
                        val horarioSnapshot = rutaSnapshot.child("horarioLunVie")
                        val horLunVie1 = horarioSnapshot.child("0").value.toString()
                        val horLunVie2 = horarioSnapshot.child("1").value.toString()
                        val horSab1 = rutaSnapshot.child("horarioSab").child("0").value.toString()
                        val horSab2 = rutaSnapshot.child("horarioSab").child("1").value.toString()
                        val horDomFest1 =
                            rutaSnapshot.child("horarioDomFest").child("0").value.toString()
                        val horDomFest2 =
                            rutaSnapshot.child("horarioDomFest").child("1").value.toString()

                        val objetoHorario = DatoHorario(
                            idruta,
                            horLunVie1,
                            horLunVie2,
                            horSab1,
                            horSab2,
                            horDomFest1,
                            horDomFest2
                        )

                        // Extraer frecuencia y otros datos
                        val frecLunVie = rutaSnapshot.child("frecuenciaLunVie").value.toString()
                        val frecSab = rutaSnapshot.child("frecuenciaSab").value.toString()
                        val frecDomFest = rutaSnapshot.child("frecuenciaDomFest").value.toString()
                        val sitios = rutaSnapshot.child("sitios").value.toString()
                        val sitiosExtend = rutaSnapshot.child("sitiosExtend").value.toString()
                        val enabled = rutaSnapshot.child("enabled").getValue(Boolean::class.java)

                        val objetoFrecuencia =
                            DatoFrecuencia(idruta, frecLunVie, frecSab, frecDomFest)

                        // Crear objeto Route
                        val route = Route(
                            idruta,
                            enabled ?: false,
                            objetoFrecuencia.frec_mon_fri,
                            objetoFrecuencia.frec_sat,
                            objetoFrecuencia.frec_sun_holi,
                            sitios,
                            sitiosExtend
                        )

                        // Llamar al callback para cada ruta
                        callback.onRouteReceived(
                            route,
                            objetoHorario,
                            listaCoorPrimParte,
                            listaCoorSegParte
                        )
                    }
                    callAll.todosDatosRecibidos()

                }.addOnFailureListener {
                    println("Algo salió mal en la recepción de datos de rutas.")
                }
        }
    }


    fun descargarInformacion(
        contexto: Context,
        fireInstance: FirebaseDatabase,
        callback: allDatosRutas
    ) {

        val roomdb = AppDatabase.getDatabase(contexto)

        recibirCoordenadasRuta(fireInstance, callback, object : DatosDeFirebaseCallback {

            override fun onRouteReceived(
                route: Route,
                listaHor: DatoHorario,
                listaCoorPrimParte: MutableList<LatLng>,
                listaCoorSegParte: MutableList<LatLng>
            ) {
                CoroutineScope(Dispatchers.IO).launch {

                    //ROUTE SAVE
                    try {
                        // SAVE ROUTE
                        roomdb.routeDao().inserNewRoute(route)
                    } catch (e: Exception) {
                        // Manejar excepciones
                        e.printStackTrace()
                    }


                    //SCHEDULE SAVE
                    try {
                        // SAVE SCHEDULES
                        val scheduleLV = Schedule(
                            period = RoomPeriod.LUN_VIE.toString(),
                            id_route = listaHor.numRuta,
                            sche_start = listaHor.horaInicioLunesViernes,
                            sche_end = listaHor.horaFinalLunesViernes
                        )

                        val scheduleS = Schedule(
                            period = RoomPeriod.SAB.toString(),
                            id_route = listaHor.numRuta,
                            sche_start = listaHor.horaInicioSab,
                            sche_end = listaHor.horaFinalSab
                        )

                        val scheduleD = Schedule(
                            period = RoomPeriod.DOM_FEST.toString(),
                            id_route = listaHor.numRuta,
                            sche_start = listaHor.horaInicioDom,
                            sche_end = listaHor.horaFinalDom
                        )
                        roomdb.scheduleDao()
                            .insertSchedules(listOf(scheduleLV, scheduleS, scheduleD))

                    } catch (e: Exception) {
                        // Manejar excepciones
                        e.printStackTrace()
                    }

                    //COORDINATES SAVE
                    try {
                        // Listas no estén vacías
                        if (listaCoorPrimParte.isNotEmpty()) {
                            // Guardar coordenadas de la primera parte (salida)
                            roomdb.coordinateDao().insertCoordinate(
                                RoomTypePath.DEPARTURE.toString(),
                                route.id_route,
                                listaCoorPrimParte
                            )
                        }

                        if (listaCoorSegParte.isNotEmpty()) {
                            // Guardar coordenadas de la segunda parte (llegada)
                            roomdb.coordinateDao().insertCoordinate(
                                RoomTypePath.RETURN.toString(),
                                route.id_route,
                                listaCoorSegParte
                            )
                        }

                    } catch (e: Exception) {
                        // Manejar excepciones
                        e.printStackTrace()
                    }
                }
            }
        })
    }
}
