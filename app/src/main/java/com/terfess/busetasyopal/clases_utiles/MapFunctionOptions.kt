package com.terfess.busetasyopal.clases_utiles

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.enums.RoomTypePath
import com.terfess.busetasyopal.room.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapFunctionOptions {
    fun addParkingPoints(mapa: GoogleMap, context: Context) {

        //--------------PARKING LOTS------------------------------
        var listaRutas: List<Int>

        CoroutineScope(Dispatchers.IO).launch {

            val dbHelper = AppDatabase.getDatabase(context)

            listaRutas = dbHelper.routeDao().getAllIdsRoute()

            for (i in 0..listaRutas.size - 1) {
                val iterator = listaRutas[i]


                val datosSeleccionRuta = dbHelper.coordinateDao()
                    .getCoordRoutePath(iterator, RoomTypePath.RETURN.toString())

                val lat = datosSeleccionRuta[datosSeleccionRuta.size - 1].latitude
                val lng = datosSeleccionRuta[datosSeleccionRuta.size - 1].longitude

                withContext(Dispatchers.Main){
                    agregarMarcador(
                        mapa,
                        LatLng(lat, lng),
                        R.drawable.ic_parqueadero,
                        "Parqueadero Ruta $iterator"
                    )
                }
            }
        }
    }

    private fun agregarMarcador(
        mapInstance: GoogleMap,
        punto: LatLng,
        icono: Int,
        titulo: String
    ): Marker? {
        // add marker on mapInstance
        val opcionesMarcador = MarkerOptions()
            .position(punto).icon(BitmapDescriptorFactory.fromResource(icono))
            .title(titulo)

        return mapInstance.addMarker(opcionesMarcador)
    }
}