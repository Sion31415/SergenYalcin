package com.mustafaburak.sergenyalcn.ml

import com.mustafaburak.sergenyalcn.data.db.entity.KosuSonucu

class TahminMotoru(private val skorHesaplayici: SkorHesaplayici) {

    // Bir koşudaki tüm atları skorla ve sırala
    fun kosuTahmini(
        atlar: List<AtGirisi>,
        tumSonuclar: List<KosuSonucu>,
        pistDurumu: String,
        mesafe: Int,
        pistMap: Map<Int, String>,
        mesafeMap: Map<Int, Int>
    ): List<AtSkoru> {
        val sikletler = atlar.map { it.siklet }

        return atlar.map { at ->
            skorHesaplayici.toplamSkorHesapla(
                atId = at.atId,
                atIsmi = at.atIsmi,
                startNo = at.startNo,
                jokey = at.jokey,
                antrenor = at.antrenor,
                siklet = at.siklet,
                kosudakiTumSikletler = sikletler,
                pistDurumu = pistDurumu,
                mesafe = mesafe,
                tumSonuclar = tumSonuclar,
                pistMap = pistMap,
                mesafeMap = mesafeMap
            )
        }.sortedByDescending { it.toplamSkor }
    }

    // Ganyan — en yüksek skorlu at
    fun ganyanTahmini(skorlar: List<AtSkoru>): AtSkoru? {
        return skorlar.firstOrNull()
    }

    // Plase — ilk 3
    fun plaseTahmini(skorlar: List<AtSkoru>): List<AtSkoru> {
        return skorlar.take(3)
    }

    // Tabela — ilk 4
    fun tabelaTahmini(skorlar: List<AtSkoru>): List<AtSkoru> {
        return skorlar.take(4)
    }

    // İkili — 2 koşuda kazananlar
    fun ikiliTahmini(
        kosu1Skorlar: List<AtSkoru>,
        kosu2Skorlar: List<AtSkoru>
    ): Pair<AtSkoru?, AtSkoru?> {
        return Pair(
            kosu1Skorlar.firstOrNull(),
            kosu2Skorlar.firstOrNull()
        )
    }

    // Sıralı ikili — 1. ve 2. sıradaki at
    fun siraliIkiliTahmini(skorlar: List<AtSkoru>): Pair<AtSkoru?, AtSkoru?> {
        return Pair(
            skorlar.getOrNull(0),
            skorlar.getOrNull(1)
        )
    }

    // Üçlü — 3 koşuda kazananlar
    fun ucluTahmini(
        kosu1Skorlar: List<AtSkoru>,
        kosu2Skorlar: List<AtSkoru>,
        kosu3Skorlar: List<AtSkoru>
    ): Triple<AtSkoru?, AtSkoru?, AtSkoru?> {
        return Triple(
            kosu1Skorlar.firstOrNull(),
            kosu2Skorlar.firstOrNull(),
            kosu3Skorlar.firstOrNull()
        )
    }

    // Altılı — 6 koşuda kazananlar + alternatifler
    fun altiliTahmini(tumKosularSkorlari: List<List<AtSkoru>>): AltiliSonuc {
        val anaSecimler = tumKosularSkorlari.map { it.firstOrNull() }
        val alternatifler = tumKosularSkorlari.map { it.getOrNull(1) }
        val kombinasyonSayisi = tumKosularSkorlari.fold(1) { acc, liste ->
            acc * minOf(liste.size, 3)
        }
        return AltiliSonuc(
            anaSecimler = anaSecimler,
            alternatifler = alternatifler,
            kombinasyonSayisi = kombinasyonSayisi,
            tahminiMaliyet = kombinasyonSayisi * 2.0f // 2 TL per kombinasyon
        )
    }

    // Öğrenme — gerçek sonuç geldikten sonra ağırlıkları güncelle
    fun ogren(
        tahminEdilen: AtSkoru,
        gercekBirinci: Int, // atId
        agirlikYoneticisi: AgirlikYoneticisi
    ) {
        val dogruTahmin = tahminEdilen.atId == gercekBirinci

        if (dogruTahmin) {
            // Hangi faktörler yüksekti → bunlar doğru çalışıyor
            val gucluFaktorler = mutableListOf<String>()
            if (tahminEdilen.gecmisDereceSkoru > 0.6f) gucluFaktorler.add("gecmisDerece")
            if (tahminEdilen.jokeySkoru > 0.6f) gucluFaktorler.add("jokey")
            if (tahminEdilen.antrenorSkoru > 0.6f) gucluFaktorler.add("antrenor")
            if (tahminEdilen.sikletSkoru > 0.6f) gucluFaktorler.add("siklet")
            if (tahminEdilen.pistUyumSkoru > 0.6f) gucluFaktorler.add("pistUyum")
            if (tahminEdilen.mesafeUyumSkoru > 0.6f) gucluFaktorler.add("mesafeUyum")
            agirlikYoneticisi.guncelle(gucluFaktorler, emptyList())
        } else {
            // Hangi faktörler yüksekti ama yanlış çıktı → bunları azalt
            val yanlisFaktorler = mutableListOf<String>()
            if (tahminEdilen.gecmisDereceSkoru > 0.6f) yanlisFaktorler.add("gecmisDerece")
            if (tahminEdilen.jokeySkoru > 0.6f) yanlisFaktorler.add("jokey")
            if (tahminEdilen.antrenorSkoru > 0.6f) yanlisFaktorler.add("antrenor")
            if (tahminEdilen.sikletSkoru > 0.6f) yanlisFaktorler.add("siklet")
            if (tahminEdilen.pistUyumSkoru > 0.6f) yanlisFaktorler.add("pistUyum")
            if (tahminEdilen.mesafeUyumSkoru > 0.6f) yanlisFaktorler.add("mesafeUyum")
            agirlikYoneticisi.guncelle(emptyList(), yanlisFaktorler)
        }
    }
}

// Yardımcı veri sınıfları
data class AtGirisi(
    val atId: Int,
    val atIsmi: String,
    val startNo: Int,
    val jokey: String,
    val antrenor: String,
    val siklet: Float

)

data class AltiliSonuc(
    val anaSecimler: List<AtSkoru?>,
    val alternatifler: List<AtSkoru?>,
    val kombinasyonSayisi: Int,
    val tahminiMaliyet: Float
)