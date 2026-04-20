package com.mustafaburak.sergenyalcn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafaburak.sergenyalcn.data.db.entity.Kosu
import com.mustafaburak.sergenyalcn.data.repository.KosuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KosuViewModel(private val repository: KosuRepository) : ViewModel() {

    private val _kosular = MutableStateFlow<List<Kosu>>(emptyList())
    val kosular: StateFlow<List<Kosu>> = _kosular.asStateFlow()

    private val _secilenTarih = MutableStateFlow("")
    val secilenTarih: StateFlow<String> = _secilenTarih.asStateFlow()

    init {
        tumKosulariYukle()
    }

    private fun tumKosulariYukle() {
        viewModelScope.launch {
            repository.getAll().collect { _kosular.value = it }
        }
    }

    fun tarihSec(tarih: String) {
        _secilenTarih.value = tarih
        viewModelScope.launch {
            repository.getByTarih(tarih).collect { _kosular.value = it }
        }
    }

    fun ekle(kosu: Kosu) {
        viewModelScope.launch { repository.insert(kosu) }
    }

    fun topluEkle(kosular: List<Kosu>) {
        viewModelScope.launch { repository.insertAll(kosular) }
    }

    fun sil(kosu: Kosu) {
        viewModelScope.launch { repository.delete(kosu) }
    }
}