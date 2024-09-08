package com.terfess.busetasyopal.room.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "route",
    indices = [
        Index(value = ["id_route"], unique = true)
    ]
)
data class Route(
    @PrimaryKey val id_route: Int,
    val enabled: Boolean,
    val frec_mon_fri: String,
    val frec_sat: String,
    val frec_sun_holi: String,
    val sites:String,
    val sites_extended:String
)
