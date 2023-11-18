package com.terfess.busetasyopal

import com.google.android.gms.maps.model.LatLng

data class DatosSegundariosRuta(
    val TotalPuntos:List<LatLng>,
    val idTrazo:Int
)
