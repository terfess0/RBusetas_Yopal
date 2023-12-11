package com.terfess.busetasyopal.actividades

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.RutasAdapter
import com.terfess.busetasyopal.clases_utiles.DatosASqliteLocal
import com.terfess.busetasyopal.clases_utiles.DatosDeFirebase
import com.terfess.busetasyopal.clases_utiles.allDatosRutas
import com.terfess.busetasyopal.databinding.PantPrincipalBinding
import com.terfess.busetasyopal.listas_datos.ListaRutas
import com.terfess.busetasyopal.modelos_dato.EstructuraDatosBaseDatos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class RutasSeccion : AppCompatActivity() {
    private lateinit var binding: PantPrincipalBinding
    private var filtrando = false
    private lateinit var db: DatabaseReference
    private var precio: String = " $2,000"
    private var mensaje_controlado: String? = null
    private val adapter = RutasAdapter(ListaRutas.busetaRuta.toList())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PantPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //firebase
        db =
            FirebaseDatabase.getInstance("https://rutasbusetas-default-rtdb.firebaseio.com/").reference
        FirebaseApp.initializeApp(this)
        val cajaInfo = binding.cajaInfo

        cajaInfo.layoutManager = LinearLayoutManager(this)
        cajaInfo.adapter = adapter


        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>
        //DESCARGAR LOS DATOS DE COORDENADAS DE CADA RUTA DESDE FIREBASE Y GUARDARLOS EN SQLITE LOCAL
        val versionLocal: Int
        //buscar el numero de version actual (local)
        val dbHelper = DatosASqliteLocal(this)
        val cursor = dbHelper.readableDatabase.rawQuery("SELECT * FROM version", null)
        versionLocal = if (cursor.moveToFirst()){
            cursor.getInt(0) //indices de columnas inician en 0
        }else{
            0
        }
        cursor.close()

        CoroutineScope(Dispatchers.IO).launch {
            FirebaseDatabase.getInstance().getReference("/features/0/version")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val versionNube = snapshot.value.toString().toInt()
                        if (versionLocal != versionNube){
                            dbHelper.insertarVersionDatos(versionNube)
                            descargarDatos()
                            Toast.makeText(this@RutasSeccion, "Descargando informacion", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@RutasSeccion, "La version no se pudo recibir desde internet",Toast.LENGTH_SHORT).show()
                    }
                })
        }


        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>



        //supportActionBar?.title = "Rutas"
        supportActionBar?.themedContext

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
                    Toast.makeText(binding.root.context, "El precio no se pudo recibir desde internet",Toast.LENGTH_SHORT).show()
                }
            })

        //mensaje controlado en vivo base datos
        if (mensaje_controlado == null){
            binding.mensajeControlado.visibility = View.GONE
        }
        FirebaseDatabase.getInstance().getReference("/features/0/mensaje")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val mensaje = snapshot.value.toString()
                    if (mensaje != "null"){
                        binding.mensajeControlado.visibility = View.VISIBLE
                        binding.mensajeControlado.text = mensaje
                    }else{
                        binding.mensajeControlado.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(binding.root.context, "El precio no se pudo recibir desde internet",Toast.LENGTH_SHORT).show()
                }
            })
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
                    binding.cabezera.visibility = View.GONE
                    filtro.setText("")
                    filtro.visibility = View.VISIBLE //mostrar el campo del filtro
                    filtro.requestFocus()// Establece el foco en el EditText de filtro
                    tecladoV.showSoftInput(
                        filtro,
                        InputMethodManager.SHOW_IMPLICIT
                    )//para controlar el teclado virtual
                    adapter.updateLista(ListaRutas.busetaSitios, "#2196F3")
                    //detectar lo que se va escribiendo en el filtro
                    filtro.addTextChangedListener { claveFilter ->
                        filtrando = true
                        val textFiltro = claveFilter?.toString() ?: "es nulo"
                        if (textFiltro.isEmpty()) {
                            adapter.updateLista(ListaRutas.busetaSitios, "#2196F3")
                        } else {
                            val rutasFiltradas =
                                ListaRutas.busetaSitios.filter { busqueda ->
                                    busqueda.sitios.lowercase()
                                        .contains(textFiltro.lowercase()) || busqueda.numRuta.toString()
                                        .contains(textFiltro)
                                }
                            if (rutasFiltradas.isEmpty()) {
                                binding.noResultados.visibility = View.VISIBLE
                            } else {
                                binding.noResultados.visibility = View.GONE
                            }
                            adapter.updateLista(rutasFiltradas, "#2196F3")
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
                    adapter.updateLista(ListaRutas.busetaRuta, "333333")
                    binding.cabezera.visibility = View.VISIBLE
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

    private fun descargarDatos(){
        val dbHelper = DatosASqliteLocal(this)
        DatosDeFirebase().descargarInformacion(object : allDatosRutas {
            override fun todosDatosRecibidos(listaCompleta: MutableList<EstructuraDatosBaseDatos>) {
                dbHelper.eliminarTodasLasRutas()
                for (i in listaCompleta) {
                    dbHelper.insertarRuta(i.idRuta)
                    dbHelper.insertarCoordSalida(i.idRuta, i.listPrimeraParte)
                    dbHelper.insertarCoordLlegada(i.idRuta, i.listSegundaParte)
                }
                Toast.makeText(this@RutasSeccion, "Se descargo toda la información correctamente", Toast.LENGTH_SHORT).show()
                reiniciarApp(this@RutasSeccion, RutasSeccion::class.java)

            }
        })
    }

    override fun onBackPressed() {
        if (filtrando || binding.filtro.requestFocus()) {
            val tecladoV = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            tecladoV.hideSoftInputFromWindow(binding.root.windowToken, 0)
            binding.cajaInfo.requestFocus()
            binding.filtro.setText("")
            binding.filtro.visibility = View.GONE
            binding.noResultados.visibility = View.GONE
            binding.cabezera.visibility = View.VISIBLE
            binding.cajaInfo.requestFocus()
            filtrando = false
            adapter.updateLista(ListaRutas.busetaRuta, "#333333")
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("¿Seguro que quieres salir?")
                .setPositiveButton("Sí") { _, _ ->
                    finish()
                }
            builder.setNegativeButton("No") { _, _ -> }
            val dialog = builder.create()
            dialog.show()
        }
    }
    fun reiniciarApp(context: Context, claseObjetivo: Class<*>) {
        val intent = Intent(context, claseObjetivo)

        if (context.javaClass == claseObjetivo) {
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        } else {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        // Finalizar la actividad actual si es necesario
        if (context is Activity) {
            context.finish()
        }
    }





}