package com.terfess.busetasyopal.actividades

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationListener
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
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.clases_utiles.RutaBasic
import com.terfess.busetasyopal.databinding.PantMapaBinding


class Mapa : AppCompatActivity(), LocationListener,
    ActivityCompat.OnRequestPermissionsResultCallback, OnMapReadyCallback {
    private lateinit var binding: PantMapaBinding
    private lateinit var gmap: GoogleMap
    private var contexto = this
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var idruta: Int = 0
    private var networkCallback = ConnectivityManager.NetworkCallback()
    private val tiempos = Handler(Looper.getMainLooper())
    private var puntoProvisionalGps : Circle? = null


    companion object { //accesibles desde cualquier lugar de este archivo/clase y proyectos
        const val codigoLocalizacion = 0
        var ubiUser = LatLng(0.0, 0.0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PantMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //mapa
        val fragmentoMapa = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        fragmentoMapa.getMapAsync(this)

        /*//lugares/places api   //HAY QUE PAGAR EN CLOUD POR ESO SE DESACTIVARA PLACES
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.key_mapa))

        }
        val clienteLugares = Places.createClient(this)
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete) as AutocompleteSupportFragment
        autocompleteFragment.setCountries("CO")
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))//especificar tipo de los datos a recibir
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i(TAG, "Place: ${place.name}, ${place.id}")
            }
            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred: $status")
            }
        })*/

        //seleccion ruta
        val recibirTent = intent.extras
        if (recibirTent != null) {
            idruta = recibirTent.getInt("selector")
        }

        //ubicacion del gps
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //colores en leyenda informativa
        val infoSalida = "<font color='${getColor(R.color.recorridoIda)}' >Azul</font>"
        val infoLlegada = "<font color='${getColor(R.color.recorridoVuelta)}' >Rojo</font>"
        binding.infoColor.text = Html.fromHtml(
            " Ruta Salida: $infoSalida <br> Ruta Llegada: $infoLlegada",
            Html.FROM_HTML_MODE_LEGACY
        )
        //estado de conexion
        if (!comprobarConexion(this)) {
            msjNoConection()
        }

        // Registrar un NetworkCallback para recibir actualizaciones de conectividad
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                runOnUiThread {
                    binding.failConection.visibility = View.GONE
                }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        //Cuando se calcule la ruta
        if (idruta == 0) {
            binding.infoColor.visibility = View.GONE
            //binding.parqueadero.visibility = View.GONE
        }
    }

    override fun onMapReady(mapa: GoogleMap) {
        gmap = mapa

        //getRutasCreadas = RutaBasic.ruta //usar el gmap para mandarlo a RutaBasic


        //establecer nivel de zoom maximo
        //impedir renderizado de cuadros casas
        val maxZoomLevel = 16.9 // nivel de zoom deseado
        gmap.setMaxZoomPreference(maxZoomLevel.toFloat())

        //rendimiento
        mapa.mapType =
            GoogleMap.MAP_TYPE_NORMAL //tratar de cargar un mapa simple evitar renderizados congelantes
        gmap.isBuildingsEnabled = false //para dar mejor rendimiento desactivar edificaciones
        gmap.isTrafficEnabled = false //para dar mejor rendimiento desactivar trafico
        gmap.isIndoorEnabled =
            false //desactivar vista de planos de algunas edificacoiones--rendimiento
        gmap.uiSettings.isIndoorLevelPickerEnabled =
            false //desactivar selector de piso o nivel -- rendimiento

        comprobarConexion(this)

        irYopal()

        selector()  //seleccionar que ruta cargar

        gmap.uiSettings.isMyLocationButtonEnabled = false //desactivar el boton default de gps

        //pedir/comprobar permiso ubicacion y gps On
        binding.irgps.setOnClickListener {
            activarLocalizacion()
            irPosGps()
        }

        supportActionBar?.title = "Mapa con Recorrido de Ruta $idruta"  //titulo actionbar

        //botones verdistancia y calcular punto cercano
        binding.verDistancia.setOnClickListener {
            binding.verDistancia.visibility = View.GONE
            binding.sentidoSubida.visibility = View.VISIBLE
            binding.sentidoLlegada.visibility = View.VISIBLE
            RutaBasic.CreatRuta.estamarcado1 = false
            RutaBasic.CreatRuta.estamarcado2 = false
        }
        binding.sentidoSubida.setOnClickListener {
            if (RutaBasic.CreatRuta.estamarcado1 == false) {
                calcularDistancia("salida")
                mostrarIndicaciones()
            }
        }
        binding.sentidoLlegada.setOnClickListener {
            if (RutaBasic.CreatRuta.estamarcado2 == false) {
                calcularDistancia("llegada")
                mostrarIndicaciones()
            }
        }

        //botones cambiar ajustes del mapa -----------------------------------------------
        binding.ajustes.setOnClickListener {
            if (binding.configuraciones.visibility != View.VISIBLE){
                binding.configuraciones.visibility = View.VISIBLE
                binding.ajustes.visibility = View.GONE
                binding.irgps.visibility = View.GONE
            }
        }
        binding.opcionesTipoMapa.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (parent?.getItemAtPosition(position).toString()){
                    "Mapa Normal" -> {
                        gmap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    }
                    "Mapa Hybrido" -> {
                        gmap.mapType = GoogleMap.MAP_TYPE_HYBRID
                    }
                    "Mapa Satelital" -> {
                        gmap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    }
                    "Mapa Relieve" -> {
                        gmap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                gmap.mapType = GoogleMap.MAP_TYPE_NORMAL
            }

        }
        binding.opcionTrafico.setOnCheckedChangeListener { _, isChecked ->
            // Acciones a realizar cuando el estado del CheckBox cambia
            if (isChecked) {
                gmap.isTrafficEnabled = true
            }else{
                gmap.isTrafficEnabled = false
            }
        }
        binding.guardarAjustes.setOnClickListener {
            binding.configuraciones.visibility = View.GONE
            binding.ajustes.visibility = View.VISIBLE
            binding.irgps.visibility = View.VISIBLE
        }

    }

    private fun selector() {
        val buildRuta = RutaBasic(contexto, gmap)
        when (idruta) {
            0 -> {
                activarLocalizacion()
                irPosGps()
            }
            in listOf(2, 3, 6, 7, 8, 9, 10, 11, 13) -> buildRuta.crearRuta(idruta)
        }
        /*when (idruta) {//IMPORTANTE PARA CONSTRUIR CADA RUTA
            0 -> {
                activarLocalizacion()
                irPosGps()
            }

            2 -> {
                buildRuta.crearRuta(
                    idruta
                )
            }

            3 -> {
                buildRuta.crearRuta(
                    idruta
                )
            }

            6 -> {
                buildRuta.crearRuta(
                    idruta
                )
            }

            7 -> {
                buildRuta.crearRuta(
                    idruta
                )
            }

            8 -> {
                buildRuta.crearRuta(
                    idruta
                )
            }

            9 -> {
                buildRuta.crearRuta(
                    idruta
                )
            }

            10 -> {
                buildRuta.crearRuta(
                    idruta
                )
            }

            11 -> {
                buildRuta.crearRuta(
                    idruta
                )
            }

            13 -> {
                buildRuta.crearRuta(
                    idruta
                )
            }
        }*/
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
        if (!comprobarConexion(this)) {
            activarGpsTelefono()
            irPosGps()
        } else {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                //pedir activar gps estilo de google
                val locationRequest =
                    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5).apply {
                        setMinUpdateDistanceMeters(10f)
                        setGranularity(Granularity.GRANULARITY_FINE)
                        setWaitForAccurateLocation(true)
                    }.build()
                val client: SettingsClient = LocationServices.getSettingsClient(this)
                val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                val task = client.checkLocationSettings(builder.build())
                task.addOnFailureListener(this) { e ->
                    if (e is ResolvableApiException) {
                        try {
                            e.startResolutionForResult(this, 1)
                            binding.irgps.setImageResource(R.drawable.ic_progress_gps)
                            irPosGps()
                        } catch (sendEx: IntentSender.SendIntentException) {
                            // Error al intentar abrir la configuración de ubicación
                            Log.e(
                                "GPS",
                                "Error al intentar abrir la configuración de ubicación: ${sendEx.message}"
                            )
                        }
                    }
                }
                irPosGps()
            }
        }

    }

    private fun irYopal() {
        val cameraPosicion = CameraPosition.Builder()
            .target(LatLng(5.329894555473376, -72.40242298156761))
            .zoom(14f)
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
                binding.irgps.setImageResource(R.drawable.ic_gps_find)
                gmap.animateCamera(CameraUpdateFactory.newLatLngZoom((latLng), 16.5f), 3000, null)
                ubiUser = latLng
                if (binding.sentidoSubida.visibility != View.VISIBLE) {
                    binding.verDistancia.visibility = View.VISIBLE
                }
                //marcador en ubi de respaldo
                val accuracyRadius = 14.0  // Cambia esto con la precisión real
                val opcionesCirculo = CircleOptions()
                    .center(latLng)
                    .radius(accuracyRadius)
                    .strokeWidth(1f)
                    .fillColor(0x55ff00ff)
                puntoProvisionalGps = if (puntoProvisionalGps == null){
                    gmap.addCircle(opcionesCirculo)
                }else{
                    puntoProvisionalGps?.remove()
                    gmap.addCircle(opcionesCirculo)
                }
            } else {
                irPosGps()
            }
        }
    }


    //verificar si tiene permiso de ubicacion
    private fun permisoUbiCondecido() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun activarLocalizacion() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        ActivityCompat.requestPermissions(this, arrayOf(permission), codigoLocalizacion)
    }

    @SuppressLint("MissingPermission")
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
    @SuppressLint("MissingPermission")
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

    private fun comprobarConexion(context: Context): Boolean {
        val returne: Boolean
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager //transformar el valor resultante en un connectibitymanager mediante as
        val networkCapabilities =
            connectivityManager.activeNetwork ?: return false //devuelve identificador de red
        val actNw =
            connectivityManager.getNetworkCapabilities(networkCapabilities)
                ?: return false //obtiene propiedades de la red apartir del identificardor de networkcapabilities
        returne = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
        return returne
    }

    private fun msjNoConection() {
        runOnUiThread {
            binding.failConection.visibility = View.VISIBLE
            binding.failConection.text = Html.fromHtml(
                "<font color='${getColor(R.color.anuncioGrave)}'>¡En este momento no tienes conexión a Internet es posible que no veas las rutas!</font>",
                Html.FROM_HTML_MODE_LEGACY
            )
            tiempos.postDelayed({ // terminar o ejecutar tareas despues de cierto tiempo
                if (RutaBasic.CreatRuta.rutasCreadas) {
                    binding.failConection.visibility = View.GONE

                } else {
                    binding.failConection.visibility = View.VISIBLE
                    binding.failConection.text = Html.fromHtml(
                        "<font color='${getColor(R.color.anuncioLeve)}'>¡Por favor conectate a Internet para ver las rutas y cargar el mapa!</font>",
                        Html.FROM_HTML_MODE_LEGACY
                    )
                }
            }, 7000) // 10 segundos
        }
    }

    private fun activarGpsTelefono() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialog.setTitle("Activar GPS sin conexión")
            alertDialog.setMessage("El dispositivo no tiene conexión a internet, ¿Desea activar el gps del dispositivo que funciona sin internet?")
            alertDialog.setPositiveButton("Si") { _, _ ->
                binding.irgps.setImageResource(R.drawable.ic_progress_gps)
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            alertDialog.setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
            alertDialog.show()
            irPosGps()
        }
    }

    private fun calcularDistancia(sentido: String) {
        val defecto = LatLng(0.0, 0.0)
        if (ubiUser != defecto) {
            RutaBasic(contexto, this.gmap).rutaMasCerca(ubiUser, sentido)
        } else {
            activarLocalizacion()
            irPosGps()
        }

    }

    private fun mostrarIndicaciones() {
        val metros = RutaBasic.CreatRuta.masCortaInicio[1]
        val punto = RutaBasic.CreatRuta.masCortaInicio[0]
        binding.indicaciones.visibility = View.VISIBLE
        "Camina $metros m hasta el punto $punto marcado con el icono.".also { binding.indicaciones.text = it }

    }

    override fun onDestroy() {
        super.onDestroy()
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun onLocationChanged(p0: Location) {
        val ubiGps = LatLng(p0.latitude, p0.longitude)
        gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubiGps, 20f), 3000, null)
        irPosGps()
    }
}