package com.terfess.busetasyopal.admin.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
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
}