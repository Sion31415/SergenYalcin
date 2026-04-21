package com.mustafaburak.sergenyalcn.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mustafaburak.sergenyalcn.data.db.entity.KosuSonucu
import kotlinx.coroutines.flow.Flow

@Dao
interface KosuSonucuDao {
    @Query("SELECT * FROM kosu_sonuclari")
    fun getAll(): Flow<List<KosuSonucu>>

    @Query("SELECT * FROM kosu_sonuclari WHERE kosuId = :kosuId ORDER BY sonuc ASC")
    fun getByKosuId(kosuId: Int): Flow<List<KosuSonucu>>

    // YENİ EKLENEN KISIM: Ata göre arama
    @Query("SELECT * FROM kosu_sonuclari WHERE atId = :atId ORDER BY id DESC")
    fun getByAtId(atId: Int): Flow<List<KosuSonucu>>

    // YENİ EKLENEN KISIM: Jokeye göre arama
    @Query("SELECT * FROM kosu_sonuclari WHERE jokey = :jokey ORDER BY id DESC")
    fun getByJokey(jokey: String): Flow<List<KosuSonucu>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sonuc: KosuSonucu)

    // YENİ EKLENEN KISIM: Toplu ekleme
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sonuclar: List<KosuSonucu>)

    @Delete
    suspend fun delete(sonuc: KosuSonucu)
}