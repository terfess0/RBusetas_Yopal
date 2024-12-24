package com.terfess.busetasyopal.actividades.mapa.functions.calculate_route

import android.content.Context
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.terfess.busetasyopal.actividades.mapa.functions.MapFunctionOptions
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.enums.RoomTypePath
import com.terfess.busetasyopal.room.AppDatabase
import com.terfess.busetasyopal.room.model.Coordinate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlanearRutaDestino(private val mapa: Context) {

    suspend fun rutaToDestino(
        ubicacionUsuario: LatLng,
        ubicacionDestino: LatLng,
        callback: com.terfess.busetasyopal.callbacks.mapa.CalculateRoute
    ) {
        val dbhelper = AppDatabase.getDatabase(mapa)

        val mapFunctionsIns = MapFunctionOptions()
        val ubiStart = mapFunctionsIns.createObjLocation(ubicacionUsuario, "SELECT_FROM_MAP")
        val ubiEnd = mapFunctionsIns.createObjLocation(ubicacionDestino, "SELECT FROM MAP")

        withContext(Dispatchers.IO) {
            //PREPARE
            val listIdRoutes = dbhelper.routeDao().getAllIdsRoute()

            val listPossibles1 = mutableListOf<CalculateRoute.PossibleRoute>()
            val listPossibles2 = mutableListOf<CalculateRoute.PossibleRoute>()

            val sense1 = mapFunctionsIns.getSense(
                ubicacionUsuario,
                ubicacionDestino
            )

            println("El sentido es: $sense1")

            // FIRST STEP: GET POSSIBLE ROUTES
            for (i in 0..listIdRoutes.size - 1) {
                val idRuta = listIdRoutes[i]

                val dataSpecialStart = dbhelper.coordinateDao().getCoordRoutePath(
                    idRuta,
                    sense1.toString()
                )

                val dataDeparture = dbhelper.coordinateDao().getCoordRoutePath(
                    idRuta,
                    RoomTypePath.DEPARTURE.toString()
                )

                val dataReturn = dbhelper.coordinateDao().getCoordRoutePath(
                    idRuta,
                    RoomTypePath.RETURN.toString()
                )

                val dataAll = dataDeparture + dataReturn

                val listPoss1 =
                    getPossibleRoutes(ubiStart, dataSpecialStart, idRuta)

                val listPoss2 =
                    getPossibleRoutes(ubiEnd, dataAll, idRuta)

                if (listPoss1.isPossible) listPossibles1.add(listPoss1)
                if (listPoss2.isPossible) listPossibles2.add(listPoss2)
            }

            // CHECK OBTAIN ROUTES
            if (listPossibles1.isEmpty() || listPossibles2.isEmpty()) {
                println("No hay rutas o en el inicio o en el destino")
                callback.onResult(false, mutableListOf())
                return@withContext
            }

            val routeResult = verifyRouteResult(
                listPossibles1,
                listPossibles2
            )

            if (routeResult.isNotEmpty()) {
                val ptsRoutee = mutableListOf<LatLng>()

                val listResult = mutableListOf<CalculateRoute.RouteCalculate>()

                for (i in 0..routeResult.size - 1) {
                    ptsRoutee.clear()

                    if (sense1 == RoomTypePath.DEPARTURE) {
                        val pts = dbhelper.coordinateDao()
                            .getCoordRoutePath(
                                routeResult[i].idRuta,
                                sense1.toString()
                            )

                        val ptsPrepare = UtilidadesMenores()
                            .extractCoordToLatLng(
                                pts,
                                sense1.toString(),
                                routeResult[i].idRuta
                            )

                        ptsRoutee.addAll(ptsPrepare)
                    } else {
                        val pts = dbhelper.coordinateDao()
                            .getCoordRoutePath(
                                routeResult[i].idRuta,
                                RoomTypePath.DEPARTURE.toString()
                            )

                        val ptsPrepare = UtilidadesMenores()
                            .extractCoordToLatLng(
                                pts,
                                RoomTypePath.DEPARTURE.toString(),
                                routeResult[i].idRuta
                            )

                        val pts2 = dbhelper.coordinateDao()
                            .getCoordRoutePath(
                                routeResult[i].idRuta,
                                RoomTypePath.RETURN.toString()
                            )

                        val ptsPrepare2 = UtilidadesMenores()
                            .extractCoordToLatLng(
                                pts2,
                                RoomTypePath.RETURN.toString(),
                                routeResult[i].idRuta
                            )

                        ptsRoutee.addAll(ptsPrepare + ptsPrepare2)
                    }

                    val result = traceCalculate(
                        mapFunctionsIns,
                        ubicacionUsuario,
                        ubicacionDestino,
                        routeResult[i].idRuta,
                        ptsRoutee
                    )

                    listResult.add(result)
                }

                callback.onResult(true, listResult)
            }

            println("Ruta inmediata encontrada entre inicio y destino: $routeResult")

            // TWO STEP, IF NOT ROUTE FROM A TO B DIRECT SO RESEARCH TRANSFER FROM A-1 TO B
            if (routeResult.isEmpty()) {
                println("No se encontro ruta directa, se buscara transbordo")

                listPossibles1.let {
                    val numAttemps = if (listPossibles1.size >= 3) {
                        3
                    } else {
                        listPossibles1.size
                    }


                    if (listPossibles1.isNotEmpty()) {
                        // Buscar transbordos si es necesario

                        val listTransfers1 = mutableListOf<CalculateRoute.PossibleRouteTransfer>()
                        val listResultTransfers = mutableListOf<CalculateRoute.RouteCalculate>()

                        listPossibles1.forEach {

                            val dataDepSelect1 = dbhelper.coordinateDao().getCoordRoutePath(
                                it.idRuta,
                                RoomTypePath.DEPARTURE.toString()
                            )
                            val dataRetSelect2 = dbhelper.coordinateDao().getCoordRoutePath(
                                it.idRuta,
                                RoomTypePath.RETURN.toString()
                            )

                            for (i in 0..listIdRoutes.size - 1) {

                                val idRuta = listIdRoutes[i]

                                if (idRuta != it.idRuta) {

                                    val dataDeparture = dbhelper.coordinateDao().getCoordRoutePath(
                                        idRuta,
                                        RoomTypePath.DEPARTURE.toString()
                                    )
                                    val dataReturn = dbhelper.coordinateDao().getCoordRoutePath(
                                        idRuta,
                                        RoomTypePath.RETURN.toString()
                                    )

                                    val dataAll = dataDeparture + dataReturn

                                    val listPos1 =
                                        searchTransfer(
                                            dataDepSelect1 + dataRetSelect2,
                                            it.idRuta,
                                            dataAll,
                                            idRuta
                                        )

                                    if (listPos1.isPossible) {
                                        listTransfers1.add(listPos1)
                                    }
                                }
                            }
                        }


                        val routeResultComp2 = verifyRouteTransferResult(
                            listTransfers1,
                            listPossibles2
                        )

                        if (routeResultComp2.isNotEmpty()) {

                            val pts = dbhelper.coordinateDao()
                                .getCoordRoutePath(
                                    routeResultComp2[0].idRuta,
                                    RoomTypePath.DEPARTURE.toString()
                                )

                            val ptsPrepare = UtilidadesMenores()
                                .extractCoordToLatLng(
                                    pts,
                                    RoomTypePath.DEPARTURE.toString(),
                                    routeResultComp2[0].idRuta
                                )

                            val pts2 = dbhelper.coordinateDao()
                                .getCoordRoutePath(
                                    routeResultComp2[0].idRuta,
                                    RoomTypePath.RETURN.toString()
                                )

                            val ptsPrepare2 = UtilidadesMenores()
                                .extractCoordToLatLng(
                                    pts2,
                                    RoomTypePath.RETURN.toString(),
                                    routeResultComp2[0].idRuta
                                )

                            val ptsLast = dbhelper.coordinateDao()
                                .getCoordRoutePath(
                                    routeResultComp2[0].idRutaAnterior,
                                    RoomTypePath.DEPARTURE.toString()
                                )

                            val ptsPrepareLast = UtilidadesMenores()
                                .extractCoordToLatLng(
                                    ptsLast,
                                    RoomTypePath.DEPARTURE.toString(),
                                    routeResultComp2[0].idRutaAnterior
                                )

                            val ptsLast2 = dbhelper.coordinateDao()
                                .getCoordRoutePath(
                                    routeResultComp2[0].idRutaAnterior,
                                    RoomTypePath.RETURN.toString()
                                )

                            val ptsPrepareLast2 = UtilidadesMenores()
                                .extractCoordToLatLng(
                                    ptsLast2,
                                    RoomTypePath.RETURN.toString(),
                                    routeResultComp2[0].idRutaAnterior
                                )


                            val sense2 = mapFunctionsIns.getSense(
                                routeResultComp2[0].puntoConectRuta,
                                routeResultComp2[0].puntoRutaConectAnterior
                            )

                            val ptsFirstRoute = mutableListOf<LatLng>()
                            val ptsSecondBackRoute = mutableListOf<LatLng>()

                            if (sense2 == RoomTypePath.DEPARTURE) {
                                ptsFirstRoute.addAll(ptsPrepare)
                            } else {
                                ptsFirstRoute.addAll(ptsPrepare + ptsPrepare2)
                            }

                            ptsSecondBackRoute.addAll(ptsPrepareLast + ptsPrepareLast2)


                            // Trace first route
                            val routeTransfer = getCutsPointTransfers(
                                mapFunctionsIns,
                                ubicacionUsuario,
                                ubicacionDestino,
                                routeResultComp2[0].idRuta,
                                routeResultComp2[0].idRutaAnterior,
                                routeResultComp2[0].puntoConectRuta,
                                routeResultComp2[0].puntoRutaConectAnterior,
                                ptsFirstRoute,
                                ptsSecondBackRoute
                            )

                            listResultTransfers.add(routeTransfer)
                        }

                        println("Resultado transbordos: $routeResultComp2")
                        callback.onResult(true, listResultTransfers)
                    } else {
                        println("No se encontraron opciones de ruta intermedia")
                        callback.onResult(false, mutableListOf())
                    }
                }
            }
        }
    }

    private suspend fun traceCalculate(
        mapFunctionsIns: MapFunctionOptions,
        ubicacionUsuario: LatLng,
        ubicacionDestino: LatLng,
        idRuta: Int,
        ptsCuts: List<LatLng>
    ): CalculateRoute.RouteCalculate {

        var result: CalculateRoute.RouteCalculate

        withContext(Dispatchers.Default) {

            val cutProcess = mapFunctionsIns.rutaMasCerca(
                ubicacionUsuario,
                ptsCuts
            )
            val cutProcess2 = mapFunctionsIns.rutaMasCerca(
                ubicacionDestino,
                ptsCuts
            )

            result = CalculateRoute.RouteCalculate(
                idRuta,
                ptsCuts,
                cutProcess,
                cutProcess2,
                isTransfer = false,
                idRutaAnterior = null,
                pointConectThisRoute = null,
                pointConectRutaAnterior = null,
                pointsAnteriorRoute = null,
                cutPoint1RutaAnterior = null,
                cutPoint2RutaAnterior = null,
                ubiStartGeneral = ubicacionUsuario,
                ubiEndGeneral = ubicacionDestino
            )
        }

        return result
    }

    private suspend fun getCutsPointTransfers(
        mapFunctionsIns: MapFunctionOptions,
        ubicacionUsuario: LatLng,
        ubicacionDestino: LatLng,
        idRuta: Int,
        idRutaAnterior: Int,
        pointConectThisRoute: LatLng,
        pointConectAnteriorRoute: LatLng,
        ptsPrepare: List<LatLng>,
        ptsSecondBackRoute: List<LatLng>
    ): CalculateRoute.RouteCalculate {
        val result: CalculateRoute.RouteCalculate

        withContext(Dispatchers.Default) {
            val cutProcess = mapFunctionsIns.rutaMasCerca(
                pointConectThisRoute,
                ptsPrepare
            )
            val cutProcess2 = mapFunctionsIns.rutaMasCerca(
                ubicacionDestino,
                ptsPrepare
            )

            val cutProcess3 = mapFunctionsIns.rutaMasCerca(
                ubicacionUsuario,
                ptsSecondBackRoute
            )
            val cutProcess4 = mapFunctionsIns.rutaMasCerca(
                pointConectAnteriorRoute,
                ptsSecondBackRoute
            )

            result = CalculateRoute.RouteCalculate(
                idRuta,
                ptsPrepare,
                cutProcess,
                cutProcess2,
                true,
                idRutaAnterior,
                pointConectThisRoute,
                pointConectAnteriorRoute,
                ptsSecondBackRoute,
                cutProcess3,
                cutProcess4,
                ubicacionUsuario,
                ubicacionDestino
            )
        }
        return result
    }


    suspend fun searchTransfer(
        routeCoords: List<Coordinate>,
        idRuta: Int,
        route2Coords: List<Coordinate>,
        id2Ruta: Int
    ): CalculateRoute.PossibleRouteTransfer {
        var isPossible = false
        var idConectRuta = LatLng(0.0, 0.0)
        var idConectRutaAnterior = LatLng(0.0, 0.0)
        var cont = 0

        withContext(Dispatchers.Default) {

            for (station in 0..routeCoords.size - 1) {
                cont++

                val point1 = Location("UBI LIST").apply {
                    latitude = routeCoords[station].latitude
                    longitude = routeCoords[station].longitude
                }

                for (station2 in 0..route2Coords.size - 1) {
                    val point2 = Location("UBI LIST").apply {
                        latitude = route2Coords[station2].latitude
                        longitude = route2Coords[station2].longitude
                    }

                    // Calcular la distancia entre la ubicación y el punto de la estación
                    val distancia = point1.distanceTo(point2).toInt()

                    // Si la distancia es menor a 200 metros, se considera posible la ruta
                    if (distancia < 100) {
                        isPossible = true
                        idConectRuta = LatLng(
                            route2Coords[station2].latitude,
                            route2Coords[station2].longitude
                        )
                        idConectRutaAnterior =
                            LatLng(routeCoords[station].latitude, routeCoords[station].longitude)
                    }
                }
            }
        }
        // Devuelve el resultado después de que el cálculo se haya completado
        return CalculateRoute.PossibleRouteTransfer(
            isPossible,
            id2Ruta,
            idConectRuta,
            idRuta,
            idConectRutaAnterior
        )

    }


    suspend fun verifyRouteResult(
        listRoutes1: List<CalculateRoute.PossibleRoute>,
        listRoutes2: List<CalculateRoute.PossibleRoute>
    ): MutableList<CalculateRoute.PossibleRoute> {
        return withContext(Dispatchers.Default) {
            listRoutes1.filter { route1 ->
                listRoutes2.any { it.idRuta == route1.idRuta }
            }.map { it }.toMutableList()
        }
    }


    suspend fun verifyRouteTransferResult(
        listTransfer: List<CalculateRoute.PossibleRouteTransfer>,
        listAnteriorPossibles: List<CalculateRoute.PossibleRoute>
    ): MutableList<CalculateRoute.PossibleRouteTransfer> {
        return withContext(Dispatchers.Default) {
            listTransfer.filter { route1 ->
                listAnteriorPossibles.any { it.idRuta == route1.idRuta }
            }.map {
                it
            }.toMutableList()
        }
    }


    suspend fun getBestOptionsCount(
        listPossibles: MutableList<CalculateRoute.PossibleRoute>,
        numAttemps: Int
    ): List<Int> {
        return withContext(Dispatchers.Default) {
            // Asegúrate de que `numAttemps` no sea mayor que la cantidad de rutas posibles
            val maxIntentos = minOf(listPossibles.size, numAttemps)
            val rutasSeleccionadas = mutableListOf<Int>()

            for (i in 0 until maxIntentos) {
                rutasSeleccionadas.add(listPossibles[i].idRuta)
            }

            rutasSeleccionadas
        }
    }

    private suspend fun getPossibleRoutes(
        ubi: Location,
        routeCoords: List<Coordinate>,
        idRuta: Int
    ): CalculateRoute.PossibleRoute {
        var isPossible = false

        withContext(Dispatchers.Default) {

            for (station in 0..routeCoords.size - 1) {
                val point = Location("UBI LIST").apply {
                    latitude = routeCoords[station].latitude
                    longitude = routeCoords[station].longitude
                }

                // Calcular la distancia entre la ubicación y el punto de la estación
                val distancia = ubi.distanceTo(point).toInt()

                // Si la distancia es menor a 500 metros, se considera posible la ruta
                if (distancia < 500) {
                    isPossible = true
                }
            }
        }

        return CalculateRoute.PossibleRoute(isPossible, idRuta, null)
    }

}
