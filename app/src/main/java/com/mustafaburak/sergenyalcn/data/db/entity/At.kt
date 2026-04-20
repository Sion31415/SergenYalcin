package com.mustafaburak.sergenyalcn.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "atlar")
data class At(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val isim: String,
    val yas: Int,
    val cinsiyet: String, // e, d, k
    val baba: String = "",
    val anne: String = ""
)