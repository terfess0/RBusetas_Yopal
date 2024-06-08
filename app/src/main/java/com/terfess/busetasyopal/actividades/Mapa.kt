package com.terfess.busetasyopal.actividades

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.terfess.busetasyopal.OpMapaAdapterHolder
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.clases_utiles.AlertaCallback
import com.terfess.busetasyopal.clases_utiles.DatosASqliteLocal
import com.terfess.busetasyopal.clases_utiles.PlanearRutaDestino
import com.terfess.busetasyopal.clases_utiles.PlanearRutaDestino.Datos
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.PantMapaBinding
import com.terfess.busetasyopal.modelos_dato.DatoOpMapa


class Mapa : AppCompatActivity(), LocationListener, OnMapReadyCallback, AlertaCallback {
    private lateinit var binding: PantMapaBinding
    private lateinit var gmap: GoogleMap
    private var contexto = this
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var idruta: Int = 0
    private var networkCallback = ConnectivityManager.NetworkCallback()
    private val tiempos = Handler(Looper.getMainLooper())
    private var puntoProvisionalGps: Circle? = null
    private lateinit var marcador: Marker
    private lateinit var mAdView: AdView //anuncios
    private lateinit var tareaActual: String //que esta haciendo el mapa?

    private lateinit var FragmentMap : SupportMapFragment


    companion object { //accesibles desde cualquier lugar de este archivo/clase y proyectos
        const val codigoLocalizacion = 0
        var ubiUser = LatLng(0.0, 0.0)
        var hayConexion = true
        var contador = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PantMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //mapa
        FragmentMap = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        FragmentMap.getMapAsync(this)


        cargarAnuncios()

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

//        //api autocomplete openstreetmap
//        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.autocompleteTextView)
//        val addressAutocomplete = AddressAutocomplete(autoCompleteTextView, this)
//
//        // Configurar el TextWatcher para activar la búsqueda de sugerencias de direcciones cuando el texto cambia
//        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//
//            override fun afterTextChanged(s: Editable?) {
//                // Iniciar la búsqueda de sugerencias de direcciones cuando el texto cambia
//                addressAutocomplete.start()
//            }
//        })


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
            " Ruta Salida: $infoSalida - Ruta Llegada: $infoLlegada",
            Html.FROM_HTML_MODE_LEGACY
        )
        //estado de conexion
        if (!UtilidadesMenores().comprobarConexion(this)) {
            msjNoConection()
            !hayConexion
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

        //inicializar arrays que se usaran para calcular rutas
        Datos.mejorPuntoaInicio[0] = -1 // inicializa el índice de la estación más cercana en -1
        Datos.mejorPuntoaInicio[1] = Int.MAX_VALUE // inicializa la distancia con un valor alto

        Datos.mejorPuntoaDestino[0] = -1
        Datos.mejorPuntoaDestino[1] = Int.MAX_VALUE
        //-----------------------------------------------------


    }


