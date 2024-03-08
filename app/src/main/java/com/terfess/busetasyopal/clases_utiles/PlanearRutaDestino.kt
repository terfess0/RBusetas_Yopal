package com.terfess.busetasyopal.clases_utiles

import android.content.Context
import android.location.Location
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.actividades.Mapa
import com.terfess.busetasyopal.modelos_dato.DatoCalcularRuta

class PlanearRutaDestino(private val mapa: Context, private val gmap: GoogleMap) {

    private var polylineOptions = PolylineOptions()

    object Datos {
        val mejorPuntoaInicio = IntArray(3) // 0: estacion  1: distancia  2: idRuta
        val mejorPuntoaDestino = IntArray(3) // 0: estacion  1: distancia  2: idRuta
    }

    fun rutaToDestino(
        ubicacionUsuario: LatLng,
        ubicacionDestino: LatLng,
        instanciaMapa: Mapa
    ) {
        val dbhelper = DatosASqliteLocal(mapa)
        val rutaIds = intArrayOf(2, 3, 6, 7, 8, 9, 10, 13)//indices para recuperar las rutas
        var puntosRutaSalida: List<DatoCalcularRuta>
        var puntosRutaLlegada: List<DatoCalcularRuta>
        println("---------------------------------------")

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

        for (i in 0..rutaIds.size - 1) {
            val iterador = rutaIds[i]
            //recuperar las coordenadas de las almacenadas localmente desde firebase
            puntosRutaSalida = dbhelper.obtenerCoordenadasCalcularRuta(iterador, "coordenadas1")
            puntosRutaLlegada =
                dbhelper.obtenerCoordenadasCalcularRuta(iterador, "coordenadas2")
            val idRuta = puntosRutaSalida[0].idRuta
            val datosCompletos = puntosRutaSalida[0].coordenadas + puntosRutaLlegada[0].coordenadas
            //-------------------------------------------------------------

            //compara las distancias entre el usuario y cada estacion
            Distancia().compararDistanciasConDestino(
                datosCompletos,
                ubiInicial,
                ubiEstacion,
                true,
                idRuta
            )
            Distancia().compararDistanciasConDestino(
                datosCompletos,
                ubiDestino,
                ubiEstacion,
                false,
                idRuta
            )
        }
        println("--IDRuta:${Datos.mejorPuntoaInicio[2]}  PuntoCorte1: ${Datos.mejorPuntoaInicio[0]}")
        println("IDRuta:${Datos.mejorPuntoaDestino[2]}  PuntoCorte2: ${Datos.mejorPuntoaDestino[0]}")
        //-------------------------------------------------------------
        //MODIFICAR/PREPARAR LOS PUNTOS DE LAS POLYLINEAS

        val idMejorRuta = Datos.mejorPuntoaInicio[2] //da lo mismo el de inicio o destino
        val puntosRutaSalida1 = dbhelper.obtenerCoordenadasCalcularRuta(idMejorRuta, "coordenadas1")
        val puntosRutaLlegada1 =
            dbhelper.obtenerCoordenadasCalcularRuta(idMejorRuta, "coordenadas2")

        //-----------------------------------------------------------------------------------------
        //en caso de que el puntocorte2 sea mayor a los puntos de llegada de la ruta
        //debido a posible cruce de rutas distintas de hara de nuevo la comparacion
        //usando el idRuta de la variable mejorpuntoinicio

        if (Datos.mejorPuntoaInicio[2] != Datos.mejorPuntoaDestino[2]) {
            Datos.mejorPuntoaDestino[1] = Int.MAX_VALUE
            val puntosSalida =
                dbhelper.obtenerCoordenadasCalcularRuta(idMejorRuta, "coordenadas1")
            val puntosLlegada =
                dbhelper.obtenerCoordenadasCalcularRuta(idMejorRuta, "coordenadas2")

            val puntosReparadores = puntosSalida + puntosLlegada
            Distancia().compararDistanciasConDestino(
                puntosReparadores[0].coordenadas.toMutableList(),
                ubiDestino,
                ubiEstacion,
                false,
                idMejorRuta
            )
            println("nnReparados")
        }

        println("--IDRuta:${Datos.mejorPuntoaInicio[2]}  PuntoCorte1: ${Datos.mejorPuntoaInicio[0]}")
        println("IDRuta:${Datos.mejorPuntoaDestino[2]}  PuntoCorte2: ${Datos.mejorPuntoaDestino[0]}")
        //-----------------------------------------------------------------------------------------

        //------AGREGAR POLYLINEAS TENUES DE LA RUTA ORIGINAL-----
        val puntosSalida = puntosRutaSalida1[0].coordenadas.toMutableList()
        val puntosLlegada = puntosRutaLlegada1[0].coordenadas.toMutableList()
        if (puntosSalida.isNotEmpty() && puntosLlegada.isNotEmpty()) {
            val opcionesPolylineaSubida = PolylineOptions()
            val opcionesPolylineaBajada = PolylineOptions()

            opcionesPolylineaSubida.points.addAll(puntosSalida)
            opcionesPolylineaSubida.color(ContextCompat.getColor(mapa, R.color.salida_parcial))
                .width(14f)
            gmap.addPolyline(opcionesPolylineaSubida)

            opcionesPolylineaBajada.points.addAll(puntosLlegada)
            opcionesPolylineaBajada.color(ContextCompat.getColor(mapa, R.color.llegada_parcial))
                .width(14f)
            gmap.addPolyline(opcionesPolylineaBajada)
        }
        //-----------------------------------------------------------------------------------------


        val puntoCorte1 =
            Datos.mejorPuntoaInicio[0] //numero elemento de arreglo de coordenadas mas cercano al inicio
        val puntoCorte2 =
            Datos.mejorPuntoaDestino[0] //numero elemento de arreglo de coordenadas mas cercano al destino


        if (Datos.mejorPuntoaInicio[2] == Datos.mejorPuntoaDestino[2]) {

            val totalSubida1 = puntosSalida.size - 1
            val totalLlegada1 = puntosLlegada.size - 1
            var puntosFinal1 = mutableListOf<LatLng>()

            println("** TamañoSubida: $totalSubida1")
            println("** TamañoLlegada: $totalLlegada1")

            //comienza creacion de los puntos a trazar de acuerdo al caso
            if (puntoCorte1 < totalSubida1 && puntoCorte2 < totalSubida1 && puntoCorte1 < puntoCorte2) {
                //si el puntocorte1 esta en salida y puntocorte2 esta en llegada y puntocorte1 es menor a puntocorte2

                puntosFinal1 = puntosSalida.toMutableList().subList(puntoCorte1, puntoCorte2 + 1)
                println("Caso If numero 1 salida")

                if (puntosFinal1.isNotEmpty()) {
                    //agregar marcador para indicar el lugar de bajada de la buseta

                    agregarMarcador(
                        puntosFinal1.last(),
                        R.drawable.ic_estacion_fin_ruta,
                        "Baja de la buseta"
                    )

                    val dosPuntosCaminata = mutableListOf<LatLng>()
                    dosPuntosCaminata.add(puntosFinal1.last())
                    dosPuntosCaminata.add(ubicacionDestino)

                    val polylineOptions = PolylineOptions()
                    polylineOptions.color(
                        ContextCompat.getColor(
                            mapa,
                            R.color.distancia_caminar_destino
                        )
                    )
                    polylineOptions.pattern(listOf(Dash(10f), Gap(9f)))
                    polylineOptions.addAll(dosPuntosCaminata)

                    gmap.addPolyline(polylineOptions)
                    gmap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(puntosFinal1[0], 16f),
                        3000,
                        null
                    )
                }
            } else if (puntoCorte1 < totalSubida1 && puntoCorte2 < totalSubida1 && puntoCorte1 > puntoCorte2) {
                //no se pudo generar camino
                instanciaMapa.mostrarIndicacionesCalculadas(true)
                println("Caso If negacion numero 1 salida")
            } else if (puntoCorte1 < totalSubida1 && puntoCorte2 > totalSubida1 && puntoCorte1 < puntoCorte2) {
                puntosFinal1 = puntosSalida.subList(puntoCorte1, totalSubida1)
                println("Caso If 2 salida")
            } else if (puntoCorte1 < totalSubida1 && puntoCorte2 > totalSubida1 && puntoCorte1 > puntoCorte2) {
                //no se pudo generar camino
                instanciaMapa.mostrarIndicacionesCalculadas(true)
                println("Caso If negacion numero 2 salida")
            }

            polylineOptions.points.clear()
            polylineOptions.width(9f).color(
                ContextCompat.getColor(
                    mapa,
                    R.color.recorridoIda
                )
            )//color polyline salida

            val rutaResultante1 = gmap.addPolyline(polylineOptions) //agregar polyline
            //demas propiedades polyline
            rutaResultante1.points = puntosFinal1 //puntos polyline salida
            rutaResultante1.jointType = JointType.ROUND
            rutaResultante1.endCap = RoundCap()
            rutaResultante1.startCap = RoundCap()

            if (puntosFinal1.isNotEmpty()) {
                //agregar marcador para indicar el lugar de subida a la buseta
                val opcionesMarcador1 = MarkerOptions().position(puntosFinal1[0])
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_estacion_inicio_ruta))
                    .title("Toma la buseta")
                gmap.addMarker(opcionesMarcador1)

