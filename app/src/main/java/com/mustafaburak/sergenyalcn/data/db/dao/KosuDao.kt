package com.mustafaburak.sergenyalcn.data.db.dao

import androidx.room.*
import com.mustafaburak.sergenyalcn.data.db.entity.Kosu
import kotlinx.coroutines.flow.Flow

@Dao
interface KosuDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(kosu: Kosu)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(kosular: List<Kosu>)

    @Delete
    suspend fun delete(kosu: Kosu)

    @Query("SELECT * FROM kosular ORDER BY tarih DESC")
    fun getAll(): Flow<List<Kosu>>

    @Query("SELECT * FROM kosular WHERE tarih = :tarih ORDER BY kosuNo ASC")
    fun getByTarih(tarih: String): Flow<List<Kosu>>

    @Query("SELECT * FROM kosular WHERE id = :id")
    suspend fun getById(id: Int): Kosu?
}