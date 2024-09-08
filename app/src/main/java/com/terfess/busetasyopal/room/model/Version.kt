package com.terfess.busetasyopal.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "version"
)
data class Version(
    @PrimaryKey val id_version: Int = 1,
    val num_version: Int
)
