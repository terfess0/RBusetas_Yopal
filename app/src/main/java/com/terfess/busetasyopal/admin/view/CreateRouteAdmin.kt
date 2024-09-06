package com.terfess.busetasyopal.admin.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.admin.model.DatoRuta
import com.terfess.busetasyopal.admin.viewmodel.AddRouteViewModel
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.ActivityCreateRouteAdminBinding

class CreateRouteAdmin : AppCompatActivity() {
    private lateinit var binding: ActivityCreateRouteAdminBinding
    private val instVM : AddRouteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCreateRouteAdminBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnGuardar.setOnClickListener {
            collectData()
        }

        instVM.resultAdd.observe(this, Observer { result ->
            if (result == true){
                UtilidadesMenores().crearSnackbar(
                    "Creada Correctamente",
                    binding.root
                )
                val intent = Intent(this, AdminPanel::class.java)
                startActivity(intent)
                finish()
            }else{
                UtilidadesMenores().crearSnackbar(
                    "Error al crear Ruta",
                    binding.root
                )
            }
        })
    }

    private fun collectData() {

        val fields = listOf(
            binding.etNumRuta,
            binding.etSitios,
            binding.etFrecuenciaSab,
            binding.etFrecuenciaDomFest,
            binding.etHorarioLunVie,
            binding.etHorarioSab,
            binding.etHorarioDomFest,
            binding.etFrecuenciaLunVie
        )

        // Comprobar que ninguno esté vacío o en blanco
        for (field in fields) {
            if (field.text.isNullOrBlank()) {
                field.requestFocus()  // Hacer focus en el campo vacío
                field.error = getString(R.string.este_campo_no_puede_estar_vacio)  // Mostrar mensaje de error
                return
            }
        }

        val numRutaList = binding.etNumRuta.text.toString()
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }  // Convertir a List<Int>

        val horarioLunVieList = binding.etHorarioLunVie.text.toString()
            .split(",")
            .map { it.trim() }  // Convertir a List<String>

        val horarioSabList = binding.etHorarioSab.text.toString()
            .split(",")
            .map { it.trim() }  // Convertir a List<String>

        val horarioDomFestList = binding.etHorarioDomFest.text.toString()
            .split(",")
            .map { it.trim() }  // Convertir a List<String>

        val data = DatoRuta(
            numRuta = numRutaList,
            horarioLunVie = horarioLunVieList,
            sitios = binding.etSitios.text.toString(),  // Sitios es un String
            frecuenciaLunVie = binding.etFrecuenciaLunVie.text.toString(),  // String
            frecuenciaSab = binding.etFrecuenciaSab.text.toString(),  // String
            frecuenciaDomFest = binding.etFrecuenciaDomFest.text.toString(),  // String
            horarioSab = horarioSabList,
            horarioDomFest = horarioDomFestList,
            enabled = binding.swEnabled.isChecked  // Boolean
        )

        val context = binding.root.context
        val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)

        builder.setTitle(context.getString(R.string.alert_text_literal))
            .setIcon(R.drawable.ic_panel_admin)
            .setMessage("¿Seguro que quieres crear una nueva ruta? Revisa que el nuevo id no exista!")
            .setPositiveButton(context.getString(R.string.confirm)) { _, _ ->
                // create route is first confirm

                builder.setTitle(context.getString(R.string.alert_text_literal))
                    .setIcon(R.drawable.ic_panel_admin)
                    .setMessage("¿Totalmente seguro?")
                    .setPositiveButton(context.getString(R.string.confirm)) { _, _ ->
                        // create route is confirm
                        instVM.requestAddRoute(data)
                    }
                    .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                        // cancel
                        dialog.dismiss()
                    }
                // Crear y mostrar el diálogo
                val dialog = builder.create()
                dialog.show()
            }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                // cancel
                dialog.dismiss()
            }
        // Crear y mostrar el diálogo
        val dialog = builder.create()
        dialog.show()
    }

}