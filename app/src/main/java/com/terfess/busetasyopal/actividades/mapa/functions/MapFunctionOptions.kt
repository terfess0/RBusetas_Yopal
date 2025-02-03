package com.terfess.busetasyopal.actividades.mapa.functions

import android.content.Context
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal.CreatRuta.polyCaminata
import com.terfess.busetasyopal.actividades.mapa.functions.calculate_route.CalculateRoute
import com.terfess.busetasyopal.clases_utiles.Distancia
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.enums.RoomTypePath
import com.terfess.busetasyopal.room.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class MapFunctionOptions {
    fun addParkingPoints(mapa: GoogleMap, context: Context) {

        //--------------PARKING LOTS------------------------------
        var listaRutas: List<Int>

        CoroutineScope(Dispatchers.IO).launch {

            val dbHelper = AppDatabase.getDatabase(context)

            listaRutas = dbHelper.routeDao().getAllIdsRoute()

            for (i in 0..listaRutas.size - 1) {
                val iterator = listaRutas[i]


                val datosSeleccionRuta = dbHelper.coordinateDao()
                    .getCoordRoutePath(iterator, RoomTypePath.RETURN.toString())

                val lat = datosSeleccionRuta[datosSeleccionRuta.size - 1].latitude
                val lng = datosSeleccionRuta[datosSeleccionRuta.size - 1].longitude

                withContext(Dispatchers.Main) {
                    val options = getOptionsMarker(
                        LatLng(lat, lng),
                        R.drawable.ic_parqueadero,
                        "Parqueadero Ruta $iterator"
                    )
                    mapa.addMarker(options)
                }
            }
        }
    }

    fun getOptionsMarker(
        punto: LatLng,
        icono: Int?,
        titulo: String
    ): MarkerOptions {
        // add marker on mapInstance
        val value = MarkerOptions()
            .position(punto)
            .title(titulo)
        if (icono != null) value.icon(BitmapDescriptorFactory.fromResource(icono))
        return value
    }

    fun createObjLocation(ubication: LatLng, providerName: String): Location {
        val location = Location(providerName)
        location.longitude = ubication.longitude
        location.latitude = ubication.latitude

        return location
    }


    fun rutaMasCerca(
        ubicacion: LatLng,
        ptsSentido: List<LatLng>
    ): CalculateRoute.PuntoMasCerca {
        val mapFunctionsInstance = MapFunctionOptions()

        //preparar ubicacion de estaciones cercanas para comparar distancia
        val ubiEstacion1 = mapFunctionsInstance.createObjLocation(
            ubicacion,
            "UBI SELECTED"
        )

        val mejorPuntoResult: CalculateRoute.PuntoMasCerca = Distancia().compararDistanciasInicio(
            ptsSentido,
            ubiEstacion1
        )

        return mejorPuntoResult
    }

    suspend fun rutaMasCercaCalculate(
        ubicacion: LatLng,
        ptsSentido: List<LatLng>
    ): CalculateRoute.PuntoMasCerca {
        val mapFunctionsInstance = MapFunctionOptions()

        //preparar ubicacion de estaciones cercanas para comparar distancia
        val ubiEstacion1 = mapFunctionsInstance.createObjLocation(
            ubicacion,
            "UBI SELECTED"
        )

        var mejorPuntoResult: CalculateRoute.PuntoMasCerca
        withContext(Dispatchers.IO) {
            //compara las distancias entre el usuario y cada estacion
            mejorPuntoResult = Distancia().compararDistanciasInicio(
                ptsSentido,
                ubiEstacion1
            )
        }

        return mejorPuntoResult
    }

    fun tracePartRoute(
        puntosRoute: List<LatLng>,
        pointCut: Int,
        context: Context,
        mapa: GoogleMap
    ) {

        val polylineOptionsC = PolylineOptions()
        val ptsCalculate = mutableListOf<LatLng>()

        polylineOptionsC.points.clear()
        polylineOptionsC.width(7f).color(
            ContextCompat.getColor(
                context,
                UtilidadesMenores().colorRandom()
            )
        )

        val recorte: MutableList<LatLng>

        //mueve la camara al marcador de estacion cercana
        mapa.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                puntosRoute[pointCut],
                16.5f
            ), 3000, null
        )

        //se calcula y traza una nueva polylinea
        val totalPuntos = puntosRoute
        recorte = totalPuntos.subList(pointCut + 1, totalPuntos.size).toMutableList()
        ptsCalculate.addAll(recorte)


        val polyCalculada = mapa.addPolyline(polylineOptionsC)
        polyCalculada.points = ptsCalculate
        polyCalculada.jointType = JointType.ROUND
        polyCalculada.endCap = RoundCap()
        polyCalculada.startCap = RoundCap()

    }

    fun traceSectionRoute(
        puntosRoute: List<LatLng>,
        pointCutStart: Int,
        pointCutEnd: Int,
        context: Context,
        mapa: GoogleMap,
        colorRuta: Int
    ) {

        val polylineOptionsC = PolylineOptions()
        val ptsCalculate = mutableListOf<LatLng>()

        polylineOptionsC.points.clear()
        polylineOptionsC.width(7f).color(
            ContextCompat.getColor(
                context,
                colorRuta
            )
        )

        var recorte = mutableListOf<LatLng>()

        //mueve la camara al marcador de estacion cercana
//        mapa.animateCamera(
//            CameraUpdateFactory.newLatLng(
//                puntosRoute[pointCutStart]
//            ), 2000, null
//        )

        //se calcula y traza una nueva polylinea

        if (pointCutStart > pointCutEnd) {
            val s = puntosRoute.subList(pointCutStart, puntosRoute.size).toMutableList()
            val d = puntosRoute.subList(0, pointCutEnd + 1).toMutableList()
            recorte = (s + d).toMutableList()
            println("Corte especial solo el final")

        } else {
            recorte = puntosRoute.subList(pointCutStart, pointCutEnd + 1).toMutableList()
            println("Caso corte 1")
        }

        ptsCalculate.addAll(recorte)

        val polyCalculada = mapa.addPolyline(polylineOptionsC)
        polyCalculada.points = recorte
        polyCalculada.jointType = JointType.ROUND
        polyCalculada.endCap = RoundCap()
        polyCalculada.startCap = RoundCap()

    }


    fun traceWalkRoute(
        ubiInicial: LatLng,
        ubiFinal: LatLng,
        mapa: GoogleMap,
        context: Context,
        isStart: Boolean,
        addMarker: Boolean
    ): CalculateRoute.WalkRoute {
        var marker: Marker? = null

        val ptsCaminata = ArrayList<LatLng>(2)

        ptsCaminata.add(ubiInicial)
        ptsCaminata.add(ubiFinal)

        val iconoIf: Int
        val titleMarkerIf: String

        if (isStart) {
            iconoIf = R.drawable.ic_estacion_inicio_ruta
            titleMarkerIf = "Tome la buseta"
        } else {
            iconoIf = R.drawable.ic_estacion_fin_ruta
            titleMarkerIf = "Baje de la buseta"
        }

        val markerOptions = MapFunctionOptions().getOptionsMarker(
            ubiFinal,
            iconoIf,
            titleMarkerIf
        )

        if (addMarker) {
            marker = mapa.addMarker(markerOptions)
        }

        val polylineOptionsCaminar = PolylineOptions()
        polylineOptionsCaminar.color(
            ContextCompat.getColor(
                context,
                R.color.distancia_caminar
            )
        )

        polylineOptionsCaminar.pattern(listOf(Dash(20f), Gap(10f)))
        polylineOptionsCaminar.addAll(ptsCaminata)
        polyCaminata = mapa.addPolyline(polylineOptionsCaminar)


        return CalculateRoute.WalkRoute(marker, polyCaminata)
    }

    fun traceWalkRouteSmart(
        ubiInicial: LatLng,
        ubiFinal: LatLng,
        mapa: GoogleMap,
        context: Context,
        isStart: Boolean,
        addMarker: Boolean
    ): CalculateRoute.WalkRoute {
        var marker: Marker? = null

        val ptsCaminata = ArrayList<LatLng>(2)

        ptsCaminata.add(ubiInicial)
        ptsCaminata.add(ubiFinal)

        val iconoIf: Int
        val titleMarkerIf: String

        if (isStart) {
            iconoIf = R.drawable.ic_estacion_inicio_ruta
            titleMarkerIf = "Tome la buseta"
        } else {
            iconoIf = R.drawable.ic_estacion_fin_ruta
            titleMarkerIf = "Baje de la buseta"
        }

        val markerOptions = MapFunctionOptions().getOptionsMarker(
            ubiFinal,
            iconoIf,
            titleMarkerIf
        )

        if (addMarker) {
            marker = mapa.addMarker(markerOptions)
        }

        val polylineOptionsCaminar = PolylineOptions()
        polylineOptionsCaminar.color(
            ContextCompat.getColor(
                context,
                R.color.distancia_caminar
            )
        )


        polylineOptionsCaminar.pattern(listOf(Dash(20f), Gap(10f)))
        polylineOptionsCaminar.addAll(ptsCaminata)
        polyCaminata = mapa.addPolyline(polylineOptionsCaminar)


        return CalculateRoute.WalkRoute(marker, polyCaminata)
    }

    fun animateCameraMap(
        zoom: Float,
        gmap: GoogleMap,
        coordinates: LatLng
    ) {
        val cameraPosicion = CameraPosition.Builder()
            .target(coordinates)
            .zoom(zoom)
            .build()

        gmap.animateCamera(
            CameraUpdateFactory.newCameraPosition(cameraPosicion), 2000,
            null
        )

    }

    fun moveCameraMap(
        zoom: Float,
        gmap: GoogleMap,
        coordinates: LatLng
    ) {
        val cameraPosicion = CameraPosition.Builder()
            .target(coordinates)
            .zoom(zoom)
            .build()

        gmap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosicion))
    }

    fun getDegreeCoordinates(
        coor1: LatLng,
        coor2: LatLng
    ): Double {
        val lat1Rad = Math.toRadians(coor1.latitude)
        val lat2Rad = Math.toRadians(coor2.latitude)
        val deltaLonRad = Math.toRadians(coor1.longitude - coor2.longitude)

        val y = sin(deltaLonRad) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(deltaLonRad)

        var acimut = Math.toDegrees(atan2(y, x))

        if (acimut < 0) {
            acimut += 360
        }

        return acimut
    }

    fun getSense(
        coordinateStart: LatLng,
        coordinateEnd: LatLng
    ): RoomTypePath {
        val acimut = getDegreeCoordinates(
            coordinateStart,
            coordinateEnd
        )
        return if (acimut >= 270 || acimut <= 90) {
            RoomTypePath.DEPARTURE
        } else {
            RoomTypePath.RETURN
        }
    }
}