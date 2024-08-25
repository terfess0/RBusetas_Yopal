package com.terfess.busetasyopal.admin.view

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.admin.recycler.AdapterReportsAdmin
import com.terfess.busetasyopal.admin.viewmodel.ReportViewModel
import com.terfess.busetasyopal.databinding.ActivityReportsAdminBinding

class ReportsAdmin : AppCompatActivity() {
    private lateinit var binding: ActivityReportsAdminBinding
    private var adapter = AdapterReportsAdmin(emptyList())
    private val viewModelInst: ReportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityReportsAdminBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val recicler = binding.reportsAdminRecycler
        recicler.layoutManager = LinearLayoutManager(this)
        recicler.adapter = adapter

        viewModelInst.requestReports()

        viewModelInst.reports.observe(this, Observer { reports ->
            val txtReportsEmpty = binding.reportsEmpty
            if (reports.isNotEmpty()){
                adapter.notyChange(reports)
                txtReportsEmpty.visibility = View.GONE
            }else{
                txtReportsEmpty.visibility = View.VISIBLE
            }
        })

//        viewModelInst.resultDeleteReport.observe(this, Observer{
//            println("Borrado")
//            if (it == true){
//                val snk = Snackbar.make(
//                    binding.root,
//                    "Reporte Eliminado",
//                    Snackbar.LENGTH_SHORT
//                )
//                snk.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
//                snk.setBackgroundTint(
//                    ContextCompat.getColor(
//                        binding.root.context,
//                        R.color.verde
//                    )
//                )
//                snk.show()
//            }else{
//                val snk = Snackbar.make(
//                    binding.root,
//                    "Algo salió mal, no se eliminó.",
//                    Snackbar.LENGTH_SHORT
//                )
//                snk.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
//                snk.setBackgroundTint(
//                    ContextCompat.getColor(
//                        binding.root.context,
//                        R.color.catorceSalida
//                    )
//                )
//                snk.show()
//            }
//
//            viewModelInst.requestReports()
//        })
    }
}