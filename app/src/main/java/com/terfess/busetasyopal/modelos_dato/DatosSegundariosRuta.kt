package com.terfess.busetasyopal.modelos_dato

import com.google.android.gms.maps.model.LatLng

data class DatosSegundariosRuta(
    val totalPuntos:List<LatLng>,
    val idTrazo:Int
)
