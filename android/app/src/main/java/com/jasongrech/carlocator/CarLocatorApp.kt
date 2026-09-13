package com.jasongrech.carlocator

import android.app.Application
import com.jasongrech.carlocator.data.AppDatabase
import com.jasongrech.carlocator.data.CarDevice
import com.jasongrech.carlocator.data.PrefsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CarLocatorApp : Application() {
    val prefs by lazy { PrefsRepository(this) }
    val db by lazy { AppDatabase.get(this) }

    override fun onCreate() {
        super.onCreate()
        migrateLegacyCarDevice()
    }

    /**
     * Multiple cars replaced the single saved device. If someone had one set up
     * from before, carry it over into the new list once instead of making them
     * re-pick it.
     */
    private fun migrateLegacyCarDevice() {
        CoroutineScope(Dispatchers.IO).launch {
            if (db.carDeviceDao().getAllOnce().isNotEmpty()) return@launch
            val address = prefs.legacyCarDeviceAddress.first() ?: return@launch
            val name = prefs.legacyCarDeviceName.first() ?: address
            db.carDeviceDao().insert(CarDevice(address = address, name = name))
            prefs.clearLegacyCarDevice()
        }
    }
}
