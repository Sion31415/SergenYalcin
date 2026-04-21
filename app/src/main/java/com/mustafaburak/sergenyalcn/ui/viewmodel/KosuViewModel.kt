package com.mustafaburak.sergenyalcn.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafaburak.sergenyalcn.data.db.entity.Kosu
import com.mustafaburak.sergenyalcn.data.db.entity.KosuSonucu
import com.mustafaburak.sergenyalcn.data.repository.KosuRepository
import com.mustafaburak.sergenyalcn.data.repository.KosuSonucuRepository
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

class KosuViewModel(
    private val repository: KosuRepository,
    private val sonucRepository: KosuSonucuRepository
) : ViewModel() {

    private val _kosular = MutableStateFlow<List<Kosu>>(emptyList())
    val kosular: StateFlow<List<Kosu>> = _kosular.asStateFlow()

    private val _secilenTarih = MutableStateFlow("")
    val secilenTarih: StateFlow<String> = _secilenTarih.asStateFlow()

    init { tumKosulariYukle() }

    private fun tumKosulariYukle() {
        viewModelScope.launch {
            repository.getAll().collect { _kosular.value = it }
        }
    }

    fun ekle(kosu: Kosu) { viewModelScope.launch { repository.insert(kosu) } }
    fun sil(kosu: Kosu) { viewModelScope.launch { repository.delete(kosu) } }

    fun pdfOku(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                PDFBoxResourceLoader.init(context)
                val metin = withContext(Dispatchers.IO) { pdfMetniOku(uri, context) }
                analizVeKaydet(metin)
            } catch (e: Exception) {
                Log.e("PDF_ERROR", "Hata: ${e.message}")
            }
        }
    }

    private fun pdfMetniOku(uri: Uri, context: Context): String {
        var document: PDDocument? = null
        return try {
            val stream: InputStream? = context.contentResolver.openInputStream(uri)
            if (stream != null) {
                document = PDDocument.load(stream)
                PDFTextStripper().getText(document)
            } else ""
        } finally {
            document?.close()
        }
    }

    private fun analizVeKaydet(metin: String) {
        viewModelScope.launch(Dispatchers.IO) {

            // ── Tarih ──────────────────────────────────────────────────────
            val aylar = "Ocak|Şubat|Mart|Nisan|Mayıs|Haziran|Temmuz|Ağustos|Eylül|Ekim|Kasım|Aralık"
            val tarihRegex = """(\d{1,2})\s+($aylar)\s+(\d{4})""".toRegex(RegexOption.IGNORE_CASE)
            val tarih = tarihRegex.find(metin)?.value ?: "2026-01-01"

            // ── Hipodrom ───────────────────────────────────────────────────
            val hipodrom = when {
                metin.contains("İstanbul", ignoreCase = true) -> "istanbul"
                metin.contains("Ankara",   ignoreCase = true) -> "ankara"
                metin.contains("İzmir",    ignoreCase = true) -> "izmir"
                metin.contains("Bursa",    ignoreCase = true) -> "bursa"
                metin.contains("Elazığ",   ignoreCase = true) -> "elazığ"
                metin.contains("Adana",    ignoreCase = true) -> "adana"
                else -> "bilinmiyor"
            }

            // ── Koşu başlığı ───────────────────────────────────────────────
            // Örnek: "1.Koşu Saat:17.45 Handikap14 /H3 2000 Kum"
            val kosuBaslikRegex = """(\d+)\.Koşu\s.+?(\d{3,5})\s+(Kum|Çim|Sentetik)""".toRegex(RegexOption.IGNORE_CASE)

            // ── At sonucu ─────────────────────────────────────────────────
            // Örnek: "1 GÜNEYİM(3) 3yd e KLIMT(USA) - SHANTI(GER) 58 G.KOCAKAYA ÜMİ.CAN M.H.ELĞAÇ 1.38.32 6,75 ..."
            val sonucRegex = """^(\d{1,2})\s+([A-ZÇĞİÖŞÜa-zçğışöü ]+?)\((\d{1,2})\)\s*(?:\([A-Z]+\)\s+)?(\d+y[a-z]\s+[a-z])\s+(.+?)\s+(\d+(?:[.,]\d+)?)\s+([A-ZÇĞİÖŞÜa-zçğışöü.]+(?:\s+[A-ZÇĞİÖŞÜa-zçğışöü.]+)?)\s+(.+?)\s+([A-ZÇĞİÖŞÜa-zçğışöü.]+(?:\s+[A-ZÇĞİÖŞÜa-zçğışöü.]+)?)\s+(\d+\.\d+\.\d+)\s+([\d,]+)""".toRegex()

            var suAnkiKosuId = -1

            for (satir in metin.lines()) {
                val s = satir.trim()
                if (s.isEmpty()) continue

                // Koşu başlığı mı?
                val baslik = kosuBaslikRegex.find(s)
                if (baslik != null) {
                    val no     = baslik.groupValues[1].toIntOrNull() ?: 1
                    val mesafe = baslik.groupValues[2].toIntOrNull() ?: 1000
                    val pist   = baslik.groupValues[3].replaceFirstChar { it.uppercase() }
                    val id     = repository.insert(Kosu(tarih = tarih, hipodrom = hipodrom,
                        kosuNo = no, mesafe = mesafe, pistDurumu = pist, havaDurumu = "Açık"))
                    suAnkiKosuId = id.toInt()
                    Log.d("PDF_PARSE", "Koşu: $no — $mesafe $pist (id=$suAnkiKosuId)")
                    continue
                }

                // At sonuç satırı mı?
                if (suAnkiKosuId == -1) continue
                val m = sonucRegex.find(s) ?: continue

                val sonuc = KosuSonucu(
                    kosuId   = suAnkiKosuId,
                    atId     = 0,
                    sonuc    = m.groupValues[1].toIntOrNull() ?: 0,
                    atIsmi   = m.groupValues[2].trim(),
                    startNo  = m.groupValues[3].toIntOrNull() ?: 0,
                    yas      = m.groupValues[4].trim(),
                    orijin   = m.groupValues[5].trim(),
                    siklet   = m.groupValues[6].replace(",", ".").toFloatOrNull() ?: 0f,
                    jokey    = m.groupValues[7].trim(),
                    sahip    = m.groupValues[8].trim(),
                    antrenor = m.groupValues[9].trim(),
                    derece   = m.groupValues[10].trim(),
                    gny      = m.groupValues[11].replace(",", ".").toFloatOrNull() ?: 0f,
                    agf      = 0f, fark = "", gikis = 0, hp = 0
                )
                sonucRepository.insert(sonuc)
                Log.d("PDF_PARSE", "✅ ${sonuc.atIsmi} — ${sonuc.sonuc}.")
            }
            tumKosulariYukle()
        }
    }
}