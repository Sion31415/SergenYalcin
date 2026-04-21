package com.mustafaburak.sergenyalcn.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mustafaburak.sergenyalcn.data.db.entity.Kosu
import kotlinx.coroutines.flow.Flow

@Dao
interface KosuDao {

    @Query("SELECT * FROM kosular ORDER BY id DESC")
    fun getAll(): Flow<List<Kosu>>

    @Query("SELECT * FROM kosular WHERE tarih = :tarih ORDER BY kosuNo ASC")
    fun getByTarih(tarih: String): Flow<List<Kosu>>

    @Query("SELECT * FROM kosular WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Kosu?

    // ÇÖZÜM BURADA: Artık geriye Long (eklenen satırın ID'sini) döndürüyor
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(kosu: Kosu): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(kosular: List<Kosu>)

    @Delete
    suspend fun delete(kosu: Kosu)
}