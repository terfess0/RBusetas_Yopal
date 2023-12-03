package com.terfess.busetasyopal.modelos_dato

data class DatoHorario (
    val numRuta:Int,
    val horaInicioLunesViernes:String,
    val horaFinalLunesViernes:String,
    val horaInicioSab:String,
    val horaFinalSab:String,
    val horaInicioDom:String,
    val horaFinalDom:String
)