package com.terfess.busetasyopal.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedule",
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
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id_schedule: Int = 0,
    val period: String,
    val id_route: Int,
    val sche_start: String,
    val sche_end: String
)