package com.terfess.rutasbusetasyopal

import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RutaBasic(private val mapa:Context, val gmap: GoogleMap) {
    var polylineOptions = PolylineOptions()
    lateinit var puntos: MutableList<LatLng>
    private val databaseRef = FirebaseDatabase.getInstance()

    fun crearRuta(path1parte:String, path2parte:String, idruta:Int) {
        //RUTA - PRIMERA PARTE
        databaseRef.getReference(path1parte).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                puntos = mutableListOf()
                var ubicacion: LatLng
                for (childSnapshot in dataSnapshot.children) {
                    polylineOptions.width(12f).color(
                        ContextCompat.getColor(
                            mapa,
                            R.color.recorridoIda
                        )
                    ) //ancho de la linea y color
                    val lat = childSnapshot.child("1").getValue(Double::class.java)
                    val lng = childSnapshot.child("0").getValue(Double::class.java)
                    //en caso de nulos por lat lng
                    val latValue = lat ?: 0.0
                    val lngValue = lng ?: 0.0
                    ubicacion = LatLng(latValue, lngValue)
                    puntos.add(ubicacion)
                }
                val polyline1 = gmap.addPolyline(polylineOptions)
                polyline1.points = puntos
                polyline1.startCap = RoundCap()
                polyline1.endCap = RoundCap()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores si los hay
                Toast.makeText(
                    mapa,
                    "Algo salio mal en la creacion de la ruta-1.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        //RUTA - SEGUNDA PARTE
        databaseRef.getReference(path2parte).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                puntos = mutableListOf()
                for (childSnapshot in dataSnapshot.children) {
                    polylineOptions.width(14f).color(ContextCompat.getColor(mapa, R.color.recorridoVuelta)) //ancho de la linea y color
                    val lat = childSnapshot.child("1").getValue(Double::class.java)
                    val lng = childSnapshot.child("0").getValue(Double::class.java)
                    //en caso de nulos por lat lng
                    val latValue = lat ?: 0.0
                    val lngValue = lng ?: 0.0
                    val ubicacion2 = LatLng(latValue, lngValue)
                    puntos.add(ubicacion2)
                }
                val polyline2 = gmap.addPolyline(polylineOptions)
                polyline2.points = puntos
                polyline2.startCap = RoundCap()
                polyline2.endCap = RoundCap()
                val medio = (puntos.size -1)
                val markerOptions = MarkerOptions().position(puntos[medio])
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.parqueadero_icon))
                    .title("Parqueadero Ruta $idruta")
                gmap.addMarker(markerOptions)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores si los hay
                Toast.makeText(
                    mapa,
                    "Algo salio mal en la creacion de la ruta-2.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}