package com.jasongrech.carlocator

import android.app.Application
import com.jasongrech.carlocator.data.AppDatabase
import com.jasongrech.carlocator.data.PrefsRepository

class CarLocatorApp : Application() {
    val prefs by lazy { PrefsRepository(this) }
    val db by lazy { AppDatabase.get(this) }
}
