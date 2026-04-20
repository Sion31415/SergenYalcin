package com.mustafaburak.sergenyalcn.ml

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

class AgirlikYoneticisi(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "agirliklar", Context.MODE_PRIVATE
    )

    // Başlangıç ağırlıkları
    private val varsayilanAgirliklar = mapOf(
        "gecmisDerece" to 0.30f,
        "jokey" to 0.25f,
        "antrenor" to 0.15f,
        "siklet" to 0.10f,
        "pistUyum" to 0.10f,
        "mesafeUyum" to 0.10f
    )

    fun getAgirlik(anahtar: String): Float {
        return prefs.getFloat(anahtar, varsayilanAgirliklar[anahtar] ?: 0.10f)
    }

    fun tumAgirliklar(): Map<String, Float> {
        return varsayilanAgirliklar.keys.associateWith { getAgirlik(it) }
    }

    // Doğru tahmin → ağırlığı artır
    // Yanlış tahmin → ağırlığı azalt
    fun guncelle(dogruFaktorler: List<String>, yanlisFaktorler: List<String>) {
        val editor = prefs.edit()
        val ogrenmeHizi = 0.01f

        dogruFaktorler.forEach { faktor ->
            val mevcutAgirlik = getAgirlik(faktor)
            editor.putFloat(faktor, (mevcutAgirlik + ogrenmeHizi).coerceAtMost(0.50f))
        }

        yanlisFaktorler.forEach { faktor ->
            val mevcutAgirlik = getAgirlik(faktor)
            editor.putFloat(faktor, (mevcutAgirlik - ogrenmeHizi).coerceAtLeast(0.05f))
        }

        // Toplam 1.0 olacak şekilde normalize et
        editor.apply()
        normalize()
    }

    private fun normalize() {
        val agirliklar = tumAgirliklar()
        val toplam = agirliklar.values.sum()
        val editor = prefs.edit()
        agirliklar.forEach { (anahtar, deger) ->
            editor.putFloat(anahtar, deger / toplam)
        }
        editor.apply()
    }

    fun sifirla() {
        val editor = prefs.edit()
        varsayilanAgirliklar.forEach { (anahtar, deger) ->
            editor.putFloat(anahtar, deger)
        }
        editor.apply()
    }
}