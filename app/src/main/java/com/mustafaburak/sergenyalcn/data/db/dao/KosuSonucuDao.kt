package com.mustafaburak.sergenyalcn.data.db.dao

import androidx.room.*
import com.mustafaburak.sergenyalcn.data.db.entity.KosuSonucu
import kotlinx.coroutines.flow.Flow

@Dao
interface KosuSonucuDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sonuc: KosuSonucu)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sonuclar: List<KosuSonucu>)

    @Delete
    suspend fun delete(sonuc: KosuSonucu)

    @Query("SELECT * FROM kosu_sonuclari WHERE kosuId = :kosuId ORDER BY sonuc ASC")
    fun getByKosuId(kosuId: Int): Flow<List<KosuSonucu>>

    @Query("SELECT * FROM kosu_sonuclari WHERE atId = :atId ORDER BY sonuc ASC")
    fun getByAtId(atId: Int): Flow<List<KosuSonucu>>

    @Query("""
        SELECT * FROM kosu_sonuclari 
        WHERE jokey = :jokey 
        ORDER BY sonuc ASC
    """)
    fun getByJokey(jokey: String): Flow<List<KosuSonucu>>
}