package com.mustafaburak.sergenyalcn.ml

data class AtSkoru(
    val atId: Int,
    val atIsmi: String,
    val startNo: Int,
    val jokey: String,
    val toplamSkor: Float,
    val gecmisDereceSkoru: Float,
    val antrenor: String,
    val jokeySkoru: Float,
    val antrenorSkoru: Float,
    val sikletSkoru: Float,
    val pistUyumSkoru: Float,
    val mesafeUyumSkoru: Float,
    val kazanmaTahmini: Float  // yüzde olarak
)