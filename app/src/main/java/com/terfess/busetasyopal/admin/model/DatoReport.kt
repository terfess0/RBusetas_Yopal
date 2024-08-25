package com.terfess.busetasyopal.admin.model


data class DatoReport(
    var id : String = "",
    val dateReport : String = "",
    val timeReport : String = "",
    val situationReport : String = "",
    val location : String = "",
    val currentTask : String = "",
    val origin : String = ""
)
