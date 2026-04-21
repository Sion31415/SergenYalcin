package com.mustafaburak.sergenyalcn.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kosu_sonuclari")
data class KosuSonucu(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val kosuId: Int,
    val atId: Int,
    val atIsmi: String,       // İŞTE BİZİM EKSİK OLAN VE HATAYI ÇÖZECEK SATIR BURASI!
    val jokey: String,
    val antrenor: String,
    val sahip: String,
    val siklet: Float,
    val derece: String,       // Örn: 1.38.32
    val gny: Float,           // Ganyan oranı
    val agf: Float,
    val startNo: Int,
    val fark: String,         // Örn: 2 Boy
    val gikis: Int,           // Giriş sırası (G. Çık)
    val hp: Int,              // Handikap puanı
    val sonuc: Int,           // Kaçıncı olduğu (S)
    val yas: String,          // Örn: 3y d e
    val orijin: String        // Örn: KLIMT (USA) - SHANTI (GER)
)