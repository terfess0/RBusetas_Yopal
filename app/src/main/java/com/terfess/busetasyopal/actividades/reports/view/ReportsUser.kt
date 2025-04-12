package com.terfess.busetasyopal.actividades.reports.view

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.actividades.reports.viewmodel.ViewModelReports
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.ActivityReportsUserBinding

class ReportsUser : AppCompatActivity() {
    private lateinit var binding: ActivityReportsUserBinding
    private val vmReports: ViewModelReports by viewModels()

    override fun onStart() {
        super.onStart()
        //save mode night/light
        UtilidadesMenores().applySavedNightMode(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityReportsUserBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //actionbar
        val toolbar = binding.toolbarUserReports
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = getString(R.string.mis_reportes)
            setDisplayHomeAsUpEnabled(true)
        }

        val themeColor = UtilidadesMenores().getColorHambugerIcon()
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, themeColor))
        //..

        vmReports.getReports()

        val adapterRep = AdapterHolderReportsUser(emptyList(), vmReports, this)
        setRecycler(adapterRep)

        vmReports.myReports.observe(this, Observer { reps ->
            val haveReports = reps.isNotEmpty()
            if (haveReports) {
                adapterRep.notyChange(reps)
            }
            binding.reportUserEmpty.visibility = if (haveReports) {
                View.GONE
            } else {
                View.VISIBLE
            }
        })
    }

    private fun setRecycler(adapterRep: AdapterHolderReportsUser) {
        val recyler = binding.reportsUserRecycler
        recyler.layoutManager = LinearLayoutManager(this)
        recyler.adapter = adapterRep
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