    override fun onMapReady(mapa: GoogleMap) {
        gmap = mapa

        //progressbar del mapa (ya cargó)
        gmap.setOnMapLoadedCallback {
            binding.loadingMapa.visibility = View.GONE
            println("Mapa ha terminado de cargarse")
        }

        mapa.mapType =
            GoogleMap.MAP_TYPE_NORMAL


        //definir estado accion del mapa (que esta mostrando)
        tareaActual = "Mostrando ruta $idruta"

        irYopal() //colocar la camara en la ciudad de yopal

        selector()  //seleccionar que ruta cargar

        var marcadorIndicador: Marker? = null
        gmap.setOnMapClickListener { it ->
            if (marcadorIndicador != null) {
                // Si el marcador ya existe, eliminarlo
                marcadorIndicador?.remove()
                marcadorIndicador = null
            } else {
                // Si el marcador no existe, agregarlo
                val optionsMarker = MarkerOptions()
                    .position(it)
                    .title("Mi Destino")
                marcadorIndicador = gmap.addMarker(optionsMarker)
            }
        }


        //pedir/comprobar permiso ubicacion y gps On
        binding.irgps.setOnClickListener {
            activarLocalizacion()
        }

        supportActionBar?.subtitle = "Camino de salida y de llegada"
        supportActionBar?.title = "Recorrido Ruta $idruta"  //titulo actionbar
        if (idruta == 0) {
            supportActionBar?.title =
                "Calcular Ruta Inicio a Destino"  //titulo opcion calcular ruta
            supportActionBar?.subtitle = "Elíge un inicio y un destino"

        } else if (idruta == 20) {
            supportActionBar?.subtitle = "Todas las rutas habilitadas"
            supportActionBar?.title =
                "Ver Mapa con las Rutas"  //titulo opcion ver mapa con ruta

        } else if (idruta == 40) {
            supportActionBar?.subtitle = "Parqueaderos de donde sale la buseta"
            supportActionBar?.title =
                "Ver Mapa con Parqueaderos"  //titulo opcion mapa con parqueaderos

        }

        //-------------------------------------------------------------------------------

        //botones verdistancia y calcular punto cercano
        binding.verDistancia.setOnClickListener {
            //ocultar - mostrar elementos
            binding.verDistancia.visibility = View.GONE
            binding.sentidoSubida.visibility = View.VISIBLE
            binding.sentidoLlegada.visibility = View.VISIBLE

            PolylinesPrincipal.CreatRuta.estamarcado1 = false //evitar dobles marcadores de estacion
            PolylinesPrincipal.CreatRuta.estamarcado2 = false //evitar dobles marcadores de estacion
        }
        binding.sentidoSubida.setOnClickListener {
            if (binding.infoColor.isVisible) {
                binding.infoColor.visibility = View.GONE //ocultar leyenda al ver distancia
            }
            if (PolylinesPrincipal.CreatRuta.estamarcado1 == false) {
                calcularDistancia("salida")
                mostrarIndicaciones()
            }
        }
        binding.sentidoLlegada.setOnClickListener {
            if (binding.infoColor.isVisible) {
                binding.infoColor.visibility = View.GONE //ocultar leyenda al ver distancia
            }
            if (PolylinesPrincipal.CreatRuta.estamarcado2 == false) {
                calcularDistancia("llegada")
                mostrarIndicaciones()
            }
        }

        //-------------------------------------------------------------------------------
        //botones cambiar ajustes del mapa -----------------------------------------------

        binding.ajustes.setOnClickListener {
            if (binding.configuraciones.visibility != View.VISIBLE) {
                binding.configuraciones.visibility = View.VISIBLE
                binding.ajustes.visibility = View.GONE
                binding.irgps.visibility = View.GONE
                binding.listaRutasOpMapa.visibility = View.GONE
            }
        }
        binding.opcionesTipoMapa.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when (parent?.getItemAtPosition(position).toString()) {
                        "Mapa Normal" -> {
                            gmap.mapType = GoogleMap.MAP_TYPE_NORMAL //set tipo de mapa normal
                        }

                        "Mapa Hybrido" -> {
                            gmap.mapType = GoogleMap.MAP_TYPE_HYBRID //set tipo de mapa hybrido
                        }

                        "Mapa Satelital" -> {
                            gmap.mapType = GoogleMap.MAP_TYPE_SATELLITE //set tipo de mapa satelital
                        }

                        "Mapa Relieve" -> {
                            gmap.mapType = GoogleMap.MAP_TYPE_TERRAIN //set tipo de mapa relieve
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    gmap.mapType =
                        GoogleMap.MAP_TYPE_HYBRID //en caso de no haber seleccion se dejara el por defecto (hibrido)
                }

            }
        binding.guardarAjustes.setOnClickListener {//cerrar ventana de ajustes
            binding.configuraciones.visibility = View.GONE
            binding.ajustes.visibility = View.VISIBLE
            if (idruta == 20) binding.listaRutasOpMapa.visibility = View.VISIBLE
            if (idruta != 0) binding.irgps.visibility = View.VISIBLE
        }

    }

