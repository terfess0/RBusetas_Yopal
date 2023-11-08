package com.terfess.busetasyopal

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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.location.Location
import com.google.android.gms.maps.model.Polyline
import com.terfess.busetasyopal.RutaBasic.CreatRuta.puntosLlegada
import com.terfess.busetasyopal.RutaBasic.CreatRuta.puntosSalida

class RutaBasic(val mapa: Context, val gmap: GoogleMap) {
    var polylineOptions = PolylineOptions()
    private val databaseRef = FirebaseDatabase.getInstance()
    private lateinit var polySalida: Polyline
    private lateinit var polyLlegada: Polyline

    //objeto con variables global
    object CreatRuta {
        var rutasCreadas = false
        var puntosSalida = mutableListOf<LatLng>()
        var puntosLlegada  = mutableListOf<LatLng>()
    }

    fun crearRuta(path1parte: String, path2parte: String, idruta: Int) {
        //limpiar cache o polylines hechas anteriormente
        puntosSalida.clear()
        puntosLlegada.clear()
        polylineOptions.points.clear()
        //RUTA - PRIMERA PARTE
        databaseRef.getReference(path1parte).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var ubicacion: LatLng
                for (childSnapshot in dataSnapshot.children) {
                    if (idruta != 1){
                        polylineOptions.width(12f).color(
                            ContextCompat.getColor(
                                mapa,
                                R.color.recorridoIda
                            )
                        ) //ancho de la linea y color
                    }
                    val lat = childSnapshot.child("1").getValue(Double::class.java)
                    val lng = childSnapshot.child("0").getValue(Double::class.java)
                    //en caso de nulos por lat lng
                    val latValue = lat ?: 0.0
                    val lngValue = lng ?: 0.0
                    if (lat != 0.0) {
                        CreatRuta.rutasCreadas = true
                    }
                    ubicacion = LatLng(latValue, lngValue)
                    puntosSalida.add(ubicacion)
                }
                if (idruta != 1){
                    polySalida = gmap.addPolyline(polylineOptions) //crear polyline salida
                    polySalida.points = puntosSalida //darle las coordenadas que componen la polyline
                    polySalida.startCap = RoundCap() //redondear extremo inicial polyline
                    polySalida.endCap = RoundCap() //redondear extremo final polyline
                    polySalida.jointType = JointType.ROUND
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores si los hay
                Toast.makeText(
                    mapa,
                    "Algo salio mal en la creacion de salida ruta $idruta.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        //RUTA - SEGUNDA PARTE
        databaseRef.getReference(path2parte).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    if (idruta != 1){
                        polylineOptions.width(9f).color(
                            ContextCompat.getColor(
                                mapa,
                                R.color.recorridoVuelta
                            )
                        ) //ancho de la linea y color
                    }
                    val lat = childSnapshot.child("1").getValue(Double::class.java)
                    val lng = childSnapshot.child("0").getValue(Double::class.java)
                    //en caso de nulos por lat lng
                    val latValue = lat ?: 0.0
                    val lngValue = lng ?: 0.0
                    if (lat != 0.0) {
                        CreatRuta.rutasCreadas = true
                    }
                    val ubicacion2 = LatLng(latValue, lngValue)
                    puntosLlegada.add(ubicacion2)
                }
                if (idruta != 1){
                    polyLlegada = gmap.addPolyline(polylineOptions) //crear polyline
                    polyLlegada.points = puntosLlegada //dar los puntos de la polyline
                    polyLlegada.startCap = RoundCap() //redondear extremo inicial polyline
                    polyLlegada.endCap = RoundCap() //redondear extremo final polyline
                    polyLlegada.jointType = JointType.ROUND
                    val medio = (puntosLlegada.size - 1)
                    val markerOptions = MarkerOptions().position(puntosLlegada[medio])
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.parqueadero_icon))
                        .title("Parqueadero Ruta $idruta")
                    gmap.addMarker(markerOptions)//se usa title marcador para colocarle titulo al icono
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores si los hay
                Toast.makeText(
                    mapa,
                    "Algo salio mal en la creacion de la llegada ruta $idruta.",
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }

    fun rutaMasCerca(location: LatLng) {
        if (polylineOptions.isVisible && puntosLlegada.isNotEmpty()) {
            // Obtener 40 puntos equidistantes de la polySalida
            //val numEquidistanteSalida = puntosSalida.size / 80
            // Obtener 40 puntos equidistantes de la polyLlegada
            //val numEquidistanteLlegada = puntosLlegada.size / 80

            //seran las nuevas listas con los puntos con saltos de los equisdistantes
            val estacionesSalida = mutableListOf<LatLng>()
            val estacionesllegada = mutableListOf<LatLng>()

            var contador1 = 0
            var contadorPuntos1 = 0
            estacionesSalida.clear()
            for (i in 0 until puntosSalida.size) {
                //llenar la lista de estaciones
                //if (contador1 == numEquidistanteSalida && contadorPuntos1 < 80) {
                    estacionesSalida.add(puntosSalida[i])
                    //contador1 = 0
                    //contadorPuntos1 += 1
            }

            var contador2 = 0
            var contadorPuntos2 = 0
            estacionesllegada.clear()
            for (f in 0 until puntosLlegada.size) {
                //llenar la lista de estaciones
                //if (contador2 == numEquidistanteLlegada && contadorPuntos2 < 80) {
                    estacionesllegada.add(puntosLlegada[f])
            }

            //preparar ubicacion del usuario para comparar distancia
            val ubiUser = Location("punto1")
            ubiUser.latitude = location.latitude
            ubiUser.longitude = location.longitude

            //preparar ubicacion de estaciones salida para comparar distancia
            val ubiEstacion1 = Location("punto2-1")

            //array de datos: distancia y numero de estacion mas cercana
            val masCorta = IntArray(2)
            masCorta[0] =
                -1 // Inicializa el índice de la estación más cercana en -1 (indicando que aún no se ha encontrado ninguna)
            masCorta[1] = Int.MAX_VALUE // Inicializa la distancia con un valor alto

            //recuperar posicion de la estacion cercana para colocar marcador
            var puntoCercano = LatLng(0.0,0.0)

            //comparar distancias entre ubiUser y uniEstacion salida
            estacionesSalida.indices.forEach { i ->
                val estacion = estacionesSalida[i]
                ubiEstacion1.latitude = estacion.latitude
                ubiEstacion1.longitude = estacion.longitude

                val distancia = (ubiUser.distanceTo(ubiEstacion1)).toInt()

                if (distancia < masCorta[1]) {
                    puntoCercano = LatLng(estacion.latitude, estacion.longitude)
                    masCorta[0] = i
                    masCorta[1] = distancia
                }
            }

            //preparar ubicacion de estaciones llegada para comparar distancia
            val ubiEstacion2 = Location("punto2-2")

            //comparar distancias entre ubiUser y uniEstacion salida
            estacionesllegada.indices.forEach { i ->
                val estacion = estacionesllegada[i]
                ubiEstacion2.latitude = estacion.latitude
                ubiEstacion2.longitude = estacion.longitude

                val distancia = (ubiUser.distanceTo(ubiEstacion2)).toInt()

                if (distancia < masCorta[1]) {
                    puntoCercano = LatLng(estacion.latitude, estacion.longitude)
                    masCorta[0] = i
                    masCorta[1] = distancia
                }

            }
            //colocar marcador en la ubi de la estacion mas cercana
            val markerOptions = MarkerOptions().position(puntoCercano)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.estacion_cercana))
                .title("Ruta mas Cercana")
            gmap.addMarker(markerOptions)//se usa title marcador para colocarle titulo al icono

            Toast.makeText(
                mapa,
                "La mejor distancia es ${masCorta[1]}m de estación ${masCorta[0]}",
                Toast.LENGTH_LONG
            ).show()
        }else{
            Toast.makeText(
                mapa,
                "Datos de recorridos no han sido recibidos",
                Toast.LENGTH_LONG
            ).show()
        }

    }
}