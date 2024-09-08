package com.terfess.busetasyopal.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "coordinate",
    indices = [Index(value = ["id_route"])],
    foreignKeys = [
        ForeignKey(
            entity = Route::class,
            parentColumns = ["id_route"],
            childColumns = ["id_route"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Coordinate(
    @PrimaryKey(autoGenerate = true) val id_coord: Int = 0,
    val type_path: String,
    val id_route: Int,
    val latitude: Double,
    val longitude: Double
)