package com.terfess.busetasyopal.actividades

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.terfess.busetasyopal.FiltroAdapterHolder
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.AdapterPrincipal
import com.terfess.busetasyopal.clases_utiles.AlertaCallback
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.PantPrincipalBinding
import com.terfess.busetasyopal.modelos_dato.DatosPrimariosRuta
import com.terfess.busetasyopal.room.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar


class RutasSeccion : AppCompatActivity(), AlertaCallback {
    //CLASE DE LAYOUT PANTALLA PRINCIPAL
    private lateinit var binding: PantPrincipalBinding
    var filtrando = false
    private lateinit var db: DatabaseReference
    private var precio: String = " $ 2,000 "
    private lateinit var adapter: AdapterPrincipal
    private var adapterFiltro = FiltroAdapterHolder("")
    private lateinit var colorTema: String
    private var currentTask = "Viendo Menu Principal"
    private lateinit var mAdView: AdView //anuncios
    private var listaRutas = emptyList<Int>()
    private var listaFilter = emptyList<DatosPrimariosRuta>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PantPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //firebase
        db =
            FirebaseDatabase.getInstance(getString(R.string.linkBaseDatos)).reference
        FirebaseApp.initializeApp(this)

        //Local Database Instance
        val dbRoom = AppDatabase.getDatabase(this)

        CoroutineScope(Dispatchers.Default).launch {
            listaRutas = dbRoom.routeDao().getAllIdsRoute()
            adapter.updateLista(listaRutas, UtilidadesMenores().colorTituloTema(this@RutasSeccion))

            listaFilter = dbRoom.routeDao().getAllSitesExtended()
        }
        //recycler
        val cajaInfo = binding.cajaInfo

        adapter =
            AdapterPrincipal(
                listaRutas,
                UtilidadesMenores().colorTituloTema(this@RutasSeccion)
            )
        cajaInfo.layoutManager = LinearLayoutManager(this)
        cajaInfo.adapter = adapter

        //ads
        MobileAds.initialize(this) {}//inicializar sdk de anuncios google
        cargarAnuncios()

        //save mode night/light
        UtilidadesMenores().applySavedNightMode(this)

        //pedir permiso notificacion si es mayor a android 13
        if (VERSION.SDK_INT >= 33) {
            pedirPermisoNotificacionesV33()
        }


        //mostrar saludo buenos dias
        val saludo: String = when (getHora()) {
            in 0..11 -> "Buenos días"
            in 12..17 -> "Buenas tardes"
            else -> "Buenas noches"
        }


        //cabezera
        binding.saludo.text = saludo
        binding.precio.text = precio
        //cabezera -- precio
        FirebaseDatabase.getInstance().getReference(getString(R.string.ruta_firebase_price_nube))
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    precio = snapshot.value.toString()
                    binding.precio.text = precio
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
        FirebaseDatabase.getInstance().getReference("/features/0/mensajes")
            .addValueEventListener(object : ValueEventListener {
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

        //el usuario elige boton calcular la ruta al destino
        binding.calcularRuta.setOnClickListener {
            val intent = Intent(this, Mapa::class.java)
            intent.putExtra("selector", 0)
            startActivity(intent)
        }

        //el usuario elige boton ver mapa con rutas
        binding.mapaRutas.setOnClickListener {
            val intent = Intent(this, Mapa::class.java)
            intent.putExtra("selector", 20)
            startActivity(intent)
        }

        //el usuario elige boton ver parqueaderos
        binding.mapaParqueaderos.setOnClickListener {
            val intent = Intent(this, Mapa::class.java)
            intent.putExtra("selector", 40)
            startActivity(intent)
        }

        colorTema = UtilidadesMenores().colorTituloTema(this)


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
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
        })


        handleIntent(intent)
    }

    private fun wantOut() {
        val builder = AlertDialog.Builder(this@RutasSeccion, R.style.AlertDialogTheme)
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
            UtilidadesMenores().crearAlertaSencilla(this, response)
            // Limpiar el intent
            intent.removeExtra("respuesta")
        }
    }


    //menu en el ActionBar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_rutas, menu)
        return true
    }

    //controlar las opciones del menu en ActionBar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val filtro = binding.filtro

        when (item.itemId) {
            R.id.buscar -> {
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
                UtilidadesMenores().reportar(this, null, currentTask)
            }

            R.id.modoTema -> {
                val nightMode = AppCompatDelegate.getDefaultNightMode()
                val newNightMode = if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.MODE_NIGHT_NO
                } else {
                    AppCompatDelegate.MODE_NIGHT_YES
                }

                AppCompatDelegate.setDefaultNightMode(newNightMode)
                recreate()


                val sharedPreferences =
                    getSharedPreferences(
                        getString(R.string.nombre_shared_preferences),
                        Context.MODE_PRIVATE
                    )
                sharedPreferences.edit().putInt("night_mode", newNightMode).apply()

            }
        }
        return true
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
        binding.botonesRapidos.visibility = visibility
        binding.separador1.visibility = visibility
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
            UtilidadesMenores().crearAlertaSencilla(
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
                UtilidadesMenores().crearAlerta(
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

    override fun onOpcionSeleccionada(opcion: Int, tiypeSoli: String) {
        //devolucion de llamada de la opcion seleccionada en UtilidadesMenores.CrearAlerta()
        //si acepta en la alerta de iniciara el pedido de permiso noti
        if (tiypeSoli == "permiso_notificacion" && opcion == 1) {
            if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    //---------------------------------------------------------
    private fun cargarAnuncios() {
        //anuncios
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

}



