package com.terfess.busetasyopal.clases_utiles

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import android.location.Location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.actividades.Splash
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal.CreatRuta.estamarcado1
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal.CreatRuta.estamarcado2
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal.CreatRuta.marcador1
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal.CreatRuta.marcador2
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal.CreatRuta.masCortaInicio
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal.CreatRuta.polyCalculada
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal.CreatRuta.polyCaminata
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal.CreatRuta.polyLlegada
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal.CreatRuta.polySalida
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal.CreatRuta.puntosCalculada
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal.CreatRuta.puntosCaminata
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal.CreatRuta.puntosLlegada
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal.CreatRuta.puntosSalida


class PolylinesPrincipal(private val mapa: Context, private val gmap: GoogleMap) {
    private var polylineOptions = PolylineOptions()
    private var dbAuxiliar = DatosASqliteLocal(mapa)


    //objeto con variables global
    object CreatRuta {
        var rutasCreadas = false

        //array de datos: distancia y numero de estacion mas cercana
        val masCortaInicio = IntArray(2)

        var puntosSalida = mutableListOf<LatLng>()
        var puntosLlegada = mutableListOf<LatLng>()
        var puntosCalculada = mutableListOf<LatLng>()
        var puntosCaminata = mutableListOf<LatLng>()
        lateinit var polySalida: Polyline
        lateinit var polyLlegada: Polyline
        lateinit var polyCalculada: Polyline
        lateinit var polyCaminata: Polyline

        //declarar los marcadores posibles para poder trabajarlos mejor
        var marcador1: Marker? = null
        var marcador2: Marker? = null
        var estamarcado1: Boolean? = null
        var estamarcado2: Boolean? = null

    }

    fun crearRuta(idruta: Int) {
        limpiarPolylines()
        //limpiar cache o polylines hechas anteriormente cuando no se este buscando entre todas las rutas

        //RUTA - PRIMERA PARTE-----------------------------------------------------------------------

        polylineOptions.width(9f).color(
            ContextCompat.getColor(
                mapa,
                R.color.recorridoIda
            )
        ) //ancho de la linea y color

        val listaPrimeraParte = dbAuxiliar.obtenerCoordenadas(idruta, "coordenadas1")
        puntosSalida = listaPrimeraParte.subList(
            0,
            listaPrimeraParte.size
        ).toMutableList() //tomutablelist para compatilidad
        val listaSegundaParte = dbAuxiliar.obtenerCoordenadas(idruta, "coordenadas2")

        if (listaPrimeraParte.size < 2 && listaSegundaParte.size < 2) { //comprobar si hay datos, posible causa para cumplirse es que no hay conexion interenet
            //cambiar la version de informacion a 0 para que en splash de haga la descarga correspondiente ----
            val db = DatosASqliteLocal(mapa)
            db.insertarVersionDatos(0)
            //---------------------------------
            // reiniciar app en pantalla splash para obtener informacion de firebase a local
            UtilidadesMenores().reiniciarApp(
                mapa,
                Splash::class.java
            ) //reinicia la app a la primera pantalla
            UtilidadesMenores().crearToast(mapa, "Se Necesita Conexión a Internet")
        } else {

            if (idruta != 0) { //acciones si se va a usar el mapa para calcular ruta
                polySalida = gmap.addPolyline(polylineOptions) //crear polyline salida
                polySalida.points = puntosSalida //darle las coordenadas que componen la polyline
                polySalida.startCap = RoundCap() //redondear extremo inicial polyline
                polySalida.endCap = RoundCap() //redondear extremo final polyline
                polySalida.jointType = JointType.ROUND

                var contador = 0
                val puntosArrowsSalida = polySalida.points
                if (puntosArrowsSalida.isNotEmpty()) {
                    // Recorre los puntos para agregar los markers con direccion (flechas)
                    for (i in 0 until puntosArrowsSalida.size - 1) {
                        if (contador == 6) {
                            val start = puntosArrowsSalida[i]
                            val end = puntosArrowsSalida[i + 1]

                            // Calcula el ángulo de dirección entre el punto actual y el siguiente
                            val bearing = calculatrDireccionFlechas(start, end)

                            // Crea el marker con la rotación calculada
                            gmap.addMarker(
                                MarkerOptions()
                                    .position(start)
                                    .rotation(bearing)
                                    .anchor(0.5f, 0.5f)
                                    .flat(true) //para que no rote si se mueve o gira el mapa
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_poli_rise))
                            )
                            contador = 0
                        }
                        contador++
                    }
                }
            }


