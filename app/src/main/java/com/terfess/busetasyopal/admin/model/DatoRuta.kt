package com.terfess.busetasyopal.admin.model


data class DatoRuta(
    val numRuta: List<Int> = emptyList(),
    val horarioLunVie: List<String> = emptyList(),
    val sitios: String? = null,
    val frecuenciaLunVie: String = "",
    val frecuenciaSab: String = "",
    val frecuenciaDomFest: String = "",
    val horarioSab: List<String> = emptyList(),
    val horarioDomFest: List<String> = emptyList(),
    val enabled: Boolean = false
)
