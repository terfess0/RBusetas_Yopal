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
import com.terfess.busetasyopal.admin.model.DatoRecord
import com.terfess.busetasyopal.admin.recycler.records.AdapterRecordsAdmin
import com.terfess.busetasyopal.admin.viewmodel.ViewModelRecords
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.ActivityRecordsAdminBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordsAdmin : AppCompatActivity() {
    private lateinit var binding : ActivityRecordsAdminBinding
    private val viewModel : ViewModelRecords by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRecordsAdminBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val adapter = AdapterRecordsAdmin(emptyList())
        val recycler = binding.recordsAdminRecycler
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        viewModel.requestRecords()

        //actionbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarAdminRecords)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = getString(R.string.registros_de_acciones_administrador)
            setDisplayHomeAsUpEnabled(true)
        }

        val themeColor = UtilidadesMenores().getColorHambugerIcon()
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, themeColor))
        //..

        viewModel.listRecord.observe(this, Observer { data ->
            adapter.pushData(orderByDate(data))

            if (data.isNullOrEmpty()) {
                binding.recordsEmpty.visibility = View.VISIBLE
            }else {
                binding.recordsEmpty.visibility = View.GONE
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

    fun orderByDate(lista: List<DatoRecord>): List<DatoRecord> {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        return lista.sortedByDescending { elemento ->
            formato.parse(elemento.fecha) ?: Date(0) // Si hay error, usa fecha mínima
        }
    }
}