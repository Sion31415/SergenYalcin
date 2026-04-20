package com.mustafaburak.sergenyalcn.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafaburak.sergenyalcn.data.db.entity.KosuSonucu
import com.mustafaburak.sergenyalcn.data.repository.KosuRepository
import com.mustafaburak.sergenyalcn.data.repository.KosuSonucuRepository
import com.mustafaburak.sergenyalcn.ml.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TahminViewModel(
    private val kosuRepository: KosuRepository,
    private val kosuSonucuRepository: KosuSonucuRepository,
    private val context: Context
) : ViewModel() {

    private val agirlikYoneticisi = AgirlikYoneticisi(context)
    private val skorHesaplayici = SkorHesaplayici(agirlikYoneticisi)
    private val tahminMotoru = TahminMotoru(skorHesaplayici)

    private val _tahminSonuclari = MutableStateFlow<List<AtSkoru>>(emptyList())
    val tahminSonuclari: StateFlow<List<AtSkoru>> = _tahminSonuclari.asStateFlow()

    private val _altiliSonuc = MutableStateFlow<AltiliSonuc?>(null)
    val altiliSonuc: StateFlow<AltiliSonuc?> = _altiliSonuc.asStateFlow()

    private val _yukleniyor = MutableStateFlow(false)
    val yukleniyor: StateFlow<Boolean> = _yukleniyor.asStateFlow()

    // Tek koşu tahmini
    fun tahminYap(
        atlar: List<AtGirisi>,
        pistDurumu: String,
        mesafe: Int,
        kosuId: Int
    ) {
        viewModelScope.launch {
            _yukleniyor.value = true

            // Tüm geçmiş sonuçları çek
            val tumSonuclar = mutableListOf<KosuSonucu>()
            atlar.forEach { at ->
                kosuSonucuRepository.getByAtId(at.atId).first().forEach {
                    tumSonuclar.add(it)
                }
            }

            // Pist ve mesafe map'leri oluştur
            val tumKosular = kosuRepository.getAll().first()
            val pistMap = tumKosular.associate { it.id to it.pistDurumu }
            val mesafeMap = tumKosular.associate { it.id to it.mesafe }

            // Tahmin yap
            val skorlar = tahminMotoru.kosuTahmini(
                atlar = atlar,
                tumSonuclar = tumSonuclar,
                pistDurumu = pistDurumu,
                mesafe = mesafe,
                pistMap = pistMap,
                mesafeMap = mesafeMap
            )

            _tahminSonuclari.value = skorlar
            _yukleniyor.value = false
        }
    }

    // Altılı tahmini
    fun altiliTahminYap(kosular: List<Pair<List<AtGirisi>, Int>>, pistDurumu: String) {
        viewModelScope.launch {
            _yukleniyor.value = true

            val tumKosular = kosuRepository.getAll().first()
            val pistMap = tumKosular.associate { it.id to it.pistDurumu }
            val mesafeMap = tumKosular.associate { it.id to it.mesafe }

            val tumSonuclar = mutableListOf<KosuSonucu>()
            kosular.forEach { (atlar, _) ->
                atlar.forEach { at ->
                    kosuSonucuRepository.getByAtId(at.atId).first().forEach {
                        tumSonuclar.add(it)
                    }
                }
            }

            val tumKosularSkorlari = kosular.map { (atlar, mesafe) ->
                tahminMotoru.kosuTahmini(
                    atlar = atlar,
                    tumSonuclar = tumSonuclar,
                    pistDurumu = pistDurumu,
                    mesafe = mesafe,
                    pistMap = pistMap,
                    mesafeMap = mesafeMap
                )
            }

            _altiliSonuc.value = tahminMotoru.altiliTahmini(tumKosularSkorlari)
            _yukleniyor.value = false
        }
    }

    // Gerçek sonuç gelince öğren
    fun gercekSonucGir(tahminEdilen: AtSkoru, gercekBirinciAtId: Int) {
        tahminMotoru.ogren(tahminEdilen, gercekBirinciAtId, agirlikYoneticisi)
    }

    fun agirliklarSifirla() {
        agirlikYoneticisi.sifirla()
    }
}