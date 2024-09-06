package com.terfess.busetasyopal.admin.mapa

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.ActivityMapaAdminBinding


class MapaAdmin : AppCompatActivity(),
    OnMapReadyCallback {
    private lateinit var binding: ActivityMapaAdminBinding
    private lateinit var fragmentMapAdmin: SupportMapFragment
    private lateinit var gMap: GoogleMap
    private var rutaId: Int = 0

    private val COORDINATES_YOPAL = LatLng(5.329894555473376, -72.40242298156761)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapaAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //map
        fragmentMapAdmin =
            supportFragmentManager.findFragmentById(R.id.mapAdm) as SupportMapFragment
        fragmentMapAdmin.getMapAsync(this)

        //get ruta id from extras
        val extrasPrev = intent.extras
        if (extrasPrev != null) {
            rutaId = extrasPrev.getInt("rutaId")
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        gMap = p0

        //animate camera on Yopal
        adjustMapCamera(COORDINATES_YOPAL)

        //draw routes
        crearRuta(rutaId)

        //activate button gps
        gpsNativeOn()

        //options admin add coord
        binding.btnAddCoorSalida.setOnClickListener {
            selectUbiWithMarker("salida")
        }

        binding.btnAddCordLlegada.setOnClickListener {
            selectUbiWithMarker("llegada")
        }
        //..


        //options admin delete points group
        binding.btnDelSalida.setOnClickListener {
            val context = binding.root.context
            val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)

            builder.setTitle(context.getString(R.string.alert_text_literal))
                .setIcon(R.drawable.ic_panel_admin)
                .setMessage("¿Quieres eliminar el recorrido de 'Salida'?")
                .setPositiveButton(context.getString(R.string.confirm)) { _, _ ->
                    // Delete line/route is confirm
                    deleteLine("salida")
                }
                .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                    // cancel
                    dialog.dismiss()
                }
            // show dialog
            val dialog = builder.create()
            dialog.show()

        }

        binding.btnDelLlegada.setOnClickListener {
            val context = binding.root.context
            val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)

            builder.setTitle(context.getString(R.string.alert_text_literal))
                .setIcon(R.drawable.ic_panel_admin)
                .setMessage("¿Quieres eliminar el recorrido de 'Llegada'?")
                .setPositiveButton(context.getString(R.string.confirm)) { _, _ ->
                    // Delete line/route is confirm
                    deleteLine("llegada")
                }
                .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                    // cancel
                    dialog.dismiss()
                }
            val dialog = builder.create()
            dialog.show()
        }
        //..
    }

    private fun gpsNativeOn() {
        //verify permission
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //if permission granted, enable gps button on map
            gMap.isMyLocationEnabled = true
        }
    }


    private fun adjustMapCamera(coordinates: LatLng) {
        val zoom = 14f

        val cameraPosicion = CameraPosition.Builder()
            .target(coordinates)
            .zoom(zoom)
            .build()

        gMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosicion))
    }

    private var polyline1: Polyline? = null
    private var polyline2: Polyline? = null

    private var polylineOptions = PolylineOptions()

    lateinit var puntosSalida: MutableList<LatLng>
    lateinit var puntosLlegada: MutableList<LatLng>

    private val databaseRef = FirebaseDatabase.getInstance()
    private val markersMap =
        mutableMapOf<Marker, infoMarker>()

    private var lastKeySalida = 0
    private var lastKeyLlegada = 0


    private fun crearRuta(idruta: Int) {
        // Ruta - Primera Parte
        gMap.clear()

        databaseRef.getReference("features/0/rutas/$idruta/salida")
            .addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    puntosSalida = mutableListOf()
                    puntosSalida.clear()

                    polyline1?.remove()

                    //save last field key
                    val lastKey = dataSnapshot.children.lastOrNull()?.key
                    lastKeySalida = lastKey?.toInt() ?: 0

                    if (dataSnapshot.exists()) {
                        for (childSnapshot in dataSnapshot.children) {
                            polylineOptions.width(12f).color(
                                ContextCompat.getColor(this@MapaAdmin, R.color.recorridoIda)
                            )
                            val childId = childSnapshot.key
                            val lat = childSnapshot.child("1").getValue(Double::class.java)
                            val lng = childSnapshot.child("0").getValue(Double::class.java)
                            val latValue = lat ?: 0.0
                            val lngValue = lng ?: 0.0

                            val ubicacion = LatLng(latValue, lngValue)
                            puntosSalida.add(ubicacion)

                            val markerOptions = MarkerOptions()
                                .position(ubicacion)
                                .draggable(true)
                                .title("Lista Salida")

                            val existingMarker =
                                markersMap.entries.find { it.value.idMarkerUbi == childId?.toInt() && it.value.isSalida }

                            //if marker exists so remove from markersMap and from gMap
                            if (existingMarker != null && existingMarker.value.isSalida) {
                                markersMap.remove(existingMarker.key)
                                existingMarker.key.remove()

                                Log.i("Marker", "Marker removed salida")
                            }

                            val marker = gMap.addMarker(markerOptions)
                            marker?.let {
                                markersMap[marker] = infoMarker(childId!!.toInt(), true)
                            }

                        }
                    } else {
                        markersMap.entries.removeIf {
                            if (it.value.isSalida) {
                                it.key.remove()
                                true
                            } else {
                                false
                            }
                        }
                    }

                    polyline1 = gMap.addPolyline(polylineOptions)
                    polyline1?.points = puntosSalida
                    polyline1?.jointType = JointType.ROUND

                    println("${markersMap.size} markers in MapaAdmin")
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@MapaAdmin,
                        "Algo salio mal en la creacion de salida ruta $idruta.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        // Ruta - Segunda Parte
        databaseRef.getReference("features/0/rutas/$idruta/llegada")
            .addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    puntosLlegada = mutableListOf()
                    puntosLlegada.clear()

                    polyline2?.remove()

                    //save last field key
                    val lastKey = dataSnapshot.children.lastOrNull()?.key
                    lastKeyLlegada = lastKey?.toInt() ?: 0

                    if (dataSnapshot.exists()) {
                        for (childSnapshot in dataSnapshot.children) {
                            polylineOptions.width(9f).color(
                                ContextCompat.getColor(this@MapaAdmin, R.color.recorridoVuelta)
                            )
                            val childId = childSnapshot.key

                            val lat = childSnapshot.child("1").getValue(Double::class.java)
                            val lng = childSnapshot.child("0").getValue(Double::class.java)
                            val latValue = lat ?: 0.0
                            val lngValue = lng ?: 0.0

                            val ubicacion2 = LatLng(latValue, lngValue)
                            puntosLlegada.add(ubicacion2)

                            //comprobation marker for evite duplicated
                            val existingMarker =
                                markersMap.entries.find {
                                    it.value.idMarkerUbi == childId?.toInt() && !it.value.isSalida
                                }

                            // Añadir marcador
                            val markerOptions = MarkerOptions()
                                .position(ubicacion2)
                                .draggable(true) // Permitir que el marcador sea arrastrable
                                .title("Lista Llegada")

                            if (existingMarker != null && !existingMarker.value.isSalida) {
                                markersMap.remove(existingMarker.key)
                                existingMarker.key.remove()

                                Log.i("Marker", "Marker removed llegada")
                            }

                            val marker = gMap.addMarker(markerOptions)
                            marker?.let {
                                markersMap[marker] = infoMarker(childId!!.toInt(), false)
                            }
                        }
                    } else {
                        markersMap.entries.removeIf {
                            if (!it.value.isSalida) {
                                it.key.remove()
                                true
                            } else {
                                false
                            }
                        }
                    }

                    polyline2 = gMap.addPolyline(polylineOptions)
                    polyline2?.points = puntosLlegada
                    polyline2?.jointType = JointType.ROUND

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@MapaAdmin,
                        "Algo salio mal en la creacion de la llegada ruta $idruta.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })

        editPointMarker()
        onClickMarkerOptions()
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun editPointMarker() {

        gMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {

            }

            override fun onMarkerDrag(marker: Marker) {

            }

            override fun onMarkerDragEnd(marker: Marker) {
                marker.remove()
                val index = markersMap[marker]

                val newLatLng = marker.position

                val sentido = if (index?.isSalida == true) {
                    "salida"
                } else {
                    "llegada"
                }

                // update data point in data base---------
                val urlRefPoint =
                    "features/0/rutas/$rutaId/$sentido/${index?.idMarkerUbi}"

                val pointRef =
                    databaseRef.getReference(urlRefPoint)

                pointRef.setValue(
                    mapOf(
                        "0" to newLatLng.longitude,
                        "1" to newLatLng.latitude
                    )
                ).addOnSuccessListener {
                    println("Marker at succes update ::= ${marker.position}")
                }.addOnFailureListener {
                    UtilidadesMenores().crearSnackbar("Error al actualizar el punto", binding.root)
                }
                //..
            }
        })
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun onClickMarkerOptions() {
        val opts = binding.containInfoMarker
        gMap.setOnMarkerClickListener { marker ->

            val marcador = markersMap[marker]
            //set info
            val strIdMarker = marcador?.idMarkerUbi.toString()

            val sentido = if (marcador?.isSalida == true) {
                "salida"
            } else {
                "llegada"
            }

            binding.tvIsSalidaMarker.text = if (marcador?.isSalida == true) {
                "Punto $strIdMarker en recorrido $sentido"
            } else {
                "Punto $strIdMarker en recorrido ${sentido.uppercase()}"
            }

            opts.visibility = View.VISIBLE

            binding.btnDelPoint.setOnClickListener {
                marcador?.let {
                    removePointFirebase(sentido, marcador.idMarkerUbi)
                    opts.visibility = View.GONE
                }
            }
            true
        }

        gMap.setOnMapClickListener { _ ->
            //si toca el mapa ocultar card con detalles de marker
            opts.visibility = View.GONE

        }

    }

    private fun removePointFirebase(sentido: String, index: Int) {
        // get reference to point
        val pointRef =
            databaseRef.getReference("features/0/rutas/$rutaId/$sentido/$index")

        // delete in data base
        pointRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // success
                Log.d("FirebaseDEl", "Punto eliminado correctamente")
            } else {
                // Error
                UtilidadesMenores().crearSnackbar("Error al eliminar el punto", binding.root)
                Log.e("FirebaseDEl", "Error al eliminar el punto", task.exception)
            }
        }
    }


    private fun deleteLine(sentido: String) {
        val pointRef =
            databaseRef.getReference("features/0/rutas/$rutaId/$sentido")

        // Eliminar el punto correspondiente en la base de datos de Firebase
        pointRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Éxito en la eliminación

                Log.d("FirebaseDEl", "recorrido $sentido eliminado correctamente")
            } else {
                // Error al eliminar el punto
                Log.e(
                    "FirebaseDEl",
                    "Error al eliminar el recorrido en $sentido",
                    task.exception
                )
            }
        }
    }

    private fun selectUbiWithMarker(sentido: String) {
        // marker on position with listener for change ubi ---------------------
        val centerMarker = MarkerOptions()
        centerMarker.position(COORDINATES_YOPAL)
        val marcador = gMap.addMarker(centerMarker)

        gMap.setOnCameraMoveListener { //listener
            marcador?.let { safeMarker ->
                safeMarker.position = gMap.cameraPosition.target
            }
        }


        turnOffOnSetUbi(true)

        binding.btnSetUbi.setOnClickListener {
            marcador?.let {
                val countPoint = when (sentido) {
                    "salida" -> lastKeySalida
                    "llegada" -> lastKeyLlegada
                    else -> throw IllegalArgumentException("Sentido inválido: $sentido")
                }

                val pointRef =
                    databaseRef.getReference("features/0/rutas/$rutaId/$sentido/${countPoint + 1}")
                pointRef.setValue(
                    mapOf(
                        "0" to it.position.longitude,
                        "1" to it.position.latitude
                    )
                ).addOnFailureListener {
                    UtilidadesMenores().crearSnackbar("Error al añadir punto", binding.root)
                }

                turnOffOnSetUbi(false)
            }
            marcador?.remove()
        }
    }

    private fun turnOffOnSetUbi(isSavePos: Boolean) {
        if (isSavePos) {
            binding.contSavePos.visibility = View.VISIBLE
            binding.contSalida.visibility = View.GONE
            binding.contLlegada.visibility = View.GONE
        } else {
            binding.contSavePos.visibility = View.GONE
            binding.contSalida.visibility = View.VISIBLE
            binding.contLlegada.visibility = View.VISIBLE
        }
    }

}

data class infoMarker(var idMarkerUbi: Int, var isSalida: Boolean)