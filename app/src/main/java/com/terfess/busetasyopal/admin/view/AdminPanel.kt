package com.terfess.busetasyopal.admin.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.admin.viewmodel.AdminViewModel
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.ActivityAdminPanelBinding
import com.terfess.busetasyopal.services.AuthFirebase

class AdminPanel : AppCompatActivity() {
    private lateinit var binding: ActivityAdminPanelBinding
    private val viewModel: AdminViewModel by viewModels()
    private var currentPrice = ""
    private val instUtilidadesMen = UtilidadesMenores()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAdminPanelBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Save mode night/light
        instUtilidadesMen.applySavedNightMode(this)

        setToolbar()

        // Get current price
        viewModel.requestGetCurrentPrice()

        priceObservers()

        // Buttons actions
        binding.btnPriceAdmin.setOnClickListener {
            dialogPriceInput()
        }

        binding.btnRouteAdmin.setOnClickListener {
            val intent = Intent(this, RoutesAdmin::class.java)
            startActivity(intent)
        }

        binding.btnReportsAdmin.setOnClickListener {
            val intent = Intent(this, ReportsAdmin::class.java)
            startActivity(intent)
        }

        binding.btnRecordsAdmin.setOnClickListener {
            val intent = Intent(this, RecordsAdmin::class.java)
            startActivity(intent)
        }

        binding.btnAddRouteAdmin.setOnClickListener {
            val intent = Intent(this, CreateRouteAdmin::class.java)
            startActivity(intent)
        }

        binding.btnCheckGhostRoutes.setOnClickListener {
            viewModel.checkGhostRoutesData()
        }

        binding.btnUpdateVersionInfo.setOnClickListener {
            val text = "¿Seguro que quieres actualizar la versión de los datos?"
            instUtilidadesMen.buildDialogConfirmAction(
                this,
                text
            ) { response ->
                if (response) {
                    viewModel.reqUpdateVersionInfo()
                }
            }
        }

        viewModel.onUpVersion.observe(this, Observer { result ->
            if (result == true) {
                instUtilidadesMen.crearSnackbar(
                    "Versión de los datos Actualizada",
                    binding.root
                )
            } else {
                instUtilidadesMen.crearSnackbar(
                    "Error al actualizar la versión.",
                    binding.root
                )
            }
        })

        viewModel.strResultDelGhostRoutes.observe(this, Observer { result ->
            instUtilidadesMen.crearSnackbar(
                result.toString(),
                binding.root
            )
        })

    }

    private fun priceObservers() {
        viewModel.onEditPrice.observe(this, Observer { result ->
            if (result == true) {
                instUtilidadesMen.crearSnackbar(
                    "Precio Actualizado",
                    binding.root
                )
            } else {
                instUtilidadesMen.crearSnackbar(
                    "Error al actualizar el precio",
                    binding.root
                )
            }
        })

        viewModel.currentPricePassage.observe(this, Observer { price ->
            currentPrice = price
        })
    }

    private fun setToolbar() {
        //actionbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarAdminPanel)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Administrador"
            setDisplayHomeAsUpEnabled(true)
        }

        val themeColor = instUtilidadesMen.getColorHambugerIcon()
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, themeColor))
        //..
    }

    private fun dialogPriceInput() {
        // create edt
        val input = EditText(this)
        input.hint = getString(R.string.admin_edt_hint_price) + currentPrice
        input.maxLines = 1
        input.setHintTextColor(Color.GRAY)

        if (instUtilidadesMen.isNightMode()) {
            input.setTextColor(Color.WHITE)
        }

        //create dialog
        val dialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle(getString(R.string.actualizar_precio_pasaje))
            .setView(input)
            .setPositiveButton(getString(R.string.accept)) { _, _ ->
                val userInput = input.text.toString()
                if (userInput.isNotBlank()) {
                    viewModel.updatePrice(userInput)
                } else {
                    instUtilidadesMen.crearSnackbar("Campo Vacío", binding.root)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.show()
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

    override fun onDestroy() {
        AuthFirebase().logOutSessionUser()
        super.onDestroy()
    }
}