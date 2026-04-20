package com.mustafaburak.sergenyalcn.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mustafaburak.sergenyalcn.data.db.dao.AtDao
import com.mustafaburak.sergenyalcn.data.db.dao.KosuDao
import com.mustafaburak.sergenyalcn.data.db.dao.KosuSonucuDao
import com.mustafaburak.sergenyalcn.data.db.entity.At
import com.mustafaburak.sergenyalcn.data.db.entity.Kosu
import com.mustafaburak.sergenyalcn.data.db.entity.KosuSonucu

@Database(
    entities = [At::class, Kosu::class, KosuSonucu::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun atDao(): AtDao
    abstract fun kosuDao(): KosuDao
    abstract fun kosuSonucuDao(): KosuSonucuDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sergen_yalcin_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}