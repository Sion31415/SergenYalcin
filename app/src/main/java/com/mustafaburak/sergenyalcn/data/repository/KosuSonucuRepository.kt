package com.mustafaburak.sergenyalcn.data.repository

import com.mustafaburak.sergenyalcn.data.db.dao.KosuSonucuDao
import com.mustafaburak.sergenyalcn.data.db.entity.KosuSonucu
import kotlinx.coroutines.flow.Flow

class KosuSonucuRepository(private val kosuSonucuDao: KosuSonucuDao) {

    fun getAll(): Flow<List<KosuSonucu>> = kosuSonucuDao.getAll()

    fun getByKosuId(kosuId: Int): Flow<List<KosuSonucu>> = kosuSonucuDao.getByKosuId(kosuId)

    // YENİ EKLENEN KISIM
    fun getByAtId(atId: Int): Flow<List<KosuSonucu>> = kosuSonucuDao.getByAtId(atId)

    // YENİ EKLENEN KISIM
    fun getByJokey(jokey: String): Flow<List<KosuSonucu>> = kosuSonucuDao.getByJokey(jokey)

    suspend fun insert(sonuc: KosuSonucu) {
        kosuSonucuDao.insert(sonuc)
    }

    // YENİ EKLENEN KISIM
    suspend fun insertAll(sonuclar: List<KosuSonucu>) {
        kosuSonucuDao.insertAll(sonuclar)
    }

    suspend fun delete(sonuc: KosuSonucu) {
        kosuSonucuDao.delete(sonuc)
    }
}