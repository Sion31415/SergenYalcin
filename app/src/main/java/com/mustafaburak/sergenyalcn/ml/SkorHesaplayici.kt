package com.mustafaburak.sergenyalcn.ml

import com.mustafaburak.sergenyalcn.data.db.entity.KosuSonucu
import kotlin.math.abs
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class SkorHesaplayici(private val agirlikYoneticisi: AgirlikYoneticisi) {

    fun toplamSkorHesapla(
        atId: Int,
        atIsmi: String,
        yas: String,
        orijin: String,
        startNo: Int,
        jokey: String,
        antrenor: String,
        siklet: Float,
        kosudakiTumSikletler: List<Float>,
        pistDurumu: String,
        mesafe: Int,
        tumSonuclar: List<KosuSonucu>,
        pistMap: Map<Int, String>,
        mesafeMap: Map<Int, Int>,
        tarihMap: Map<Int, String>   // ✅ YENİ: kosuId -> tarih string
    ): AtSkoru {

        val atinGecmisKosuSonuclari = tumSonuclar.filter {
            it.atIsmi.equals(atIsmi, ignoreCase = true)
        }

        // 1. Klasik İstatistikler
        val gecmisDereceSkoru = hesaplaGecmisDerece(atinGecmisKosuSonuclari)
        val jokeySkoru        = hesaplaJokeyBasarisi(jokey, tumSonuclar)
        val antrenorSkoru     = hesaplaAntrenorBasarisi(antrenor, tumSonuclar)
        val sikletSkoru       = hesaplaSikletAvantaji(siklet, kosudakiTumSikletler)

        // 2. Pist, Mesafe ve Genetik
        val pistUyumSkoru   = hesaplaPistUyumu(atinGecmisKosuSonuclari, pistDurumu, pistMap)
        val mesafeUyumSkoru = hesaplaMesafeUyumu(atinGecmisKosuSonuclari, mesafe, mesafeMap)
        val genetikSkor     = hesaplaOrijinGenetigi(orijin, pistDurumu, mesafe)

        val finalPistSkoru   = if (pistUyumSkoru == 0.5f) genetikSkor else pistUyumSkoru
        val finalMesafeSkoru = if (mesafeUyumSkoru == 0.5f) genetikSkor else mesafeUyumSkoru

        // 3. Jokey-At kimyası
        val jokeyAtUyumSkoru = hesaplaJokeyAtUyumu(atIsmi, jokey, tumSonuclar)

        // ✅ DÜZELTİLDİ: Gerçek tarih farkıyla dinleniklik hesabı
        val dinleniklikSkoru = hesaplaDinleniklik(atinGecmisKosuSonuclari, tarihMap)

        // Ağırlıkları çek
        val agirliklar = agirlikYoneticisi.agirliklariGetir()
        val wGecmis   = agirliklar["gecmisDerece"] ?: 0.15f
        val wJokey    = agirliklar["jokey"]        ?: 0.15f
        val wAntrenor = agirliklar["antrenor"]     ?: 0.10f
        val wSiklet   = agirliklar["siklet"]       ?: 0.10f
        val wPist     = agirliklar["pistUyum"]     ?: 0.15f
        val wMesafe   = agirliklar["mesafeUyum"]   ?: 0.15f
        val wJokeyAt  = agirliklar["jokeyAtUyum"]  ?: 0.10f
        val wDinlenik = agirliklar["dinleniklik"]  ?: 0.10f

        val toplamSkor =
            (gecmisDereceSkoru * wGecmis) +
                    (jokeySkoru        * wJokey)  +
                    (antrenorSkoru     * wAntrenor) +
                    (sikletSkoru       * wSiklet) +
                    (finalPistSkoru    * wPist)   +
                    (finalMesafeSkoru  * wMesafe) +
                    (jokeyAtUyumSkoru  * wJokeyAt) +
                    (dinleniklikSkoru  * wDinlenik)

        return AtSkoru(
            atId              = atId,
            atIsmi            = atIsmi,
            jokey             = jokey,
            antrenor          = antrenor,
            yas               = yas,
            orijin            = orijin,
            startNo           = startNo,
            siklet            = siklet,
            gecmisDereceSkoru = gecmisDereceSkoru,
            jokeySkoru        = jokeySkoru,
            antrenorSkoru     = antrenorSkoru,
            sikletSkoru       = sikletSkoru,
            pistUyumSkoru     = finalPistSkoru,
            mesafeUyumSkoru   = finalMesafeSkoru,
            toplamSkor        = toplamSkor,
            kazanmaTahmini    = 0f
        )
    }

    // --- JOKEY - AT KİMYASI ---
    private fun hesaplaJokeyAtUyumu(
        atIsmi: String,
        jokey: String,
        tumSonuclar: List<KosuSonucu>
    ): Float {
        val beraberKosular = tumSonuclar.filter {
            it.atIsmi.equals(atIsmi, ignoreCase = true) &&
                    it.jokey.equals(jokey, ignoreCase = true)
        }
        if (beraberKosular.isEmpty()) return 0.5f
        val basari = beraberKosular.count { it.sonuc in 1..3 }
        return (basari.toFloat() / beraberKosular.size).coerceIn(0.1f, 1.0f)
    }

    // ✅ DÜZELTİLDİ: Gerçek tarih farkı hesabı
    private fun hesaplaDinleniklik(
        atinGecmisKosuSonuclari: List<KosuSonucu>,
        tarihMap: Map<Int, String>
    ): Float {
        if (atinGecmisKosuSonuclari.isEmpty()) return 0.5f

        // Atin en son koştuğu yarışın tarihini bul
        val sonKosuId = atinGecmisKosuSonuclari.maxOfOrNull { it.kosuId } ?: return 0.5f
        val sonKosuTarihStr = tarihMap[sonKosuId] ?: return 0.5f

        val bugun = LocalDate.now()
        val sonKosuTarih = tarihiCozumle(sonKosuTarihStr) ?: return 0.5f

        val gunFarki = ChronoUnit.DAYS.between(sonKosuTarih, bugun).toInt()

        // Dinlenme tablosu (hipodrom verileriyle uyumlu):
        // 0-6 gün   → çok yorgun (0.1)
        // 7-13 gün  → yorgun     (0.4)
        // 14-21 gün → ideal      (1.0)
        // 22-35 gün → biraz soğumuş (0.7)
        // 36-60 gün → uzun ara   (0.5)
        // 60+ gün   → çok uzun ara / form bilinmiyor (0.3)
        return when {
            gunFarki < 0   -> 0.5f   // Veri tutarsızlığı, nötr ver
            gunFarki <= 6  -> 0.1f
            gunFarki <= 13 -> 0.4f
            gunFarki <= 21 -> 1.0f   // İdeal dinlenme penceresi
            gunFarki <= 35 -> 0.7f
            gunFarki <= 60 -> 0.5f
            else           -> 0.3f
        }
    }

    /**
     * TJK PDF'lerinden gelen farklı tarih formatlarını çözümler.
     * Desteklenen formatlar:
     *   "2026-04-19"         (ISO)
     *   "19 Nisan 2026"      (Türkçe uzun)
     *   "19/04/2026"         (slash)
     *   "19.04.2026"         (nokta)
     */
    private fun tarihiCozumle(tarihStr: String): LocalDate? {
        val turkceAylar = mapOf(
            "ocak" to 1, "şubat" to 2, "mart" to 3, "nisan" to 4,
            "mayıs" to 5, "haziran" to 6, "temmuz" to 7, "ağustos" to 8,
            "eylül" to 9, "ekim" to 10, "kasım" to 11, "aralık" to 12
        )

        // ISO formatı
        try {
            return LocalDate.parse(tarihStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: Exception) {}

        // "19 Nisan 2026" formatı
        val turkceRegex = """(\d{1,2})\s+(\w+)\s+(\d{4})""".toRegex()
        val m = turkceRegex.find(tarihStr.trim())
        if (m != null) {
            val gun = m.groupValues[1].toIntOrNull() ?: return null
            val ay  = turkceAylar[m.groupValues[2].lowercase()] ?: return null
            val yil = m.groupValues[3].toIntOrNull() ?: return null
            return try { LocalDate.of(yil, ay, gun) } catch (_: Exception) { null }
        }

        // "19/04/2026" veya "19.04.2026" formatı
        val noktaSlashRegex = """(\d{1,2})[./](\d{1,2})[./](\d{4})""".toRegex()
        val m2 = noktaSlashRegex.find(tarihStr.trim())
        if (m2 != null) {
            val gun = m2.groupValues[1].toIntOrNull() ?: return null
            val ay  = m2.groupValues[2].toIntOrNull() ?: return null
            val yil = m2.groupValues[3].toIntOrNull() ?: return null
            return try { LocalDate.of(yil, ay, gun) } catch (_: Exception) { null }
        }

        return null
    }

    // --- ESKİ YARDIMCI FONKSİYONLAR (aynı kaldı) ---

    private fun hesaplaOrijinGenetigi(orijin: String, pistDurumu: String, mesafe: Int): Float {
        val baba = orijin.split("-").firstOrNull()?.trim()?.uppercase() ?: return 0.5f
        val kumVeSentetikUstaları   = listOf("KANEKO","VICTORY GALLOP","CUVEE","MENDIP","TOCCET","TURBO","ÖZGÜNHAN","YAVUZKAYA")
        val cimUstaları             = listOf("LUXOR","NATIVE KHAN","LION HEART","TOROK","AYABAKAN","CAŞ","TAMERİNOĞLU")
        val kisaSuratUstaları       = listOf("LION HEART","PACO BOY","CUVEE","UÇANBEY","KARAKEMAL")
        val uzunDayaniklilikUstaları= listOf("VICTORY GALLOP","KANEKO","TOROK","AYABAKAN","TAMERİNOĞLU","ÖZGÜNHAN")

        var genetikPuan = 0.5f
        if ((pistDurumu == "Kum" || pistDurumu == "Sentetik") && kumVeSentetikUstaları.any { baba.contains(it) }) genetikPuan += 0.2f
        else if (pistDurumu == "Çim" && cimUstaları.any { baba.contains(it) }) genetikPuan += 0.2f

        if (mesafe <= 1400 && kisaSuratUstaları.any { baba.contains(it) }) genetikPuan += 0.2f
        else if (mesafe >= 1900 && uzunDayaniklilikUstaları.any { baba.contains(it) }) genetikPuan += 0.2f

        return genetikPuan.coerceIn(0.1f, 0.9f)
    }

    private fun hesaplaGecmisDerece(sonuclar: List<KosuSonucu>): Float {
        if (sonuclar.isEmpty()) return 0.5f
        val basariSayisi = sonuclar.count { it.sonuc in 1..3 }
        return (basariSayisi.toFloat() / sonuclar.size).coerceIn(0.1f, 1.0f)
    }

    private fun hesaplaJokeyBasarisi(jokey: String, tumSonuclar: List<KosuSonucu>): Float {
        val jokeySonuclari = tumSonuclar.filter { it.jokey.equals(jokey, ignoreCase = true) }
        if (jokeySonuclari.isEmpty()) return 0.5f
        val birincilikler = jokeySonuclari.count { it.sonuc == 1 }
        return (birincilikler.toFloat() / jokeySonuclari.size).coerceIn(0.1f, 1.0f)
    }

    private fun hesaplaAntrenorBasarisi(antrenor: String, tumSonuclar: List<KosuSonucu>): Float {
        val antrenorSonuclari = tumSonuclar.filter {
            it.antrenor?.equals(antrenor, ignoreCase = true) == true
        }
        if (antrenorSonuclari.isEmpty()) return 0.5f
        val basari = antrenorSonuclari.count { it.sonuc in 1..3 }
        return (basari.toFloat() / antrenorSonuclari.size).coerceIn(0.1f, 1.0f)
    }

    private fun hesaplaSikletAvantaji(siklet: Float, tumSikletler: List<Float>): Float {
        if (tumSikletler.isEmpty()) return 0.5f
        val ortalamaSiklet = tumSikletler.average().toFloat()
        val fark = ortalamaSiklet - siklet
        return (0.5f + (fark * 0.05f)).coerceIn(0.1f, 0.9f)
    }

    private fun hesaplaPistUyumu(
        sonuclar: List<KosuSonucu>,
        hedefPist: String,
        pistMap: Map<Int, String>
    ): Float {
        val ayniPistSonuclari = sonuclar.filter { pistMap[it.kosuId] == hedefPist }
        if (ayniPistSonuclari.isEmpty()) return 0.5f
        val basari = ayniPistSonuclari.count { it.sonuc in 1..3 }
        return (basari.toFloat() / ayniPistSonuclari.size).coerceIn(0.1f, 1.0f)
    }

    private fun hesaplaMesafeUyumu(
        sonuclar: List<KosuSonucu>,
        hedefMesafe: Int,
        mesafeMap: Map<Int, Int>
    ): Float {
        val ayniMesafeSonuclari = sonuclar.filter {
            val kosuMesafe = mesafeMap[it.kosuId] ?: 0
            abs(kosuMesafe - hedefMesafe) <= 200
        }
        if (ayniMesafeSonuclari.isEmpty()) return 0.5f
        val basari = ayniMesafeSonuclari.count { it.sonuc in 1..3 }
        return (basari.toFloat() / ayniMesafeSonuclari.size).coerceIn(0.1f, 1.0f)
    }
}