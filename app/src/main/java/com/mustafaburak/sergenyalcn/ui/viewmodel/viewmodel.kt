package com.mustafaburak.sergenyalcn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafaburak.sergenyalcn.data.db.entity.At
import com.mustafaburak.sergenyalcn.data.repository.AtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AtViewModel(private val repository: AtRepository) : ViewModel() {

    private val _atlar = MutableStateFlow<List<At>>(emptyList())
    val atlar: StateFlow<List<At>> = _atlar.asStateFlow()

    private val _aramaMetni = MutableStateFlow("")
    val aramaMetni: StateFlow<String> = _aramaMetni.asStateFlow()

    init {
        tumAtlariYukle()
    }

    private fun tumAtlariYukle() {
        viewModelScope.launch {
            repository.getAll().collect { liste ->
                _atlar.value = liste
            }
        }
    }

    fun ara(metin: String) {
        _aramaMetni.value = metin
        viewModelScope.launch {
            if (metin.isEmpty()) {
                repository.getAll().collect { _atlar.value = it }
            } else {
                repository.search(metin).collect { _atlar.value = it }
            }
        }
    }

    fun ekle(at: At) {
        viewModelScope.launch { repository.insert(at) }
    }

    fun guncelle(at: At) {
        viewModelScope.launch { repository.update(at) }
    }

    fun sil(at: At) {
        viewModelScope.launch { repository.delete(at) }
    }
}