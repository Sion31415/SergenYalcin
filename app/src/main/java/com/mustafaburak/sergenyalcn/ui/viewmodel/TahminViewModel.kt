package com.mustafaburak.sergenyalcn.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafaburak.sergenyalcn.data.repository.KosuRepository
import com.mustafaburak.sergenyalcn.data.repository.KosuSonucuRepository
import com.mustafaburak.sergenyalcn.ml.*
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

class TahminViewModel(
    private val kosuRepository: KosuRepository,
    private val sonucRepository: KosuSonucuRepository,
    private val context: Context
) : ViewModel() {

    private val _tahminSonuclari = MutableStateFlow<List<AtSkoru>>(emptyList())
    val tahminSonuclari: StateFlow<List<AtSkoru>> = _tahminSonuclari.asStateFlow()

    private val _yukleniyor = MutableStateFlow(false)
    val yukleniyor: StateFlow<Boolean> = _yukleniyor.asStateFlow()

    private val agirlikYoneticisi = AgirlikYoneticisi(context)
    private val skorHesaplayici   = SkorHesaplayici(agirlikYoneticisi)
    private val tahminMotoru      = TahminMotoru(skorHesaplayici)

    fun programdanTahminEt(uri: Uri) {
        viewModelScope.launch {
            _yukleniyor.value = true
            try {
                PDFBoxResourceLoader.init(context)

                val metin = withContext(Dispatchers.IO) {
                    var doc: PDDocument? = null
                    try {
                        val stream: InputStream? = context.contentResolver.openInputStream(uri)
                        if (stream != null) {
                            doc = PDDocument.load(stream)
                            PDFTextStripper().getText(doc)
                        } else ""
                    } finally { doc?.close() }
                }

                // ── Koşu şartları ──────────────────────────────────────────
                // Örnek başlık: "1.Koşu Saat:17.45 Handikap14 /H3 2000 Kum"
                var kosuMesafe = 1200
                var kosuPist   = "Kum"
                val baslikRegex = """(\d{3,5})\s+(Kum|Çim|Sentetik)""".toRegex(RegexOption.IGNORE_CASE)
                val baslikMatch = baslikRegex.find(metin)
                if (baslikMatch != null) {
                    kosuMesafe = baslikMatch.groupValues[1].toIntOrNull() ?: 1200
                    kosuPist   = baslikMatch.groupValues[2].replaceFirstChar { it.uppercase() }
                }
                Log.d("TAHMIN_MOTORU", "Şartlar: $kosuMesafe m — $kosuPist")

                // ── Atları ayıkla ──────────────────────────────────────────
                val programAtlar = withContext(Dispatchers.Default) {
                    programAtlariniAyikla(metin)
                }
                Log.d("TAHMIN_MOTORU", "Bulunan at: ${programAtlar.size}")

                if (programAtlar.isNotEmpty()) {
                    val gecmisSonuclar = withContext(Dispatchers.IO) {
                        sonucRepository.getAll().first()
                    }
                    val tumKosular = withContext(Dispatchers.IO) {
                        kosuRepository.getAll().first()
                    }
                    val pistMap   = tumKosular.associate { it.id to it.pistDurumu }
                    val mesafeMap = tumKosular.associate { it.id to it.mesafe }
                    val tarihMap  = tumKosular.associate { it.id to it.tarih }

                    val skorlar = withContext(Dispatchers.Default) {
                        tahminMotoru.kosuTahmini(
                            atlar       = programAtlar,
                            tumSonuclar = gecmisSonuclar,
                            pistDurumu  = kosuPist,
                            mesafe      = kosuMesafe,
                            pistMap     = pistMap,
                            mesafeMap   = mesafeMap,
                            tarihMap    = tarihMap
                        )
                    }
                    _tahminSonuclari.value = skorlar
                } else {
                    Log.e("TAHMIN_ERROR", "PDF'den at verisi çıkartılamadı.")
                }

            } catch (e: Exception) {
                Log.e("TAHMIN_ERROR", "Hata: ${e.message}")
            } finally {
                _yukleniyor.value = false
            }
        }
    }

    private fun programAtlariniAyikla(metin: String): List<AtGirisi> {
        val liste = mutableListOf<AtGirisi>()

        // Gerçek TJK program formatı:
        // "1.MELİSHANIM 4yd k HAKKAR- KIZGINBULUT 62,5 MEH.TAŞKAYA ERK. YILDIRIM M.BULUÇ 5 43 ..."
        // StartNo.AtIsmi  Yaş  Orijin  Sıklet  Jokey  Sahip  Antrenör  St  HP  ...
        val re = Regex(
            """^(\d{1,2})\.([A-ZÇĞİÖŞÜa-zçğışöü ]+?)\s+(\d+y[a-z]\s+[a-z])\s+(.+?)\s+([\d,]+)\s+([A-ZÇĞİÖŞÜa-zçğışöü.]+(?:\s+[A-ZÇĞİÖŞÜa-zçğışöü.]+)?)\s+(.+?)\s+([A-ZÇĞİÖŞÜa-zçğışöü.]+(?:\s+[A-ZÇĞİÖŞÜa-zçğışöü.]+)?)\s+\d+\s+\d+"""
        )

        for (satir in metin.lines()) {
            val s = satir.trim()
            val m = re.find(s) ?: continue

            val at = AtGirisi(
                atId     = 0,
                atIsmi   = m.groupValues[2].trim(),
                startNo  = m.groupValues[1].toIntOrNull() ?: 0,
                yas      = m.groupValues[3].trim(),
                orijin   = m.groupValues[4].trim(),
                siklet   = m.groupValues[5].replace(",", ".").toFloatOrNull() ?: 0f,
                jokey    = m.groupValues[6].trim(),
                antrenor = m.groupValues[8].trim()
            )
            liste.add(at)
            Log.d("PDF_PARSE", "At: ${at.atIsmi} | Jokey: ${at.jokey} | Sıklet: ${at.siklet}")
        }
        return liste
    }
}