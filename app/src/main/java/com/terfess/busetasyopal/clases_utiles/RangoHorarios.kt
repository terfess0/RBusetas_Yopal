package com.terfess.busetasyopal.clases_utiles

import android.os.Build
import java.util.*
import java.time.LocalTime

class RangoHorarios {
    fun busetaEnServicio(horaInicio: String, horaFinal: String): Int {
        //obtener hora actual
        val horaActual = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalTime.now()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val horaInicioTipoLT = LocalTime.parse(horaInicio)
        var horaFinalTipoLT = LocalTime.parse(horaFinal)

        // agregar 12 horas a la hora final debido a que se usan los mismos datos que se presentan en ui y estan en am y pm
        if (horaFinal != "00:00" && horaFinal != "12:00") {
            horaFinalTipoLT = horaFinalTipoLT.plusHours(12)
        }

        //comprueba si la hora actual está dentro del rango
        // LT es LocalTime
        return if (horaActual.isAfter(horaInicioTipoLT) && horaActual.isBefore(
                horaFinalTipoLT
            )
        ) {
            1
        } else {
            0
        }
    }

    fun busetaEnDia(): Int {
        val calendario = Calendar.getInstance()
        return calendario.get(Calendar.DAY_OF_WEEK)
    }

    fun afterStartHour(horaInicio: String): Boolean {
        //obtener hora actual
        val horaActual = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalTime.now()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val horaInicioTipoLT = LocalTime.parse(horaInicio)


        //comprueba si la hora actual está dentro del rango
        // LT es LocalTime
        return if (horaActual.isAfter(horaInicioTipoLT)
        ) {
            true
        } else {
            false
        }
    }

    fun afterEndHour(horaInicio: String): Boolean {
        //obtener hora actual
        val horaActual = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalTime.now()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val horaFinalTipoLT = LocalTime.parse(horaInicio)
        val horaComp = horaFinalTipoLT.plusHours(12)

        //comprueba si la hora actual está dentro del rango
        // LT es LocalTime
        return horaActual.isAfter(horaComp)
    }
}