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

class AdminPanel : AppCompatActivity() {
    private lateinit var binding: ActivityAdminPanelBinding
    private val viewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAdminPanelBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //save mode night/light
        UtilidadesMenores().applySavedNightMode(this)

        binding.btnRouteAdmin.setOnClickListener {
            val intent = Intent(this, RoutesAdmin::class.java)
            startActivity(intent)
        }

        binding.btnPriceAdmin.setOnClickListener {
            dialogPriceInput()
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

        viewModel.onEditPrice.observe(this, Observer { result ->
            if (result == true) {
                UtilidadesMenores().crearSnackbar(
                    "Precio Actualizado",
                    binding.root
                )
            } else {
                UtilidadesMenores().crearSnackbar(
                    "Error al actualizar el precio",
                    binding.root
                )
            }
        })

        //actionbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarAdminPanel)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Administrador"
            setDisplayHomeAsUpEnabled(true)
        }

        val themeColor = UtilidadesMenores().getColorHambugerIcon()
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, themeColor))
        //..
    }

    private fun dialogPriceInput() {
        // create edt
        val input = EditText(this)
        input.hint = getString(R.string.admin_edt_hint_price)
        input.maxLines = 1
        input.setHintTextColor(Color.GRAY)

        if (UtilidadesMenores().isNightMode()) {
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
                    UtilidadesMenores().crearSnackbar("Campo Vacío", binding.root)
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
}