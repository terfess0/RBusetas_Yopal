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
                    filtro.visibility = View.VISIBLE //mostrar el campo del filtro
                    filtro.requestFocus()// Establece el foco en el EditText de filtro
                    tecladoV.showSoftInput(
                        filtro,
                        InputMethodManager.SHOW_IMPLICIT
                    )//para controlar el teclado virtual
                    //item.setIcon(R.drawable.cerrar) //cambia el icono del "buscar" por una X
                    //detectar lo que se va escribiendo en el filtro
                    filtro.addTextChangedListener { claveFilter ->
                        filtrando = true
                        val textFiltro = claveFilter?.toString() ?: "es nulo"
                        if (textFiltro.isEmpty()) {
                            adapter.updateLista(ListaRutas.busetaRuta)
                        } else {
                            val rutasFiltradas =
                                ListaRutas.busetaRuta.filter { busqueda ->
                                    busqueda.sitios.lowercase().contains(textFiltro.lowercase()) || busqueda.numRuta.toString().contains(textFiltro) }
                            if (rutasFiltradas.isEmpty()) {
                                binding.noResultados.visibility = View.VISIBLE
                            } else {
                                binding.noResultados.visibility = View.GONE
                            }
                            adapter.updateLista(rutasFiltradas)
                        }
                    }
                } else {
                    filtro.visibility = View.GONE
                    item.setIcon(R.drawable.buscar)
                    adapter.updateLista(ListaRutas.busetaRuta)
                    tecladoV.hideSoftInputFromWindow(binding.root.windowToken, 0) //ocultar teclado virtual en esa ventana
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

    override fun onBackPressed() = if (filtrando || binding.filtro.requestFocus()) {
        val tecladoV = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        tecladoV.hideSoftInputFromWindow(binding.root.windowToken,0)
        adapter.updateLista(ListaRutas.busetaRuta)
        binding.cajaInfo.requestFocus()
        binding.filtro.setText("")
        binding.filtro.visibility = View.GONE
        binding.noResultados.visibility = View.GONE
    } else {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("¿Seguro que quieres salir?")
        builder.setPositiveButton("Sí") { _, _ ->
            finish()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
        //onBackPressedDispatcher.onBackPressed()
    }
}