package com.terfess.busetasyopal.clases_utiles

import android.content.Context
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
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
        val rutaIds = intArrayOf(2, 3, 6, 7, 8, 9, 10, 11, 13)//indices para recuperar las rutas
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

        for (iterador in rutaIds) {
            //recuperar las coordenadas de las almacenadas localmente desde firebase
            puntosRutaSalida = dbhelper.obtenerCoordenadasCalcularRuta(iterador, "coordenadas1")
            puntosRutaLlegada =
                dbhelper.obtenerCoordenadasCalcularRuta(iterador, "coordenadas2")
            val idRuta = puntosRutaSalida[0].idRuta
            val datosCompletos = puntosRutaSalida[0].coordenadas + puntosRutaLlegada[0].coordenadas
            //-------------------------------------------------------------

            //compara las distancias entre el usuario y cada estacion
            //compara las distancias entre el destino y cada estacion
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

        val puntoCorte1 =
            Datos.mejorPuntoaInicio[0] //numero elemento de arreglo de coordenadas mas cercano al inicio
        val puntoCorte2 =
            Datos.mejorPuntoaDestino[0] //numero elemento de arreglo de coordenadas mas cercano al destino

        //-----------------------------------------------------------------------------------------
        //en caso de que el puntocorte2 sea mayor a los puntos de llegada de la ruta
        //debido a posible cruce de rutas distintas de hara de nuevo la comparacion
        //usando el idRuta de la variable mejorpuntoinicio


        if (Datos.mejorPuntoaInicio[2] != Datos.mejorPuntoaDestino[2]) {
            Datos.mejorPuntoaDestino[1] = Int.MAX_VALUE
            val puntosReparadores =
                dbhelper.obtenerCoordenadasCalcularRuta(idMejorRuta, "coordenadas2")
            Distancia().compararDistanciasConDestino(
                puntosReparadores[0].coordenadas.toMutableList(),
                ubiDestino,
                ubiEstacion,
                false,
                idMejorRuta
            )
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
            opcionesPolylineaSubida.color(ContextCompat.getColor(mapa, R.color.azul_parcial))
                .width(10f)
            gmap.addPolyline(opcionesPolylineaSubida)

            opcionesPolylineaBajada.points.addAll(puntosLlegada)
            opcionesPolylineaBajada.color(ContextCompat.getColor(mapa, R.color.rojo_parcial))
                .width(10f)
            gmap.addPolyline(opcionesPolylineaBajada)
        }

        //-----------------------------------------------------------------------------------------

        if (Datos.mejorPuntoaInicio[2] == Datos.mejorPuntoaDestino[2]) {

            val totalSubida1 = puntosRutaSalida1[0].coordenadas.size
            val totalLlegada1 = puntosRutaLlegada1[0].coordenadas.size
            var puntosFinal1 = mutableListOf<LatLng>()

            println("** TamañoSubida: $totalSubida1")
            println("** TamañoLlegada: $totalLlegada1")

            //comienza creacion de los puntos a trazar de acuerdo al caso
            if (puntoCorte1 < totalSubida1 && puntoCorte2 > puntoCorte1 && puntoCorte2 < totalSubida1) {
                //si el puntocorte1 es menor a total pts salida y puntocorte2 es mayor a puntocorte1 y si puntocorte2 esta en pts salida
                puntosFinal1 =
                    puntosRutaSalida1[0].coordenadas.toMutableList()
                        .subList(puntoCorte1, puntoCorte2)
                println("Caso If numero 1")
                if (puntosFinal1.isNotEmpty()) {
                    //agregar marcador para indicar el lugar de bajada de la buseta
                    val puntoFinalPuntos = puntosFinal1.size - 1
                    val opcionesMarcador = MarkerOptions().position(puntosFinal1[puntoFinalPuntos])
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_estacion_fin_ruta))
                        .title("Baja de la buseta")
                    gmap.addMarker(opcionesMarcador)

                    val dosPuntosCaminata = mutableListOf<LatLng>()
                    dosPuntosCaminata.add(puntosFinal1[puntosFinal1.size - 1])
                    dosPuntosCaminata.add(ubicacionDestino)
                    val polylineOptions = PolylineOptions()
                    polylineOptions.color(ContextCompat.getColor(mapa, R.color.distancia_caminar))
                    polylineOptions.pattern(listOf(Dash(10f), Gap(9f)))
                    polylineOptions.addAll(dosPuntosCaminata)

                    gmap.addPolyline(polylineOptions)
                }


            } else if (puntoCorte1 < totalSubida1 && puntoCorte2 < puntoCorte1) {
                //si el puntocorte1 es menor a total pts salida y puntocorte2 es mayor a puntocorte1 y si puntocorte2 esta en pts salida
                puntosFinal1 =
                    puntosRutaSalida1[0].coordenadas.toMutableList()
                        .subList(puntoCorte2, puntoCorte1)
                println("Caso If numero 1-1-2")
                if (puntosFinal1.isNotEmpty()) {
                    //agregar marcador para indicar el lugar de bajada de la buseta
                    val puntoFinalPuntos = puntosFinal1.size - 1
                    val opcionesMarcador = MarkerOptions().position(puntosFinal1[puntoFinalPuntos])
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_estacion_fin_ruta))
                        .title("Baja de la buseta")
                    gmap.addMarker(opcionesMarcador)

                    val dosPuntosCaminata = mutableListOf<LatLng>()
                    dosPuntosCaminata.add(puntosFinal1[puntosFinal1.size - 1])
                    dosPuntosCaminata.add(ubicacionDestino)
                    val polylineOptions = PolylineOptions()
                    polylineOptions.color(ContextCompat.getColor(mapa, R.color.distancia_caminar))
                    polylineOptions.pattern(listOf(Dash(10f), Gap(9f)))
                    polylineOptions.addAll(dosPuntosCaminata)

                    gmap.addPolyline(polylineOptions)
                }
            } else if (puntoCorte2 > puntoCorte1 && puntoCorte2 > totalSubida1 && puntoCorte1 < totalSubida1) {
                println("Caso If numero prueba")
                //si puntocorte2 es mayor a puntocorte1 y puntocorte2 esta en pts llegada y puntocorte1 esta dentro de pts salida
                puntosFinal1 =
                    puntosRutaSalida1[0].coordenadas.toMutableList()
                        .subList(puntoCorte1, totalSubida1)//+1 ya que comienza de 0 los arreglos
                println("Caso If numero 2")


            } else if (puntoCorte1 > puntoCorte2 && puntoCorte1 < totalSubida1) {
                //si puntocorte1 es mayor a puntocorte2 y puntocorte1 esta dentro de pts salida
                puntosFinal1 =
                    puntosRutaSalida1[0].coordenadas.toMutableList()
                        .subList(puntoCorte2, puntoCorte1)
                println("Caso If numero 3")


            } else if (puntoCorte2 < puntoCorte1 && puntoCorte1 > totalSubida1 && puntoCorte2 < totalSubida1) {
                //si puntocorte2 es menor a puntocorte1 y puntocorte1 esta en pts llegada
                //ESTE ES CASO 4 RUTA SALIDA

                puntosFinal1 =
                    puntosRutaSalida1[0].coordenadas.toMutableList()
                        .subList(puntoCorte2, totalSubida1)
                println("Caso If numero 4")
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
                polylineOptionsA.color(ContextCompat.getColor(mapa, R.color.distancia_caminar))
                polylineOptionsA.pattern(listOf(Dash(10f), Gap(9f)))
                polylineOptionsA.addAll(dosPuntosCaminata)

                gmap.addPolyline(polylineOptionsA)
            }


            //-----------------------------------------------------------------------------------------

            //puntos polyline de llegada
            var puntosFinal = mutableListOf<LatLng>()

            //comienza creacion de los puntos a trazar de acuerdo al caso
            if (puntoCorte2 > puntoCorte1 && puntoCorte2 > totalSubida1 && puntoCorte1 < totalSubida1) {
                //si puntocorte2 es mayor a puntocorte1 y puntocorte2 esta en pts llegada y puntocorte1 esta dentro del rango de pts salida
                val x = puntoCorte2 - totalSubida1
                puntosFinal =
                    puntosRutaLlegada1[0].coordenadas.toMutableList()
                        .subList(0, x)
                println("Caso If numero 6")


            } else if (puntoCorte1 > totalSubida1 && puntoCorte2 > puntoCorte1) {
                //si puntocorte1 esta en pts llegada y puntocorte2 es mayor a puntocorte1
                puntosFinal =
                    puntosRutaLlegada1[0].coordenadas.toMutableList()
                        .subList(puntoCorte1 - totalSubida1, puntoCorte2 - totalSubida1)
                println("Caso If numero 8")


            } else if (puntoCorte2 < puntoCorte1 && puntoCorte1 > totalSubida1 && puntoCorte2 < totalSubida1) {
                //si puntocorte2 es menor a puntocorte1 y puntocorte1 esta en pts llegada
                //esta es contraparte del caso 4 de ruta salida
                puntosFinal =
                    puntosRutaLlegada1[0].coordenadas.toMutableList()
                        .subList(0, (puntoCorte1 - totalSubida1))
                println("Caso If numero 9")


            } else if (puntoCorte1 > puntoCorte2 && puntoCorte2 > totalSubida1) {
                //si el puntocorte1 es menor a total pts salida y puntocorte2 es mayor a puntocorte1 y si puntocorte2 esta en pts salida
                puntosFinal =
                    puntosRutaLlegada1[0].coordenadas.toMutableList()
                        .subList(puntoCorte2 - totalSubida1, puntoCorte1 - totalSubida1)
                println("Caso If numero 1-1-2")
                if (puntosFinal.isNotEmpty()) {
                    //agregar marcador para indicar el lugar de bajada de la buseta

                    val opcionesMarcador = MarkerOptions().position(puntosFinal[0])
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_estacion_inicio_ruta))
                        .title("Baja de la buseta")
                    gmap.addMarker(opcionesMarcador)

                    val dosPuntosCaminata = mutableListOf<LatLng>()
                    dosPuntosCaminata.add(ubicacionUsuario)
                    dosPuntosCaminata.add(puntosFinal[0])
                    val polylineOptions = PolylineOptions()
                    polylineOptions.color(ContextCompat.getColor(mapa, R.color.distancia_caminar))
                    polylineOptions.pattern(listOf(Dash(10f), Gap(9f)))
                    polylineOptions.addAll(dosPuntosCaminata)

                    gmap.addPolyline(polylineOptions)
                }
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
                val puntoFinalPuntos = puntosFinal.size - 1
                val opcionesMarcador = MarkerOptions().position(puntosFinal[puntoFinalPuntos])
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_estacion_fin_ruta))
                    .title("Baja de la buseta")
                gmap.addMarker(opcionesMarcador)

                val dosPuntosCaminata = mutableListOf<LatLng>()
                dosPuntosCaminata.add(puntosFinal[puntosFinal.size - 1])
                dosPuntosCaminata.add(ubicacionDestino)

                val polylineOptionsB = PolylineOptions()
                polylineOptionsB.color(ContextCompat.getColor(mapa, R.color.distancia_caminar))
                polylineOptionsB.pattern(listOf(Dash(10f), Gap(9f)))
                polylineOptionsB.addAll(dosPuntosCaminata)

                gmap.addPolyline(polylineOptionsB)
            }

            println("--IDRuta:${Datos.mejorPuntoaInicio[2]}  PuntoCorte1: ${Datos.mejorPuntoaInicio[0]} TamañoSubida: $totalSubida1")
            println("IDRuta:${Datos.mejorPuntoaDestino[2]}  PuntoCorte2: ${Datos.mejorPuntoaDestino[0]} TamañoLlegada: $totalLlegada1")


        }
    }

}