package com.terfess.busetasyopal.admin.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.admin.viewmodel.EstadisticasViewModel
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.ActivityEstadisticasAdminBinding

class EstadisticasAdmin : AppCompatActivity() {
    private lateinit var binding: ActivityEstadisticasAdminBinding
    private val instanciaUtils = UtilidadesMenores()
    private val viewModelEstadisticas: EstadisticasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityEstadisticasAdminBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //actionbar
        val toolbar = binding.toolbarEstadisticas
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Estadísticas de Usuarios"
            setDisplayHomeAsUpEnabled(true)
        }

        val themeColor = instanciaUtils.getColorHambugerIcon()
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, themeColor))
        //..

        viewModelEstadisticas.requestTotalReports()
        viewModelEstadisticas.requestRouteMaxClicks()
        viewModelEstadisticas.requestRouteMinClicks()
        viewModelEstadisticas.requestTotalClicks()
        viewModelEstadisticas.requestDailyStarts()
        viewModelEstadisticas.requestTotalStarts()

        setObservers()
    }

    private fun setObservers() {
        viewModelEstadisticas.totalReports.observe(this, Observer { totalReports ->
            binding.totalReportes.text = totalReports.toString()
        })

        viewModelEstadisticas.routeMaxClicks.observe(this, Observer { maxRoute ->
            binding.routeMaxClicks.text = maxRoute
        })

        viewModelEstadisticas.routeMinClicks.observe(this, Observer { minRoute ->
            binding.routeMinClicks.text = minRoute
        })

        viewModelEstadisticas.totalClicks.observe(this, Observer { total ->
            binding.totalCountClicks.text = instanciaUtils.formatNumberWithCommas(total)
        })

        viewModelEstadisticas.todayStarts.observe(this, Observer { total ->
            binding.totalDailyStarts.text = total
        })

        viewModelEstadisticas.totalStarts.observe(this, Observer { total ->
            binding.totalAllStarts.text = total
        })
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