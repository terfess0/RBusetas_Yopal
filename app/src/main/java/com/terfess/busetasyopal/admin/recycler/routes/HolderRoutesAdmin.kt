package com.terfess.busetasyopal.admin.recycler.routes

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.admin.mapa.MapaAdmin
import com.terfess.busetasyopal.admin.model.DatoRuta
import com.terfess.busetasyopal.admin.viewmodel.RoutesViewModel
import com.terfess.busetasyopal.databinding.FormatRecyclerRoutesAdminBinding
import com.terfess.busetasyopal.modelos_dato.DatoFrecuencia
import com.terfess.busetasyopal.modelos_dato.DatoHorario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HolderRoutesAdmin(vista: View) : RecyclerView.ViewHolder(vista) {
    private val binding = FormatRecyclerRoutesAdminBinding.bind(vista)

    fun mostrar(dato: DatoRuta, viewModelInstance: RoutesViewModel) {
        val ruta = "Ruta\n" + dato.numRuta
        val contextoHolder = binding.root.context
        val defaultValue = ""

        // Usa coroutines para manejar la carga de datos
        CoroutineScope(Dispatchers.IO).launch {
            // Construcción de objetos pesados en un hilo de fondo
            val horarioRuta = DatoHorario(
                dato.numRuta[0],
                dato.horarioLunVie.getOrNull(0) ?: defaultValue,
                dato.horarioLunVie.getOrNull(1) ?: defaultValue,
                dato.horarioSab.getOrNull(0) ?: defaultValue,
                dato.horarioSab.getOrNull(1) ?: defaultValue,
                dato.horarioDomFest.getOrNull(0) ?: defaultValue,
                dato.horarioDomFest.getOrNull(1) ?: defaultValue
            )


            val frecuenciaRuta = DatoFrecuencia(
                dato.numRuta[0],
                dato.frecuenciaLunVie ?: defaultValue,
                dato.frecuenciaSab ?: defaultValue,
                dato.frecuenciaDomFest ?: defaultValue
            )


            // Una vez que los datos están listos, cambia al hilo principal para actualizar la UI
            withContext(Dispatchers.Main) {
                // Mostrar los horarios
                setHorario(
                    horarioRuta.horaInicioLunesViernes,
                    horarioRuta.horaFinalLunesViernes,
                    binding.horLVAdminInicio,
                    binding.horLVAdminFin,
                    contextoHolder
                )

                setHorario(
                    horarioRuta.horaInicioSab,
                    horarioRuta.horaFinalSab,
                    binding.horSabAdminInicio,
                    binding.horSabAdminFin,
                    contextoHolder
                )

                setHorario(
                    horarioRuta.horaInicioDom,
                    horarioRuta.horaFinalDom,
                    binding.horDomAdminInicio,
                    binding.horDomAdminFin,
                    contextoHolder
                )

                // Mostrar las frecuencias
                setFrecuencia(frecuenciaRuta.frec_mon_fri, binding.frecLVAdmin, contextoHolder)
                setFrecuencia(frecuenciaRuta.frec_sat, binding.frecSabAdmin, contextoHolder)
                setFrecuencia(frecuenciaRuta.frec_sun_holi, binding.frecDomAdmin, contextoHolder)

                // Actualizar otros elementos de la UI
                binding.rutaIdAdmin.text = ruta
                binding.sitiosAdmin.setText(dato.sitios)
                binding.isEnabledAdmin.isChecked = dato.enabled
            }
        }

        // Listener para editar la ruta en el mapa
        binding.btnEditInMap.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                val idRuta = dato.numRuta[0]
                val intent = Intent(binding.root.context, MapaAdmin::class.java)
                intent.putExtra("rutaId", idRuta)
                withContext(Dispatchers.Main) {
                    binding.root.context.startActivity(intent)
                }
            }
        }

        // Listener para guardar los cambios en la ruta
        binding.btnSaveChangesRoute.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                saveSitios(dato.sitios!!, dato.numRuta, viewModelInstance)
                saveFrecuencias(
                    dato.frecuenciaLunVie,
                    dato.frecuenciaSab,
                    dato.frecuenciaDomFest,
                    dato.numRuta[0],
                    viewModelInstance
                )
                saveHorarios(
                    dato.horarioLunVie[0],
                    dato.horarioLunVie[1],
                    binding.horLVAdminInicio,
                    binding.horLVAdminFin,
                    dato.numRuta[0],
                    viewModelInstance,
                    "horarioLunVie"
                )
                saveHorarios(
                    dato.horarioSab[0],
                    dato.horarioSab[1],
                    binding.horSabAdminInicio,
                    binding.horSabAdminInicio,
                    dato.numRuta[0],
                    viewModelInstance,
                    "horarioSab"
                )
                saveHorarios(
                    dato.horarioDomFest[0],
                    dato.horarioDomFest[1],
                    binding.horDomAdminInicio,
                    binding.horDomAdminFin,
                    dato.numRuta[0],
                    viewModelInstance,
                    "horarioDomFest"
                )
            }
        }

        // Listener for enabled/disabled route
        binding.isEnabledAdmin.setOnClickListener {
            viewModelInstance.updateStatusEnabled(dato.numRuta[0], binding.isEnabledAdmin.isChecked)
        }
    }

    // Resto del código sin cambios, pero se puede optimizar también si es necesario
    private fun setHorario(
        horarioInicio: String,
        horarioFin: String,
        editText1: EditText,
        editText2: EditText,
        contexto: Context
    ) {
        if (horarioInicio.isNotBlank() && horarioFin.isNotBlank()) {
            editText1.setText(horarioInicio)
            editText2.setText(horarioFin)
        } else {
            editText1.setText(contexto.getString(R.string.vacio))
            editText2.setText(contexto.getString(R.string.vacio))
        }
    }

    private fun setFrecuencia(
        frecuencia: String,
        edtFrec: EditText,
        contexto: Context
    ) {
        if (frecuencia.isNotBlank()) {
            edtFrec.setText(frecuencia)
        } else {
            edtFrec.setText(contexto.getString(R.string.vacio))
        }
    }

    private fun saveSitios(
        sitiosNube: String,
        idRuta: List<Int>,
        viewModelInstance: RoutesViewModel
    ) {
        val sitiosNew = binding.sitiosAdmin.text.toString()

        if (sitiosNew.isNotBlank() && sitiosNube != sitiosNew) {
            viewModelInstance.updateSitiosRuta(idRuta[0], sitiosNew)
        } else {
            return
        }
    }

    private fun saveFrecuencias(
        frec1Nube: String,
        frec2Nube: String,
        frec3Nube: String,
        idRuta: Int,
        viewModelInstance: RoutesViewModel
    ) {
        val frec1New = binding.frecLVAdmin.text.toString()
        val frec2New = binding.frecSabAdmin.text.toString()
        val frec3New = binding.frecDomAdmin.text.toString()

        if (frec1New.isNotBlank() && frec1Nube != frec1New) {
            viewModelInstance.updateFrecuenciaRuta(idRuta, frec1New, "frecuenciaLunVie")
        }
        if (frec2New.isNotBlank() && frec2Nube != frec2New) {
            viewModelInstance.updateFrecuenciaRuta(idRuta, frec2New, "frecuenciaSab")
        }
        if (frec3New.isNotBlank() && frec3Nube != frec3New) {
            viewModelInstance.updateFrecuenciaRuta(idRuta, frec3New, "frecuenciaDomFest")
        }
    }

    private fun saveHorarios(
        horInicioNube: String,
        horFinNube: String,
        horInNew: EditText,
        horFNew: EditText,
        idRuta: Int,
        viewModelInstance: RoutesViewModel,
        fieldName: String
    ) {
        val horInicioNew = horInNew.text.toString()
        val horFinNew = horFNew.text.toString()

        if (horInicioNew.isNotBlank() && horFinNew.isNotBlank()) {
            if (horFinNube != horFinNew || horInicioNube != horInicioNew) {
                viewModelInstance.updateHorarioRuta(idRuta, horInicioNew, horFinNew, fieldName)
            }
        }
    }
}
