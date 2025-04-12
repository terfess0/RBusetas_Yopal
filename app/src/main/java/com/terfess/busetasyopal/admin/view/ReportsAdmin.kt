package com.terfess.busetasyopal.admin.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.modelos_dato.reports_system.DatoReport
import com.terfess.busetasyopal.admin.recycler.reports.AdapterReportsAdmin
import com.terfess.busetasyopal.admin.viewmodel.ReportViewModel
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.ActivityReportsAdminBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportsAdmin : AppCompatActivity() {
    private lateinit var binding: ActivityReportsAdminBinding
    private val viewModelInst: ReportViewModel by viewModels()
    private val utilidadesMenInst = UtilidadesMenores()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityReportsAdminBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        val adapter = AdapterReportsAdmin(emptyList(), viewModelInst, this)
        val recicler = binding.reportsAdminRecycler
        recicler.layoutManager = LinearLayoutManager(this)
        recicler.adapter = adapter

        //actionbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarAdminReports)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = getString(R.string.reportes_de_usuarios)
            setDisplayHomeAsUpEnabled(true)
        }

        val themeColor = UtilidadesMenores().getColorHambugerIcon()
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, themeColor))
        //..

        viewModelInst.requestReports()

        viewModelInst.reports.observe(this, Observer { reports ->
            val txtReportsEmpty = binding.reportsEmpty
            if (reports.isNotEmpty()) {
                adapter.notyChange(orderByDate(reports))
                txtReportsEmpty.visibility = View.GONE
            } else {
                txtReportsEmpty.visibility = View.VISIBLE
            }
        })

        viewModelInst.resultDeleteReport.observe(this, Observer {
            if (it == true) {
                utilidadesMenInst.crearSnackbar(
                    "Reporte Eliminado", binding.root
                )
            } else {
                utilidadesMenInst.crearSnackbar(
                    "Algo salió mal, no se eliminó el reporte.", binding.root
                )
            }
        })

        viewModelInst.resultDeleteResponse.observe(this, Observer {
            if (it == true) {
                utilidadesMenInst.crearSnackbar(
                    "Respuesta Eliminada", binding.root
                )
            } else {
                utilidadesMenInst.crearSnackbar(
                    "Algo salió mal, no se eliminó la respuesta.", binding.root
                )
            }
        })

        viewModelInst.resultReplyReport.observe(this, Observer { result ->

            var txtResult = ""
            if (result) {
                txtResult = "Respuesta Enviada Correctamente"

                utilidadesMenInst.crearSnackbar(txtResult, binding.root)
            } else {
                val erorrtxt = viewModelInst.errorReplyReport
                txtResult = "Algo salió mal, no se respondio el reporte. $erorrtxt"

                utilidadesMenInst.crearSnackbar(txtResult, binding.root)
            }
        })

        viewModelInst.alertOnScreen.observe(this, Observer { text ->
            //Mostrar algun mensaje enviado por el view model
            if (!text.isNullOrBlank()) {
                utilidadesMenInst.crearSnackbar(
                    text,
                    binding.root
                )
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //inflate menu resource
        menuInflater.inflate(R.menu.menu_admin, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return true
    }

    fun orderByDate(lista: List<DatoReport>): List<DatoReport> {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        return lista.sortedByDescending { elemento ->
            formato.parse(elemento.dateReport) ?: Date(0) // Si hay error, usa fecha mínima
        }
    }
}