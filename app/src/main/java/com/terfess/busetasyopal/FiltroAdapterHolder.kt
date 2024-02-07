package com.terfess.busetasyopal

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.actividades.Mapa
import com.terfess.busetasyopal.databinding.FormatoRecyclerPrincBinding
import com.terfess.busetasyopal.modelos_dato.DatosListaFiltro

class FiltroAdapterHolder(var textoFiltro: String) :
    RecyclerView.Adapter<FiltroAdapterHolder.FiltroViewHolder>() {

    private var listaFiltro: List<DatosListaFiltro> = emptyList()


    inner class FiltroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding: FormatoRecyclerPrincBinding = FormatoRecyclerPrincBinding.bind(itemView)

        fun bind(item: DatosListaFiltro) {
            //acomodar margenes y objetos
            binding.tocaParaVer.visibility = View.GONE
            binding.contenedorHor.visibility = View.GONE
            binding.guideline2.setGuidelinePercent(1.0f)//se establece porcentage de 100% para que se adapte bien al contenido

            val spannableString = getSpannableString(item.Sitios, textoFiltro)
            binding.numRuta.text = "Ruta\n ${item.idRuta}"
            binding.sitios.text = spannableString

            //el usuario selecciona una de las rutas
            binding.contenedor.setOnClickListener {
                val selector = item.idRuta
                val intent = Intent(binding.contenedor.context, Mapa::class.java)
                intent.putExtra("selector", selector)
                binding.root.context.startActivity(intent)
            }
        }

        private fun getSpannableString(texto: String, filtro: String): SpannableString {
            val spannable = SpannableString(texto)
            val lowerTexto = texto.lowercase()
            val lowerFiltro = filtro.lowercase()

            var startIndex = lowerTexto.indexOf(lowerFiltro)

            while (startIndex != -1) {
                val endIndex = startIndex + filtro.length
                spannable.setSpan(
                    ForegroundColorSpan(Color.BLUE),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )

                // Agregar StyleSpan para hacer el texto en negrita
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )

                startIndex = lowerTexto.indexOf(lowerFiltro, endIndex)
            }

            return spannable
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FiltroViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.formato_recycler_princ, parent, false)
        return FiltroViewHolder(view)
    }

    override fun onBindViewHolder(holder: FiltroViewHolder, position: Int) {
        val item = listaFiltro[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return listaFiltro.size
    }

    fun actualizarLista(listaFiltro: List<DatosListaFiltro>, textoFiltro: String) {
        this.listaFiltro = listaFiltro
        this.textoFiltro = textoFiltro
        notifyDataSetChanged()
    }
}
