package com.mustafaburak.sergenyalcn.data.repository

import com.mustafaburak.sergenyalcn.data.db.dao.AtDao
import com.mustafaburak.sergenyalcn.data.db.entity.At
import kotlinx.coroutines.flow.Flow

class AtRepository(private val atDao: AtDao) {

    fun getAll(): Flow<List<At>> = atDao.getAll()

    fun search(isim: String): Flow<List<At>> = atDao.searchByIsim(isim)

    suspend fun insert(at: At) = atDao.insert(at)

    suspend fun insertAll(atlar: List<At>) = atDao.insertAll(atlar)

    suspend fun update(at: At) = atDao.update(at)

    suspend fun delete(at: At) = atDao.delete(at)

    suspend fun getById(id: Int): At? = atDao.getById(id)
}