package com.terfess.busetasyopal.actividades.mapa.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.terfess.busetasyopal.actividades.mapa.functions.calculate_route.PlanearRutaDestino
import com.terfess.busetasyopal.callbacks.mapa.CalculateRoute
import com.terfess.busetasyopal.actividades.mapa.functions.calculate_route.CalculateRoute.RouteCalculate as RouteCalculate1

class ViewModelMapa : ViewModel(),
    CalculateRoute {
    var resultCalculate =
        MutableLiveData<com.terfess.busetasyopal.actividades.mapa.functions.calculate_route.CalculateRoute.ResultCalculate>()

    suspend fun calculateRoute(
        ubiStart: LatLng,
        ubiEnd: LatLng,
        context: Context
    ) {
        PlanearRutaDestino(context).rutaToDestino(
            ubiStart,
            ubiEnd,
            this
        )
    }


    override fun onResult(
        result: Boolean,
        routes: List<RouteCalculate1>
    ) {
        resultCalculate.postValue(
            com.terfess.busetasyopal.actividades.mapa.functions.calculate_route.CalculateRoute.ResultCalculate(
                result,
                routes
            )
        )
    }
}