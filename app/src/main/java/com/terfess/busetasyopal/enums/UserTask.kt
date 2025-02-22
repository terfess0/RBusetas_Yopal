package com.terfess.busetasyopal.enums

enum class UserTask(val messageValue: String) {
    USER_IN_MAIN_SCREEN("Viendo la pantalla principal"),
    USER_IS_SEARCH_IN_FILTER("En filtro de sitios y rutas"),
    USER_IS_IN_OPTION_ALL_ROUTES_MAP("Viendo mapa con todas las rutas"),
    USER_IS_VIEWING_ROUTE_MAP("Viendo mapa con la ruta: "),
    USER_IS_IN_OPTION_PARKINGS_ROUTES_MAP("Viendo mapa con parqueaderos rutas"),
    USER_IS_IN_OPTION_CALCULATE_ROUTE_MAP("Calculando ruta")
}

fun getRouteOnTask(idRuta: Int): String {
    return UserTask.USER_IS_VIEWING_ROUTE_MAP.messageValue + idRuta
}
