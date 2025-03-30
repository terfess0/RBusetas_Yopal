package com.terfess.busetasyopal.actividades

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.ads.AdView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.terfess.busetasyopal.FiltroAdapterHolder
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.AdapterPrincipal
import com.terfess.busetasyopal.actividades.mapa.view.Mapa
import com.terfess.busetasyopal.actividades.reports.view.ReportsUser
import com.terfess.busetasyopal.actividades.shoping.ShopScreen
import com.terfess.busetasyopal.clases_utiles.AlertaCallback
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.PantPrincipalBinding
import com.terfess.busetasyopal.enums.MapRouteOption
import com.terfess.busetasyopal.modelos_dato.DatosPrimariosRuta
import com.terfess.busetasyopal.room.AppDatabase
import com.terfess.busetasyopal.services.workers.ListenerNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit


class PantallaPrincipal : AppCompatActivity(), AlertaCallback,
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: PantPrincipalBinding
    var filtrando = false
    private lateinit var db: DatabaseReference
    private var precio: String = " $ 2,200 "
    private lateinit var adapter: AdapterPrincipal
    private var adapterFiltro = FiltroAdapterHolder("")
    private lateinit var colorTema: String
    private var currentTask = "Viendo Menu Principal"

    private lateinit var mAdView: AdView //anuncios
    private lateinit var btnOptShop: ImageButton

    private var listaRutas = emptyList<Int>()
    private var listaFilter = emptyList<DatosPrimariosRuta>()
    private var firebaseInstance = FirebaseDatabase.getInstance()

    private var instanciaUtilidadesMenores = UtilidadesMenores()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PantPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseDatabase.getInstance().goOnline()

        //firebase
        db =
            FirebaseDatabase.getInstance(getString(R.string.linkBaseDatos)).reference
        FirebaseApp.initializeApp(this)

        //recycler
        val cajaInfo = binding.cajaInfo

        adapter =
            AdapterPrincipal(
                listaRutas,
                instanciaUtilidadesMenores.colorTituloTema(this@PantallaPrincipal)
            )
        cajaInfo.layoutManager = LinearLayoutManager(this)
        cajaInfo.adapter = adapter
        //adapter.showHorFrec(false)

        //Local Database Instance
        val dbRoom = AppDatabase.getDatabase(this)

        CoroutineScope(Dispatchers.Default).launch {
            listaRutas = dbRoom.routeDao().getAllIdsRoute()
            adapter.updateLista(
                listaRutas,
                instanciaUtilidadesMenores.colorTituloTema(this@PantallaPrincipal)
            )

            listaFilter = dbRoom.routeDao().getAllSitesExtended()
        }

        // Menu view
        val navigationView = binding.navView
        val toolbar = binding.toolbar

        navigationView.bringToFront()
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Busetas Yopal"
        }

        iniciarWorkManager(this)

        val drawer = binding.draweLayoutPrinc

        val actionToggle =
            ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close)

        val themeColor = instanciaUtilidadesMenores.getColorHambugerIcon()
        actionToggle.drawerArrowDrawable.color = ContextCompat.getColor(this, themeColor)

        drawer.addDrawerListener(actionToggle)
        actionToggle.syncState()


        navigationView.setNavigationItemSelectedListener(this)
        //..

        //ads
        mAdView = binding.adView
        btnOptShop = binding.noAdsOption
        instanciaUtilidadesMenores.loadAds(this, mAdView, btnOptShop)


        //save mode night/light
        instanciaUtilidadesMenores.applySavedNightMode(this)

        //pedir permiso notificacion si es mayor a android 13
        if (VERSION.SDK_INT >= 33) {
            pedirPermisoNotificacionesV33()
        }


        val saludo = getGratingHead()


        //cabezera
        binding.saludo.text = saludo
        binding.precio.text = precio

        //cabezera -- precio
        val ref0 = firebaseInstance.getReference(getString(R.string.ruta_firebase_price_nube))

        ref0.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                precio = snapshot.value.toString()
                binding.precio.text = precio

                ref0.removeEventListener(this)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    binding.root.context,
                    "El precio no se pudo recibir desde internet",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })


        //mensajes controlados en vivo base datos
        //variables
        val msj1 = binding.mensaje1
        val msj2 = binding.mensaje2
        val msj3 = binding.mensaje3
        msj1.text = ""
        //contenido mensajes ->
        val ref = firebaseInstance.getReference("/features/0/mensajes")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Obtener los valores de la base de datos
                val mensaje1 = snapshot.child("mensaje1").value?.toString()
                val mensaje2 = snapshot.child("mensaje2").value?.toString()
                val mensaje3 = snapshot.child("mensaje3").value?.toString()

                // Establecer el texto de los elementos
                msj1.text = mensaje1
                msj2.text = mensaje2
                msj3.text = mensaje3

                // Mostrar u ocultar los elementos según los valores obtenidos
                msj1.visibility =
                    if (mensaje1 != "" && mensaje1 != null) View.VISIBLE else View.GONE
                msj2.visibility =
                    if (mensaje2 != "" && mensaje2 != null) View.VISIBLE else View.GONE
                msj3.visibility =
                    if (mensaje3 != "" && mensaje3 != null) View.VISIBLE else View.GONE

                doAnimInformativeTextsAction()
                ref.removeEventListener(this)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    binding.root.context,
                    "Mensajes en vivo no se pudo recibir desde internet",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        //termina mensajes en vivo-------------------------------------------------

        binding.buscarOpt.setOnClickListener {
            val menuItem = findViewById<ImageView>(R.id.buscar_opt)
            val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate)
            menuItem.startAnimation(rotation)

            searchFilter()
        }

        binding.toggleTheme.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                toggleNightModeWithAnim()
            }
        }

        btnOptShop.setOnClickListener {
            val intent = Intent(this, ShopScreen::class.java)
            startActivity(intent)
        }

        // Filters
        // ------ Setters states
        val nameStateValueSites = getString(R.string.nombre_shared_show_sites_value)
        val nameStateValueHF = getString(R.string.nombre_shared_show_horfrecs_value)

        verifyIniShowStates(nameStateValueSites, nameStateValueHF)

        // Prepare for edit
        val btnShowSites = binding.showSites
        btnShowSites.addOnCheckedStateChangedListener { _, state ->
            adapter.showSites(state == MaterialCheckBox.STATE_CHECKED)
            //Save to shared preference the value
            instanciaUtilidadesMenores.saveToSharedPreferences(
                this,
                nameStateValueSites,
                state == MaterialCheckBox.STATE_CHECKED
            )
        }

        val btnShowHorFrecs = binding.showHorFrecs
        btnShowHorFrecs.addOnCheckedStateChangedListener { _, state ->
            adapter.showHorFrec(state == MaterialCheckBox.STATE_CHECKED)
            //Save to shared preference the value
            instanciaUtilidadesMenores.saveToSharedPreferences(
                this,
                nameStateValueHF,
                state == MaterialCheckBox.STATE_CHECKED
            )
        }
        //..

        colorTema = instanciaUtilidadesMenores.colorTituloTema(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val drawerLayout = binding.draweLayoutPrinc
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {

                    if (filtrando || binding.filtro.requestFocus()) {

                        filterPreparing(false)

                        showElementsHead(true)

                        binding.cajaInfo.requestFocus()

                        //cambiar el adaptador del recyclerView
                        binding.cajaInfo.adapter = adapter

                        adapter.updateLista(listaRutas, colorTema)
                    } else {
                        wantOut()
                    }
                }
            }
        })

        handleIntent(intent)
    }

    fun iniciarWorkManager(context: Context) {
        val workManager = WorkManager.getInstance(context)

        //Ejecutar una vez inmediatamente
        val oneTimeWork = OneTimeWorkRequestBuilder<ListenerNotifications>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        workManager.enqueue(oneTimeWork)


        // Programar ejecución cada 15 minutos
        val periodicWork = PeriodicWorkRequestBuilder<ListenerNotifications>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        workManager.enqueueUniquePeriodicWork(
            "firebase_listener_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )

        Log.d("WorkManager", "✅ Worker programado: inmediato y luego cada 15 minutos")
    }


    private fun verifyIniShowStates(nameStrSites: String, nameStrHorFrec: String) {
        val isCheckedSites: Boolean =
            instanciaUtilidadesMenores.readSharedBooleanShowStatesPrincPref(
                this,
                nameStrSites
            )
        binding.showSites.isChecked = isCheckedSites

        val isCheckedHorFrec: Boolean =
            instanciaUtilidadesMenores.readSharedBooleanShowStatesPrincPref(this, nameStrHorFrec)
        binding.showHorFrecs.isChecked = isCheckedHorFrec

        adapter.let {
            adapter.showSites(isCheckedSites)
            adapter.showHorFrec(isCheckedHorFrec)
        }
    }

    override fun onResume() {
        super.onResume()
        instanciaUtilidadesMenores.loadAds(this, mAdView, btnOptShop)
    }

    private fun getGratingHead(): String {
        //mostrar saludo buenos dias
        val saludo: String = when (getHora()) {
            in 0..11 -> "Buenos días"
            in 12..17 -> "Buenas tardes"
            else -> "Buenas noches"
        }
        return saludo
    }


    private fun wantOut() {
        val builder = AlertDialog.Builder(this@PantallaPrincipal, R.style.AlertDialogTheme)
        builder.setMessage("¿Seguro que quieres salir?")
            .setPositiveButton("Sí") { _, _ ->
                // Finish on confirm
                finishAffinity()
            }
        builder.setNegativeButton("No") { _, _ -> }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        intent.getStringExtra("link")?.let { link ->
            val openLinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(openLinkIntent)
            // Limpiar el intent
            intent.removeExtra("link")
        }

        intent.getStringExtra("respuesta")?.let { response ->
            instanciaUtilidadesMenores.crearAlertaSencilla(this, response)
            // Limpiar el intent
            intent.removeExtra("respuesta")
        }
    }

    private fun filterPreparing(mode: Boolean) {
        val tecladoV = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val filtro = binding.filtro

        if (mode) {
            filtro.text.clear()

            filtro.visibility = View.VISIBLE // Show Filter Field
            filtro.requestFocus()// Focus on filter field
            tecladoV.showSoftInput(
                filtro,
                InputMethodManager.SHOW_IMPLICIT
            )// Show keyboard
        } else {
            filtro.visibility = View.GONE
            filtrando = false
            binding.noResultados.visibility = View.GONE
            tecladoV.hideSoftInputFromWindow(
                binding.root.windowToken,
                0
            ) // Hide keyboard
        }
    }

    private fun showElementsHead(mode: Boolean) {
        // Hide/show elements
        val visibility = if (mode) View.VISIBLE else View.GONE

        binding.cabezera.visibility = visibility
    }

    private fun getHora(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.HOUR_OF_DAY)
    }


    //PERMISO NOTIFICACIONES--------------------------------------------
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // El usuario concedió el permiso
        } else {
            // El usuario denegó el permiso noti
            instanciaUtilidadesMenores.crearAlertaSencilla(
                this,
                "El permiso de notificaciones ha sido denegado"
            )
        }
    }

    private fun pedirPermisoNotificacionesV33() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Aquí podrías mostrar un diálogo o mensaje explicativo al usuario
                instanciaUtilidadesMenores.crearAlerta(
                    this,
                    "notificacion",
                    "Permiso de notificación ha sido denegado." +
                            "\n\nPermite que Busetas Yopal tenga permiso de " +
                            "notificaciónes para estar al dia con nuestras novedades!.",
                    "Dar permiso",
                    "Cancelar",
                    this
                )
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun searchFilter() {
        val filtro = binding.filtro

        if (filtro.visibility == View.GONE) {
            //Hide elements
            showElementsHead(false)

            //Show filter field
            filterPreparing(true)

            adapter.updateLista(listaRutas, "#2196F3")

            //Listener on filter textchanged
            filtro.addTextChangedListener { claveFilter ->
                filtrando = true

                val textFiltro = claveFilter?.toString()?.lowercase().orEmpty()

                if (textFiltro.isBlank()) {
                    adapter.updateLista(listaRutas, "#2196F3")
                } else {
                    val sitiosFiltrados = listaFilter.filter { busqueda ->

                        busqueda.sites_extended.lowercase().contains(textFiltro)
                                ||
                                busqueda.id_route.toString().contains(textFiltro)

                    }.map { busqueda ->
                        DatosPrimariosRuta(busqueda.id_route, busqueda.sites_extended)
                    }

                    // Hide/show message if there are no results
                    binding.noResultados.visibility =
                        if (sitiosFiltrados.isEmpty()) View.VISIBLE else View.GONE

                    // Notify update of adapter
                    adapterFiltro.actualizarLista(sitiosFiltrados, textFiltro)
                    binding.cajaInfo.adapter = adapterFiltro
                }
            }

        } else {
            filterPreparing(false)

            // Change adapter
            binding.cajaInfo.adapter = adapter
            adapter.updateLista(listaRutas, colorTema)

            showElementsHead(true)
        }
    }

    override fun onOpcionSeleccionada(opcion: Int, tipoDeSolicitud: String) {
        //devolucion de llamada de la opcion seleccionada en UtilidadesMenores.CrearAlerta()
        //si acepta en la alerta de iniciara el pedido de permiso noti
        if (tipoDeSolicitud == "permiso_notificacion" && opcion == 1) {
            if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    //---------------------------------------------------------

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val filtro = binding.filtro
        val drawer = binding.draweLayoutPrinc

        when (item.itemId) {

            R.id.acercade -> {
                val intent = Intent(this, AcercaDe::class.java)
                startActivity(intent)
            }

            R.id.reportar -> {
                if (filtro.visibility == View.VISIBLE) {
                    currentTask = "En filtro de sitios"
                }
                if (filtrando) {
                    currentTask = "Filtrando - buscando sitios"
                }
                instanciaUtilidadesMenores.reportar(this, null, currentTask)
            }

            R.id.misReportes -> {
                val intent = Intent(this, ReportsUser::class.java)
                startActivity(intent)
            }

            R.id.todasLasRutas -> {
                val intent = Intent(this, Mapa::class.java)
                val typeMapOption = MapRouteOption.ALL_ROUTES.toString()

                intent.putExtra("type_option", typeMapOption)
                startActivity(intent)
            }

            R.id.calcularViaje -> {
                val intent = Intent(this, Mapa::class.java)
                val typeMapOption = MapRouteOption.CALCULATE_ROUTE_USER.toString()

                intent.putExtra("type_option", typeMapOption)

                startActivity(intent)
            }

            R.id.verParqueaderos -> {
                val intent = Intent(this, Mapa::class.java)
                val typeMapOption = MapRouteOption.PARKING_ROUTES.toString()

                intent.putExtra("type_option", typeMapOption)
                startActivity(intent)
            }

            R.id.ajustes -> {
                val intent = Intent(this, Configurations::class.java)

                startActivity(intent)
            }

            R.id.shopping -> {
                val intent = Intent(this, ShopScreen::class.java)

                startActivity(intent)
            }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }


    private suspend fun toggleNightModeWithAnim() {

        val menuItem = binding.toggleTheme
        val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        menuItem.startAnimation(rotation)

        val rootLayout = binding.fondox
        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_out)
        rootLayout.startAnimation(slideAnimation)

        delay(rotation.duration)

        instanciaUtilidadesMenores.toggleNightMode(
            this
        )
    }

    private fun doAnimInformativeTextsAction() {
        val scrollView = binding.scrollTextsInfo
        val scrollAmount = 100 // Px to displace

        scrollView.post {
            scrollView.smoothScrollBy(0, scrollAmount) //Anim down

            scrollView.postDelayed({
                scrollView.smoothScrollBy(0, -scrollAmount)
            }, 1000) // Await 1 second for back
        }
    }


}



