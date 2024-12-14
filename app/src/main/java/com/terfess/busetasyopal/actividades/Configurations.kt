package com.terfess.busetasyopal.actividades

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.ActivityConfigurationsBinding

class Configurations : AppCompatActivity() {

    private lateinit var binding: ActivityConfigurationsBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapTypeSpinner = binding.mapTypeSpinner

        val nameShared = getString(R.string.nombre_shared_preferences)
        sharedPreferences = getSharedPreferences(nameShared, Context.MODE_PRIVATE)

        val selectedIndex = UtilidadesMenores().getIndexTypeMap(this)

        if (selectedIndex >= 0) {
            mapTypeSpinner.setSelection(selectedIndex)
        }

        listenLanguajeChange(mapTypeSpinner)

        //actionbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarConfig)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Ajustes"
            setDisplayHomeAsUpEnabled(true)
        }

        val themeColor = UtilidadesMenores().getColorHambugerIcon()
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, themeColor))
        //..
    }

    private fun listenLanguajeChange(maptype: Spinner) {
        maptype.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (parent.getItemAtPosition(position).toString()) {
                    "Mapa Normal" -> {
                        val typeCode = GoogleMap.MAP_TYPE_NORMAL
                        with(sharedPreferences.edit()) {
                            putInt("type_map_user", typeCode)
                            apply()
                        }
                    }

                    "Mapa Hybrido" -> {
                        val typeCode = GoogleMap.MAP_TYPE_HYBRID
                        with(sharedPreferences.edit()) {
                            putInt("type_map_user", typeCode)
                            apply()
                        } //save tipo de mapa hybrido
                    }

                    "Mapa Satelital" -> {
                        val typeCode = GoogleMap.MAP_TYPE_SATELLITE
                        with(sharedPreferences.edit()) {
                            putInt("type_map_user", typeCode)
                            apply()
                        }//save tipo de mapa satelital
                    }

                    "Mapa Relieve" -> {
                        val typeCode = GoogleMap.MAP_TYPE_TERRAIN
                        with(sharedPreferences.edit()) {
                            putInt("type_map_user", typeCode)
                            apply()
                        }//save tipo de mapa relieve
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            android.R.id.home -> {
                onBackPressed()
            }
        }
        return true
    }
}