package com.terfess.busetasyopal

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
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
import com.terfess.busetasyopal.databinding.ActivityMainBinding
import java.util.Calendar

class RutasSeccion : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var filtrando = false
    private lateinit var db: DatabaseReference
    private var precio: String = "$2,000"
    private val adapter = RutasAdapter(ListaRutas.busetaRuta.toList())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //firebase
        db =
            FirebaseDatabase.getInstance("https://rutasbusetas-default-rtdb.firebaseio.com/").reference
        FirebaseApp.initializeApp(this)
        val cajaInfo = binding.cajaInfo

        cajaInfo.layoutManager = LinearLayoutManager(this)
        cajaInfo.adapter = adapter

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
                    Log.d(TAG, "precio es: $precio")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "fallido leer precio.", error.toException())
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
                    tecladoV.hideSoftInputFromWindow(
                        binding.root.windowToken,
                        0
                    ) //ocultar teclado virtual en esa ventana
                    adapter.updateLista(ListaRutas.busetaRuta, "333333")
                    binding.cabezera.visibility = View.VISIBLE
                }
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
            builder.setNegativeButton("No") { _, _ ->

            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun getHora(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.HOUR_OF_DAY)
    }

}