            //RUTA - SEGUNDA PARTE-------------------------------------------------------------------------------

            polylineOptions.width(9f).color(
                ContextCompat.getColor(
                    mapa,
                    R.color.recorridoVuelta
                )
            ) //ancho de la linea y color

            puntosLlegada = listaSegundaParte.subList(0, listaSegundaParte.size)
                .toMutableList() //compatibilidad

            if (idruta != 0) {
                polyLlegada = gmap.addPolyline(polylineOptions) //crear polyline salida
                polyLlegada.points = puntosLlegada//darle las coordenadas que componen la polyline
                polyLlegada.startCap = RoundCap() //redondear extremo inicial polyline
                polyLlegada.endCap = RoundCap() //redondear extremo final polyline
                polyLlegada.jointType = JointType.ROUND

                //agregar icono parqueadero
                agregarMarcador(
                    listaSegundaParte[listaSegundaParte.size - 1],
                    R.drawable.ic_parqueadero,
                    "Parqueadero de la ruta $idruta"
                )

                var contador = 0
                val puntosArrowsLlegada = polyLlegada.points
                if (puntosArrowsLlegada.isNotEmpty()) {
                    // Recorre los puntos para agregar los markers
                    for (i in 0 until puntosArrowsLlegada.size - 1) {

                        if (contador == 6) {
                            val start = puntosArrowsLlegada[i]
                            val end = puntosArrowsLlegada[i + 1]

                            // Calcula el ángulo de dirección entre el punto actual y el siguiente
                            val bearing = calculatrDireccionFlechas(start, end)

                            // Crea el marker con la rotación calculada
                            gmap.addMarker(
                                MarkerOptions()
                                    .position(start)
                                    .rotation(bearing)
                                    .anchor(0.5f, 0.5f)
                                    .flat(true)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_poli_fall))
                            )

                            contador = 0
                        }

                        contador++

                    }
                }
            }
        }
    }

    fun rutaMasCerca(ubicacionUsuario: LatLng, sentido: String) {

        if (polylineOptions.isVisible && puntosLlegada.isNotEmpty()) {

            masCortaInicio[0] = -1 // inicializa el índice de la estación más cercana en -1
            masCortaInicio[1] = Int.MAX_VALUE // inicializa la distancia con un valor alto

            if (puntosCalculada.size > 5) {
                //remover la polylin que ya haya sido creada antes (calculada)
                polyCalculada.remove()
                puntosCalculada.clear()
            }

            //salvar los puntos de las rutas para no perderlos cuando
            //se borren las rutas para ser reempazadas por la calculada
            val puntos1 = mutableListOf<LatLng>()
            puntos1.addAll(puntosSalida)
            val puntos2 = mutableListOf<LatLng>()
            puntos2.addAll(puntosLlegada)

            //preparar ubicacion inicial para comparar distancia
            val ubiInicial = Location("Ubicacion Inicial")
            ubiInicial.longitude = ubicacionUsuario.longitude
            ubiInicial.latitude = ubicacionUsuario.latitude

            //preparar ubicacion de estaciones cercanas para comparar distancia
            val ubiEstacion1 = Location("punto1-2")

            //compara las distancias entre el usuario y cada estacion
            if (sentido == "salida") {
                Distancia().compararDistanciasInicio(puntos1, ubiInicial, ubiEstacion1)

                //se elimina el marcador del otro sentido si lo hay
                estamarcado2 = if (estamarcado2 == true) {
                    marcador2?.remove()
                    false
                } else {
                    false
                }
            } else if (sentido == "llegada") {
                Distancia().compararDistanciasInicio(puntos2, ubiInicial, ubiEstacion1)

                //se elimina el marcador del otro sentido si lo hay
                estamarcado1 = if (estamarcado1 == true) {
                    marcador1?.remove()
                    false
                } else {
                    false
                }
            }
            //-------------------------------------------------------------
            //se hace el nuevo trazo
            val estacionCerca = masCortaInicio[0]
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
                //quitar arrows sobre polilineas del mapa (quita all)
                gmap.clear()

                //se coloca un marcador en la estacion cercana
                marcador1 = agregarMarcador(
                    puntos1[masCortaInicio[0]],
                    R.drawable.ic_estacion,
                    "Estación más cercana subiendo"
                )
                estamarcado1 = true
                //mueve la camara al marcador de estacion cercana
                gmap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        puntos1[masCortaInicio[0]],
                        17f
                    ), 3000, null
                )

                //se calcula y traza una nueva polylinea
                val totalPuntos = puntos1 + puntos2
                recorte = totalPuntos.subList(estacionCerca, totalPuntos.size).toMutableList()
                puntosCalculada.addAll(recorte)
                polySalida.remove()
                polyLlegada.remove()

                polyCalculada = gmap.addPolyline(polylineOptionsC)
                polyCalculada.points = puntosCalculada
                polyCalculada.jointType = JointType.ROUND
                polyCalculada.endCap = RoundCap()
                polyCalculada.startCap = RoundCap()

                //Trazar caminata
                if (puntosCaminata.size > 1) {
                    polyCaminata.remove()
                    puntosCaminata.clear()
                }
                puntosCaminata.add(ubicacionUsuario)
                puntosCaminata.add(puntosCalculada[0])
                val polylineOptionsCaminar = PolylineOptions()
                polylineOptionsCaminar.color(
                    ContextCompat.getColor(
                        mapa,
                        R.color.distancia_caminar
                    )
                )
                polylineOptionsCaminar.pattern(listOf(Dash(20f), Gap(10f)))
                polylineOptionsCaminar.addAll(puntosCaminata)
                polyCaminata = gmap.addPolyline(polylineOptionsCaminar)


            } else {
                //quitar arrows sobre polilineas del mapa (quita all)
                gmap.clear()

                //se coloca un marcador en la estacion cercana
                marcador2 = agregarMarcador(
                    puntos2[masCortaInicio[0]],
                    R.drawable.ic_estacion,
                    "Estación más cercana bajando"
                )
                estamarcado2 = true

                //mueve la camara al marcador de estacion cercana
                gmap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        puntos2[masCortaInicio[0]],
                        17f
                    ), 3000, null
                )

                //se calcula y traza una nueva polylinea
                val totalPuntos = puntos2 + puntos1
                recorte = totalPuntos.subList(estacionCerca, totalPuntos.size).toMutableList()
                puntosCalculada.addAll(recorte)
                polySalida.remove()
                polyLlegada.remove()
                polyCalculada = gmap.addPolyline(polylineOptionsC)
                polyCalculada.points = puntosCalculada
                polyCalculada.jointType = JointType.ROUND
                polyCalculada.endCap = RoundCap()
                polyCalculada.startCap = RoundCap()

                //Trazar caminata
                if (puntosCaminata.size > 1) {
                    polyCaminata.remove()
                    puntosCaminata.clear()
                }
                puntosCaminata.add(ubicacionUsuario)
                puntosCaminata.add(puntosCalculada[0])
                val polylineOptionsCaminar = PolylineOptions()
                polylineOptionsCaminar.color(
                    ContextCompat.getColor(
                        mapa,
                        R.color.distancia_caminar
                    )
                )
                polylineOptionsCaminar.pattern(listOf(Dash(20f), Gap(10f)))
                polylineOptionsCaminar.addAll(puntosCaminata)
                polyCaminata = gmap.addPolyline(polylineOptionsCaminar)
            }
        } else {
            UtilidadesMenores().crearToast(mapa, "Datos de recorridos no han sido recibidos")
        }

    }

    private fun limpiarPolylines() {
        puntosSalida.clear()
        puntosLlegada.clear()
        polylineOptions.points.clear()
    }

    private fun agregarMarcador(punto: LatLng, icono: Int, titulo: String): Marker? {
        val opcionesMarcador = MarkerOptions()
            .position(punto).icon(BitmapDescriptorFactory.fromResource(icono))
            .title(titulo)
        return gmap.addMarker(opcionesMarcador)
    }


    private fun colorRandom(): Int {
        val numero = (0..3).random()
        var color = 0
        when (numero) {
            0 -> color = R.color.rojo
            1 -> color = R.color.verde
            2 -> color = R.color.azul
            3 -> color = R.color.amarillo
        }
        return color
    }

    fun calculatrDireccionFlechas(from: LatLng, to: LatLng): Float {
        val lat1 = Math.toRadians(from.latitude)
        val lon1 = Math.toRadians(from.longitude)
        val lat2 = Math.toRadians(to.latitude)
        val lon2 = Math.toRadians(to.longitude)

        val dLon = lon2 - lon1

        val y = Math.sin(dLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)
        var bearing = Math.toDegrees(Math.atan2(y, x))
        bearing = (bearing + 360) % 360

        return bearing.toFloat()
    }

}