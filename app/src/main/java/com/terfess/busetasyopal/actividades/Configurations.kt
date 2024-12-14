package com.terfess.busetasyopal.actividades

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.databinding.ActivityConfigurationsBinding
import java.util.Locale

class Configurations : AppCompatActivity() {

    private lateinit var binding: ActivityConfigurationsBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val supportedLanguages = listOf("es", "en") // Add supported languages here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nameShared = getString(R.string.nombre_shared_preferences)
        sharedPreferences = getSharedPreferences(nameShared, Context.MODE_PRIVATE)
        val savedLanguageCode =
            sharedPreferences.getString("language", "es") // Default language is English


        val languageSpinner = binding.languageSpinner

    }



}