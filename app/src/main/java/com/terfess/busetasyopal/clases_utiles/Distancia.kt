package com.terfess.busetasyopal.clases_utiles

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.terfess.busetasyopal.actividades.mapa.functions.MapFunctionOptions
import com.terfess.busetasyopal.actividades.mapa.functions.calculate_route.CalculateRoute
import com.terfess.busetasyopal.room.model.Coordinate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Distancia {

    fun compararDistanciasConDestino(
        puntosRuta: List<LatLng>,
        ubiInicio: Location,
        ubisPuntos: Location,
        idRuta: Int
    ): CalculateRoute.PossibleRoute {
        //devuelve el punto mas cercano entre una ubicacion y una lista de ubicaciones
        var isPosible = false

        for (puntoEstacion in 0..puntosRuta.size - 1) {

            val estacion = puntosRuta[puntoEstacion]
            ubisPuntos.latitude = estacion.latitude
            ubisPuntos.longitude = estacion.longitude

            val distancia =
                (ubiInicio.distanceTo(ubisPuntos)).toInt() //aqui se saca la distancia en metros

            if (distancia < 500) {
                isPosible = true
            }
        }
        val result = CalculateRoute.PossibleRoute(isPosible, idRuta, null)
        return result
    }


    fun compararRutas(
        puntosRuta: List<Coordinate>,
        puntosRuta2: List<Coordinate>
    ): CalculateRoute.PossibleRoute {
        var isPosible = false
        var rutaP = 0

        // Iterar sobre todos los puntos de la primera ruta
        for (punto1 in puntosRuta) {

            // Preparar la ubicación inicial
            val ubiInicial = Location("Ubicacion Inicial").apply {
                latitude = punto1.latitude
                longitude = punto1.longitude
            }

            // Comparar con todos los puntos de la segunda ruta
            for (punto2 in puntosRuta2) {

                // Preparar la ubicación destino
                val ubiDestino = Location("Ubicacion Destino").apply {
                    latitude = punto2.latitude
                    longitude = punto2.longitude
                }

                // Calcular la distancia entre ambos puntos
                val distancia = ubiInicial.distanceTo(ubiDestino).toInt()

                // Si hay un punto que está a menos de 500 metros, marcar como posible y salir del bucle
                if (distancia < 200) {
                    isPosible = true
                    rutaP = punto2.id_route
                }
            }

        }

        // Retornar el resultado
        return CalculateRoute.PossibleRoute(isPosible, rutaP, null)
    }


    fun compararDistanciasInicio(
        puntos: List<LatLng>,
        ubiInicio: Location
    ): CalculateRoute.PuntoMasCerca {
        //devuelve el punto mas cercano entre una ubicacion y una lista de ubicaciones

        val functionsInstance = MapFunctionOptions()
        var mejorDistancia = 10000
        var puntoStation = 0

        for (f in 0..puntos.size - 1) {

            val estacion = functionsInstance.createObjLocation(
                puntos[f],
                "UBI FROM LIST"
            )

            val distancia =
                (ubiInicio.distanceTo(estacion)).toInt() //aqui se saca la distancia en metros

            //comprobar distancia obtenida con la distancia guardada en la iteracion
            if (distancia < mejorDistancia) {
                puntoStation = f
                mejorDistancia = distancia
            }
        }

        return CalculateRoute.PuntoMasCerca(puntoStation, mejorDistancia)
    }

    fun bestFirstDistance(
        puntos: List<LatLng>,
        ubiInicio: Location
    ): CalculateRoute.PuntoMasCerca {
        //devuelve el punto mas cercano entre una ubicacion y una lista de ubicaciones

        val functionsInstance = MapFunctionOptions()
        var mejorDistancia = 100
        var puntoStation = -1


        for (f in 0..puntos.size - 1) {

            val estacion = functionsInstance.createObjLocation(
                puntos[f],
                "UBI FROM LIST"
            )

            val distancia =
                (ubiInicio.distanceTo(estacion)).toInt() //aqui se saca la distancia en metros

            //comprobar distancia obtenida con la distancia guardada en la iteracion
            if (distancia < mejorDistancia) {
                puntoStation = f
                mejorDistancia = distancia
                break
            }
        }

        return CalculateRoute.PuntoMasCerca(puntoStation, mejorDistancia)
    }

    suspend fun compararDistanciasInicioCalculate(
        puntos: List<LatLng>,
        ubiInicio: Location
    ): CalculateRoute.PuntoMasCerca {
        //devuelve el punto mas cercano entre una ubicacion y una lista de ubicaciones
        var puntoMasCerca = LatLng(0.0, 0.0)
        var distancia: Int
        val functionsInstance = MapFunctionOptions()
        var mejorDistancia = 10000
        var puntoStation = 0

        withContext(Dispatchers.IO) {
            for (f in puntos.indices) {

                val estacion = functionsInstance.createObjLocation(
                    puntos[f],
                    "UBI FROM LIST"
                )

                distancia =
                    (ubiInicio.distanceTo(estacion)).toInt() //aqui se saca la distancia en metros

                //comprobar distancia obtenida con la distancia guardada en la iteracion
                if (distancia < mejorDistancia) {
                    puntoStation = f
                    mejorDistancia = distancia
                }

            }
        }
        return CalculateRoute.PuntoMasCerca(puntoStation, mejorDistancia)
    }

}
