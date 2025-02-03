package com.terfess.busetasyopal.actividades.mapa.functions.calculate_route.adapterholder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.actividades.mapa.functions.MapFunctionOptions
import com.terfess.busetasyopal.actividades.mapa.functions.calculate_route.CalculateRoute
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.ItemRouteCalculateBinding

class AdapterHolderCalculates(
    var list: MutableList<CalculateRoute.RouteCalculate>,
    context: Context,
    map: GoogleMap,
    instanceFunctions: MapFunctionOptions
) :
    RecyclerView.Adapter<AdapterHolderCalculates.ViewHolder>() {

    private val contextMap = context
    private val mapInstance = map
    private val instFunctions = instanceFunctions

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_route_calculate, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    fun notyList(newlist: MutableList<CalculateRoute.RouteCalculate>) {
        this.list = newlist
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding: ItemRouteCalculateBinding = ItemRouteCalculateBinding.bind(itemView)

        fun bind(item: CalculateRoute.RouteCalculate, posItem: Int) {
            binding.title.setOnClickListener {
                turnVisibilityContent(binding.containVisibilityObjects)
            }

            // Limpieza de mapa común para ambos casos
            binding.btnDetails.setOnClickListener {
                mapInstance.clear()


                // Manejar cada ruta calculada previniendo errores con try-catch
                if (item.isTransfer) {
                    try {
                        handleTransferRoute(item)
                    } catch (e: Exception) {
                        list.remove(item)
                        notyList(list)

                        UtilidadesMenores().crearAlertaSencilla(
                            contextMap,
                            "Se elimino la ruta calculada de la lista debido a un error inesperado."
                        )
                        println("Error: ${e.message.toString()}")
                    }
                } else {
                    try {
                        handleSingleRoute(item)
                    } catch (e: Exception) {
                        list.remove(item)
                        notyList(list)

                        UtilidadesMenores().crearAlertaSencilla(
                            contextMap,
                            "Se elimino la ruta calculada de la lista debido a un error. [${e.message.toString()}]"
                        )
                    }
                }
            }

            // Configuración común
            binding.title.text = contextMap.getString(R.string.title_calculada, item.idruta.toString())

            binding.btnDeleteCalculate.setOnClickListener {
                turnVisibilityContent(binding.containVisibilityObjects)
                list.remove(item)
                notyList(list)
            }

            if (item.isTransfer) {
                setupTransferRouteTexts(item)
            } else {
                setupSingleRouteTexts(item)
            }
        }

        private fun turnVisibilityContent(content: LinearLayout) {
            if (content.visibility == View.VISIBLE) {
                content.visibility = View.GONE
            } else {
                content.visibility = View.VISIBLE
            }
        }

        private fun handleTransferRoute(item: CalculateRoute.RouteCalculate) {
            traceWalkRoutesTransfer(item)

            mapInstance.addMarker(
                instFunctions.getOptionsMarker(
                    item.ubiStartGeneral,
                    R.drawable.ic_point_a_start,
                    "Punto A (Partida)"
                )
            )

            mapInstance.addMarker(
                instFunctions.getOptionsMarker(
                    item.ubiEndGeneral,
                    R.drawable.ic_point_b_end,
                    "Punto B (Destino)"
                )
            )

            traceRoute(
                item.points,
                item.cutPoint1Ruta.idPunto,
                item.cutPoint2Ruta.idPunto,
                R.color.diecisieteSalida
            )
            traceRoute(
                item.pointsAnteriorRoute!!,
                item.cutPoint1RutaAnterior!!.idPunto,
                item.cutPoint2RutaAnterior!!.idPunto,
                R.color.RutaCalculada
            )
        }

        private fun handleSingleRoute(item: CalculateRoute.RouteCalculate) {
            traceWalkRoutes(item)

            mapInstance.addMarker(
                instFunctions.getOptionsMarker(
                    item.ubiStartGeneral,
                    R.drawable.ic_point_a_start,
                    "Punto A (Partida)"
                )
            )

            mapInstance.addMarker(
                instFunctions.getOptionsMarker(
                    item.ubiEndGeneral,
                    R.drawable.ic_point_b_end,
                    "Punto B (Destino)"
                )
            )

            traceRoute(
                item.points,
                item.cutPoint1Ruta.idPunto,
                item.cutPoint2Ruta.idPunto,
                R.color.RutaCalculada
            )
        }

        private fun setupTransferRouteTexts(item: CalculateRoute.RouteCalculate) {
            binding.descriptStep1.text =
                contextMap.getString(
                    R.string.toma_buseta,
                    item.idRutaAnterior.toString(),
                    item.cutPoint1RutaAnterior?.distancia.toString()
                )

            binding.descriptStep2.text =
                contextMap.getString(R.string.step2_text_calculate)
            binding.lastStep.text =
                contextMap.getString(R.string.laststep1_text_calculate, item.idruta.toString())

            binding.imageLastStep1.setImageResource(R.drawable.ic_calculates_btn)
            binding.secondPartForTransfers.visibility = View.VISIBLE

            binding.descriptStep4.text =
                contextMap.getString(R.string.fourstep_text_calculate, item.idruta.toString())
            binding.descriptStep5.text =
                contextMap.getString(R.string.fivestep_text_calculate)
            binding.lastStep2.text =
                contextMap.getString(
                    R.string.laststep2_text_calculate,
                    item.cutPoint2Ruta.distancia.toString()
                )
        }

        private fun setupSingleRouteTexts(item: CalculateRoute.RouteCalculate) {
            binding.secondPartForTransfers.visibility = View.GONE

            binding.descriptStep1.text =
                contextMap.getString(
                    R.string.step1_single_text_calculate,
                    item.idruta.toString(),
                    item.cutPoint1Ruta.distancia.toString()
                )

            binding.descriptStep2.text =
                contextMap.getString(R.string.step2_single_text_calculate)

            binding.lastStep.text =
                contextMap.getString(
                    R.string.laststep1_single_text_calculate,
                    item.cutPoint2Ruta.distancia.toString()
                )
        }


        private fun traceWalkRoutes(
            item: CalculateRoute.RouteCalculate
        ) {
            val ptsRoute = item.points

            instFunctions.traceWalkRoute(
                item.ubiStartGeneral,
                ptsRoute[item.cutPoint1Ruta.idPunto],
                mapInstance,
                contextMap,
                true,
                addMarker = true
            )

            instFunctions.traceWalkRoute(
                item.ubiEndGeneral,
                ptsRoute[item.cutPoint2Ruta.idPunto],
                mapInstance,
                contextMap,
                false,
                addMarker = true
            )
        }

        private fun traceWalkRoutesTransfer(
            item: CalculateRoute.RouteCalculate
        ) {
            val ptsRoute = item.points
            instFunctions.traceWalkRoute(
                item.ubiStartGeneral,
                item.pointsAnteriorRoute!![item.cutPoint1RutaAnterior!!.idPunto],
                mapInstance,
                contextMap,
                isStart = true,
                addMarker = true
            )

            instFunctions.traceWalkTransferRoute(
                item.pointConectThisRoute!!,
                item.pointConectRutaAnterior!!,
                mapInstance,
                contextMap
            )

            instFunctions.traceWalkRoute(
                item.ubiEndGeneral,
                ptsRoute[item.cutPoint2Ruta.idPunto],
                mapInstance,
                contextMap,
                isStart = false,
                addMarker = true
            )
        }

        private fun traceRoute(
            puntos: List<LatLng>,
            ptCut1: Int,
            ptCut2: Int,
            colorRuta: Int
        ) {
            instFunctions.traceSectionRoute(
                puntos,
                ptCut1,
                ptCut2,
                contextMap,
                mapInstance,
                colorRuta
            )
        }
    }
}



