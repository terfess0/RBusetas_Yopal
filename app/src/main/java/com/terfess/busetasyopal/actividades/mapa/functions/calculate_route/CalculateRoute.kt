package com.terfess.busetasyopal.actividades.mapa.functions.calculate_route

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline

class CalculateRoute {
    data class PossibleRoute(
        val isPossible: Boolean,
        val idRuta: Int,
        val idRutaAnterior: Int?
    )

    data class PossibleRouteTransfer(
        val isPossible: Boolean,
        val idRuta: Int,
        val puntoConectRuta: LatLng,
        val idRutaAnterior: Int,
        val puntoRutaConectAnterior: LatLng
    )

    data class PuntoMasCerca(
        val idPunto: Int,
        val distancia: Int
    )

    data class WalkRoute(
        val marker: Marker?,
        val lineWalk: Polyline
    )

    data class RouteCalculate(
        val idruta: Int,
        val points: List<LatLng>,
        val cutPoint1Ruta: PuntoMasCerca,
        val cutPoint2Ruta: PuntoMasCerca,
        val isTransfer: Boolean,
        val idRutaAnterior: Int?,
        val pointConectThisRoute: LatLng?,
        val pointConectRutaAnterior: LatLng?,
        val pointsAnteriorRoute: List<LatLng>?,
        val cutPoint1RutaAnterior: PuntoMasCerca?,
        val cutPoint2RutaAnterior: PuntoMasCerca?,
        val ubiStartGeneral: LatLng,
        val ubiEndGeneral: LatLng
    )

    data class ResultCalculate(
        val resultInfo: Boolean,
        val dataResult: List<RouteCalculate>
    )
}