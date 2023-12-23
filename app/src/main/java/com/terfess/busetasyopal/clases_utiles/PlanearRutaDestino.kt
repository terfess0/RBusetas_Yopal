package com.terfess.busetasyopal.clases_utiles

import android.content.Context
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.listas_datos.DatoCalcularRuta

class PlanearRutaDestino(private val mapa: Context, private val gmap: GoogleMap) {

    private var polylineOptions = PolylineOptions()

    object Datos {
        val mejorPuntoaInicio = IntArray(3) // 0: estacion  1: distancia  2: idRuta
        val mejorPuntoaDestino = IntArray(3) // 0: estacion  1: distancia  2: idRuta
    }

    fun rutaToDestino(
        ubicacionUsuario: LatLng,
        ubicacionDestino: LatLng
    ) {
        val dbhelper = DatosASqliteLocal(mapa)
        val rutaIds = intArrayOf(2, 3, 6, 7, 8, 9, 10, 11, 13)
        var puntosRutaSalida: List<DatoCalcularRuta>
        var puntosRutaLlegada: List<DatoCalcularRuta>

        //preparar ubicacion inicial para comparar distancia
        val ubiInicial = Location("Ubicacion Inicial")
        ubiInicial.longitude = ubicacionUsuario.longitude
        ubiInicial.latitude = ubicacionUsuario.latitude

        //preparar ubicacion destino para comparar distancia
        val ubiDestino = Location("Ubicacion Destino")
        ubiDestino.longitude = ubicacionDestino.longitude
        ubiDestino.latitude = ubicacionDestino.latitude

        //preparar ubicacion de estaciones cercanas para comparar distancia
        val ubiEstacion = Location("punto_comparar")

        for (iterador in rutaIds) {
            //recuperar las coordenadas de las almacenadas localmente desde firebase
            puntosRutaSalida = dbhelper.obtenerCoordenadasCalcularRuta(iterador, "coordenadas1")
            puntosRutaLlegada =
                dbhelper.obtenerCoordenadasCalcularRuta(iterador, "coordenadas2")
            val idRuta = puntosRutaSalida[0].idRuta
            //-------------------------------------------------------------

            //compara las distancias entre el usuario y cada estacion
            //compara las distancias entre el destino y cada estacion
            Distancia().compararDistanciasConDestino(
                puntosRutaSalida[0].coordenadas,
                ubiInicial,
                ubiEstacion,
                true,
                idRuta
            )
            Distancia().compararDistanciasConDestino(
                puntosRutaLlegada[0].coordenadas,
                ubiDestino,
                ubiEstacion,
                false,
                idRuta
            )
        }

        //-------------------------------------------------------------
        //MODIFICAR/PREPARAR LOS PUNTOS DE LAS POLYLINEAS

        val idMejorRuta = Datos.mejorPuntoaInicio[2] //da lo mismo el de inicio o destino
        val puntosRutaSalida1 = dbhelper.obtenerCoordenadasCalcularRuta(idMejorRuta, "coordenadas1")
        val puntosRutaLlegada1 =
            dbhelper.obtenerCoordenadasCalcularRuta(idMejorRuta, "coordenadas2")

        val puntoCorte1 =
            Datos.mejorPuntoaInicio[0] //numero elemento de arreglo de coordenadas mas cercano al inicio
        val puntoCorte2 =
            Datos.mejorPuntoaDestino[0] //numero elemento de arreglo de coordenadas mas cercano al destino

        //-----------------------------------------------------------------------------------------

        val totalSubida1 = puntosRutaSalida1[0].coordenadas.size
        val puntosFinal1 =
            puntosRutaSalida1[0].coordenadas.toMutableList().subList(puntoCorte1, totalSubida1 - 1)
                .toMutableList()

        polylineOptions.color(
            ContextCompat.getColor(
                mapa,
                R.color.recorridoIda
            )
        ) //color polyline salida

        val rutaResultante1 = gmap.addPolyline(polylineOptions) //agregar polyline
        //demas propiedades polyline
        rutaResultante1.points = puntosFinal1 //puntos polyline salida
        rutaResultante1.jointType = JointType.ROUND
        rutaResultante1.endCap = RoundCap()
        rutaResultante1.startCap = RoundCap()

        //-----------------------------------------------------------------------------------------
        //en caso de que el puntocorte2 sea mayor a los puntos de llegada de la ruta
        //debido a posible cruce de rutas distintas de hara de nuevo la comparacion
        //usando el idRuta de la variable mejorpuntoinicio
        if (puntoCorte2 > puntosRutaLlegada1.size) {
            val puntosReparadores =
                dbhelper.obtenerCoordenadasCalcularRuta(idMejorRuta, "coordenadas2")
            Distancia().compararDistanciasConDestino(
                puntosReparadores[0].coordenadas.toMutableList(),
                ubiInicial,
                ubiDestino,
                false,
                idMejorRuta
            )
        }
        //puntos polyline de llegada
        val puntosFinal =
            puntosRutaLlegada1[0].coordenadas.toMutableList().subList(0, puntoCorte2)
                .toMutableList()

        polylineOptions.color(
            ContextCompat.getColor(
                mapa,
                R.color.recorridoVuelta
            )
        ) //color polyline llegada
        //agregar polyline y propiedades
        val rutaResultante = gmap.addPolyline(polylineOptions)
        rutaResultante.points = puntosFinal
        rutaResultante.jointType = JointType.ROUND
        rutaResultante.endCap = RoundCap()
        rutaResultante.startCap = RoundCap()
    }

}