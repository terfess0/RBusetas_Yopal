package com.terfess.busetasyopal.admin.view

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.terfess.busetasyopal.admin.recycler.records.AdapterRecordsAdmin
import com.terfess.busetasyopal.admin.viewmodel.ViewModelRecords
import com.terfess.busetasyopal.databinding.ActivityRecordsAdminBinding

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

        viewModel.listRecord.observe(this, Observer { data ->
            adapter.pushData(data)

            if (data.isNullOrEmpty()) {
                binding.recordsEmpty.visibility = View.VISIBLE
            }else {
                binding.recordsEmpty.visibility = View.GONE
            }
        })
    }
}