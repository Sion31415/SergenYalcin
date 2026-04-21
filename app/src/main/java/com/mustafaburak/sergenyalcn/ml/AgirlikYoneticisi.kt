package com.mustafaburak.sergenyalcn.ml

import android.content.Context
import android.content.SharedPreferences

class AgirlikYoneticisi(context: Context) {
    // Modelin hafızası: Öğrendikçe bu oranları güncelleyeceğiz
    private val prefs: SharedPreferences = context.getSharedPreferences("SergenYalcinML", Context.MODE_PRIVATE)

    fun agirliklariGetir(): Map<String, Float> {
        return mapOf(
            "gecmisDerece" to prefs.getFloat("gecmisDerece", 0.15f), // %15
            "jokey" to prefs.getFloat("jokey", 0.15f),               // %15
            "antrenor" to prefs.getFloat("antrenor", 0.10f),         // %10
            "siklet" to prefs.getFloat("siklet", 0.10f),             // %10
            "pistUyum" to prefs.getFloat("pistUyum", 0.15f),         // %15
            "mesafeUyum" to prefs.getFloat("mesafeUyum", 0.15f),     // %15
            "jokeyAtUyum" to prefs.getFloat("jokeyAtUyum", 0.10f),   // %10 (YENİ)
            "dinleniklik" to prefs.getFloat("dinleniklik", 0.10f)    // %10 (YENİ)
        )
    }

    // Makine Öğrenimi: Doğru tahmin ettikçe başarılı faktörlerin ağırlığını artırır
    fun guncelle(gucluFaktorler: List<String>, zayifFaktorler: List<String>) {
        val editor = prefs.edit()
        gucluFaktorler.forEach { faktor ->
            val mevcut = prefs.getFloat(faktor, 0.15f)
            editor.putFloat(faktor, (mevcut + 0.01f).coerceAtMost(0.40f))
        }
        zayifFaktorler.forEach { faktor ->
            val mevcut = prefs.getFloat(faktor, 0.15f)
            editor.putFloat(faktor, (mevcut - 0.01f).coerceAtLeast(0.05f))
        }
        editor.apply()
    }
}