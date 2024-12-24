package com.terfess.busetasyopal.actividades.mapa.functions.calculate_route.adapterholder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.actividades.mapa.functions.MapFunctionOptions
import com.terfess.busetasyopal.actividades.mapa.functions.calculate_route.CalculateRoute
import com.terfess.busetasyopal.databinding.ItemRouteCalculateBinding

class AdapterHolderCalculates(
    var list: List<CalculateRoute.RouteCalculate>,
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


    fun notyList(newlist: List<CalculateRoute.RouteCalculate>) {
        this.list = newlist
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding: ItemRouteCalculateBinding = ItemRouteCalculateBinding.bind(itemView)

        fun bind(item: CalculateRoute.RouteCalculate, posItem: Int) {
            // Limpieza de mapa común para ambos casos
            binding.btnDetails.setOnClickListener {
                mapInstance.clear()
                if (item.isTransfer) {
                    handleTransferRoute(item)
                } else {
                    handleSingleRoute(item)
                }
            }

            println("INFO ITEM: ${item.idruta} ----- $item")
            // Configuración común
            binding.title.text = contextMap.getString(R.string.title_calculada, posItem.toString())

            if (item.isTransfer) {
                setupTransferRouteTexts(item)
            } else {
                setupSingleRouteTexts(item)
            }
        }

        private fun handleTransferRoute(item: CalculateRoute.RouteCalculate) {
            traceWalkRoutesTransfer(item)
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
                "Haz el recorrido en la buseta para bajarte en el punto marcado con: "
            binding.lastStep.text =
                "Camina hasta tomar el transbordo a la ruta #${item.idruta}"

            binding.imageLastStep1.setImageResource(R.drawable.ic_calculates_btn)
            binding.secondPartForTransfers.visibility = View.VISIBLE

            binding.descriptStep4.text = "Toma la buseta #${item.idruta} marcada con: "
            binding.descriptStep5.text =
                "Haz el recorrido en la buseta para bajarte en el punto marcado con: "
            binding.lastStep2.text =
                "Camina ${item.cutPoint2Ruta.distancia} metros hasta el punto destino marcado con: "
        }

        private fun setupSingleRouteTexts(item: CalculateRoute.RouteCalculate) {
            binding.secondPartForTransfers.visibility = View.GONE

            binding.descriptStep1.text =
                "Toma la buseta #${item.idruta} a ${item.cutPoint1Ruta.distancia} metros marcada con: "
            binding.descriptStep2.text =
                "Haz el recorrido en la buseta para bajarte en el punto marcado con: "
            binding.lastStep.text =
                "Camina ${item.cutPoint2Ruta.distancia} metros hasta el punto destino marcado: "
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

            instFunctions.traceWalkRoute(
                item.pointConectThisRoute!!,
                item.pointConectRutaAnterior!!,
                mapInstance,
                contextMap,
                isStart = true,
                addMarker = false
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



