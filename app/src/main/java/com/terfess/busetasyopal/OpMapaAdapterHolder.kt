package com.terfess.busetasyopal

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.terfess.busetasyopal.actividades.Mapa
import com.terfess.busetasyopal.clases_utiles.PolylinesOpMapa
import com.terfess.busetasyopal.databinding.FormatoRecyclerBtnRutasBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OpMapaAdapterHolder(listaRutasOpMapa: IntArray, contexto: Context, mapa: GoogleMap) :
    RecyclerView.Adapter<OpMapaAdapterHolder.OpMapaHolder>() {
    private var lista = listaRutasOpMapa.toList()
    private var context = contexto
    private var objmap = mapa

    inner class OpMapaHolder(vista: View, contexto: Context, mapa: GoogleMap) :
        RecyclerView.ViewHolder(vista) {
        private var binding = FormatoRecyclerBtnRutasBinding.bind(vista)
        private var contextoMapa = contexto
        private var objMapa = mapa
        private var colorSalida = binding.colorSalidaOpMapa
        private var colorLlegada = binding.colorLlegadaOpMapa
        private var claseRutas = PolylinesOpMapa(objMapa, contextoMapa)

        fun mostrar(numRuta: Int) {
            binding.numRutaOpMapa.text = "Ruta $numRuta"

            //el usuario selecciona una de las rutas
            binding.verRutaOpMapa.setOnClickListener {

                claseRutas.trazarRuta(numRuta)

                if (binding.verRutaOpMapa.text == "ver ruta") {
                    binding.verRutaOpMapa.text = "ocultar ruta"
                    binding.verRutaOpMapa.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.verde)
                } else {
                    binding.verRutaOpMapa.text = "ver ruta"
                    binding.verRutaOpMapa.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.btn_op_mapa)
                }

                when (numRuta) {
                    1 -> {
                        colorSalida.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.unoSalida)
                        colorLlegada.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.unoLlegada)
                    }

                    2 -> {
                        colorSalida.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.dosSalida)
                        colorLlegada.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.dosLlegada)
                    }

                    3 -> {
                        colorSalida.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.tresSalida)
                        colorLlegada.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.tresLlegada)
                    }

                    4 -> {
                        colorSalida.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.cuatroSalida)
                        colorLlegada.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.cuatroLlegada)
                    }

                    5 -> {
                        colorSalida.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.cincoSalida)
                        colorLlegada.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.cincoLlegada)
                    }

                    6 -> {
                        colorSalida.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.seisSalida)
                        colorLlegada.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.seisLlegada)
                    }

                    7 -> {
                        colorSalida.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.sieteSalida)
                        colorLlegada.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.sieteLlegada)
                    }

                    8 -> {
                        colorSalida.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.ochoSalida)
                        colorLlegada.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.ochoLlegada)
                    }

                    9 -> {
                        colorSalida.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.nueveSalida)
                        colorLlegada.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.nueveLlegada)
                    }

                    10 -> {
                        colorSalida.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.diezSalida)
                        colorLlegada.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.diezLlegada)
                    }

                    11 -> {
                        colorSalida.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.onceSalida)
                        colorLlegada.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.onceLlegada)
                    }

                    12 -> {
                        colorSalida.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.doceSalida)
                        colorLlegada.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.doceLlegada)
                    }

                    13 -> {
                        colorSalida.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.treceSalida)
                        colorLlegada.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.treceLlegada)
                    }

                    14 -> {
                        colorSalida.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.catorceSalida)
                        colorLlegada.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.catorceLlegada)
                    }
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpMapaHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.formato_recycler_btn_rutas, parent, false)
        return OpMapaHolder(view, context, objmap)
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    override fun onBindViewHolder(holder: OpMapaHolder, position: Int) {
        val item = lista[position]
        holder.mostrar(item)
    }

}
