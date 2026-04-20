package com.mustafaburak.sergenyalcn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafaburak.sergenyalcn.data.db.entity.KosuSonucu
import com.mustafaburak.sergenyalcn.data.repository.KosuSonucuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KosuSonucuViewModel(private val repository: KosuSonucuRepository) : ViewModel() {

    private val _sonuclar = MutableStateFlow<List<KosuSonucu>>(emptyList())
    val sonuclar: StateFlow<List<KosuSonucu>> = _sonuclar.asStateFlow()

    fun kosuYukle(kosuId: Int) {
        viewModelScope.launch {
            repository.getByKosuId(kosuId).collect { _sonuclar.value = it }
        }
    }

    fun atYukle(atId: Int) {
        viewModelScope.launch {
            repository.getByAtId(atId).collect { _sonuclar.value = it }
        }
    }

    fun jokeyYukle(jokey: String) {
        viewModelScope.launch {
            repository.getByJokey(jokey).collect { _sonuclar.value = it }
        }
    }

    fun ekle(sonuc: KosuSonucu) {
        viewModelScope.launch { repository.insert(sonuc) }
    }

    fun topluEkle(sonuclar: List<KosuSonucu>) {
        viewModelScope.launch { repository.insertAll(sonuclar) }
    }

    fun sil(sonuc: KosuSonucu) {
        viewModelScope.launch { repository.delete(sonuc) }
    }
}