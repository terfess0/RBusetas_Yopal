package com.terfess.busetasyopal.modelos_dato.reports_system

data class ResponseReportDato(
    var idResponse : String = "",
    val idReport : String = "",
    val dateResponse : String = "",
    val timeResponse : String = "",
    val textResponse : String = "",
    val authorResponse : String = "",
    val statusCheckedReceivedNoti: Boolean = false
)
