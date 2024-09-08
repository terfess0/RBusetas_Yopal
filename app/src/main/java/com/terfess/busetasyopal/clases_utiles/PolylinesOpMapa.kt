package com.terfess.busetasyopal.clases_utiles

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.enums.RoomTypePath
import com.terfess.busetasyopal.room.AppDatabase

class PolylinesOpMapa(private val mapa: GoogleMap, private val contexto: Context) {
    private val opcionesRutaSalida = PolylineOptions().width(9.0f)
    private val opcionesRutaLlegada = PolylineOptions().width(9.0f)

    // Listas dinámicas para las polylines
    private val polyRutasSalida = mutableListOf<Polyline>()
    private val polyRutasLlegada = mutableListOf<Polyline>()

    // Map para asociar cada ruta con sus colores
    private val rutaColoresSalida = mapOf(
        1 to R.color.unoSalida,
        2 to R.color.dosSalida,
        3 to R.color.tresSalida,
        // Agregar más colores según las rutas
    )

    private val rutaColoresLlegada = mapOf(
        1 to R.color.unoLlegada,
        2 to R.color.dosLlegada,
        3 to R.color.tresLlegada,
        // Agregar más colores según las rutas
    )

    init {
        // Inicializa las polylines para tantas rutas como sea necesario
        for (i in 1..14) {  // Por ejemplo, 14 rutas
            polyRutasSalida.add(mapa.addPolyline(opcionesRutaSalida))
            polyRutasLlegada.add(mapa.addPolyline(opcionesRutaLlegada))
        }
    }

    fun trazarRuta(idRuta: Int) {
        val sqlRoom = AppDatabase.getDatabase(contexto)
        val obj1 = sqlRoom.coordinateDao().getCoordRoutePath(
            idRuta,
            RoomTypePath.DEPARTURE.toString()
        ).toMutableList()

        val puntosSalida = UtilidadesMenores().extractCoordToLatLng(
            obj1,
            RoomTypePath.DEPARTURE.toString(),
            idRuta
        )

        val obj2 = sqlRoom.coordinateDao().getCoordRoutePath(
            idRuta,
            RoomTypePath.RETURN.toString()
        ).toMutableList()

        val puntosLlegada = UtilidadesMenores().extractCoordToLatLng(
            obj2,
            RoomTypePath.RETURN.toString(),
            idRuta
        )


        // Cambiar el color dinámicamente según la ruta
        opcionesRutaSalida.color(
            ContextCompat.getColor(
                contexto,
                rutaColoresSalida[idRuta] ?: R.color.defaultSalida
            )
        )
        opcionesRutaLlegada.color(
            ContextCompat.getColor(
                contexto,
                rutaColoresLlegada[idRuta] ?: R.color.defaultLlegada
            )
        )

        val polySalida = polyRutasSalida[idRuta - 1]
        val polyLlegada = polyRutasLlegada[idRuta - 1]

        // Elimina las rutas si ya tienen suficientes puntos
        if (polySalida.points.size > 5) {
            polySalida.remove()
            polyLlegada.remove()
        } else {
            agregarMarcador(
                puntosLlegada.last(),
                R.drawable.ic_parqueadero,
                "Parqueadero Ruta $idRuta"
            )
        }

        // Añade los puntos a las polylines
        polySalida.points = puntosSalida
        polyLlegada.points = puntosLlegada
    }


    // Función para agregar marcador
    private fun agregarMarcador(posicion: LatLng, iconoRes: Int, titulo: String): Marker? {
        val newMarker = mapa.addMarker(
            MarkerOptions()
                .position(posicion)
                .title(titulo)
                .icon(BitmapDescriptorFactory.fromResource(iconoRes))
        )

        return newMarker
    }
}