package com.terfess.busetasyopal.admin.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.terfess.busetasyopal.admin.viewmodel.LoginAdminVM
import com.terfess.busetasyopal.databinding.ActivityLoginAdminBinding

class LoginAdmin : AppCompatActivity() {
    private lateinit var binding: ActivityLoginAdminBinding
    private val viewModel: LoginAdminVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginAdminBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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

        viewModel.loggedSucces.observe(this, Observer{
            if (it == true){
                val intent = Intent(this, AdminPanel::class.java)
                startActivity(intent)

                val email = binding.edtEmail.text
                val passwrd = binding.editPass.text
                email?.clear()
                passwrd?.clear()
            }
        })
    }
}