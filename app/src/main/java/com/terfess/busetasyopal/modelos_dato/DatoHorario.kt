package com.terfess.busetasyopal.modelos_dato

data class DatoHorario (
    var numRuta:Int,
    var horaInicioLunesViernes:String,
    var horaFinalLunesViernes:String,
    var horaInicioSab:String,
    var horaFinalSab:String,
    var horaInicioDom:String,
    var horaFinalDom:String
)