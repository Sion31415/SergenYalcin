package com.mustafaburak.sergenyalcn

import android.app.Application
import com.mustafaburak.sergenyalcn.data.db.AppDatabase

class SergenApp : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }
}