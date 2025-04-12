package com.terfess.busetasyopal.modelos_dato.reports_system


data class DatoReport(
    var id : String = "",
    val dateReport : String = "",
    val timeReport : String = "",
    val situationReport : String = "",
    val location : String = "",
    val currentTask : String = "",
    val idUser : String = "",
    val statusCheckedView:Boolean = false,
    val hasResponse : Boolean = false
)