    private fun selector() { // decide que ruta creara o que tipo de mapa creara
        val buildRuta = PolylinesPrincipal(contexto, gmap)
        when (idruta) {

            //--------------------------------------------------------------------------------------
            //---------------------------OPCIONES BOTONES----------------------------------------------
            //--------------------------------------------------------------------------------------
            //el numero 0 de id_ruta sera el que cree un mapa para calcular ruta
            0 -> {
                //identificar estado mapa - tarea
                tareaActual = "Calculando ruta"
                //---------------------------------

                //Cuando se calcule la ruta
                binding.infoColor.visibility = View.GONE
                binding.textPruebas.visibility = View.VISIBLE
                supportActionBar?.title = "Calcular viaje Inicio - Destino"  //titulo actionbar

                var ubiInicio = LatLng(0.0, 0.0)
                var ubiDestino = LatLng(0.0, 0.0)
                //mostrar botones para guardar ubicacion inicio - destino
                binding.posInicio.visibility = View.VISIBLE
                binding.posDestino.visibility = View.VISIBLE

                //ocultar boton gps y leyenda
                binding.irgps.visibility = View.GONE
                binding.infoColor.visibility = View.GONE


                binding.posInicio.setOnClickListener {
                    UtilidadesMenores().crearToast(this, "Arrastra la pantalla")
                    binding.posInicio.isClickable =
                        false //despues de seleccionado se desactiva el boton

                    Datos.mejorPuntoaInicio[0] =
                        -1 // inicializa el índice de la estación más cercana en -1
                    Datos.mejorPuntoaInicio[1] =
                        Int.MAX_VALUE // inicializa la distancia con un valor alto
                    Datos.mejorPuntoaInicio[2] = -1

                    //marcador en el centro de la pantalla - apuntador para definir ubicacion---------------------
                    val centerMarker = MarkerOptions()
                    centerMarker.position(
                        LatLng(
                            5.329894555473376,
                            -72.40242298156761
                        )
                    ) //misma ubicacion inicial de la camara
                    marcador = gmap.addMarker(centerMarker)!!

                    //mostrar botones setubi y setubigps
                    binding.setUbicacion.visibility = View.VISIBLE
                    binding.setUbiGps.visibility = View.VISIBLE
                    gmap.setOnCameraMoveListener { //listener que permite que el marcador se mueva de acuerdo al movimiento de la camara del mapa
                        marcador.position = gmap.cameraPosition.target
                    }

                    //guardar la ubicacion y representarla con un marcador
                    binding.setUbicacion.setOnClickListener {
                        binding.setUbicacion.visibility = View.GONE
                        binding.setUbiGps.visibility = View.GONE

                        ubiInicio = LatLng(
                            marcador.position.latitude,
                            marcador.position.longitude
                        ) //guardar ubicacion

                        gmap.addMarker(
                            MarkerOptions().position(ubiInicio).title("Punto de Partida").icon(
                                BitmapDescriptorFactory.fromResource(R.drawable.ic_salida)
                            )
                        ) //representar la ubicacion guardada con un marcador con icono personalizado

                        binding.posInicio.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_gps_find,
                            0
                        ) // cambiar el icono al final del boton cuando se guarda la ubicacion
                        binding.posInicio.text = "Partida   "

                        marcador.remove() //borrar el marcador inicial (solo queda el de la ubicacion guardada)

                        // si las dos ubicaciones han sido obtenidas (inicio - destino) comenzara a calcularse la ruta
                        if (ubiInicio != LatLng(0.0, 0.0) && ubiDestino != LatLng(0.0, 0.0)) {
                            PlanearRutaDestino(this, gmap).rutaToDestino(
                                ubiInicio,
                                ubiDestino,
                                this
                            )
                            binding.infoColor.visibility = View.VISIBLE

                            //si la distancia hasta el punto salida es menor a 200 m mostrar indicaciones calculadas
                            mostrarIndicacionesCalculadas(false)

                            //cambiar iconos de ubicacion inicio y fin en boton por checks verdes
                            binding.posInicio.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_check,
                                0
                            )
                            binding.posDestino.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_check,
                                0
                            )
                        }
                    }

                    //guardar la ubicacion y representarla con un marcador
                    binding.setUbiGps.setOnClickListener {
                        binding.setUbiGps.visibility = View.GONE
                        UtilidadesMenores().crearToast(this, "Localizando...")

                        activarLocalizacion()
                        irPosGps()

                        ubiInicio = ubiUser//guardar ubicacion gps

                        if (ubiInicio != LatLng(0.0, 0.0)) {
                            gmap.addMarker(
                                MarkerOptions().position(ubiInicio).title("Punto de Partida").icon(
                                    BitmapDescriptorFactory.fromResource(R.drawable.ic_salida)
                                )
                            ) //representar la ubicacion guardada con un marcador con icono personalizado

                            binding.posInicio.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_gps_find,
                                0
                            ) // cambiar el icono al final del boton cuando se guarda la ubicacion
                            binding.posInicio.text = "Partida   "

                            marcador.remove() //borrar el marcador inicial (solo queda el de la ubicacion guardada)
                        }

                        // si las dos ubicaciones han sido obtenidas (inicio - destino) comenzara a calcularse la ruta
                        if (ubiInicio != LatLng(0.0, 0.0) && ubiDestino != LatLng(0.0, 0.0)) {
                            PlanearRutaDestino(this, gmap).rutaToDestino(
                                ubiInicio,
                                ubiDestino,
                                this
                            )
                            binding.infoColor.visibility = View.VISIBLE

                            //si la distancia hasta el punto salida es menor a 200 m mostrar indicaciones calculadas
                            mostrarIndicacionesCalculadas(false)

                            //cambiar iconos de ubicacion inicio y fin en boton por checks verdes
                            binding.posInicio.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_check,
                                0
                            )
                            binding.posDestino.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_check,
                                0
                            )
                        }
                    }
                }

                //--------------------------------------------------------------------
                //boton guardar ubicacion de destino
                binding.posDestino.setOnClickListener {
                    UtilidadesMenores().crearToast(this, "Arrastra la pantalla")
                    binding.posDestino.isClickable =
                        false //despues de oprimido no se puede presionar de nuevo

                    Datos.mejorPuntoaDestino[0] = -1 //inicializa variable "global" en -1
                    Datos.mejorPuntoaDestino[1] =
                        Int.MAX_VALUE // inicializa la distancia con un valor alto
                    Datos.mejorPuntoaDestino[2] = -1

                    //agregar marcador al centro de la pantalla - apuntador para seleccionar ubicacion--------
                    val centerMarker = MarkerOptions()
                    centerMarker.position(
                        LatLng(
                            5.329894555473376,
                            -72.40242298156761
                        )
                    ) //misma ubicacion inicial de la camara
                    marcador = gmap.addMarker(centerMarker)!!
                    binding.setUbicacion.visibility = View.VISIBLE
                    gmap.setOnCameraMoveListener { //listener de la posicion de la camara, actualizara la posicion
                        marcador.position = gmap.cameraPosition.target
                    }

                    //guardar la ubicacion y representarla con un marcador
                    binding.setUbicacion.setOnClickListener {
                        binding.setUbicacion.visibility = View.GONE

                        ubiDestino = LatLng(marcador.position.latitude, marcador.position.longitude)
                        gmap.addMarker(
                            MarkerOptions().position(ubiDestino).title("Punto Destino").icon(
                                BitmapDescriptorFactory.fromResource(R.drawable.ic_destino)
                            )
                        ) //agregar un marcador con la ubicacion guardada de destino y con icono personalizado

                        binding.posDestino.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_gps_find,
                            0
                        )//cambiar el icono al final del boton al guardar ubicacion
                        binding.posDestino.text = "Destino   "

                        marcador.remove() //quitar marcador central en pantalla, del mapa

                        //si las dos ubicaciones (inicio - destino) fueron obtenidas entonces calcule la ruta
                        if (ubiInicio != LatLng(0.0, 0.0) && ubiDestino != LatLng(0.0, 0.0)) {
                            PlanearRutaDestino(this, gmap).rutaToDestino(
                                ubiInicio,
                                ubiDestino,
                                this
                            )
                            binding.infoColor.visibility = View.VISIBLE

                            //si la distancia hasta el punto salida es menor a 300 m mostrar indicaciones calculadas

                            mostrarIndicacionesCalculadas(false)


                            //cambiar iconos de ubicacion inicio y fin en boton por checks verdes
                            binding.posInicio.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_check,
                                0
                            )
                            binding.posDestino.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_check,
                                0
                            )
                        }
                    }
                }
            }

            //--------------------------------------------------------------------------------------
            //--------------------------------------------------------------------------------------
            //cuando 20 se vea el mapa con todas las rutas
            20 -> {
                //identificar estado mapa - tarea
                tareaActual = "Mapa con todas las rutas"
                //---------------------------------

                supportActionBar?.title = "Ver Mapa con Rutas"
                binding.infoColor.visibility = View.GONE
                binding.listaRutasOpMapa.visibility = View.VISIBLE


                //mostrar info relacionada
                binding.indicaciones.visibility = View.VISIBLE
                binding.indicaciones.text =
                    "Compara cualquier recorrido de las rutas habilitadas con tu posición en el mapa, presiona el boton gps, elige 'VER RUTA' y aleja el mapa."


                // Obtener la referencia del layout inflado
                val listaOpMapa = binding.listaOpMapa
                val listaViewRutas = binding.espacioMapaUtil

                listaViewRutas.visibility = View.VISIBLE

                listaOpMapa.visibility = View.VISIBLE
                val listaRutas = intArrayOf(2, 3, 6, 7, 8, 9, 10, 13)
                val listaRutasOpMapa = mutableListOf<DatoOpMapa>()
                for (i in 0..listaRutas.size - 1) {
                    listaRutasOpMapa.add(
                        DatoOpMapa(
                            listaRutas[i],
                            0,
                            R.color.ida_venida_op_mapa,
                            R.color.ida_venida_op_mapa
                        )
                    )
                }

                // Configurar el LinearLayoutManager y el Adapter

                listaOpMapa.layoutManager = LinearLayoutManager(this)
                listaOpMapa.adapter = OpMapaAdapterHolder(listaRutasOpMapa, gmap, this)

                binding.listaRutasOpMapa.setOnClickListener {
                    if (!listaViewRutas.isVisible) {
                        listaViewRutas.visibility = View.VISIBLE
                    } else {
                        listaViewRutas.visibility = View.GONE
                    }
                }

            }

            //--------------------------------------------------------------------------------------
            //--------------------------------------------------------------------------------------
            //cuando 40 se vea el mapa con parqueaderos
            40 -> {
                //identificar estado mapa - tarea
                tareaActual = "Mostrando mapa con parqueaderos"
                //---------------------------------

                supportActionBar?.title = "Ver Mapa con Parqueaderos"
                binding.infoColor.visibility = View.GONE

                //mostrar info relacionada
                binding.indicaciones.visibility = View.VISIBLE
                binding.indicaciones.text =
                    "Los puntos rojos son parqueaderos, toca cualquiera de ellos para saber a que ruta pertenecen."
                val listaRutas = intArrayOf(2, 3, 6, 7, 8, 9, 10, 13)
                for (i in 0..listaRutas.size - 1) {
                    val iterator = listaRutas[i]
                    val dbHelper = DatosASqliteLocal(this)
                    val datosSeleccionRuta = dbHelper.obtenerCoordenadas(iterator, "coordenadas2")
                    agregarMarcador(
                        datosSeleccionRuta[datosSeleccionRuta.size - 1],
                        R.drawable.ic_parqueadero,
                        "Parqueadero Ruta $iterator"
                    )
                }
            }

            //crear las rutas normales dependiendo de la elegida por el usuario
            //se identifica por un id_ruta que se envia desde la pantalla principal por medio del holder y usando intent.extras
            in listOf(2, 3, 6, 7, 8, 9, 10, 13) -> buildRuta.crearRuta(idruta)
        }
    }

    private fun activarGps() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!permisoUbiCondecido()) {
            UtilidadesMenores().crearToast(
                this,
                "Permiso de ubicación no permitido--Activalo en ajustes."
            )
            binding.irgps.setImageResource(R.drawable.ic_gps_off)
        }
        if (!::gmap.isInitialized) return
        if (!UtilidadesMenores().comprobarConexion(this)) {
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

                            // El usuario rechazó la solicitud de activación del GPS
                            UtilidadesMenores().crearToast(
                                this,
                                "Debes activar el GPS para usar esta función."
                            )
                            binding.irgps.setImageResource(R.drawable.ic_gps_off)
                        }
                    }
                }
                irPosGps()
            } else { //si ya esta activado el gps entonces ir a la posicion
                binding.irgps.setImageResource(R.drawable.ic_gps_find)
                irPosGps()
            }
        }

    }

    private fun irYopal() {
        //mayor zoom si se ve parqueaderos (opcion)
        val zoom = if (idruta == 40) {
            12f
        } else {
            14f
        }

        val cameraPosicion = CameraPosition.Builder()
            .target(LatLng(5.329894555473376, -72.40242298156761))
            .zoom(zoom)
            .build()
        gmap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosicion))
    }

    fun irPosGps() {
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
                ubiUser = latLng
                binding.irgps.setImageResource(R.drawable.ic_gps_find)
                val zoomLevel = 16.5f // Nivel de zoom deseado

                val cameraPosition = CameraPosition.Builder()
                    .target(latLng) // Coordenadas del centro del mapa
                    .zoom(zoomLevel) // Nivel de zoom
                    .build()

                val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)

                gmap.animateCamera(cameraUpdate, 2000, null)

                if (binding.sentidoSubida.visibility != View.VISIBLE && idruta != 20 && idruta != 40 && idruta != 0) { //diferente de 0, 20 y 40 para evitar la activacion del distancia a recorrido en opcion mostrar mapa con rutas, calcular ruta y parqueaderos
                    binding.verDistancia.visibility = View.VISIBLE
                }

                //marcador en ubi de respaldo
                val accuracyRadius = 12.0
                val opcionesCirculo = CircleOptions()
                    .center(latLng)
                    .radius(accuracyRadius)
                    .strokeWidth(2f)
                    .strokeColor(R.color.RutaEnServicio)
                    .fillColor(0x551cfc03)
                puntoProvisionalGps = if (puntoProvisionalGps == null) {
                    gmap.addCircle(opcionesCirculo)
                } else {
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

    fun activarLocalizacion() { //pide permiso de localicaion
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            activarGps()
            gmap.isMyLocationEnabled = true
        } else {
            //esta es la solicitud del permiso definido por la variable permission
            requestLocationPermissionLauncher.launch(permission)
        }
        ActivityCompat.requestPermissions(this, arrayOf(permission), codigoLocalizacion)
    }


    //esta es la respuesta a la solicitud de permiso
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->


        if (isGranted) {//el permiso de localizacion fue concedido, activar el gps
            activarGps()
        } else {
            //el permiso fue negado

            contador++ // omitir el primer click del usuario al boton gps
            if (contador == 2) {
                contador = 0
                //mostrar alerta sobre persmiso denegado y dar opcion de activarlo desde ajustes -- se usa devolucion de llamada a onOpcionSeleccionada()
                UtilidadesMenores().crearAlerta(
                    this,
                    "ubicacion",
                    "Permiso de localización ha sido denegado.\n\nPermite que Busetas Yopal tenga permiso de ubicación desde ajustes para aprovechar la aplicación al maximo!.",
                    "Aceptar",
                    "Ajustes",
                    this
                )
            }
        }

    }


    private fun msjNoConection() {
        runOnUiThread {
            binding.failConection.visibility = View.VISIBLE
            if (idruta == 0) {
                binding.failConection.text = Html.fromHtml(
                    "<font color='${getColor(R.color.anuncioGrave)}'>¡En este momento no tienes conexión a Internet, es posible que el mapa no se cargue correctamente y no puedas calcular ruta!</font>",
                    Html.FROM_HTML_MODE_LEGACY
                )
            } else {
                binding.failConection.text = Html.fromHtml(
                    "<font color='${getColor(R.color.anuncioGrave)}'>¡En este momento no tienes conexión a Internet, es posible que el mapa no se cargue correctamente!</font>",
                    Html.FROM_HTML_MODE_LEGACY
                )
            }
            tiempos.postDelayed({ // terminar o ejecutar tareas despues de cierto tiempo
                if (PolylinesPrincipal.CreatRuta.rutasCreadas) {
                    binding.failConection.visibility = View.GONE

                } else {
                    binding.failConection.visibility = View.VISIBLE
                    if (idruta == 0) {
                        binding.failConection.text = Html.fromHtml(
                            "<font color='${getColor(R.color.anuncioLeve)}'>Sin conexión a Internet - Conéctate</font>",
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    } else {
                        binding.failConection.text = Html.fromHtml(
                            "<font color='${getColor(R.color.anuncioLeve)}'>Sin conexión a Internet</font>",
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    }
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
            PolylinesPrincipal(contexto, this.gmap).rutaMasCerca(ubiUser, sentido)
        } else {
            activarLocalizacion()
        }
    }


    private fun mostrarIndicaciones() {
        //indicaciones para tomar la buseta en las rutas individuales
        val metros = PolylinesPrincipal.CreatRuta.masCortaInicio[1]
        //val punto = PolylinesPrincipal.CreatRuta.masCortaInicio[0]
        binding.indicaciones.visibility = View.VISIBLE
        "Camina $metros m hasta el icono y toma la buseta (ruta $idruta).".also {
            binding.indicaciones.text = it
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        //liberar memoria del mapa
        FragmentMap.onDestroy()
        println("Fragmento mapa fue liberado-finalizado")

        //desregistrar listener de la conexion a internet
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
        println("Escucha a internet (Pant Mapa) desactivado-finalizado")
    }

    override fun onLocationChanged(p0: Location) {
        val ubiGps = LatLng(p0.latitude, p0.longitude)
        gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubiGps, 20f), 3000, null)
        irPosGps()
    }

    override fun onOpcionSeleccionada(
        opcion: Int,
        tipo_de_solicitud: String
    ) { //devolucion de llamada de la opcion seleccionada en UtilidadesMenores.CrearAlerta()
        if (tipo_de_solicitud == "permiso_ubicacion" && opcion == 1) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
    }

    private fun agregarMarcador(punto: LatLng, icono: Int, titulo: String): Marker? {
        //FUNCION AÑADIR MARCADOR AL MAPA
        val opcionesMarcador = MarkerOptions()
            .position(punto).icon(BitmapDescriptorFactory.fromResource(icono))
            .title(titulo)

        return gmap.addMarker(opcionesMarcador)
    }

    fun mostrarIndicacionesCalculadas(hayImpedimento: Boolean) {
        val numeroRuta = Datos.mejorPuntoaInicio[2]
        //si la distancia hasta el punto salida es menor a 300 m mostrar indicaciones calculadas
        if (Datos.mejorPuntoaInicio[1] > 200 || hayImpedimento) {
            println("Distancia: ${Datos.mejorPuntoaInicio[1]}")
            //mostrar mensaje "no se pudo generar recorrido"
            binding.msjNoSeEncontroRuta.visibility = View.VISIBLE
            binding.opcionesNoCalculada.text = "\n-Se esta mostrando la ruta: $numeroRuta"
        } else {
            //mostrar indicaciones
            binding.indicacionesCalcular.visibility = View.VISIBLE
            binding.indicacion1.text =
                " Camina desde tu posicion hasta tomar la ruta $numeroRuta."
            binding.indicacion2.text =
                " Haz el recorrido para bajarte en el punto marcado."
            binding.indicacion3.text = " Camina hasta el punto de destino."
        }

    }

    private fun cargarAnuncios() {
        //anuncios
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    //inflar menu mapa opciones actionbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_mapa, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //variable para identificar la opcion entorno al reporte
        val opcionActual = tareaActual

        //opcion reportar
        when (item.itemId) {
            R.id.reportar -> {
                UtilidadesMenores().reportar(this, this, opcionActual)
            }
        }

        return true
    }

}