package com.terfess.busetasyopal.admin.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.terfess.busetasyopal.admin.recycler.AdapterRoutesAdmin
import com.terfess.busetasyopal.admin.viewmodel.RoutesViewModel
import com.terfess.busetasyopal.databinding.ActivityRoutesAdminBinding

class RoutesAdmin : AppCompatActivity() {
    private lateinit var binding : ActivityRoutesAdminBinding
    private val viewModel : RoutesViewModel by viewModels()
    private var adapter = AdapterRoutesAdmin(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRoutesAdminBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.getAllDataAdmin()

        val recycler = binding.rutasAdminRecycler
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        viewModel.allData.observe(this, Observer{
            adapter.setNewData(it)
        })
    }
}