                val dosPuntosCaminata = mutableListOf<LatLng>()
                dosPuntosCaminata.add(ubicacionUsuario)
                dosPuntosCaminata.add(puntosFinal1[0])

                val polylineOptionsA = PolylineOptions()
                polylineOptionsA.color(
                    ContextCompat.getColor(
                        mapa,
                        R.color.distancia_caminar_inicio
                    )
                )
                polylineOptionsA.pattern(listOf(Dash(10f), Gap(9f)))
                polylineOptionsA.addAll(dosPuntosCaminata)

                gmap.addPolyline(polylineOptionsA)

                gmap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(puntosFinal1[0], 16f),
                    3000,
                    null
                )
            }


            //-----------------------------------------------------------------------------------------

            //puntos polyline de llegada
            var puntosFinal = mutableListOf<LatLng>()

            //comienza creacion de los puntos a trazar de acuerdo al caso
            if (puntoCorte1 > totalSubida1 && puntoCorte2 > totalSubida1 && puntoCorte2 > puntoCorte1) {
                //si el puntocorte1 es menor a total pts salida y puntocorte2 es mayor a puntocorte1 y si puntocorte2 esta en pts salida
                val todosPuntos = puntosSalida + puntosLlegada

                Distancia().compararDistanciasConDestino(
                    puntosLlegada,
                    ubiDestino,
                    ubiEstacion,
                    false,
                    idMejorRuta
                )

                puntosFinal = todosPuntos.toMutableList()
                    .subList(puntoCorte1, puntoCorte2 + 1)

                println("Caso If numero 1 llegada")
                if (puntosFinal.isNotEmpty()) {
                    //agregar marcador para indicar el lugar de bajada de la buseta

                    val opcionesMarcador = MarkerOptions().position(puntosFinal[0])
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_estacion_inicio_ruta))
                        .title("Toma la buseta")
                    gmap.addMarker(opcionesMarcador)

                    val dosPuntosCaminata = mutableListOf<LatLng>()
                    dosPuntosCaminata.add(ubicacionUsuario)
                    dosPuntosCaminata.add(puntosFinal[0])
                    val polylineOptions = PolylineOptions()
                    polylineOptions.color(
                        ContextCompat.getColor(
                            mapa,
                            R.color.distancia_caminar_inicio
                        )
                    )
                    polylineOptions.pattern(listOf(Dash(10f), Gap(9f)))
                    polylineOptions.addAll(dosPuntosCaminata)

                    gmap.addPolyline(polylineOptions)
                    gmap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(puntosFinal[0], 16f),
                        3000,
                        null
                    )
                }

            } else if (puntoCorte1 > totalSubida1 && puntoCorte2 > totalSubida1 && puntoCorte2 < puntoCorte1) {
                //no se pudo generar camino
                instanciaMapa.mostrarIndicacionesCalculadas(true)
                println("Caso If negacion numero 1 llegada")
            } else if (puntoCorte1 < totalSubida1 && puntoCorte2 > totalSubida1 && puntoCorte1 < puntoCorte2) {
                val topeLlegada = puntoCorte2 - totalSubida1
                puntosFinal = puntosLlegada.subList(0, topeLlegada)
                println("Caso If 2 llegada")
            } else if (puntoCorte1 < totalSubida1 && puntoCorte2 > totalSubida1 && puntoCorte1 > puntoCorte2) {
                //no se pudo generar camino
                instanciaMapa.mostrarIndicacionesCalculadas(true)
                println("Caso If negacion numero 2 llegada")
            }

            polylineOptions.points.clear()
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

            if (puntosFinal.isNotEmpty()) {
                //agregar marcador para indicar el lugar de bajada de la buseta
                val opcionesMarcador = MarkerOptions().position(puntosFinal.last())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_estacion_fin_ruta))
                    .title("Baja de la buseta")
                gmap.addMarker(opcionesMarcador)

                val dosPuntosCaminata = mutableListOf<LatLng>()
                dosPuntosCaminata.add(puntosFinal.last())
                dosPuntosCaminata.add(ubicacionDestino)

                val polylineOptionsB = PolylineOptions()
                polylineOptionsB.color(
                    ContextCompat.getColor(
                        mapa,
                        R.color.distancia_caminar_destino
                    )
                )
                polylineOptionsB.pattern(listOf(Dash(10f), Gap(9f)))
                polylineOptionsB.addAll(dosPuntosCaminata)

                gmap.addPolyline(polylineOptionsB)
            }

            println("--IDRuta:${Datos.mejorPuntoaInicio[2]}  PuntoCorte1: ${Datos.mejorPuntoaInicio[0]} TamañoSubida: $totalSubida1")
            println("IDRuta:${Datos.mejorPuntoaDestino[2]}  PuntoCorte2: ${Datos.mejorPuntoaDestino[0]} TamañoLlegada: $totalLlegada1")


        }
    }

    private fun agregarMarcador(punto: LatLng, icono: Int, titulo: String) {
        //FUNCION AÑADIR MARCADOR AL MAPA
        val opcionesMarcador = MarkerOptions()
            .position(punto).icon(BitmapDescriptorFactory.fromResource(icono))
            .title(titulo)
        gmap.addMarker(opcionesMarcador)
    }

}