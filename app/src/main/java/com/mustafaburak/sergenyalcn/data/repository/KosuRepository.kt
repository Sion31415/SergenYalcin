package com.mustafaburak.sergenyalcn.data.repository

import com.mustafaburak.sergenyalcn.data.db.dao.KosuDao
import com.mustafaburak.sergenyalcn.data.db.entity.Kosu
import kotlinx.coroutines.flow.Flow

class KosuRepository(private val kosuDao: KosuDao) {

    fun getAll(): Flow<List<Kosu>> = kosuDao.getAll()

    fun getByTarih(tarih: String): Flow<List<Kosu>> = kosuDao.getByTarih(tarih)

    suspend fun getById(id: Int): Kosu? = kosuDao.getById(id)

    // KosuRepository.kt içindeki hali şuna benzemeli:
    suspend fun insert(kosu: Kosu): Long {
        return kosuDao.insert(kosu)
    }

    suspend fun insertAll(kosular: List<Kosu>) = kosuDao.insertAll(kosular)

    suspend fun delete(kosu: Kosu) = kosuDao.delete(kosu)
}