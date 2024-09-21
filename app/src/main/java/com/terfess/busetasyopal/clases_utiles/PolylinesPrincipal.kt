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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.actividades.Splash
import com.terfess.busetasyopal.actividades.mapa.functions.MapFunctionOptions
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal.CreatRuta.polyLlegada
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal.CreatRuta.polySalida
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal.CreatRuta.puntosLlegada
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal.CreatRuta.puntosSalida
import com.terfess.busetasyopal.enums.RoomTypePath
import com.terfess.busetasyopal.room.AppDatabase
import com.terfess.busetasyopal.room.model.Version
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PolylinesPrincipal(private val mapa: Context, private val gmap: GoogleMap) {
    private var polylineOptions = PolylineOptions()
    private var dbAuxiliar = AppDatabase.getDatabase(mapa)
    private var mapFunctionsInstance = MapFunctionOptions()


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

        var listaPrimeraParte: List<LatLng>
        var listaSegundaParte: List<LatLng>

        CoroutineScope(Dispatchers.Default).launch {
            val obj = dbAuxiliar.coordinateDao()
                .getCoordRoutePath(
                    idruta,
                    RoomTypePath.DEPARTURE.toString()
                )
            val obj2 = dbAuxiliar.coordinateDao()
                .getCoordRoutePath(
                    idruta,
                    RoomTypePath.RETURN.toString()
                )


            listaPrimeraParte = UtilidadesMenores()
                .extractCoordToLatLng(
                    obj,
                    RoomTypePath.DEPARTURE.toString(),
                    idruta
                )

            puntosSalida = listaPrimeraParte.subList(
                0,
                listaPrimeraParte.size
            ).toMutableList() //tomutablelist para compatilidad


            listaSegundaParte = UtilidadesMenores()
                .extractCoordToLatLng(
                    obj2,
                    RoomTypePath.RETURN.toString(),
                    idruta
                )

            if (listaPrimeraParte.size < 2 && listaSegundaParte.size < 2) {
                //comprobar si hay datos, posible causa para cumplirse es que no hay conexion interenet
                // Cambiar la version de informacion a 0 para que en splash de haga la descarga correspondiente ----

                CoroutineScope(Dispatchers.Default).launch {
                    dbAuxiliar.versionDao().insertVersion(
                        Version(
                            num_version = 0
                        )
                    )
                }

                //...
                // reiniciar app en pantalla splash para obtener informacion de firebase a local
                UtilidadesMenores().reiniciarApp(
                    mapa,
                    Splash::class.java
                ) //reinicia la app a la primera pantalla

                withContext(Dispatchers.Main) {
                    UtilidadesMenores().crearToast(mapa, "Se Necesita Conexión a Internet")
                }

            } else {

                if (idruta != 0) { //acciones si se va a usar el mapa para calcular ruta
                    withContext(Dispatchers.Main) {
                        polySalida = gmap.addPolyline(polylineOptions) //crear polyline salida
                        polySalida.points =
                            puntosSalida //darle las coordenadas que componen la polyline
                        polySalida.startCap = RoundCap() //redondear extremo inicial polyline
                        polySalida.endCap = RoundCap() //redondear extremo final polyline
                        polySalida.jointType = JointType.ROUND

                        var contador = 0
                        val puntosArrowsSalida = polySalida.points
                        if (puntosArrowsSalida.isNotEmpty()) {
                            // Recorre los puntos para agregar los markers con direccion (flechas)
                            for (i in 0 until puntosArrowsSalida.size - 1) {
                                if (contador == 10) {
                                    val start = puntosArrowsSalida[i]
                                    val end = puntosArrowsSalida[i + 1]

                                    // Calcula el ángulo de dirección entre el punto actual y el siguiente
                                    val bearing = DirectionFlechas(start, end)

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
                }

                //RUTA - SEGUNDA PARTE-------------------------------------------------------------------------------

                withContext(Dispatchers.Main){
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
                        polyLlegada.points =
                            puntosLlegada//darle las coordenadas que componen la polyline
                        polyLlegada.startCap = RoundCap() //redondear extremo inicial polyline
                        polyLlegada.endCap = RoundCap() //redondear extremo final polyline
                        polyLlegada.jointType = JointType.ROUND

                        //agregar icono parqueadero
                        val markerOpts = mapFunctionsInstance.getOptionsMarker(
                            listaSegundaParte[listaSegundaParte.size - 1],
                            R.drawable.ic_parqueadero,
                            "Parqueadero de la ruta $idruta"
                        )
                        gmap.addMarker(markerOpts)

                        var contador = 0
                        val puntosArrowsLlegada = polyLlegada.points
                        if (puntosArrowsLlegada.isNotEmpty()) {
                            // Recorre los puntos para agregar los markers
                            for (i in 0 until puntosArrowsLlegada.size - 1) {

                                if (contador == 10) {
                                    val start = puntosArrowsLlegada[i]
                                    val end = puntosArrowsLlegada[i + 1]

                                    // Calcula el ángulo de dirección entre el punto actual y el siguiente
                                    val bearing = DirectionFlechas(start, end)

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
        }
    }

    private fun limpiarPolylines() {
        puntosSalida.clear()
        puntosLlegada.clear()
        polylineOptions.points.clear()
    }

    private fun DirectionFlechas(from: LatLng, to: LatLng): Float {
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