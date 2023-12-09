package com.terfess.busetasyopal.modelos_dato

import com.google.android.gms.maps.model.LatLng

data class EstructuraDatosBaseDatos(
    val idRuta: Int,
    val listPrimeraParte: MutableList<LatLng>,
    val listSegundaParte: MutableList<LatLng>
)
