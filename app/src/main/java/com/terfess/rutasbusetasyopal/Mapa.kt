package com.terfess.rutasbusetasyopal

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.terfess.rutasbusetasyopal.databinding.ActivityMapaBinding


class Mapa : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapaBinding
    private lateinit var gmap: GoogleMap
    private lateinit var db: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var idruta: Int = 0

    companion object {
        const val codigoLocalizacion = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //database realtime
        db =
            FirebaseDatabase.getInstance("https://rutasbusetas-default-rtdb.firebaseio.com/").reference
        FirebaseApp.initializeApp(this)
        //mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //seleccion ruta
        val recibirTent = intent.extras
        if (recibirTent != null) {
            idruta = recibirTent.getInt("selector")
        }
        //ubicacion del gps
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //colores informativos
        val infoSalida = "<font color='${getColor(R.color.recorridoIda)}' >Azul</font>"
        val infoLlegada = "<font color='${getColor(R.color.recorridoVuelta)}' >Rojo</font>"
        binding.infoColor.text = Html.fromHtml(
            " Ruta Salida: $infoSalida <br> Ruta Llegada: $infoLlegada <br> Parqueadero:",
            Html.FROM_HTML_MODE_LEGACY
        )

    }

    override fun onMapReady(map: GoogleMap) {
        gmap = map
        //map.mapType = GoogleMap.MAP_TYPE_HYBRID
        irYopal()
        selector()
        gmap.uiSettings.isMyLocationButtonEnabled = false
        binding.irgps.setOnClickListener {
            activarLocalizacion()
            irPosGps()
        }
        supportActionBar?.title = "Mapa con la Ruta $idruta"
    }

    private fun selector() {
        val buildRuta = RutaBasic(this, this.gmap)
        when (idruta) {
            2 -> {
                buildRuta.crearRuta(
                    "features/0/rutas/$idruta/salida",
                    "features/0/rutas/$idruta/llegada", idruta
                )
            }

            3 -> {
                buildRuta.crearRuta(
                    "features/0/rutas/$idruta/salida",
                    "features/0/rutas/$idruta/llegada", idruta
                )
            }

            4 -> {
                buildRuta.crearRuta(
                    "features/0/rutas/$idruta/salida",
                    "features/0/rutas/$idruta/llegada", idruta
                )
            }

            5 -> {
                buildRuta.crearRuta(
                    "features/0/rutas/$idruta/salida",
                    "features/0/rutas/$idruta/llegada", idruta
                )
            }

            6 -> {
                buildRuta.crearRuta(
                    "features/0/rutas/$idruta/salida",
                    "features/0/rutas/$idruta/llegada", idruta
                )
            }

            7 -> {
                buildRuta.crearRuta(
                    "features/0/rutas/$idruta/salida",
                    "features/0/rutas/$idruta/llegada", idruta
                )
            }

            8 -> {
                buildRuta.crearRuta(
                    "features/0/rutas/$idruta/salida",
                    "features/0/rutas/$idruta/llegada", idruta
                )
            }

            9 -> {
                buildRuta.crearRuta(
                    "features/0/rutas/$idruta/salida",
                    "features/0/rutas/$idruta/llegada", idruta
                )
            }

            10 -> {
                buildRuta.crearRuta(
                    "features/0/rutas/$idruta/salida",
                    "features/0/rutas/$idruta/llegada", idruta
                )
            }

            11 -> {
                buildRuta.crearRuta(
                    "features/0/rutas/$idruta/salida",
                    "features/0/rutas/$idruta/llegada", idruta
                )
            }

            12 -> {
                buildRuta.crearRuta(
                    "features/0/rutas/$idruta/salida",
                    "features/0/rutas/$idruta/llegada", idruta
                )
            }

            13 -> {
                buildRuta.crearRuta(
                    "features/0/rutas/$idruta/salida",
                    "features/0/rutas/$idruta/llegada", idruta
                )
            }
        }
    }

    private fun activarGps() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!permisoUbiCondecido()) {
            Toast.makeText(
                this,
                "Permiso de ubicación no permitido--Activalo en ajustes.",
                Toast.LENGTH_LONG
            ).show()
        }
        if (!::gmap.isInitialized) return
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //pedir activar gps estilo de google
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,5).apply {
                setMinUpdateDistanceMeters(10f)
                setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                setWaitForAccurateLocation(true)
            }.build()
            val client: SettingsClient = LocationServices.getSettingsClient(this)
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            val task = client.checkLocationSettings(builder.build())
            task.addOnFailureListener(this, OnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        e.startResolutionForResult(this, 1)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Error al intentar abrir la configuración de ubicación
                        Log.e(
                            "GPS",
                            "Error al intentar abrir la configuración de ubicación: ${sendEx.message}"
                        )
                    }
                }
            })
        }
    }

    private fun irYopal() {
        val cameraPosicion = CameraPosition.Builder()
            .target(LatLng(5.330142833118871, -72.40302835546387))
            .zoom(14.5f)
            .build()
        gmap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosicion))
    }
    private fun irPosGps() {
        //este if verifica que no tiene permiso ni de FINE ni de COURSE LoCATION
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                codigoLocalizacion
            )
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            // Verifica si la ubicación no es nula
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                gmap.animateCamera(CameraUpdateFactory.newLatLngZoom((latLng), 15.5f), 3000, null)
                Toast.makeText(this, "Mostrando Ubicación..", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //veridicar si tiene permiso de ubicacion
    private fun permisoUbiCondecido() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun activarLocalizacion() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        ActivityCompat.requestPermissions(this, arrayOf(permission), codigoLocalizacion)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            codigoLocalizacion -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gmap.isMyLocationEnabled = true
                activarGps()
            } else {
                gmap.isMyLocationEnabled = false
            }

            else -> {}
        }
    }

    //retorno usuario desde el background
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::gmap.isInitialized) return
        if (!permisoUbiCondecido()) {
            gmap.isMyLocationEnabled = false
            Toast.makeText(
                this,
                "Permiso de localización no concedido. Puedes cambiarlo en ajustes.",
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    //inflar menu ActionBar Mapa
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_mapa, menu)
        return true
    }

    //controlar opcion close de manu Action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.Cerrar -> {
                val intent = Intent(this, RutasSeccion::class.java)
                startActivity(intent)
            }
        }
        return true
    }
}