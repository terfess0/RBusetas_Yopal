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
import com.google.android.material.checkbox.MaterialCheckBox
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.ActivityConfigurationsBinding
import com.terfess.busetasyopal.room.AppDatabase
import com.terfess.busetasyopal.room.model.Version
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Configurations : AppCompatActivity() {

    private lateinit var binding: ActivityConfigurationsBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var instUtilidades = UtilidadesMenores()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapTypeSpinner = binding.mapTypeSpinner

        val nameShared = getString(R.string.nombre_shared_preferences)
        sharedPreferences = getSharedPreferences(nameShared, Context.MODE_PRIVATE)

        val selectedIndex = instUtilidades.getIndexTypeMap(this)

        if (selectedIndex >= 0) {
            mapTypeSpinner.setSelection(selectedIndex)
        }

        listenLanguajeChange(mapTypeSpinner)
        iniLogicViewChecks()

        //actionbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarConfig)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Ajustes"
            setDisplayHomeAsUpEnabled(true)
        }

        val themeColor = instUtilidades.getColorHambugerIcon()
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, themeColor))
        //..

        // Change local version data and reload app for download
        binding.storeDataConfigContain.setOnClickListener {
            binding.progressFixStoreData.visibility = View.VISIBLE

            val roomDB by lazy { AppDatabase.getDatabase(this) }
            CoroutineScope(Dispatchers.IO).launch {
                val versionToFix = 0
                roomDB.versionDao()
                    .insertVersion(Version(num_version = versionToFix))
                instUtilidades.reiniciarApp(this@Configurations, Splash::class.java)
            }
        }

    }

    private fun iniLogicViewChecks() {
        val nameStateValueSites = getString(R.string.nombre_shared_show_sites_value)
        val nameStateValueHF = getString(R.string.nombre_shared_show_horfrecs_value)

        verifyIniShowStates(nameStateValueSites, nameStateValueHF)

        // Prepare for edit
        val btnShowSites = binding.showSites
        btnShowSites.addOnCheckedStateChangedListener { _, state ->
            sitesContVisibility(state == MaterialCheckBox.STATE_CHECKED)
            //Save to shared preference the value
            instUtilidades.saveToSharedPreferences(
                this,
                nameStateValueSites,
                state == MaterialCheckBox.STATE_CHECKED
            )
        }

        val btnShowHorFrecs = binding.showHorFrecs
        btnShowHorFrecs.addOnCheckedStateChangedListener { _, state ->
            horContVisibility(state == MaterialCheckBox.STATE_CHECKED)
            //Save to shared preference the value
            instUtilidades.saveToSharedPreferences(
                this,
                nameStateValueHF,
                state == MaterialCheckBox.STATE_CHECKED
            )
        }
        //..
    }

    private fun verifyIniShowStates(nameStrSites: String, nameStrHorFrec: String) {
        val isCheckedSites: Boolean =
            instUtilidades.readSharedBooleanShowStatesPrincPref(
                this,
                nameStrSites
            )

        val isCheckedHorFrec: Boolean =
            instUtilidades.readSharedBooleanShowStatesPrincPref(this, nameStrHorFrec)

        binding.showSites.isChecked = isCheckedSites
        binding.showHorFrecs.isChecked = isCheckedHorFrec

        sitesContVisibility(isCheckedSites)
        horContVisibility(isCheckedHorFrec)
    }

    private fun sitesContVisibility(show: Boolean) {
        if (show) {
            binding.sitiosExampleConfigView.visibility = View.VISIBLE
        } else {
            binding.sitiosExampleConfigView.visibility = View.GONE
        }
    }

    private fun horContVisibility(show: Boolean) {
        if (show) {
            binding.contenedorHorExampleConfigView.visibility = View.VISIBLE
        } else {
            binding.contenedorHorExampleConfigView.visibility = View.GONE
        }
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