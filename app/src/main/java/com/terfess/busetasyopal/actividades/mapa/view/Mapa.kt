package com.terfess.busetasyopal.actividades.mapa.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
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
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.terfess.busetasyopal.OpMapaAdapterHolder
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.clases_utiles.AlertaCallback
import com.terfess.busetasyopal.actividades.mapa.functions.MapFunctionOptions
import com.terfess.busetasyopal.actividades.mapa.functions.calculate_route.CalculateRoute
import com.terfess.busetasyopal.actividades.mapa.functions.calculate_route.adapterholder.AdapterHolderCalculates
import com.terfess.busetasyopal.actividades.mapa.viewmodel.ViewModelMapa
import com.terfess.busetasyopal.clases_utiles.PolylinesPrincipal
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.PantMapaBinding
import com.terfess.busetasyopal.enums.MapRouteOption
import com.terfess.busetasyopal.enums.RoomTypePath
import com.terfess.busetasyopal.enums.UserTask
import com.terfess.busetasyopal.enums.getRouteOnTask
import com.terfess.busetasyopal.modelos_dato.DatoOpMapa
import com.terfess.busetasyopal.room.AppDatabase
import com.terfess.busetasyopal.room.model.Coordinate
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Mapa : AppCompatActivity(), LocationListener, OnMapReadyCallback, AlertaCallback {
    private lateinit var binding: PantMapaBinding
    private lateinit var gmap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var idruta: Int = 0
    private lateinit var typeOptMap: MapRouteOption

    private var walkData: CalculateRoute.WalkRoute? = null

    private lateinit var marcador: Marker
    private lateinit var mAdView: AdView //anuncios
    private lateinit var tareaActual: String //que esta haciendo el mapa?

    private lateinit var fragmentMap: SupportMapFragment
    private var functionsInstance = MapFunctionOptions()

    private var contador = 0
    private val viewModel: ViewModelMapa by viewModels()

    private var hayConexion = true

    private var mInterstitialAd: InterstitialAd? = null
    private final val TAG = "MainActivity"

    companion object { //accesibles desde cualquier lugar de este archivo/clase y proyectos
        const val codigoLocalizacion = 0
        var ubiUser = LatLng(0.0, 0.0)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PantMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setActionBar()

        //seleccion ruta
        intent.extras.let {
            idruta = it!!.getInt("num_route")

            val typeOptionString = it.getString("type_option")
            typeOptMap = if (typeOptionString != null) {
                enumValueOf<MapRouteOption>(typeOptionString)  // Convert from String a enum
            } else {
                MapRouteOption.SIMPLE_ROUTE
            }
        }


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

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                runOnUiThread {
                    binding.failConection.visibility = View.GONE
                }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onStart() {
        super.onStart()
        //mapa
        fragmentMap = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        fragmentMap.getMapAsync(this)

        cargarAnuncios()

        //ubicacion del gps
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private lateinit var sharedPreferences: SharedPreferences

    override fun onMapReady(mapa: GoogleMap) {
        gmap = mapa

        gmap.uiSettings.setAllGesturesEnabled(false)

        // Progressbar while map loading
        gmap.setOnMapLoadedCallback {
            binding.loadingMapa.visibility = View.GONE
            gmap.uiSettings.setAllGesturesEnabled(true)
        }

        // Type map
        val saveTypeMap = UtilidadesMenores().getSavedTypeMap(this)
        gmap.mapType = saveTypeMap
        //..

        // Initialize current task
        tareaActual = getRouteOnTask(idruta)

        irYopal() //colocar la camara en la ciudad de yopal

        selector()  //seleccionar que ruta cargar

        //pedir/comprobar permiso ubicacion y gps On
        binding.irgps.setOnClickListener {
            requestLocationPermission()
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

                val dbRoom = AppDatabase.getDatabase(this)

                CoroutineScope(Dispatchers.IO).launch {
                    val ptsSentido = dbRoom.coordinateDao()
                        .getCoordRoutePath(
                            idruta,
                            RoomTypePath.DEPARTURE.toString()
                        )

                    withContext(Dispatchers.Main) {
                        calcularDistancia(RoomTypePath.DEPARTURE, ptsSentido)
                    }
                }
            }
        }

        binding.sentidoLlegada.setOnClickListener {
            if (binding.infoColor.isVisible) {
                binding.infoColor.visibility = View.GONE //ocultar leyenda al ver distancia
            }
            if (PolylinesPrincipal.CreatRuta.estamarcado2 == false) {
                val dbRoom = AppDatabase.getDatabase(this)
                CoroutineScope(Dispatchers.IO).launch {
                    val ptsSentido = dbRoom.coordinateDao()
                        .getCoordRoutePath(
                            idruta,
                            RoomTypePath.RETURN.toString()
                        )

                    withContext(Dispatchers.Main) {
                        calcularDistancia(RoomTypePath.RETURN, ptsSentido)
                    }
                }
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
                binding.containBtnCalculates.visibility = View.GONE
            }
        }

        //..
        val nameShared = getString(R.string.nombre_shared_preferences)
        val sharedPreferences = getSharedPreferences(nameShared, Context.MODE_PRIVATE)

        val mapTypeSpinner = binding.opcionesTipoMapa

        val selectedIndex = UtilidadesMenores().getIndexTypeMap(this)

        if (selectedIndex >= 0) {
            mapTypeSpinner.setSelection(selectedIndex)
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
                            val typeCode = GoogleMap.MAP_TYPE_NORMAL
                            with(sharedPreferences.edit()) {
                                putInt("type_map_user", typeCode)
                                apply()
                            }
                            gmap.mapType = typeCode //set tipo de mapa normal
                        }

                        "Mapa Hybrido" -> {
                            val typeCode = GoogleMap.MAP_TYPE_HYBRID
                            with(sharedPreferences.edit()) {
                                putInt("type_map_user", typeCode)
                                apply()
                            }
                            gmap.mapType = typeCode //set tipo de mapa hybrido
                        }

                        "Mapa Satelital" -> {
                            val typeCode = GoogleMap.MAP_TYPE_SATELLITE
                            with(sharedPreferences.edit()) {
                                putInt("type_map_user", typeCode)
                                apply()
                            }
                            gmap.mapType = typeCode //set tipo de mapa satelital
                        }

                        "Mapa Relieve" -> {
                            val typeCode = GoogleMap.MAP_TYPE_TERRAIN
                            with(sharedPreferences.edit()) {
                                putInt("type_map_user", typeCode)
                                apply()
                            }
                            gmap.mapType = typeCode //set tipo de mapa relieve
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

            if (typeOptMap == MapRouteOption.ALL_ROUTES) binding.listaRutasOpMapa.visibility =
                View.VISIBLE

            if (typeOptMap == MapRouteOption.CALCULATE_ROUTE_USER) binding.containBtnCalculates.visibility =
                View.VISIBLE

            binding.irgps.visibility = View.VISIBLE
        }

    }

    private fun selector() {

        when (typeOptMap) {

            MapRouteOption.SIMPLE_ROUTE -> {
                val buildRuta = PolylinesPrincipal(this, gmap)

                supportActionBar?.subtitle = "Camino de salida y de llegada"
                supportActionBar?.title = "Recorrido Ruta $idruta"

                buildRuta.crearRuta(idruta)
            }

            MapRouteOption.CALCULATE_ROUTE_USER -> {

                intersticialAdRequest()

                //identificar estado mapa - tarea
                tareaActual = UserTask.USER_IS_IN_OPTION_CALCULATE_ROUTE_MAP.messageValue
                //---------------------------------
                supportActionBar?.title =
                    getString(R.string.calcular_viaje)
                supportActionBar?.subtitle = "Elíge un inicio y un destino"

                // Show/Hide elements
                binding.infoColor.visibility = View.GONE
                binding.textAlgoritmVersion.visibility = View.VISIBLE

                var ubiInicio = LatLng(0.0, 0.0)
                var ubiDestino = LatLng(0.0, 0.0)

                //mostrar botones para guardar ubicacion inicio - destino
                binding.posInicio.visibility = View.VISIBLE
                binding.posDestino.visibility = View.VISIBLE

                //ocultar boton gps y leyenda
                binding.infoColor.visibility = View.GONE

                alertDialogCalculate()

                binding.posInicio.setOnClickListener {
                    UtilidadesMenores().crearToast(this, "Arrastra la pantalla")

                    binding.posInicio.isClickable =
                        false // Despues de seleccionado se desactiva el boton

                    binding.posDestino.isClickable =
                        false // Despues de seleccionado se desactiva el otro boton

                    //marcador en el centro de la pantalla - apuntador para definir ubicacion---------------------
                    val centerMarker = functionsInstance.getOptionsMarker(
                        LatLng(
                            5.329894555473376,
                            -72.40242298156761
                        ),
                        null,
                        "Seleccionar Inicio"
                    )
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

                        ubiInicio = marcador.position

                        val ptoMarker = functionsInstance.getOptionsMarker(
                            ubiInicio,
                            R.drawable.ic_salida,
                            "Punto de Partida"
                        )

                        gmap.addMarker(ptoMarker) //representar la ubicacion guardada con un marcador con icono personalizado

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
                            CoroutineScope(Dispatchers.IO).launch {
                                viewModel.calculateRoute(
                                    ubiInicio,
                                    ubiDestino,
                                    context = this@Mapa
                                )
                            }

                            binding.progressCalculando.visibility = View.VISIBLE
                            binding.infoColor.visibility = View.GONE

                            //si la distancia hasta el punto salida es menor a 200 m mostrar indicaciones calculadas
//                            mostrarIndicacionesCalculadas(false)

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
                        }else{
                            binding.posDestino.isClickable =
                                true // Si no estan seleccionadas las dos ubis activese el otro boton
                        }
                    }

                    //guardar la ubicacion y representarla con un marcador
                    binding.setUbiGps.setOnClickListener {
                        binding.setUbiGps.visibility = View.GONE
                        UtilidadesMenores().crearToast(this, "Localizando...")

                        requestLocationPermission()
                        getPosGps()

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
                            CoroutineScope(Dispatchers.IO).launch {
                                viewModel.calculateRoute(
                                    ubiInicio,
                                    ubiDestino,
                                    context = this@Mapa
                                )
                            }
                            binding.progressCalculando.visibility = View.VISIBLE
                            binding.infoColor.visibility = View.GONE

                            //si la distancia hasta el punto salida es menor a 200 m mostrar indicaciones calculadas
//                            mostrarIndicacionesCalculadas(false)

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
                        } else {
                            binding.posDestino.isClickable =
                                true // Si no estan seleccionadas las dos ubis activese el otro boton
                        }
                    }
                }

                //--------------------------------------------------------------------
                //boton guardar ubicacion de destino
                binding.posDestino.setOnClickListener {
                    UtilidadesMenores().crearToast(this, "Arrastra la pantalla")

                    binding.posDestino.isClickable =
                        false // Despues de oprimido no se puede presionar de nuevo

                    binding.posInicio.isClickable =
                        false // Despues de oprimido no se puede presionar el otro boton

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

                            binding.progressCalculando.visibility = View.VISIBLE
                            CoroutineScope(Dispatchers.IO).launch {
                                viewModel.calculateRoute(
                                    ubiInicio,
                                    ubiDestino,
                                    context = this@Mapa
                                )
                            }

                            binding.infoColor.visibility = View.VISIBLE

                            //si la distancia hasta el punto salida es menor a 300 m mostrar indicaciones calculadas

//                            mostrarIndicacionesCalculadas(false)


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
                        } else {
                            binding.posInicio.isClickable =
                                true // Si no estan seleccionadas las dos ubis activese el otro boton
                        }
                    }
                }
            }

            MapRouteOption.ALL_ROUTES -> {
                //identificar estado mapa - tarea
                tareaActual = UserTask.USER_IS_IN_OPTION_ALL_ROUTES_MAP.messageValue
                //---------------------------------

                supportActionBar?.subtitle = "Todas las rutas habilitadas"
                supportActionBar?.title = getString(R.string.ver_mapa_con_las_rutas)

                binding.infoColor.visibility = View.GONE
                binding.listaRutasOpMapa.visibility = View.VISIBLE


                //mostrar info relacionada
                binding.indicaciones.visibility = View.VISIBLE
                binding.indicaciones.text =
                    getString(R.string.compara_cualquier_recorrido_de_las_rutas_habilitadas_con_tu_posici_n_en_el_mapa_presiona_el_boton_gps_elige_ver_ruta_y_aleja_el_mapa)


                //--------------RECICLERVIEW-------------------------------
                // Obtener la referencia del layout bottomsheet inflado

                // Crear y configurar el BottomSheetDialog
                val btnSheetDialog = BottomSheetDialog(this, R.style.Theme_BottomSheetTheme)
                val btnSheetLayout =
                    layoutInflater.inflate(R.layout.btn_sheet_list_traces_routes, null, false)
                btnSheetDialog.setContentView(btnSheetLayout)

                // Obtener la vista del BottomSheet
                val bottomSheet =
                    btnSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

                BottomSheetBehavior.from(bottomSheet!!)

                val height = UtilidadesMenores().getScreenPercentDp(this, 0.30)

                bottomSheet.layoutParams.height = height
                bottomSheet.requestLayout()

                val listaOpMapa = btnSheetLayout.findViewById<RecyclerView>(R.id.listaOpMapa)

                val dbHelper = AppDatabase.getDatabase(this)

                val listaRutasOpMapa = mutableListOf<DatoOpMapa>()

                CoroutineScope(Dispatchers.IO).launch {
                    val listaRutas = dbHelper.routeDao().getAllIdsRoute()


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
                }

                // Configurar el LinearLayoutManager y el Adapter
                listaOpMapa.layoutManager = LinearLayoutManager(this)
                listaOpMapa.adapter = OpMapaAdapterHolder(listaRutasOpMapa, gmap, this)

                btnSheetDialog.show()


                binding.listaRutasOpMapa.setOnClickListener {
                    toggleBottomSheetVisibility(btnSheetDialog)
                }

            }

            MapRouteOption.PARKING_ROUTES -> {
                // Set info current task option
                tareaActual = UserTask.USER_IS_IN_OPTION_PARKINGS_ROUTES_MAP.messageValue

                // Hide/Show option elements
                supportActionBar?.subtitle = "Parqueaderos de donde sale la buseta"
                supportActionBar?.title =
                    getString(R.string.ver_mapa_con_parqueaderos)

                binding.infoColor.visibility = View.GONE

                binding.indicaciones.visibility = View.VISIBLE
                binding.indicaciones.text =
                    getString(R.string.los_puntos_rojos_son_parqueaderos_toca_cualquiera_de_ellos_para_saber_a_que_ruta_pertenecen)

                // Add Markers as route parking points
                MapFunctionOptions().addParkingPoints(gmap, this)
            }
        }
    }

    private fun intersticialAdRequest() {
        val adRequest = AdRequest.Builder().build()
        //TODO: cambiar antes de subir actualizacion
        val keyAd = getString(R.string.fake_key_intersticial)

        InterstitialAd.load(this, keyAd, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.toString())
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
                mInterstitialAd?.show(this@Mapa)
            }
        })

        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                mInterstitialAd = null
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.")
                mInterstitialAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }

    }

    private fun alertDialogCalculate() {
        // Crear el BottomSheetDialog
        val btnSheetDia = BottomSheetDialog(this, R.style.Theme_BottomSheetTheme)
        val btnSheetLayout = layoutInflater.inflate(R.layout.btn_sheet_calculate_route, null)
        btnSheetDia.setContentView(btnSheetLayout)

        // Configurar el BottomSheet
        setupBottomSheet(btnSheetDia)

        // Configurar el RecyclerView
        val recy = setupRecyclerView(btnSheetLayout)

        // Observar los resultados de cálculo
        observeCalculateResults(btnSheetDia, recy, btnSheetLayout)

        // Mostrar/Ocultar el BottomSheet al hacer clic en el botón
        binding.hideShowRutasCalculadas.setOnClickListener {
            toggleBottomSheetVisibility(btnSheetDia)
        }
    }

    // Configura el BottomSheet y establece su altura
    private fun setupBottomSheet(btnSheetDia: BottomSheetDialog) {
        val bottomSheet =
            btnSheetDia.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            it.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.requestLayout()

            // Establecer altura máxima como 40% de la pantalla
            val height = UtilidadesMenores().getScreenPercentDp(this, 0.35)
            BottomSheetBehavior.from(it).maxHeight = height
        }
    }

    // RecyclerView del BottomSheet Calculate route
    private fun setupRecyclerView(btnSheetLayout: View): RecyclerView? {
        val recycler = btnSheetLayout.findViewById<RecyclerView>(R.id.recyclerCalculates)
        val listaRoutes = mutableListOf<CalculateRoute.RouteCalculate>()

        val adapter = AdapterHolderCalculates(listaRoutes, this, gmap, functionsInstance)

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        return recycler
    }

    // Observa los resultados del cálculo de rutas
    private fun observeCalculateResults(
        btnSheetDia: BottomSheetDialog, recycler: RecyclerView?,
        layoutSheet: View
    ) {
        viewModel.resultCalculate.observe(this, Observer {
            // Ocultar el progreso cuando hay resultados
            binding.progressCalculando.visibility = View.GONE

            val containmessage =
                layoutSheet.findViewById<LinearLayout>(R.id.messageNotCalculates)

            if (it.resultInfo) {
                // Actualizar la lista en el adapter y mostrar el BottomSheet

                (recycler?.adapter as AdapterHolderCalculates).notyList(it.dataResult)
                btnSheetDia.show()

                recycler.visibility = View.VISIBLE
                containmessage.visibility = View.GONE
            } else {
                recycler?.visibility = View.GONE

                containmessage.visibility = View.VISIBLE

                btnSheetDia.show()
            }

            // Mostrar/ocultar los botones correspondientes
            binding.containBtnCalculates.visibility = View.VISIBLE
            binding.containBtnsPos.visibility = View.GONE
        })
    }


    private fun toggleBottomSheetVisibility(btnSheetDia: BottomSheetDialog) {
        if (!btnSheetDia.isShowing) {
            btnSheetDia.show()
        } else {
            btnSheetDia.hide()
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
            //ocultar progress de localizacion
            binding.progLocalizando.visibility = View.GONE
        }
        if (!::gmap.isInitialized) return
        if (!UtilidadesMenores().comprobarConexion(this)) {
            activarGpsTelefono()
            getPosGps()
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

                            binding.irgps.setImageResource(R.drawable.ic_gps)
                            //ocultar progress de localizacion
                            binding.progLocalizando.visibility = View.VISIBLE

                            getPosGps()
                        } catch (sendEx: IntentSender.SendIntentException) {
                            //algun error
                            binding.irgps.setImageResource(R.drawable.ic_gps_off)
                            //ocultar progress de localizacion
                            binding.progLocalizando.visibility = View.GONE
                        }
                    }
                }
                getPosGps()
            } else { //si ya esta activado el gps entonces ir a la posicion
                getPosGps()
            }

            // verificar luegod e 10 segundos si se activo el gps
            CoroutineScope(Dispatchers.Main).launch {
                delay(10000) // Esperar 10 segundos
                // Realizar verificación de ubi
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    println("Al parecer no se acepto activar el gps")
                    binding.irgps.setImageResource(R.drawable.ic_gps_off)
                    //ocultar progress de localizacion
                    binding.progLocalizando.visibility = View.GONE
                } else {
                    println("Al parecer si se acepto activar el gps")
                }
            }
        }

    }

    private fun irYopal() {
        functionsInstance.moveCameraMap(
            14f,
            gmap,
            LatLng(5.329894555473376, -72.40242298156761)
        )
    }

    fun getPosGps() {
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

                //boton irgps
                binding.irgps.setImageResource(R.drawable.ic_gps_find)
                //ocultar progress de localizando
                binding.progLocalizando.visibility = View.GONE

                val zoomLevel = 16.5f // Nivel de zoom deseado

                functionsInstance.animateCameraMap(
                    zoomLevel,
                    gmap,
                    latLng
                )

                // show btn for see distance to if the option is simple route
                if (binding.sentidoSubida.visibility != View.VISIBLE &&
                    typeOptMap == MapRouteOption.SIMPLE_ROUTE
                ) {
                    binding.verDistancia.visibility = View.VISIBLE
                }

                gmap.isMyLocationEnabled = true
            } else {
                getPosGps()
            }
        }
    }


    //verificar si tiene permiso de ubicacion
    private fun permisoUbiCondecido() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun requestLocationPermission() { //pide permiso de localicaion
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            activarGps()
            gmap.isMyLocationEnabled = true
        } else {
            // If it are not granted then do request
            requestLocationPermissionLauncher.launch(permission)
        }
        ActivityCompat.requestPermissions(this, arrayOf(permission), codigoLocalizacion)
    }


    @SuppressLint("MissingPermission")
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) { // Location permission is granted
            activarGps()
            gmap.isMyLocationEnabled = true
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

            val tiempos = Handler(Looper.getMainLooper())
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
                //mostrar progress de localizacion
                binding.progLocalizando.visibility = View.VISIBLE

                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            alertDialog.setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
            alertDialog.show()
            getPosGps()
        }
    }

    private fun calcularDistancia(sentido: RoomTypePath, ptsSentido: List<Coordinate>) {
        val defecto = LatLng(0.0, 0.0)

        if (ubiUser != defecto) {
            val pts = UtilidadesMenores().extractCoordToLatLng(
                ptsSentido,
                sentido.toString(),
                idruta
            )

            val dato = functionsInstance
                .rutaMasCerca(ubiUser, pts)

            // clean lasts
            if (walkData != null) {
                walkData?.lineWalk?.remove()
                walkData?.marker?.remove()
            }

            if (dato.idPunto in pts.indices) {
                pts[dato.idPunto].let { punto ->
                    CoroutineScope(Dispatchers.Main).launch {
                        walkData = functionsInstance.traceWalkRoute(
                            ubiUser,
                            punto,
                            gmap,
                            this@Mapa,
                            true,
                            addMarker = true
                        )
                    }

                    functionsInstance.animateCameraMap(
                        17f,
                        gmap,
                        punto
                    )
                }
            }


            mostrarIndicaciones(dato.distancia)
        } else {
            requestLocationPermission()
        }
    }


    private fun mostrarIndicaciones(
        distance: Int
    ) {
        binding.indicaciones.visibility = View.VISIBLE
        "Camina $distance m hasta el icono y toma la buseta (ruta $idruta).".also {
            binding.indicaciones.text = it
        }

    }

    override fun onLocationChanged(p0: Location) {

        val ubiGps = LatLng(p0.latitude, p0.longitude)
        functionsInstance.animateCameraMap(
            12f,
            gmap,
            ubiGps
        )
        println("Ubicacion esta cambiando - $ubiGps")
        getPosGps()
    }

    override fun onOpcionSeleccionada(
        opcion: Int,
        tipeSoli: String
    ) { //devolucion de llamada de la opcion seleccionada en UtilidadesMenores.CrearAlerta()
        if (tipeSoli == "permiso_ubicacion" && opcion == 1) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
    }

    private fun cargarAnuncios() {
        CoroutineScope(Dispatchers.IO).launch {
            // Ad request
            val adRequest = AdRequest.Builder().build()

            withContext(Dispatchers.Main) {
                mAdView = findViewById(R.id.adViewMap)
                mAdView.loadAd(adRequest)
            }
        }
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

            android.R.id.home -> {
                onBackPressed()
            }
        }

        return true
    }

    private fun setActionBar() {
        //actionbar
        setSupportActionBar(binding.toolbarMap)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        val themeColor = UtilidadesMenores().getColorHambugerIcon()
        binding.toolbarMap.navigationIcon?.setTint(ContextCompat.getColor(this, themeColor))

    }

    override fun onResume() {
        super.onResume()
        fragmentMap.onResume()
    }

    override fun onPause() {
        super.onPause()
        fragmentMap.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        fragmentMap.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        fragmentMap.onDestroy()
        finish()
    }
}