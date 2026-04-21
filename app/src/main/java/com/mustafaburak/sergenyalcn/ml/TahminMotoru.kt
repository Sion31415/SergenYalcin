package com.mustafaburak.sergenyalcn.ml

import com.mustafaburak.sergenyalcn.data.db.entity.KosuSonucu

class TahminMotoru(private val skorHesaplayici: SkorHesaplayici) {

    fun kosuTahmini(
        atlar: List<AtGirisi>,
        tumSonuclar: List<KosuSonucu>,
        pistDurumu: String,
        mesafe: Int,
        pistMap: Map<Int, String>,
        mesafeMap: Map<Int, Int>,
        tarihMap: Map<Int, String> = emptyMap()  // ✅ YENİ parametre (default boş — geriye dönük uyumlu)
    ): List<AtSkoru> {
        val sikletler = atlar.map { it.siklet }

        // 1. ADIM: Ham skorları hesapla
        val hamSkorlar = atlar.map { at ->
            skorHesaplayici.toplamSkorHesapla(
                atId                  = at.atId,
                atIsmi                = at.atIsmi,
                yas                   = at.yas,
                orijin                = at.orijin,
                startNo               = at.startNo,
                jokey                 = at.jokey,
                antrenor              = at.antrenor,
                siklet                = at.siklet,
                kosudakiTumSikletler  = sikletler,
                pistDurumu            = pistDurumu,
                mesafe                = mesafe,
                tumSonuclar           = tumSonuclar,
                pistMap               = pistMap,
                mesafeMap             = mesafeMap,
                tarihMap              = tarihMap   // ✅ iletildi
            )
        }

        // 2. ADIM: Toplam güç havuzu
        val toplamGucHavuzu = hamSkorlar.sumOf { it.toplamSkor.toDouble() }.toFloat()

        // 3. ADIM: Normalize et → %100 üzerinden paylaştır
        return hamSkorlar.map { skor ->
            val gercekYuzde = if (toplamGucHavuzu > 0f) {
                (skor.toplamSkor / toplamGucHavuzu) * 100f
            } else {
                100f / atlar.size
            }
            skor.copy(kazanmaTahmini = gercekYuzde)
        }.sortedByDescending { it.toplamSkor }
    }

    // Öğrenme — gerçek sonuç geldikten sonra ağırlıkları güncelle
    fun ogren(
        tahminEdilen: AtSkoru,
        gercekBirinci: Int,
        agirlikYoneticisi: AgirlikYoneticisi
    ) {
        val dogruTahmin = tahminEdilen.atId == gercekBirinci

        val faktorler = mutableListOf<String>()
        if (tahminEdilen.gecmisDereceSkoru > 0.6f) faktorler.add("gecmisDerece")
        if (tahminEdilen.jokeySkoru        > 0.6f) faktorler.add("jokey")
        if (tahminEdilen.antrenorSkoru     > 0.6f) faktorler.add("antrenor")
        if (tahminEdilen.sikletSkoru       > 0.6f) faktorler.add("siklet")
        if (tahminEdilen.pistUyumSkoru     > 0.6f) faktorler.add("pistUyum")
        if (tahminEdilen.mesafeUyumSkoru   > 0.6f) faktorler.add("mesafeUyum")

        if (dogruTahmin) {
            agirlikYoneticisi.guncelle(faktorler, emptyList())
        } else {
            agirlikYoneticisi.guncelle(emptyList(), faktorler)
        }
    }
}

data class AtGirisi(
    val atId: Int,
    val atIsmi: String,
    val yas: String,
    val orijin: String,
    val startNo: Int,
    val jokey: String,
    val antrenor: String,
    val siklet: Float
)