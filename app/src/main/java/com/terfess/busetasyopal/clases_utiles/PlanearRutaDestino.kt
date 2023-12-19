package com.terfess.busetasyopal.clases_utiles

import android.content.Context
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.terfess.busetasyopal.R

class PlanearRutaDestino (private val mapa: Context, private val gmap: GoogleMap){

    private var polylineOptions = PolylineOptions()
    fun rutaMasCerca(ubicacionUsuario: LatLng, sentido: String) {

        if (polylineOptions.isVisible && RutaBasic.CreatRuta.puntosLlegada.isNotEmpty()) {

            RutaBasic.CreatRuta.masCortaInicio[0] = -1 // inicializa el índice de la estación más cercana en -1
            RutaBasic.CreatRuta.masCortaInicio[1] = Int.MAX_VALUE // inicializa la distancia con un valor alto

            if (RutaBasic.CreatRuta.puntosCalculada.size > 5) {
                //remover la polylin que ya haya sido creada antes (calculada)
                RutaBasic.CreatRuta.polyCalculada.remove()
                RutaBasic.CreatRuta.puntosCalculada.clear()
            }

            //salvar los puntos de las rutas para no perderlos cuando
            //se borren las rutas para ser reempazadas por la calculada
            val puntos1 = mutableListOf<LatLng>()
            puntos1.addAll(RutaBasic.CreatRuta.puntosSalida)
            val puntos2 = mutableListOf<LatLng>()
            puntos2.addAll(RutaBasic.CreatRuta.puntosLlegada)

            //preparar ubicacion inicial para comparar distancia
            val ubiInicial = Location("Ubicacion Inicial")
            ubiInicial.longitude = ubicacionUsuario.longitude
            ubiInicial.latitude = ubicacionUsuario.latitude

            //preparar ubicacion de estaciones cercanas para comparar distancia
            val ubiEstacion1 = Location("punto1-2")

            //compara las distancias entre el usuario y cada estacion
            if (sentido == "salida") {
                compararDistancias(puntos1, ubiInicial, ubiEstacion1, true)

                //se elimina el marcador del otro sentido si lo hay
                RutaBasic.CreatRuta.estamarcado2 = if (RutaBasic.CreatRuta.estamarcado2 == true) {
                    RutaBasic.CreatRuta.marcador2?.remove()
                    false
                } else {
                    false
                }
            } else if (sentido == "llegada") {
                compararDistancias(puntos2, ubiInicial, ubiEstacion1, true)

                //se elimina el marcador del otro sentido si lo hay
                RutaBasic.CreatRuta.estamarcado1 = if (RutaBasic.CreatRuta.estamarcado1 == true) {
                    RutaBasic.CreatRuta.marcador1?.remove()
                    false
                } else {
                    false
                }
            }
            //-------------------------------------------------------------
            //se hace el nuevo trazo
            val estacionCerca = RutaBasic.CreatRuta.masCortaInicio[0]
            //val metros = masCortaInicio[1]

            val polylineOptionsC = PolylineOptions()
            polylineOptionsC.points.clear()
            polylineOptionsC.width(7f).color(
                ContextCompat.getColor(
                    mapa,
                    colorRandom()
                )
            )
            val recorte: MutableList<LatLng>

            if (sentido == "salida") {

                //se coloca un marcador en la estacion cercana
                RutaBasic.CreatRuta.marcador1 = agregarMarcador(
                    puntos1[RutaBasic.CreatRuta.masCortaInicio[0]],
                    R.drawable.ic_estacion
                )
                RutaBasic.CreatRuta.estamarcado1 = true
                //mueve la camara al marcador de estacion cercana
                gmap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        puntos1[RutaBasic.CreatRuta.masCortaInicio[0]],
                        17f
                    ), 3000, null
                )

                //se calcula y traza una nueva polylinea
                val totalPuntos = puntos1 + puntos2
                recorte = totalPuntos.subList(estacionCerca, totalPuntos.size).toMutableList()
                RutaBasic.CreatRuta.puntosCalculada.addAll(recorte)
                RutaBasic.CreatRuta.polySalida.remove()
                RutaBasic.CreatRuta.polyLlegada.remove()
                RutaBasic.CreatRuta.polyCalculada = gmap.addPolyline(polylineOptionsC)
                RutaBasic.CreatRuta.polyCalculada.points = RutaBasic.CreatRuta.puntosCalculada
                RutaBasic.CreatRuta.polyCalculada.jointType = JointType.ROUND
                RutaBasic.CreatRuta.polyCalculada.endCap = RoundCap()
                RutaBasic.CreatRuta.polyCalculada.startCap = RoundCap()

            } else {

                //se coloca un marcador en la estacion cercana
                RutaBasic.CreatRuta.marcador2 = agregarMarcador(
                    puntos2[RutaBasic.CreatRuta.masCortaInicio[0]],
                    R.drawable.ic_estacion
                )
                RutaBasic.CreatRuta.estamarcado2 = true

                //mueve la camara al marcador de estacion cercana
                gmap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        puntos2[RutaBasic.CreatRuta.masCortaInicio[0]],
                        17f
                    ), 3000, null
                )

                //se calcula y traza una nueva polylinea
                val totalPuntos = puntos2 + puntos1
                recorte = totalPuntos.subList(estacionCerca, totalPuntos.size).toMutableList()
                RutaBasic.CreatRuta.puntosCalculada.addAll(recorte)
                RutaBasic.CreatRuta.polySalida.remove()
                RutaBasic.CreatRuta.polyLlegada.remove()
                RutaBasic.CreatRuta.polyCalculada = gmap.addPolyline(polylineOptionsC)
                RutaBasic.CreatRuta.polyCalculada.points = RutaBasic.CreatRuta.puntosCalculada
                RutaBasic.CreatRuta.polyCalculada.jointType = JointType.ROUND
                RutaBasic.CreatRuta.polyCalculada.endCap = RoundCap()
                RutaBasic.CreatRuta.polyCalculada.startCap = RoundCap()
            }

            /*if (puntoCorte1 > puntoCorte2) {
                val parte1 = puntosTotales.subList(0, puntoCorte2+1)
                val parte2 = puntosTotales.subList(puntoCorte1, puntosTotales.size)
                val puntis = parte2 + parte1 // se agrega al reves para evitar uniones
                polylineOptionsC.addAll(puntis)
                val c = gmap.addPolyline(polylineOptionsC)
                c.jointType = JointType.ROUND
                c.endCap = RoundCap()
                c.startCap = RoundCap()

            } else {
                val parte1 = puntosTotales.subList(0, puntoCorte1+1)
                val parte2 = puntosTotales.subList(puntoCorte2, puntosTotales.size)
                polylineOptionsC.addAll(parte2 + parte1) // se agrega al reves para evitar uniones
                val d = gmap.addPolyline(polylineOptionsC)
                d.jointType = JointType.ROUND
                d.endCap = RoundCap()
                d.startCap = RoundCap()
            }*/
        } else {
            crearToast("Datos de recorridos no han sido recibidos")
        }

    }
}