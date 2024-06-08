package com.terfess.busetasyopal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.terfess.busetasyopal.clases_utiles.PolylinesOpMapa
import com.terfess.busetasyopal.databinding.FormatoRecyclerBtnRutasBinding
import com.terfess.busetasyopal.modelos_dato.DatoOpMapa

class OpMapaAdapterHolder(
    private val listaRutasOpMapa: MutableList<DatoOpMapa>,
    mapa: GoogleMap,
    private val contexto: Context
) : RecyclerView.Adapter<OpMapaAdapterHolder.ViewHolder>() {
    private var claseRutas = PolylinesOpMapa(mapa, contexto)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = FormatoRecyclerBtnRutasBinding.bind(itemView)

        //recuperar elementos del layout
        val textView = binding.numRutaOpMapa
        val boton= binding.verRutaOpMapa
        val salidaColoreable = binding.colorSalidaOpMapa
        val llegadaColoreable = binding.colorLlegadaOpMapa
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.formato_recycler_btn_rutas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datoOpMapa = listaRutasOpMapa[position]
        val numRuta = datoOpMapa.id

        //agregar el texto numero de ruta
        holder.textView.text = contexto.getString(R.string.ruta_y_numero, numRuta.toString())

        //recuperar el boton ver ruta/ocultar
        val boton = holder.boton

        //verificar si la lista tiene elementos
        if (listaRutasOpMapa.isNotEmpty()) {
            //dependiendo del estado se seleccionado se le dara el color inicial a el boton y a los indicadores de color salida y llegada
            if (datoOpMapa.seleccionado == 1) {
                //dar color a los indicadores salida y llegada
                darColorFondo(holder, datoOpMapa.colorIda, datoOpMapa.colorVuelta)
                //dar texto y color al boton
                boton.text = contexto.getString(R.string.ocultar)
                boton.backgroundTintList = ContextCompat.getColorStateList(contexto, R.color.verde)


            } else {


                //dar color a los indicadores salida y llegada
                darColorFondo(holder, R.color.ida_venida_op_mapa, R.color.ida_venida_op_mapa)
                //dar texto y color al boton
                boton.text = contexto.getString(R.string.ver_ruta)
                boton.backgroundTintList =
                    ContextCompat.getColorStateList(contexto, R.color.enfasis_azul)
            }
        }

        //el usuario toca el boton ver ruta/ocultar
        boton.setOnClickListener {
            //trazar la poliline correspondiente a la ruta donde se toco el boton
            claseRutas.trazarRuta(numRuta)

            //cambiar los texto y color de boton asi como los de indicadores salida y llegada colores
            cambiarColorSegunRuta(position, holder)
        }
    }

    override fun getItemCount(): Int {
        return listaRutasOpMapa.size
    }

    private fun cambiarColorSegunRuta(position: Int, holder: ViewHolder) {
        val numRuta = listaRutasOpMapa[position].id
        val boton = holder.boton

        //cambiar texto y color del boton pulsado
        if (boton.text == contexto.getString(R.string.ver_ruta)) {

            boton.text = contexto.getString(R.string.ocultar)
            boton.backgroundTintList = ContextCompat.getColorStateList(contexto, R.color.verde)
        } else {

            boton.text = contexto.getString(R.string.ver_ruta)
            boton.backgroundTintList =
                ContextCompat.getColorStateList(contexto, R.color.enfasis_azul)
        }

        //preparar los colores para los indicadores
        val routeColors = mapOf(
            1 to Pair(R.color.unoSalida, R.color.unoLlegada),
            2 to Pair(R.color.dosSalida, R.color.dosLlegada),
            3 to Pair(R.color.tresSalida, R.color.tresLlegada),
            6 to Pair(R.color.seisSalida, R.color.seisLlegada),
            7 to Pair(R.color.sieteSalida, R.color.sieteLlegada),
            8 to Pair(R.color.ochoSalida, R.color.ochoLlegada),
            9 to Pair(R.color.nueveSalida, R.color.nueveLlegada),
            10 to Pair(R.color.diezSalida, R.color.diezLlegada),
            11 to Pair(R.color.onceSalida, R.color.onceLlegada),
            13 to Pair(R.color.treceSalida, R.color.treceLlegada),
            14 to Pair(R.color.catorceSalida, R.color.catorceLlegada)
        )

        val colors = routeColors[numRuta]
        //llamada a funcion para cambiar colores de indicadores salida y llegada
        if (colors != null) {
            estaSeleccionado(holder, colors.first, colors.second, numRuta, position)
        }
    }

    private fun darColorFondo(holder: ViewHolder, dirColor: Int, dirColor2: Int) {
        //funcion para cambiar los colores de los indicadores de salida y llegada
        holder.salidaColoreable.backgroundTintList =
            ContextCompat.getColorStateList(contexto, dirColor)

        holder.llegadaColoreable.backgroundTintList =
            ContextCompat.getColorStateList(contexto, dirColor2)
    }

    private fun estaSeleccionado(
        holder: ViewHolder,
        dirColor: Int,
        dirColor2: Int,
        idRuta: Int,
        position: Int
    ) {
        val estaSeleccionado = listaRutasOpMapa[position].seleccionado

        //actualizar los elementos de la lista cuando el boton es pulsado (tambien cambia colores a indicadores)
        if (estaSeleccionado == 0) {
            listaRutasOpMapa[position] =
                DatoOpMapa(idRuta, 1, dirColor, dirColor2)
            darColorFondo(holder, dirColor, dirColor2)
        } else if (estaSeleccionado == 1) {
            listaRutasOpMapa[position] =
                DatoOpMapa(idRuta, 0, R.color.ida_venida_op_mapa, R.color.ida_venida_op_mapa)
            darColorFondo(holder, R.color.ida_venida_op_mapa, R.color.ida_venida_op_mapa)
        }
    }
}
