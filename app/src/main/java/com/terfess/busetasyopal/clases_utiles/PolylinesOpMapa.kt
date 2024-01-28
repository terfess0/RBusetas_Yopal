package com.terfess.busetasyopal.clases_utiles

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.terfess.busetasyopal.R

class PolylinesOpMapa(private val mapa: GoogleMap, private val contexto: Context) {
    private val opcionesRutaSalida = PolylineOptions().width(9.0f)
    private val opcionesRutallegada = PolylineOptions().width(9.0f)

    //valiables que simbolizaran a cada ruta
    private var polyRuta1 = mapa.addPolyline(opcionesRutaSalida)
    private var polyRuta1_1 = mapa.addPolyline(opcionesRutallegada)
    private var polyRuta2 = mapa.addPolyline(opcionesRutaSalida)
    private var polyRuta2_1 = mapa.addPolyline(opcionesRutallegada)
    private var polyRuta3 = mapa.addPolyline(opcionesRutaSalida)
    private var polyRuta3_1 = mapa.addPolyline(opcionesRutallegada)
    private var polyRuta4 = mapa.addPolyline(opcionesRutaSalida)
    private var polyRuta4_1 = mapa.addPolyline(opcionesRutallegada)
    private var polyRuta5 = mapa.addPolyline(opcionesRutaSalida)
    private var polyRuta5_1 = mapa.addPolyline(opcionesRutallegada)
    private var polyRuta6 = mapa.addPolyline(opcionesRutaSalida)
    private var polyRuta6_1 = mapa.addPolyline(opcionesRutallegada)
    private var polyRuta7 = mapa.addPolyline(opcionesRutaSalida)
    private var polyRuta7_1 = mapa.addPolyline(opcionesRutallegada)
    private var polyRuta8 = mapa.addPolyline(opcionesRutaSalida)
    private var polyRuta8_1 = mapa.addPolyline(opcionesRutallegada)
    private var polyRuta9 = mapa.addPolyline(opcionesRutaSalida)
    private var polyRuta9_1 = mapa.addPolyline(opcionesRutallegada)
    private var polyRuta10 = mapa.addPolyline(opcionesRutaSalida)
    private var polyRuta10_1 = mapa.addPolyline(opcionesRutallegada)
    private var polyRuta11 = mapa.addPolyline(opcionesRutaSalida)
    private var polyRuta11_1 = mapa.addPolyline(opcionesRutallegada)
    private var polyRuta12 = mapa.addPolyline(opcionesRutaSalida)
    private var polyRuta12_1 = mapa.addPolyline(opcionesRutallegada)
    private var polyRuta13 = mapa.addPolyline(opcionesRutaSalida)
    private var polyRuta13_1 = mapa.addPolyline(opcionesRutallegada)
    private var polyRuta14 = mapa.addPolyline(opcionesRutaSalida)
    private var polyRuta14_1 = mapa.addPolyline(opcionesRutallegada)

