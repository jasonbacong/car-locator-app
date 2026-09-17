package com.jasongrech.carlocator.service

import android.app.Service
import android.content.Intent
import android.location.Geocoder
import android.os.IBinder
import android.widget.Toast
import com.jasongrech.carlocator.CarLocatorApp
import com.jasongrech.carlocator.data.ParkingSpot
import com.jasongrech.carlocator.util.LocationUtils
import com.jasongrech.carlocator.util.NotificationHelper
import com.jasongrech.carlocator.util.WearSyncer
import com.jasongrech.carlocator.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ParkingSaveService : Service() {

    companion object {
        const val EXTRA_FORCE = "force"
        private const val HISTORY_RETENTION_DAYS = 60L
    }

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private var foregroundStarted = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
        // startForeground(type=location) throws SecurityException on Android 14+ if
        // location permission isn't actually granted yet — guard so a missing-permission
        // case stops cleanly instead of crashing the service with no visible feedback.
        foregroundStarted = try {
            startForeground(NotificationHelper.savingNotificationId, NotificationHelper.savingNotification(this))
            true
        } catch (e: SecurityException) {
            false
        }
        if (!foregroundStarted) stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!foregroundStarted) return START_NOT_STICKY
        val force = intent?.getBooleanExtra(EXTRA_FORCE, false) ?: false
        scope.launch {
            runSave(force)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    /**
     * [force] comes from the in-app "Save current location now" test button: it skips
     * the home-radius skip (so testing from home still saves) and always surfaces a
     * toast explaining the outcome, since the automatic Bluetooth-triggered path is
     * meant to stay silent when it correctly does nothing near home.
     */
    private suspend fun runSave(force: Boolean) {
        val app = application as CarLocatorApp

        val location = LocationUtils.getCurrentLocation(this)
        if (location == null) {
            if (force) {
                toast("Couldn't get a GPS fix — check that location permission is granted and GPS is on.")
            }
            return
        }

        if (!force && isInsideAnySafeZone(app, location.latitude, location.longitude)) return

        val address = reverseGeocode(location.latitude, location.longitude)

        val spot = ParkingSpot(
            timestamp = System.currentTimeMillis(),
            lat = location.latitude,
            lng = location.longitude,
            address = address
        )
        val id = app.db.parkingSpotDao().insert(spot)
        NotificationHelper.showSavedNotification(this, spot.copy(id = id))
        WidgetUpdater.updateAll(this)
        WearSyncer.push(this, spot.copy(id = id))

        val cutoff = System.currentTimeMillis() - HISTORY_RETENTION_DAYS * 24 * 60 * 60 * 1000
        app.db.parkingSpotDao().deleteOlderThan(cutoff)

        if (force) {
            toast("Saved: ${address ?: "%.5f, %.5f".format(location.latitude, location.longitude)}")
        }
    }

    /** Home zone plus any additional named safe zones (work, gym, ...). */
    private suspend fun isInsideAnySafeZone(app: CarLocatorApp, lat: Double, lng: Double): Boolean {
        val home = app.prefs.homeLocation.first()
        val homeRadius = app.prefs.homeRadiusMeters.first()
        if (home != null && LocationUtils.distanceMeters(lat, lng, home.lat, home.lng) < homeRadius) {
            return true
        }
        return app.db.safeZoneDao().getAllOnce().any { zone ->
            LocationUtils.distanceMeters(lat, lng, zone.lat, zone.lng) < zone.radiusMeters
        }
    }

    private suspend fun toast(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@ParkingSaveService, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun reverseGeocode(lat: Double, lng: Double): String? {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            @Suppress("DEPRECATION")
            val results = geocoder.getFromLocation(lat, lng, 1)
            results?.firstOrNull()?.let { addr ->
                listOfNotNull(addr.thoroughfare, addr.subThoroughfare, addr.locality)
                    .joinToString(" ").ifBlank { null }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}
