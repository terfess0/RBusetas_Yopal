package com.terfess.busetasyopal.admin.view

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.terfess.busetasyopal.admin.recycler.routes.AdapterRoutesAdmin
import com.terfess.busetasyopal.admin.viewmodel.RoutesViewModel
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.ActivityRoutesAdminBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RoutesAdmin : AppCompatActivity() {
    private lateinit var binding: ActivityRoutesAdminBinding
    private val viewModel: RoutesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRoutesAdminBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.getAllDataAdmin()

        val adapter = AdapterRoutesAdmin(emptyList(), viewModel)
        val recycler = binding.rutasAdminRecycler
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        binding.btnReload.setOnClickListener {
            viewModel.getAllDataAdmin()
            hideRecyclerOnError(false)
        }

        viewModel.allData.observe(this, Observer {
            if (it.isNotEmpty()) {
                adapter.setNewData(it)
            } else {
                hideRecyclerOnError(true)
            }
        })

        viewModel.onEditResult.observe(this, Observer { result ->

            if (result == true) {
                UtilidadesMenores().crearSnackbar(
                    "Actualizado Correctamente",
                    binding.root
                )
            } else {
                UtilidadesMenores().crearSnackbar(
                    "No se pudo actualizar",
                    binding.root
                )
            }

            progressVarVisibilFast()
            binding.main.requestFocus()
        })

        viewModel.onEditStatusEnabled.observe(this, Observer { result ->
            if (result == true) {
                UtilidadesMenores().crearSnackbar(
                    "Estado Actualizado Correctamente",
                    binding.root
                )
            } else {
                UtilidadesMenores().crearSnackbar(
                    "No se pudo actualizar",
                    binding.root
                )
            }
            progressVarVisibilFast()
            binding.main.requestFocus()
        })
    }

    private fun progressVarVisibilFast() {
        // show
        binding.progressBar.visibility = View.VISIBLE

        // hide after half a second
        CoroutineScope(Dispatchers.Main).launch {
            delay(500) // 1 second
            //hide
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun hideRecyclerOnError(mode: Boolean) {
        if (mode) {
            binding.rutasAdminRecycler.visibility = View.GONE
            binding.emptyDataInfo.visibility = View.VISIBLE
        } else {
            binding.rutasAdminRecycler.visibility = View.VISIBLE
            binding.emptyDataInfo.visibility = View.GONE
        }
    }
}