    fun trazarRuta(idRuta: Int) {
        val sqlDB = DatosASqliteLocal(contexto)
        var puntosSalida = mutableListOf<LatLng>()
        var puntosLlegada = mutableListOf<LatLng>()

        when (idRuta) {
            1 -> {
                puntosSalida = sqlDB.obtenerCoordenadas(idRuta, "coordenadas1").toMutableList()
                puntosLlegada = sqlDB.obtenerCoordenadas(idRuta, "coordenadas2").toMutableList()
                
                opcionesRutaSalida.color(ContextCompat.getColor(contexto, R.color.unoSalida))
                opcionesRutallegada.color(ContextCompat.getColor(contexto, R.color.unoLlegada))

                if (polyRuta1.points.size > 5) {
                    polyRuta1.remove()
                    polyRuta1_1.remove()
                    puntosSalida.clear()
                    puntosLlegada.clear()
                }else{
                    agregarMarcador(
                        puntosLlegada[puntosLlegada.size - 1],
                        R.drawable.ic_parqueadero,
                        "Parqueadero Ruta $idRuta"
                    )
                }

                polyRuta1 = mapa.addPolyline(opcionesRutaSalida)
                polyRuta1.points = puntosSalida

                polyRuta1_1 = mapa.addPolyline(opcionesRutallegada)
                polyRuta1_1.points = puntosLlegada

            }

            2 -> {
                puntosSalida = sqlDB.obtenerCoordenadas(idRuta, "coordenadas1").toMutableList()
                puntosLlegada = sqlDB.obtenerCoordenadas(idRuta, "coordenadas2").toMutableList()

                opcionesRutaSalida.color(ContextCompat.getColor(contexto, R.color.dosSalida))
                opcionesRutallegada.color(ContextCompat.getColor(contexto, R.color.dosLlegada))

                if (polyRuta2.points.size > 5) {
                    polyRuta2.remove()
                    polyRuta2_1.remove()
                    puntosSalida.clear()
                    puntosLlegada.clear()
                }else{
                    agregarMarcador(
                        puntosLlegada[puntosLlegada.size - 1],
                        R.drawable.ic_parqueadero,
                        "Parqueadero Ruta $idRuta"
                    )
                }

                polyRuta2 = mapa.addPolyline(opcionesRutaSalida)
                polyRuta2.points = puntosSalida

                polyRuta2_1 = mapa.addPolyline(opcionesRutallegada)
                polyRuta2_1.points = puntosLlegada

            }

            3 -> {
                puntosSalida = sqlDB.obtenerCoordenadas(idRuta, "coordenadas1").toMutableList()
                puntosLlegada = sqlDB.obtenerCoordenadas(idRuta, "coordenadas2").toMutableList()

                opcionesRutaSalida.color(ContextCompat.getColor(contexto, R.color.tresSalida))
                opcionesRutallegada.color(ContextCompat.getColor(contexto, R.color.tresLlegada))

                if (polyRuta3.points.size > 5) {
                    polyRuta3.remove()
                    polyRuta3_1.remove()
                    puntosSalida.clear()
                    puntosLlegada.clear()
                }else{
                    agregarMarcador(
                        puntosLlegada[puntosLlegada.size - 1],
                        R.drawable.ic_parqueadero,
                        "Parqueadero Ruta $idRuta"
                    )
                }

                polyRuta3 = mapa.addPolyline(opcionesRutaSalida)
                polyRuta3.points = puntosSalida

                polyRuta3_1 = mapa.addPolyline(opcionesRutallegada)
                polyRuta3_1.points = puntosLlegada

            }

            4 -> {
                puntosSalida = sqlDB.obtenerCoordenadas(idRuta, "coordenadas1").toMutableList()
                puntosLlegada = sqlDB.obtenerCoordenadas(idRuta, "coordenadas2").toMutableList()

                opcionesRutaSalida.color(ContextCompat.getColor(contexto, R.color.cuatroSalida))
                opcionesRutallegada.color(ContextCompat.getColor(contexto, R.color.cuatroLlegada))

                if (polyRuta4.points.size > 5) {
                    polyRuta4.remove()
                    polyRuta4_1.remove()
                    puntosSalida.clear()
                    puntosLlegada.clear()
                }else{
                    agregarMarcador(
                        puntosLlegada[puntosLlegada.size - 1],
                        R.drawable.ic_parqueadero,
                        "Parqueadero Ruta $idRuta"
                    )
                }

                polyRuta4 = mapa.addPolyline(opcionesRutaSalida)
                polyRuta4.points = puntosSalida

                polyRuta4_1 = mapa.addPolyline(opcionesRutallegada)
                polyRuta4_1.points = puntosLlegada

            }

            5 -> {
                puntosSalida = sqlDB.obtenerCoordenadas(idRuta, "coordenadas1").toMutableList()
                puntosLlegada = sqlDB.obtenerCoordenadas(idRuta, "coordenadas2").toMutableList()

                opcionesRutaSalida.color(ContextCompat.getColor(contexto, R.color.cincoSalida))
                opcionesRutallegada.color(ContextCompat.getColor(contexto, R.color.cincoLlegada))

                if (polyRuta5.points.size > 5) {
                    polyRuta5.remove()
                    polyRuta5_1.remove()
                    puntosSalida.clear()
                    puntosLlegada.clear()
                }else{
                    agregarMarcador(
                        puntosLlegada[puntosLlegada.size - 1],
                        R.drawable.ic_parqueadero,
                        "Parqueadero Ruta $idRuta"
                    )
                }

                polyRuta5 = mapa.addPolyline(opcionesRutaSalida)
                polyRuta5.points = puntosSalida

                polyRuta5_1 = mapa.addPolyline(opcionesRutallegada)
                polyRuta5_1.points = puntosLlegada

            }

            6 -> {
                puntosSalida = sqlDB.obtenerCoordenadas(idRuta, "coordenadas1").toMutableList()
                puntosLlegada = sqlDB.obtenerCoordenadas(idRuta, "coordenadas2").toMutableList()

                opcionesRutaSalida.color(ContextCompat.getColor(contexto, R.color.seisSalida))
                opcionesRutallegada.color(ContextCompat.getColor(contexto, R.color.seisLlegada))

                if (polyRuta6.points.size > 5) {
                    polyRuta6.remove()
                    polyRuta6_1.remove()
                    puntosSalida.clear()
                    puntosLlegada.clear()
                }else{
                    agregarMarcador(
                        puntosLlegada[puntosLlegada.size - 1],
                        R.drawable.ic_parqueadero,
                        "Parqueadero Ruta $idRuta"
                    )
                }

                polyRuta6 = mapa.addPolyline(opcionesRutaSalida)
                polyRuta6.points = puntosSalida

                polyRuta6_1 = mapa.addPolyline(opcionesRutallegada)
                polyRuta6_1.points = puntosLlegada

            }

            7 -> {
                puntosSalida = sqlDB.obtenerCoordenadas(idRuta, "coordenadas1").toMutableList()
                puntosLlegada = sqlDB.obtenerCoordenadas(idRuta, "coordenadas2").toMutableList()

                opcionesRutaSalida.color(ContextCompat.getColor(contexto, R.color.sieteSalida))
                opcionesRutallegada.color(ContextCompat.getColor(contexto, R.color.sieteLlegada))

                if (polyRuta7.points.size > 5) {
                    polyRuta7.remove()
                    polyRuta7_1.remove()
                    puntosSalida.clear()
                    puntosLlegada.clear()
                }else{
                    agregarMarcador(
                        puntosLlegada[puntosLlegada.size - 1],
                        R.drawable.ic_parqueadero,
                        "Parqueadero Ruta $idRuta"
                    )
                }

                polyRuta7 = mapa.addPolyline(opcionesRutaSalida)
                polyRuta7.points = puntosSalida

                polyRuta7_1 = mapa.addPolyline(opcionesRutallegada)
                polyRuta7_1.points = puntosLlegada

            }

            8 -> {
                puntosSalida = sqlDB.obtenerCoordenadas(idRuta, "coordenadas1").toMutableList()
                puntosLlegada = sqlDB.obtenerCoordenadas(idRuta, "coordenadas2").toMutableList()

                opcionesRutaSalida.color(ContextCompat.getColor(contexto, R.color.ochoSalida))
                opcionesRutallegada.color(ContextCompat.getColor(contexto, R.color.ochoLlegada))

                if (polyRuta8.points.size > 5) {
                    polyRuta8.remove()
                    polyRuta8_1.remove()
                    puntosSalida.clear()
                    puntosLlegada.clear()
                }else{
                    agregarMarcador(
                        puntosLlegada[puntosLlegada.size - 1],
                        R.drawable.ic_parqueadero,
                        "Parqueadero Ruta $idRuta"
                    )
                }

                polyRuta8 = mapa.addPolyline(opcionesRutaSalida)
                polyRuta8.points = puntosSalida

                polyRuta8_1 = mapa.addPolyline(opcionesRutallegada)
                polyRuta8_1.points = puntosLlegada

            }

            9 -> {
                puntosSalida = sqlDB.obtenerCoordenadas(idRuta, "coordenadas1").toMutableList()
                puntosLlegada = sqlDB.obtenerCoordenadas(idRuta, "coordenadas2").toMutableList()

                opcionesRutaSalida.color(ContextCompat.getColor(contexto, R.color.nueveSalida))
                opcionesRutallegada.color(ContextCompat.getColor(contexto, R.color.nueveLlegada))

                if (polyRuta9.points.size > 5) {
                    polyRuta9.remove()
                    polyRuta9_1.remove()
                    puntosSalida.clear()
                    puntosLlegada.clear()
                }else{
                    agregarMarcador(
                        puntosLlegada[puntosLlegada.size - 1],
                        R.drawable.ic_parqueadero,
                        "Parqueadero Ruta $idRuta"
                    )
                }

                polyRuta9 = mapa.addPolyline(opcionesRutaSalida)
                polyRuta9.points = puntosSalida

                polyRuta9_1 = mapa.addPolyline(opcionesRutallegada)
                polyRuta9_1.points = puntosLlegada

            }

            10 -> {

                puntosSalida = sqlDB.obtenerCoordenadas(idRuta, "coordenadas1").toMutableList()
                puntosLlegada = sqlDB.obtenerCoordenadas(idRuta, "coordenadas2").toMutableList()

                opcionesRutaSalida.color(ContextCompat.getColor(contexto, R.color.diezSalida))
                opcionesRutallegada.color(ContextCompat.getColor(contexto, R.color.diezLlegada))

                if (polyRuta10.points.size > 5) {
                    polyRuta10.remove()
                    polyRuta10_1.remove()
                    puntosSalida.clear()
                    puntosLlegada.clear()
                }else{
                    agregarMarcador(
                        puntosLlegada[puntosLlegada.size - 1],
                        R.drawable.ic_parqueadero,
                        "Parqueadero Ruta $idRuta"
                    )
                }


                polyRuta10 = mapa.addPolyline(opcionesRutaSalida)
                polyRuta10.points = puntosSalida

                polyRuta10_1 = mapa.addPolyline(opcionesRutallegada)
                polyRuta10_1.points = puntosLlegada

            }

            11 -> {
                puntosSalida = sqlDB.obtenerCoordenadas(idRuta, "coordenadas1").toMutableList()
                puntosLlegada = sqlDB.obtenerCoordenadas(idRuta, "coordenadas2").toMutableList()

                opcionesRutaSalida.color(ContextCompat.getColor(contexto, R.color.onceSalida))
                opcionesRutallegada.color(ContextCompat.getColor(contexto, R.color.onceLlegada))

                if (polyRuta11.points.size > 5) {
                    polyRuta11.remove()
                    polyRuta11_1.remove()
                    puntosSalida.clear()
                    puntosLlegada.clear()
                }else{
                    agregarMarcador(
                        puntosLlegada[puntosLlegada.size - 1],
                        R.drawable.ic_parqueadero,
                        "Parqueadero Ruta $idRuta"
                    )
                }

                polyRuta11 = mapa.addPolyline(opcionesRutaSalida)
                polyRuta11.points = puntosSalida

                polyRuta11_1 = mapa.addPolyline(opcionesRutallegada)
                polyRuta11_1.points = puntosLlegada

            }

            12 -> {
                puntosSalida = sqlDB.obtenerCoordenadas(idRuta, "coordenadas1").toMutableList()
                puntosLlegada = sqlDB.obtenerCoordenadas(idRuta, "coordenadas2").toMutableList()

                opcionesRutaSalida.color(ContextCompat.getColor(contexto, R.color.doceSalida))
                opcionesRutallegada.color(ContextCompat.getColor(contexto, R.color.doceLlegada))

                if (polyRuta12.points.size > 5) {
                    polyRuta12.remove()
                    polyRuta12_1.remove()
                    puntosSalida.clear()
                    puntosLlegada.clear()
                }else{
                    agregarMarcador(
                        puntosLlegada[puntosLlegada.size - 1],
                        R.drawable.ic_parqueadero,
                        "Parqueadero Ruta $idRuta"
                    )
                }

                polyRuta12 = mapa.addPolyline(opcionesRutaSalida)
                polyRuta12.points = puntosSalida

                polyRuta12_1 = mapa.addPolyline(opcionesRutallegada)
                polyRuta12_1.points = puntosLlegada

            }

            13 -> {
                puntosSalida = sqlDB.obtenerCoordenadas(idRuta, "coordenadas1").toMutableList()
                puntosLlegada = sqlDB.obtenerCoordenadas(idRuta, "coordenadas2").toMutableList()

                opcionesRutaSalida.color(ContextCompat.getColor(contexto, R.color.treceSalida))
                opcionesRutallegada.color(ContextCompat.getColor(contexto, R.color.treceLlegada))

                if (polyRuta13.points.size > 5) {
                    polyRuta13.remove()
                    polyRuta13_1.remove()
                    puntosSalida.clear()
                    puntosLlegada.clear()
                }else{
                    agregarMarcador(
                        puntosLlegada[puntosLlegada.size - 1],
                        R.drawable.ic_parqueadero,
                        "Parqueadero Ruta $idRuta"
                    )
                }

                polyRuta13 = mapa.addPolyline(opcionesRutaSalida)
                polyRuta13.points = puntosSalida

                polyRuta13_1 = mapa.addPolyline(opcionesRutallegada)
                polyRuta13_1.points = puntosLlegada

            }

            14 -> {
                puntosSalida = sqlDB.obtenerCoordenadas(idRuta, "coordenadas1").toMutableList()
                puntosLlegada = sqlDB.obtenerCoordenadas(idRuta, "coordenadas2").toMutableList()

                opcionesRutaSalida.color(ContextCompat.getColor(contexto, R.color.catorceSalida))
                opcionesRutallegada.color(ContextCompat.getColor(contexto, R.color.catorceLlegada))

                if (polyRuta14.points.size > 5) {
                    polyRuta14.remove()
                    polyRuta14_1.remove()
                    puntosSalida.clear()
                    puntosLlegada.clear()
                }else{
                    agregarMarcador(
                        puntosLlegada[puntosLlegada.size - 1],
                        R.drawable.ic_parqueadero,
                        "Parqueadero Ruta $idRuta"
                    )
                }

                polyRuta14 = mapa.addPolyline(opcionesRutaSalida)
                polyRuta14.points = puntosSalida

                polyRuta14_1 = mapa.addPolyline(opcionesRutallegada)
                polyRuta14_1.points = puntosLlegada

            }
        }

    }

    private fun agregarMarcador(punto: LatLng, icono: Int, titulo:String): Marker? {
        val opcionesMarcador = MarkerOptions()
            .position(punto).icon(BitmapDescriptorFactory.fromResource(icono))
            .title(titulo)
        return mapa.addMarker(opcionesMarcador)
    }

}