package com.terfess.busetasyopal.clases_utiles

import android.content.Context
import android.widget.Toast
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.clases_utiles.RutaBasic.CreatRuta.estamarcado1
import com.terfess.busetasyopal.clases_utiles.RutaBasic.CreatRuta.estamarcado2
import com.terfess.busetasyopal.clases_utiles.RutaBasic.CreatRuta.marcador1
import com.terfess.busetasyopal.clases_utiles.RutaBasic.CreatRuta.marcador2
import com.terfess.busetasyopal.clases_utiles.RutaBasic.CreatRuta.masCortaInicio
import com.terfess.busetasyopal.clases_utiles.RutaBasic.CreatRuta.polyCalculada
import com.terfess.busetasyopal.clases_utiles.RutaBasic.CreatRuta.polyLlegada
import com.terfess.busetasyopal.clases_utiles.RutaBasic.CreatRuta.polySalida
import com.terfess.busetasyopal.clases_utiles.RutaBasic.CreatRuta.puntosCalculada
import com.terfess.busetasyopal.clases_utiles.RutaBasic.CreatRuta.puntosLlegada
import com.terfess.busetasyopal.clases_utiles.RutaBasic.CreatRuta.puntosSalida


class RutaBasic(private val mapa: Context, private val gmap: GoogleMap) {
    private var polylineOptions = PolylineOptions()
    private var dbAuxiliar = DatosASqliteLocal(mapa)
    private val masCortaDestino = IntArray(2) //distancia y numero de estacion mas cercana destino


    //objeto con variables global
    object CreatRuta {
        var rutasCreadas = false

        //array de datos: distancia y numero de estacion mas cercana
        val masCortaInicio = IntArray(2)
        var puntosSalida = mutableListOf<LatLng>()
        var puntosLlegada = mutableListOf<LatLng>()
        var puntosCalculada = mutableListOf<LatLng>()
        lateinit var polySalida: Polyline
        lateinit var polyLlegada: Polyline
        lateinit var polyCalculada: Polyline

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
        if (idruta != 1) {
            polySalida = gmap.addPolyline(polylineOptions) //crear polyline salida
            polySalida.points = puntosSalida //darle las coordenadas que componen la polyline
            polySalida.startCap = RoundCap() //redondear extremo inicial polyline
            polySalida.endCap = RoundCap() //redondear extremo final polyline
            polySalida.jointType = JointType.ROUND
        }


        //RUTA - SEGUNDA PARTE-------------------------------------------------------------------------------

        polylineOptions.width(9f).color(
            ContextCompat.getColor(
                mapa,
                R.color.recorridoVuelta
            )
        ) //ancho de la linea y color


        val listaSegundaParte = dbAuxiliar.obtenerCoordenadas(idruta, "coordenadas2")
        puntosLlegada = listaSegundaParte.subList(0, listaSegundaParte.size).toMutableList() //compatibilidad
        if (idruta != 1) {
            polyLlegada = gmap.addPolyline(polylineOptions) //crear polyline salida
            polyLlegada.points = puntosLlegada//darle las coordenadas que componen la polyline
            polyLlegada.startCap = RoundCap() //redondear extremo inicial polyline
            polyLlegada.endCap = RoundCap() //redondear extremo final polyline
            polyLlegada.jointType = JointType.ROUND
            agregarMarcador(
                listaSegundaParte[listaSegundaParte.size-1],
                R.drawable.ic_parqueadero
            )
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
                compararDistancias(puntos1, ubiInicial, ubiEstacion1, true)

                //se elimina el marcador del otro sentido si lo hay
                estamarcado2 = if (estamarcado2 == true) {
                    marcador2?.remove()
                    false
                } else {
                    false
                }
            } else if (sentido == "llegada") {
                compararDistancias(puntos2, ubiInicial, ubiEstacion1, true)

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

                //se coloca un marcador en la estacion cercana
                marcador1 = agregarMarcador(
                    puntos1[masCortaInicio[0]],
                    R.drawable.ic_estacion
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

            } else {

                //se coloca un marcador en la estacion cercana
                marcador2 = agregarMarcador(
                    puntos2[masCortaInicio[0]],
                    R.drawable.ic_estacion
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
            }

            /*if (puntoCorte1 > puntoCorte2) {
                val parte1 = puntosTotales.subList(0, puntoCorte2+1)
                val parte2 = puntosTotales.subList(puntoCorte1, puntosTotales.size)
                val puntis = parte2 + parte1 // se agrega al reves para evitar uniones
                polylineOptionsC.addAll(puntis)
                val c = gmap.addPolyline(polylineOptionsC)
                c.jointType = JointType.ROUND
                c.endCap = RoundCap()
                c.startCap = RoundCap()

            } else {
                val parte1 = puntosTotales.subList(0, puntoCorte1+1)
                val parte2 = puntosTotales.subList(puntoCorte2, puntosTotales.size)
                polylineOptionsC.addAll(parte2 + parte1) // se agrega al reves para evitar uniones
                val d = gmap.addPolyline(polylineOptionsC)
                d.jointType = JointType.ROUND
                d.endCap = RoundCap()
                d.startCap = RoundCap()
            }*/
        } else {
            crearToast("Datos de recorridos no han sido recibidos")
        }

    }


    private fun compararDistancias(
        puntos: List<LatLng>,
        ubiInicio: Location,
        ubiDestino: Location,
        esInicio: Boolean
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

            //hay array para inicio y uno para destino, por eso la comprobacion
            if (esInicio) {
                if (distancia < masCortaInicio[1]) {
                    masCortaInicio[0] = f
                    masCortaInicio[1] = distancia
                }
            }
            if (!esInicio) {
                if (distancia < masCortaDestino[1]) {
                    masCortaDestino[0] = f
                    masCortaDestino[1] = distancia
                }
            }
            puntoMasCerca = LatLng(estacion.latitude, estacion.longitude)
        }
        return puntoMasCerca
    }

    /*private fun crearAlerta(mensaje: String, op1: String, op2: String): Boolean {
        val builder = AlertDialog.Builder(this.mapa)
        var respuesta = false
        builder.setMessage(mensaje)
            .setPositiveButton(op1) { _, _ ->
                respuesta = true
            }
        builder.setNegativeButton(op2) { _, _ ->
            respuesta = true
        }
        val dialog = builder.create()
        dialog.show()
        return respuesta
    }*/

    private fun limpiarPolylines() {
        puntosSalida.clear()
        puntosLlegada.clear()
        polylineOptions.points.clear()
    }

    private fun agregarMarcador(punto: LatLng, icono: Int): Marker? {
        val opcionesMarcador = MarkerOptions()
            .position(punto).icon(BitmapDescriptorFactory.fromResource(icono))
            .title("Punto mas cercano")
        return gmap.addMarker(opcionesMarcador)
    }

    private fun crearToast(mensaje: String) {
        Toast.makeText(
            mapa,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun colorRandom(): Int {
        val numero = (0..2).random()
        var color = 0
        when (numero) {
            0 -> color = R.color.rojo
            1 -> color = R.color.verde
            2 -> color = R.color.azul
        }
        return color
    }
}