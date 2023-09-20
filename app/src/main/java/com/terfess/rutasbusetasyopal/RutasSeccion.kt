package com.terfess.rutasbusetasyopal

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.terfess.rutasbusetasyopal.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RutasSeccion : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var filtrando = false
    private val adapter = RutasAdapter(ListaRutas.busetaRuta.toList())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cajaInfo = binding.cajaInfo

        cajaInfo.layoutManager = LinearLayoutManager(this)
        cajaInfo.adapter = adapter

        supportActionBar?.title = "Rutas"

        //mostrar saludo buenos dias
        val saludo: String = when (getHora()) {
            in 0..11 -> "Buenos días"
            in 12..17 -> "Buenas tardes"
            else -> "Buenas noches"
        }
        binding.saludo.text = saludo
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
            R.id.informacion -> {
                Toast.makeText(this, "Falta Codificar", Toast.LENGTH_SHORT).show()
            }

            R.id.buscar -> {
                if (filtro.visibility == View.GONE) {
                    filtro.setText("")
                    filtro.visibility = View.VISIBLE //mostrar el campo del filtro
                    filtro.requestFocus()// Establece el foco en el EditText de filtro
                    tecladoV.showSoftInput(
                        filtro,
                        InputMethodManager.SHOW_IMPLICIT
                    )//para controlar el teclado virtual
                    //detectar lo que se va escribiendo en el filtro
                    filtro.addTextChangedListener { claveFilter ->
                        filtrando = true
                        val textFiltro = claveFilter?.toString() ?: "es nulo"
                        if (textFiltro.isEmpty()) {
                            adapter.updateLista(ListaRutas.busetaSitios, "#CC2EFA")
                        } else {
                            val rutasFiltradas =
                                ListaRutas.busetaSitios.filter { busqueda ->
                                    busqueda.sitios.lowercase().contains(textFiltro.lowercase()) || busqueda.numRuta.toString().contains(textFiltro) }
                            if (rutasFiltradas.isEmpty()) {
                                binding.noResultados.visibility = View.VISIBLE
                            } else {
                                binding.noResultados.visibility = View.GONE
                            }
                            adapter.updateLista(rutasFiltradas, "#CC2EFA")
                        }
                    }
                } else {
                    filtro.visibility = View.GONE
                    filtrando = false
                    tecladoV.hideSoftInputFromWindow(binding.root.windowToken, 0) //ocultar teclado virtual en esa ventana
                    adapter.updateLista(ListaRutas.busetaRuta, "#DC7633")
                    }
            }

            R.id.comentarios -> {
                Toast.makeText(this, "Falta Codificar", Toast.LENGTH_SHORT).show()
            }

            R.id.acercade -> {
                Toast.makeText(this, "Falta Codificar", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }

    override fun onBackPressed() {
        if (filtrando || binding.filtro.requestFocus()) {
            val tecladoV = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            tecladoV.hideSoftInputFromWindow(binding.root.windowToken,0)
            binding.cajaInfo.requestFocus()
            binding.filtro.setText("")
            binding.filtro.visibility = View.GONE
            binding.noResultados.visibility = View.GONE
            binding.cajaInfo.requestFocus()
            filtrando = false
            adapter.updateLista(ListaRutas.busetaRuta, "#DC7633")
        } else{
            val builder = AlertDialog.Builder(this)
            builder.setMessage("¿Seguro que quieres salir?")
            builder.setPositiveButton("Sí") { _, _ ->
                finish()
            }
            builder.setNegativeButton("No") { _, _ ->

            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun getHora(): Int {
        val calendar = Calendar.getInstance()
        val horaActual = calendar.get(Calendar.HOUR_OF_DAY)
        return horaActual
    }
}