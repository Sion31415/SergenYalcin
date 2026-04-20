package com.mustafaburak.sergenyalcn.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kosu_sonuclari")
data class KosuSonucu(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val kosuId: Int,
    val atId: Int,
    val jokey: String,
    val antrenor: String,
    val sahip: String,
    val siklet: Float,
    val derece: String,       // 0.52.29
    val gny: Float,           // ganyan oranı
    val agf: Float,
    val startNo: Int,
    val fark: String,         // 2 Boy, Boyun vs
    val gikis: Int,           // giriş sırası
    val hp: Int,              // handikap puanı
    val sonuc: Int            // 1. 2. 3. vs
)