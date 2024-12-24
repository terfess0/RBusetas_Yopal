package com.terfess.busetasyopal.callbacks.mapa

import com.terfess.busetasyopal.actividades.mapa.functions.calculate_route.CalculateRoute

interface CalculateRoute {
    fun onResult(result: Boolean, routes: MutableList<CalculateRoute.RouteCalculate>)
}