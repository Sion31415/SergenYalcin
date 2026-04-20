package com.mustafaburak.sergenyalcn.data.db.dao

import androidx.room.*
import com.mustafaburak.sergenyalcn.data.db.entity.At
import kotlinx.coroutines.flow.Flow

@Dao
interface AtDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(at: At)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(atlar: List<At>)

    @Update
    suspend fun update(at: At)

    @Delete
    suspend fun delete(at: At)

    @Query("SELECT * FROM atlar ORDER BY isim ASC")
    fun getAll(): Flow<List<At>>

    @Query("SELECT * FROM atlar WHERE id = :id")
    suspend fun getById(id: Int): At?

    @Query("SELECT * FROM atlar WHERE isim LIKE '%' || :isim || '%'")
    fun searchByIsim(isim: String): Flow<List<At>>
}