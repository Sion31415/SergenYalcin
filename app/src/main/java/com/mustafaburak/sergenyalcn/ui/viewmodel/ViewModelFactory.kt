package com.mustafaburak.sergenyalcn.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mustafaburak.sergenyalcn.data.repository.AtRepository
import com.mustafaburak.sergenyalcn.data.repository.KosuRepository
import com.mustafaburak.sergenyalcn.data.repository.KosuSonucuRepository

class ViewModelFactory(
    private val atRepository: AtRepository,
    private val kosuRepository: KosuRepository,
    private val kosuSonucuRepository: KosuSonucuRepository,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AtViewModel::class.java) ->
                AtViewModel(atRepository) as T
            modelClass.isAssignableFrom(KosuViewModel::class.java) ->
                KosuViewModel(kosuRepository) as T
            modelClass.isAssignableFrom(KosuSonucuViewModel::class.java) ->
                KosuSonucuViewModel(kosuSonucuRepository) as T
            modelClass.isAssignableFrom(TahminViewModel::class.java) ->
                TahminViewModel(kosuRepository, kosuSonucuRepository, context) as T
            else -> throw IllegalArgumentException("Bilinmeyen ViewModel: ${modelClass.name}")
        }
    }
}