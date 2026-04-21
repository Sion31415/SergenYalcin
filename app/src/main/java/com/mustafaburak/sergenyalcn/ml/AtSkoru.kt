package com.mustafaburak.sergenyalcn.ml

// Motorun ürettiği tüm zeki verileri tutacak olan Güncel Karne
data class AtSkoru(
    val atId: Int,
    val atIsmi: String,          // Hata veren kısım düzeltildi
    val yas: String = "",
    val orijin: String = "",
    val startNo: Int = 0,
    val siklet: Float = 0f,      // Hata veren kısım düzeltildi
    val jokey: String,
    val antrenor: String,

    // Zeka Skorları
    val gecmisDereceSkoru: Float,
    val jokeySkoru: Float,
    val antrenorSkoru: Float,
    val sikletSkoru: Float,
    val pistUyumSkoru: Float,
    val mesafeUyumSkoru: Float,

    // Final Sonuçları
    val toplamSkor: Float,
    val kazanmaTahmini: Float
)