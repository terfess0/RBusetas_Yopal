package com.terfess.busetasyopal.clases_utiles

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.enums.RoomTypePath
import com.terfess.busetasyopal.room.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PolylinesOpMapa(private val mapa: GoogleMap, private val contexto: Context) {
    private val opcionesRutaSalida = PolylineOptions().width(9.0f)
    private val opcionesRutaLlegada = PolylineOptions().width(9.0f)

    private val sqlRoom = AppDatabase.getDatabase(contexto)

    // Listas dinámicas para las polylines
    private val polyRutasSalida = mutableListOf<Pair<Polyline, Int>>()
    private val polyRutasLlegada = mutableListOf<Pair<Polyline, Int>>()

    // Map para asociar cada ruta con sus colores
    private val rutaColoresSalida = mapOf(
        1 to R.color.unoSalida,
        2 to R.color.dosSalida,
        3 to R.color.tresSalida,
        4 to R.color.cuatroSalida,
        5 to R.color.cincoSalida,
        6 to R.color.seisSalida,
        7 to R.color.sieteSalida,
        8 to R.color.ochoSalida,
        9 to R.color.nueveSalida,
        10 to R.color.diezSalida,
        11 to R.color.onceSalida,
        12 to R.color.doceSalida,
        13 to R.color.treceSalida,
        14 to R.color.catorceSalida,
        15 to R.color.quinceSalida,
        16 to R.color.dieciseisSalida,
        17 to R.color.diecisieteSalida,
        18 to R.color.dieciochoSalida,
        19 to R.color.diecinueveSalida,
        20 to R.color.veinteSalida,
        21 to R.color.veintiunoSalida,
        22 to R.color.veintidosSalida,
        23 to R.color.veintitresSalida,
        24 to R.color.veinticuatroSalida,
        25 to R.color.veinticincoSalida,
        26 to R.color.veintiseisSalida,
        27 to R.color.veintisieteSalida,
        28 to R.color.veintiochoSalida,
        29 to R.color.veintinueveSalida,
        30 to R.color.treintaSalida
        // Add more entries as needed
    )

    private val rutaColoresLlegada = mapOf(
        1 to R.color.unoLlegada,
        2 to R.color.dosLlegada,
        3 to R.color.tresLlegada,
        4 to R.color.cuatroLlegada,
        5 to R.color.cincoLlegada,
        6 to R.color.seisLlegada,
        7 to R.color.sieteLlegada,
        8 to R.color.ochoLlegada,
        9 to R.color.nueveLlegada,
        10 to R.color.diezLlegada,
        11 to R.color.onceLlegada,
        12 to R.color.doceLlegada,
        13 to R.color.treceLlegada,
        14 to R.color.catorceLlegada,
        15 to R.color.quinceLlegada,
        16 to R.color.dieciseisLlegada,
        17 to R.color.diecisieteLlegada,
        18 to R.color.dieciochoLlegada,
        19 to R.color.diecinueveLlegada,
        20 to R.color.veinteLlegada,
        21 to R.color.veintiunoLlegada,
        22 to R.color.veintidosLlegada,
        23 to R.color.veintitresLlegada,
        24 to R.color.veinticuatroLlegada,
        25 to R.color.veinticincoLlegada,
        26 to R.color.veintiseisLlegada,
        27 to R.color.veintisieteLlegada,
        28 to R.color.veintiochoLlegada,
        29 to R.color.veintinueveLlegada,
        30 to R.color.treintaLlegada
        // Add more entries as needed
    )

    init {
        // Initialize the polylines
        CoroutineScope(Dispatchers.IO).launch {
            val idsRutas = sqlRoom.routeDao().getAllIdsRoute()

            withContext(Dispatchers.Main) {
                for (i in 1..idsRutas.size) {
                    val it = idsRutas[i - 1]

                    opcionesRutaSalida.color(
                        ContextCompat.getColor(
                            contexto,
                            rutaColoresSalida[it] ?: R.color.catorceSalida
                        )
                    )
                    opcionesRutaLlegada.color(
                        ContextCompat.getColor(
                            contexto,
                            rutaColoresLlegada[it] ?: R.color.dosLlegada
                        )
                    )

                    polyRutasSalida.add(Pair(mapa.addPolyline(opcionesRutaSalida), it))
                    polyRutasLlegada.add(Pair(mapa.addPolyline(opcionesRutaLlegada), it))
                }
            }
        }

    }

    fun trazarRuta(idRuta: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val obj1 = sqlRoom.coordinateDao().getCoordRoutePath(
                idRuta,
                RoomTypePath.DEPARTURE.toString()
            ).toMutableList()

            val obj2 = sqlRoom.coordinateDao().getCoordRoutePath(
                idRuta,
                RoomTypePath.RETURN.toString()
            ).toMutableList()

            val puntosSalida = UtilidadesMenores().extractCoordToLatLng(
                obj1,
                RoomTypePath.DEPARTURE.toString(),
                idRuta
            )

            val puntosLlegada = UtilidadesMenores().extractCoordToLatLng(
                obj2,
                RoomTypePath.RETURN.toString(),
                idRuta
            )

            val polySalida = polyRutasSalida.find { it.second == idRuta }
            val polyLlegada = polyRutasLlegada.find { it.second == idRuta }

            withContext(Dispatchers.Main) {

                // Elimina las rutas si ya tienen suficientes puntos
                val line = polySalida?.first
                val lineReturn = polyLlegada?.first

                if (line?.isVisible == true && line.points.size > 10) {
                    line.isVisible = false
                    lineReturn?.isVisible = false
                } else {
                    line?.isVisible = true
                    lineReturn?.isVisible = true

                    // Añade los puntos a las polylines
                    line?.points = puntosSalida
                    lineReturn?.points = puntosLlegada

                    agregarMarcador(
                        puntosLlegada.last(),
                        R.drawable.ic_parqueadero,
                        "Parqueadero Ruta $idRuta"
                    )
                    println("Marker?")
                }
            }
        }
    }


    // Función para agregar marcador
    private fun agregarMarcador(posicion: LatLng, iconoRes: Int, titulo: String): Marker? {
        val newMarker = mapa.addMarker(
            MarkerOptions()
                .position(posicion)
                .title(titulo)
                .icon(BitmapDescriptorFactory.fromResource(iconoRes))
        )

        return newMarker
    }
}