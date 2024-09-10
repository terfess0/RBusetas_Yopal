package com.terfess.busetasyopal.admin.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.admin.viewmodel.LoginAdminVM
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.ActivityLoginAdminBinding

class LoginAdmin : AppCompatActivity() {
    private lateinit var binding: ActivityLoginAdminBinding
    private val viewModel: LoginAdminVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginAdminBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //save mode night/light
        UtilidadesMenores().applySavedNightMode(this)

        //actionbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarAdminLogin)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = getString(R.string.inicia_sesio_n_como_administrador)
            setDisplayHomeAsUpEnabled(true)
        }

        val themeColor = UtilidadesMenores().getColorHambugerIcon()
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, themeColor))
        //..

        binding.signIn.setOnClickListener {
            val email = binding.edtEmail.text
            val passwrd = binding.editPass.text
            binding.editPass.clearFocus()

            if (!email.isNullOrBlank() && !passwrd.isNullOrBlank()) {
                viewModel.loginAdmin(email.toString(), passwrd.toString())
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.loggedSucces.observe(this, Observer {

            val email = binding.edtEmail.text
            val passwrd = binding.editPass.text

            if (it == true) {
                val intent = Intent(this, AdminPanel::class.java)
                startActivity(intent)
                finish()
            } else {
                UtilidadesMenores().crearSnackbar(
                    "Credenciales incorrectas",
                    binding.root
                )
            }

            //clear fields
            email?.clear()
            passwrd?.clear()
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
}