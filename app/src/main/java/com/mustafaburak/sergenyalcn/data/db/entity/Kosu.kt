package com.mustafaburak.sergenyalcn.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kosular")
data class Kosu(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tarih: String,        // 2024-01-15
    val hipodrom: String,     // istanbul, ankara, izmir
    val kosuNo: Int,
    val mesafe: Int,          // metre
    val pistDurumu: String,   // kuru, iyi, agir
    val havaDurumu: String
)