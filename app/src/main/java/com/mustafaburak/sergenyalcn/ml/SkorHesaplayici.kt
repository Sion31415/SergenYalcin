package com.mustafaburak.sergenyalcn.ml

import com.mustafaburak.sergenyalcn.data.db.entity.KosuSonucu

class SkorHesaplayici(private val agirlikYoneticisi: AgirlikYoneticisi) {

    // Geçmiş dereceler → skor
    fun gecmisDereceSkoru(
        atId: Int,
        gecmisKosular: List<KosuSonucu>
    ): Float {
        val atKosulari = gecmisKosular
            .filter { it.atId == atId }
            .sortedByDescending { it.id }
            .take(10) // son 10 koşu

        if (atKosulari.isEmpty()) return 0.5f // veri yoksa orta skor

        // Sonuçlara göre puan: 1. = 1.0, 2. = 0.8, 3. = 0.6, diğer = 0.2
        val puanlar = atKosulari.mapIndexed { index, sonuc ->
            val puan = when (sonuc.sonuc) {
                1 -> 1.0f
                2 -> 0.8f
                3 -> 0.6f
                4 -> 0.4f
                else -> 0.2f
            }
            // Son koşulara daha fazla ağırlık ver
            val zamanAgırlığı = 1.0f - (index * 0.05f)
            puan * zamanAgırlığı
        }

        return puanlar.average().toFloat().coerceIn(0f, 1f)
    }

    // Jokey başarı skoru
    fun jokeySkoru(
        jokey: String,
        tumSonuclar: List<KosuSonucu>
    ): Float {
        val jokeyKosulari = tumSonuclar.filter { it.jokey == jokey }
        if (jokeyKosulari.isEmpty()) return 0.5f

        val birinciler = jokeyKosulari.count { it.sonuc == 1 }
        val ilkUc = jokeyKosulari.count { it.sonuc <= 3 }

        val birinciliOrani = birinciler.toFloat() / jokeyKosulari.size
        val ilkUcOrani = ilkUc.toFloat() / jokeyKosulari.size

        return ((birinciliOrani * 0.6f) + (ilkUcOrani * 0.4f)).coerceIn(0f, 1f)
    }

    // Antrenör başarı skoru
    fun antrenorSkoru(
        antrenor: String,
        tumSonuclar: List<KosuSonucu>
    ): Float {
        val antrenorKosulari = tumSonuclar.filter { it.antrenor == antrenor }
        if (antrenorKosulari.isEmpty()) return 0.5f

        val birinciler = antrenorKosulari.count { it.sonuc == 1 }
        val ilkUc = antrenorKosulari.count { it.sonuc <= 3 }

        val birinciliOrani = birinciler.toFloat() / antrenorKosulari.size
        val ilkUcOrani = ilkUc.toFloat() / antrenorKosulari.size

        return ((birinciliOrani * 0.6f) + (ilkUcOrani * 0.4f)).coerceIn(0f, 1f)
    }

    // Sıklet skoru — düşük sıklet avantaj
    fun sikletSkoru(siklet: Float, kosudakiTumSikletler: List<Float>): Float {
        if (kosudakiTumSikletler.isEmpty()) return 0.5f
        val minSiklet = kosudakiTumSikletler.min()
        val maxSiklet = kosudakiTumSikletler.max()
        if (maxSiklet == minSiklet) return 0.5f
        return (1f - (siklet - minSiklet) / (maxSiklet - minSiklet)).coerceIn(0f, 1f)
    }

    // Pist uyum skoru
    fun pistUyumSkoru(
        atId: Int,
        pistDurumu: String,
        tumSonuclar: List<KosuSonucu>,
        tumKosular: Map<Int, String> // kosuId → pistDurumu
    ): Float {
        val atPistKosulari = tumSonuclar.filter { sonuc ->
            sonuc.atId == atId && tumKosular[sonuc.kosuId] == pistDurumu
        }
        if (atPistKosulari.isEmpty()) return 0.5f

        val birinciler = atPistKosulari.count { it.sonuc == 1 }
        val ilkUc = atPistKosulari.count { it.sonuc <= 3 }

        val birinciliOrani = birinciler.toFloat() / atPistKosulari.size
        val ilkUcOrani = ilkUc.toFloat() / atPistKosulari.size

        return ((birinciliOrani * 0.6f) + (ilkUcOrani * 0.4f)).coerceIn(0f, 1f)
    }

    // Mesafe uyum skoru
    fun mesafeUyumSkoru(
        atId: Int,
        mesafe: Int,
        tumSonuclar: List<KosuSonucu>,
        tumKosular: Map<Int, Int> // kosuId → mesafe
    ): Float {
        // Benzer mesafeler (±200 metre)
        val benzerMesafeKosular = tumSonuclar.filter { sonuc ->
            sonuc.atId == atId &&
                    Math.abs((tumKosular[sonuc.kosuId] ?: 0) - mesafe) <= 200
        }
        if (benzerMesafeKosular.isEmpty()) return 0.5f

        val birinciler = benzerMesafeKosular.count { it.sonuc == 1 }
        val ilkUc = benzerMesafeKosular.count { it.sonuc <= 3 }

        val birinciliOrani = birinciler.toFloat() / benzerMesafeKosular.size
        val ilkUcOrani = ilkUc.toFloat() / benzerMesafeKosular.size

        return ((birinciliOrani * 0.6f) + (ilkUcOrani * 0.4f)).coerceIn(0f, 1f)
    }

    // Tüm skorları birleştir
    fun toplamSkorHesapla(
        atId: Int,
        atIsmi: String,
        startNo: Int,
        jokey: String,
        antrenor: String,
        siklet: Float,
        kosudakiTumSikletler: List<Float>,
        pistDurumu: String,
        mesafe: Int,
        tumSonuclar: List<KosuSonucu>,
        pistMap: Map<Int, String>,
        mesafeMap: Map<Int, Int>
    ): AtSkoru {
        val agirliklar = agirlikYoneticisi.tumAgirliklar()

        val gecmisD = gecmisDereceSkoru(atId, tumSonuclar)
        val jokeyS = jokeySkoru(jokey, tumSonuclar)
        val antrenorS = antrenorSkoru(antrenor, tumSonuclar)
        val sikletS = sikletSkoru(siklet, kosudakiTumSikletler)
        val pistS = pistUyumSkoru(atId, pistDurumu, tumSonuclar, pistMap)
        val mesafeS = mesafeUyumSkoru(atId, mesafe, tumSonuclar, mesafeMap)

        val toplam =
            gecmisD * (agirliklar["gecmisDerece"] ?: 0.30f) +
                    jokeyS * (agirliklar["jokey"] ?: 0.25f) +
                    antrenorS * (agirliklar["antrenor"] ?: 0.15f) +
                    sikletS * (agirliklar["siklet"] ?: 0.10f) +
                    pistS * (agirliklar["pistUyum"] ?: 0.10f) +
                    mesafeS * (agirliklar["mesafeUyum"] ?: 0.10f)

        return AtSkoru(
            atId = atId,
            atIsmi = atIsmi,
            startNo = startNo,
            jokey = jokey,
            antrenor = antrenor,
            toplamSkor = toplam,
            gecmisDereceSkoru = gecmisD,
            jokeySkoru = jokeyS,
            antrenorSkoru = antrenorS,
            sikletSkoru = sikletS,
            pistUyumSkoru = pistS,
            mesafeUyumSkoru = mesafeS,
            kazanmaTahmini = toplam * 100f
        )
    }
}