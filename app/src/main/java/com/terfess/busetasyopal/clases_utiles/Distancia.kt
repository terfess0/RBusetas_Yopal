package com.terfess.busetasyopal.clases_utiles

import android.location.Location
import com.google.android.gms.maps.model.LatLng

class Distancia {
    private var mejorPuntoInicio = PlanearRutaDestino.Datos.mejorPuntoaInicio
    private var mejorPuntoDestino = PlanearRutaDestino.Datos.mejorPuntoaDestino

    fun compararDistanciasConDestino(
        puntosRuta: List<LatLng>,
        ubiInicio: Location,
        ubisPuntos: Location,
        esInicio: Boolean,
        idRuta: Int
    ){
        //devuelve el punto mas cercano entre una ubicacion y una lista de ubicaciones

        var distancia: Int
        for (puntoEstacion in puntosRuta.indices) {
            val estacion = puntosRuta[puntoEstacion]
            ubisPuntos.latitude = estacion.latitude
            ubisPuntos.longitude = estacion.longitude
            distancia =
                (ubiInicio.distanceTo(ubisPuntos)).toInt() //aqui se saca la distancia en metros

            //hay array para inicio y uno para destino, por eso la comprobacion
            if (esInicio) {
                if (distancia < mejorPuntoInicio[1]) {
                    mejorPuntoInicio[0] = puntoEstacion
                    mejorPuntoInicio[1] = distancia
                    mejorPuntoInicio[2] = idRuta

                }
            }
            if (!esInicio) {
                if (distancia < mejorPuntoDestino[1]) {
                    mejorPuntoDestino[0] = puntoEstacion
                    mejorPuntoDestino[1] = distancia
                    mejorPuntoDestino[2] = idRuta

                }
            }
        }
    }

    fun compararDistanciasInicio(
        puntos: List<LatLng>,
        ubiInicio: Location,
        ubiDestino: Location,
    ): LatLng {
        //devuelve el punto mas cercano entre una ubicacion y una lista de ubicaciones
        var puntoMasCerca = LatLng(0.0, 0.0)
        var distancia: Int
        for (f in puntos.indices) {
            val estacion = puntos[f]
            ubiDestino.latitude = estacion.latitude
            ubiDestino.longitude = estacion.longitude

            distancia =
                (ubiInicio.distanceTo(ubiDestino)).toInt() //aqui se saca la distancia en metros

            //comprobar distancia obtenida con la distancia guardada en la iteracion
            if (distancia < RutaBasic.CreatRuta.masCortaInicio[1]) {
                RutaBasic.CreatRuta.masCortaInicio[0] = f
                RutaBasic.CreatRuta.masCortaInicio[1] = distancia
            }

            puntoMasCerca = LatLng(estacion.latitude, estacion.longitude)
        }
        return puntoMasCerca
    }
}