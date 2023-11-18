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
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.Polyline
import com.terfess.busetasyopal.RutaBasic.CreatRuta.puntosLlegada
import com.terfess.busetasyopal.RutaBasic.CreatRuta.puntosSalida

class RutaBasic(val mapa: Context, val gmap: GoogleMap) {
    var polylineOptions = PolylineOptions()
    private val databaseRef = FirebaseDatabase.getInstance()
    private lateinit var polySalida: Polyline
    private lateinit var polyLlegada: Polyline
    private val masCortaInicio = IntArray(2) //array de datos: distancia y numero de estacion mas cercana
    private val masCortaDestino = IntArray(2) //distancia y numero de estacion mas cercana destino

    //objeto con variables global
    object CreatRuta {
        var rutasCreadas = false
        var puntosSalida = mutableListOf<LatLng>()
        var puntosLlegada = mutableListOf<LatLng>()
    }

    fun crearRuta(path1parte: String, path2parte: String, idruta: Int) {
        limpiarPolylines()
        //limpiar cache o polylines hechas anteriormente cuando no se este buscando entre todas las rutas

        //RUTA - PRIMERA PARTE
        databaseRef.getReference(path1parte).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var ubicacion: LatLng
                for (childSnapshot in dataSnapshot.children) {
                    if (idruta != 1) {
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
                if (idruta != 1) {
                    polySalida = gmap.addPolyline(polylineOptions) //crear polyline salida
                    polySalida.points =
                        puntosSalida //darle las coordenadas que componen la polyline
                    polySalida.startCap = RoundCap() //redondear extremo inicial polyline
                    polySalida.endCap = RoundCap() //redondear extremo final polyline
                    polySalida.jointType = JointType.ROUND
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores si los hay
                crearToast("Algo salio mal en la creacion de la llegada ruta $idruta.")
            }
        })
        //RUTA - SEGUNDA PARTE
        databaseRef.getReference(path2parte).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    if (idruta != 1) {
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
                if (idruta != 1) {
                    polyLlegada = gmap.addPolyline(polylineOptions) //crear polyline
                    polyLlegada.points = puntosLlegada //dar los puntos de la polyline
                    polyLlegada.startCap = RoundCap() //redondear extremo inicial polyline
                    polyLlegada.endCap = RoundCap() //redondear extremo final polyline
                    polyLlegada.jointType = JointType.ROUND
                    val medio = (puntosLlegada.size - 1)
                    //agregar marcador en parqueadero
                    agregarMarcador(
                        puntosLlegada[medio],
                        R.drawable.parqueadero_icon,
                        "Parqueadero Ruta $idruta"
                    )
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores si los hay
                crearToast("Algo salio mal en la creacion de la llegada ruta $idruta.")
            }

        })
    }

    fun rutaMasCerca(ubicacionUsuario: LatLng, ubicacionDestino: LatLng) {

        if (polylineOptions.isVisible && puntosLlegada.isNotEmpty()) {
            val puntos = puntosSalida + puntosLlegada
            val puntosTotales = mutableListOf<LatLng>()
            puntosTotales.addAll(puntos)
            masCortaInicio[0] =
                -1 // Inicializa el índice de la estación más cercana en -1 (indicando que aún no se ha encontrado ninguna)
            masCortaInicio[1] = Int.MAX_VALUE // Inicializa la distancia con un valor alto
            masCortaDestino[0] =
                -1 // Inicializa el índice de la estación más cercana en -1 (indicando que aún no se ha encontrado ninguna)
            masCortaDestino[1] = Int.MAX_VALUE // Inicializa la distancia con un valor alto

            //preparar ubicacion inicial para comparar distancia
            val ubiInicial = Location("Ubicacion Inicial")
            ubiInicial.longitude = ubicacionUsuario.longitude
            ubiInicial.latitude = ubicacionUsuario.latitude

            //preparar ubicacion de estaciones cercanas para comparar distancia
            val ubiEstacion1 = Location("punto1-2")
            val ubiEstacion2 = Location("punto2-2")

            //val sentido = crearAlerta("¿En que sentido te desplazaras?", "Subida", "Bajada")

            val ubicDestino = Location("Unicentro")
            ubicDestino.longitude = ubicacionDestino.longitude
            ubicDestino.latitude = ubicacionDestino.latitude

            compararDistancias(puntosTotales, ubiInicial, ubiEstacion1, true)
            compararDistancias(puntosTotales, ubicDestino, ubiEstacion2, false)

            //colocar marcador en la ubi de la estacion mas cercana
            agregarMarcador(puntosTotales[masCortaInicio[0]], R.drawable.estacion_cercana, "Punto más Cercano")
            agregarMarcador(puntosTotales[masCortaDestino[0]], R.drawable.estacion_cercana, "Punto más Cercano Prueba")
            agregarMarcador(ubicacionDestino, R.drawable.star_marker, "Punto Destino")
            //Toast con la distancia mas cercana en metros
            //crearToast("La mejor distancia es ${masCortaInicio[1]}m de estación ${masCortaInicio[0]}")
            //crearToast("La mejor distancia es ${masCortaDestino[1]}m de estación ${masCortaDestino[0]}")
            val puntoCorte1 = masCortaInicio[0]
            val puntoCorte2 = masCortaDestino[0]

            val cameraPosicion = CameraPosition.Builder()
                .target(LatLng(5.330211486448319, -72.39310208990806))
                .zoom(13.5f)
                .build()
            gmap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosicion))

            val polylineOptionsC = PolylineOptions()
            polylineOptionsC.points.clear()
            polylineOptionsC.width(6f).color(
                ContextCompat.getColor(
                    mapa,
                    R.color.RutaCalculada
                )
            )
            val pattern = listOf(
                Dash(20f), // Punto sólido
                Gap(5f) // Espacio de 10 unidades
            )
            polylineOptionsC.pattern(pattern)


            if (puntoCorte1 > puntoCorte2) {
                val parte1 = puntosTotales.subList(0, puntoCorte2+1)
                val parte2 = puntosTotales.subList(puntoCorte1, puntosTotales.size)
                val puntis = parte2 + parte1 // se agrega al reves para evitar uniones
                polylineOptionsC.addAll(puntis)
                val c = gmap.addPolyline(polylineOptionsC)
                c.jointType = JointType.ROUND
                c.endCap = RoundCap()
                c.startCap = RoundCap()

                println("Este es el ultimo: ${puntosTotales[0]}")
                println("Este es el ultimo: ${puntosTotales[puntosTotales.size -1]}")
                println("Este es primero: ${puntosTotales[puntoCorte2+1]}")
                println("Este es primero2: ${puntosTotales[puntoCorte1+1]}")
            } else {
                val parte1 = puntosTotales.subList(0, puntoCorte1+1)
                val parte2 = puntosTotales.subList(puntoCorte2, puntosTotales.size)
                polylineOptionsC.addAll(parte2 + parte1) // se agrega al reves para evitar uniones
                val d = gmap.addPolyline(polylineOptionsC)
                d.jointType = JointType.ROUND
                d.endCap = RoundCap()
                d.startCap = RoundCap()
            }

        } else {
            crearToast("Datos de recorridos no han sido recibidos")
        }

    }



    private fun compararDistancias(
        puntos: List<LatLng>,
        ubiInicio: Location,
        ubiDestino: Location,
        esInicio:Boolean
    ): LatLng {
        //devuelve el punto mas cercano entre una ubicacion y una lista de ubicaciones
        var puntoMasCerca = LatLng(0.0, 0.0)
        var distancia: Int
        for (f in 0 until puntos.size) {
            val estacion = puntos[f]
            ubiDestino.latitude = estacion.latitude
            ubiDestino.longitude = estacion.longitude

            distancia = (ubiInicio.distanceTo(ubiDestino)).toInt() //aqui se saca la distancia en metros

            //hay array para inicio y uno para destino, por eso la comprobacion
            if (esInicio){
                if (distancia < masCortaInicio[1]) {
                    masCortaInicio[0] = f
                    masCortaInicio[1] = distancia
                }
            }
            if (!esInicio){
                if (distancia < masCortaDestino[1]) {
                    masCortaDestino[0] = f
                    masCortaDestino[1] = distancia
                }
            }
            puntoMasCerca = LatLng(estacion.latitude, estacion.longitude)
            println("La coordenada: $puntoMasCerca")
        }
        return puntoMasCerca
    }

    private fun crearAlerta(mensaje: String, op1:String, op2:String): Boolean {
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
    }

    private fun limpiarPolylines() {
        puntosSalida.clear()
        puntosLlegada.clear()
        polylineOptions.points.clear()
    }

    fun agregarMarcador(punto: LatLng, idIcono: Int, titulo: String) {
        val markerOptions = MarkerOptions().position(punto)
            .icon(BitmapDescriptorFactory.fromResource(idIcono))
            .title(titulo)
        gmap.addMarker(markerOptions)//se usa title marcador para colocarle titulo al icono
    }

    fun crearToast(mensaje: String) {
        Toast.makeText(
            mapa,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }
}