package com.terfess.busetasyopal.actividades

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.PantPrincipalBinding
import com.terfess.busetasyopal.modelos_dato.DatosListaFiltro
import com.terfess.busetasyopal.listas_datos.ListaRutas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class RutasSeccion : AppCompatActivity() {
    //CLASE DE LAYOUT PANTALLA PRINCIPAL
    private lateinit var binding: PantPrincipalBinding
    var filtrando = false
    private lateinit var db: DatabaseReference
    private var precio: String = " $ 2,000 "
    private var mensajeEnVivo: String? = null
    private lateinit var adapter : AdapterPrincipal
    private var adapterFiltro = FiltroAdapterHolder("")
    private lateinit var colorTema: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PantPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //firebase
        db =
            FirebaseDatabase.getInstance(getString(R.string.linkBaseDatos)).reference
        FirebaseApp.initializeApp(this)
        val cajaInfo = binding.cajaInfo

        adapter = AdapterPrincipal(ListaRutas.busetaRuta.toList(), UtilidadesMenores().colorTema(this))
        cajaInfo.layoutManager = LinearLayoutManager(this)
        cajaInfo.adapter = adapter

        //ads
        MobileAds.initialize(this) {}//inicializar sdk de anuncios google

        //ACTIONBAR
        //supportActionBar?.title = "Rutas"

        //pedir permiso notificacion si es mayor a android 13
        if (VERSION.SDK_INT >= 33){
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
        FirebaseDatabase.getInstance().getReference("/features/0/precio")
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


        //mensaje controlado en vivo base datos
        if (mensajeEnVivo == null) {
            binding.mensajeControlado.visibility = View.GONE
        }
        FirebaseDatabase.getInstance().getReference("/features/0/mensaje")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val mensaje = snapshot.value.toString()
                    if (mensaje != "null") {
                        binding.mensajeControlado.visibility = View.VISIBLE
                        binding.mensajeControlado.text = mensaje
                    } else {
                        binding.mensajeControlado.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        binding.root.context,
                        "El precio no se pudo recibir desde internet",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })


        //el usuario elige boton calcular la ruta al destino
        binding.calcularRuta.setOnClickListener {
            UtilidadesMenores().crearToast(this, "Cargando...")
            val intent = Intent(this, Mapa::class.java)
            intent.putExtra("selector", 0)
            startActivity(intent)
        }

        //el usuario elige boton ver mapa con rutas
        binding.mapaRutas.setOnClickListener {
            UtilidadesMenores().crearToast(this, "Cargando...")
            val intent = Intent(this, Mapa::class.java)
            intent.putExtra("selector", 20)
            startActivity(intent)
        }

        //el usuario elige boton ver parqueaderos
        binding.mapaParqueaderos.setOnClickListener {
            UtilidadesMenores().crearToast(this, "Cargando...")
            val intent = Intent(this, Mapa::class.java)
            intent.putExtra("selector", 40)
            startActivity(intent)
        }

        colorTema = UtilidadesMenores().colorTema(this)
    }

    //menu en el ActionBar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_rutas, menu)
        return true
    }

    //controlar las opciones del menu en ActionBar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val tecladoV = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val filtro = binding.filtro
        when (item.itemId) {
            R.id.buscar -> {
                if (filtro.visibility == View.GONE) {
                    //ocultar elementos
                    binding.cabezera.visibility = View.GONE
                    binding.botonesRapidos.visibility = View.GONE
                    binding.separador1.visibility = View.GONE

                    filtro.setText("")
                    filtro.visibility = View.VISIBLE //mostrar el campo del filtro
                    filtro.requestFocus()// Establece el foco en el EditText de filtro
                    tecladoV.showSoftInput(
                        filtro,
                        InputMethodManager.SHOW_IMPLICIT
                    )//para controlar el teclado virtual
                    adapter.updateLista(ListaRutas.busetaSitios, "#2196F3")
                    //detectar lo que se va escribiendo en el filtro
                    CoroutineScope(Dispatchers.Default).launch {
                        filtro.addTextChangedListener { claveFilter ->
                            filtrando = true
                            val textFiltro = claveFilter?.toString() ?: ""
                            if (textFiltro.isEmpty()) {
                                adapter.updateLista(ListaRutas.busetaSitios, "#2196F3")
                            } else {

                                val sitiosFiltrados = ListaRutas.busetaSitios.filter { busqueda ->
                                    busqueda.sitios.lowercase().contains(textFiltro.lowercase())
                                }

                                //val numRutasFiltradas = ListaRutas.busetaSitios.filter { busqueda ->
                                //    busqueda.numRuta.toString().contains(textFiltro)
                                //}
                                val listaf = mutableListOf<DatosListaFiltro>()

                                sitiosFiltrados.forEach { busqueda ->
                                    val p = busqueda.numRuta
                                    val s = busqueda.sitios


                                    listaf.add(DatosListaFiltro(p, s))
                                }

                                if (sitiosFiltrados.isEmpty()) {
                                    binding.noResultados.visibility = View.VISIBLE
                                } else {
                                    binding.noResultados.visibility = View.GONE
                                }


                                adapterFiltro.actualizarLista(listaf, textFiltro)


                                //cambiar el adaptador del recyclerView
                                binding.cajaInfo.adapter = adapterFiltro
                            }
                        }
                    }
                } else {
                    filtro.visibility = View.GONE
                    filtrando = false
                    binding.noResultados.visibility = View.GONE
                    tecladoV.hideSoftInputFromWindow(
                        binding.root.windowToken,
                        0
                    ) //ocultar teclado virtual en esa ventana

                    //cambiar el adaptador del recyclerView
                    binding.cajaInfo.adapter = adapter
                    adapter.updateLista(ListaRutas.busetaRuta, colorTema)

                    binding.cabezera.visibility = View.VISIBLE
                    binding.botonesRapidos.visibility = View.VISIBLE
                    binding.separador1.visibility = View.VISIBLE
                }
            }

            R.id.acercade -> {
                val intent = Intent(this, AcercaDe::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    private fun getHora(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.HOUR_OF_DAY)
    }

    override fun onBackPressed() {
        if (filtrando || binding.filtro.requestFocus()) {
            val tecladoV = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            tecladoV.hideSoftInputFromWindow(binding.root.windowToken, 0)
            binding.cajaInfo.requestFocus()

            binding.filtro.setText("")
            binding.filtro.visibility = View.GONE

            binding.noResultados.visibility = View.GONE
            binding.botonesRapidos.visibility = View.VISIBLE

            binding.separador1.visibility = View.VISIBLE

            binding.cabezera.visibility = View.VISIBLE

            binding.cajaInfo.requestFocus()
            filtrando = false

            //cambiar el adaptador del recyclerView
            binding.cajaInfo.adapter = adapter

            adapter.updateLista(ListaRutas.busetaRuta, colorTema)
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("¿Seguro que quieres salir?")
                .setPositiveButton("Sí") { _, _ ->
                    //cerrar la app
                    finishAffinity()
                }
            builder.setNegativeButton("No") { _, _ -> }
            val dialog = builder.create()
            dialog.show()

        }
    }

    //PERMISO NOTIFICACIONES--------------------------------------------

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun pedirPermisoNotificacionesV33() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}