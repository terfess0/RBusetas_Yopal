package com.terfess.busetasyopal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.terfess.busetasyopal.clases_utiles.PolylinesOpMapa
import com.terfess.busetasyopal.databinding.FormatoRecyclerBtnRutasBinding
import com.terfess.busetasyopal.modelos_dato.DatoOpMapa

class OpMapaAdapterHolder(
    private val listaRutasOpMapa: MutableList<DatoOpMapa>,
    mapa: GoogleMap,
    private val contexto: Context
) : BaseAdapter() {
    private val inflater = contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var contador = 0
    private var claseRutas = PolylinesOpMapa(mapa, contexto)

    override fun getCount(): Int {
        return listaRutasOpMapa.size
    }

    override fun getItem(position: Int): Any {
        return listaRutasOpMapa[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(R.layout.formato_recycler_btn_rutas, null)
        val binding = FormatoRecyclerBtnRutasBinding.bind(view)
        val datoOpMapa = listaRutasOpMapa[position]

        val numRuta = datoOpMapa.id

        // Obtener el TextView del diseño
        val textView = view.findViewById<TextView>(R.id.numRutaOpMapa)

        println("Esta seleccionado? ${listaRutasOpMapa[position].seleccionado}")
        val boton = binding.verRutaOpMapa
        if (listaRutasOpMapa.isNotEmpty() && listaRutasOpMapa[position].seleccionado == 1) {
            val dato = listaRutasOpMapa[position]

            darColorFondo(binding, dato.colorIda, dato.colorVuelta)

            boton.text = "ocultar"
            boton.backgroundTintList = ContextCompat.getColorStateList(contexto, R.color.verde)
        }

        if (listaRutasOpMapa[position].seleccionado == 0) {

            darColorFondo(binding, R.color.ida_venida_op_mapa, R.color.ida_venida_op_mapa)

            boton.text = "ver ruta"
            boton.backgroundTintList =
                ContextCompat.getColorStateList(contexto, R.color.enfasis_azul)

        }

        // Configurar el texto del TextView con los datos del elemento actual
        textView.text = "Ruta $numRuta"

        // Configurar el OnClickListener
        val button = view.findViewById<Button>(R.id.verRutaOpMapa)
        button.setOnClickListener {
            this.contador = 1
            claseRutas.trazarRuta(numRuta)
            // Cambiar el color u otras propiedades visuales
            cambiarColorSegunRuta(position, binding)
        }

        return view
    }

    private fun cambiarColorSegunRuta(
        posicion: Int,
        binding: FormatoRecyclerBtnRutasBinding
    ) {
        val numRuta = listaRutasOpMapa[posicion].id

        val boton = binding.verRutaOpMapa
        if (boton.text == "ver ruta") {
            boton.text = "ocultar"
            boton.backgroundTintList = ContextCompat.getColorStateList(contexto, R.color.verde)
        } else {
            boton.text = "ver ruta"
            boton.backgroundTintList =
                ContextCompat.getColorStateList(contexto, R.color.enfasis_azul)
        }

        when (numRuta) {

            2 -> {
                darColorFondo(binding, R.color.dosSalida, R.color.dosLlegada)
                estaSeleccionado(R.color.dosSalida, R.color.dosLlegada, numRuta, posicion)
            }

            3 -> {
                darColorFondo(binding, R.color.tresSalida, R.color.tresLlegada)
                estaSeleccionado(R.color.tresSalida, R.color.tresLlegada, numRuta, posicion)
            }

            6 -> {
                darColorFondo(binding, R.color.seisSalida, R.color.seisLlegada)
                estaSeleccionado(R.color.seisSalida, R.color.seisLlegada, numRuta, posicion)
            }

            7 -> {
                darColorFondo(binding, R.color.sieteSalida, R.color.sieteLlegada)
                estaSeleccionado(R.color.sieteSalida, R.color.sieteLlegada, numRuta, posicion)
            }

            8 -> {
                darColorFondo(binding, R.color.ochoSalida, R.color.ochoLlegada)
                estaSeleccionado(R.color.ochoSalida, R.color.ochoLlegada, numRuta, posicion)
            }

            9 -> {
                darColorFondo(binding, R.color.nueveSalida, R.color.nueveLlegada)
                estaSeleccionado(R.color.nueveSalida, R.color.nueveLlegada, numRuta, posicion)
            }

            10 -> {
                darColorFondo(binding, R.color.diezSalida, R.color.diezLlegada)
                estaSeleccionado(R.color.diezSalida, R.color.diezLlegada, numRuta, posicion)
            }

            11 -> {
                darColorFondo(binding, R.color.onceSalida, R.color.onceLlegada)
                estaSeleccionado(R.color.onceSalida, R.color.onceLlegada, numRuta, posicion)
            }


            13 -> {
                darColorFondo(binding, R.color.treceSalida, R.color.treceLlegada)
                estaSeleccionado(R.color.treceSalida, R.color.treceLlegada, numRuta, posicion)
            }
        }
    }

    private fun darColorFondo(binding: FormatoRecyclerBtnRutasBinding, dirColor: Int, dirColor2: Int) {
        binding.colorSalidaOpMapa.backgroundTintList =
            ContextCompat.getColorStateList(contexto, dirColor)
        binding.colorLlegadaOpMapa.backgroundTintList =
            ContextCompat.getColorStateList(contexto, dirColor2)
    }

    private fun estaSeleccionado(dirColor: Int, dirColor2: Int, idRuta:Int, posicion:Int){
        val estaSeleccionado = listaRutasOpMapa[posicion].seleccionado

        if (estaSeleccionado == 0) {
            listaRutasOpMapa[posicion] =
                DatoOpMapa(idRuta, 1, dirColor, dirColor2)
        } else if (estaSeleccionado == 1){
            listaRutasOpMapa[posicion]= DatoOpMapa(idRuta, 0, R.color.ida_venida_op_mapa, R.color.ida_venida_op_mapa)
        }
    }

}


