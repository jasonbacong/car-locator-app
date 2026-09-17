package com.jasongrech.carlocator.util

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.jasongrech.carlocator.CarLocatorApp
import com.jasongrech.carlocator.data.ParkingSpot
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Pushes the current parking spot to any paired Wear OS watch via the Data Layer
 * API. This works over the phone-watch Bluetooth/companion pairing regardless of
 * the watch app's package name, so nothing on the phone needs to know whether a
 * watch is actually paired — a push with no watch present is just a no-op.
 */
object WearSyncer {
    private const val PATH = "/parking_spot"
    private const val TAG = "WearSyncer"

    /**
     * Awaits the Play Services task instead of firing and forgetting — every
     * caller either stops a foreground service or finishes a broadcast receiver's
     * goAsync() right after this returns, and an un-awaited task can get killed
     * along with the process before it ever reaches Play Services.
     */
    suspend fun push(context: Context, spot: ParkingSpot?) {
        val request = PutDataMapRequest.create(PATH).apply {
            dataMap.putBoolean("hasSpot", spot != null)
            if (spot != null) {
                dataMap.putLong("id", spot.id)
                dataMap.putLong("timestamp", spot.timestamp)
                dataMap.putDouble("lat", spot.lat)
                dataMap.putDouble("lng", spot.lng)
                dataMap.putString("address", spot.address ?: "")
            }
        }.asPutDataRequest().setUrgent()

        withTimeoutOrNull(10_000) {
            suspendCancellableCoroutine<Unit> { cont ->
                Wearable.getDataClient(context).putDataItem(request)
                    .addOnSuccessListener { if (cont.isActive) cont.resume(Unit) }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Failed to sync spot to watch", e)
                        if (cont.isActive) cont.resume(Unit)
                    }
            }
        }
    }

    /** Re-reads whatever is now the newest saved spot and pushes that — used after any delete. */
    suspend fun pushLatest(context: Context) {
        val app = context.applicationContext as CarLocatorApp
        push(context, app.db.parkingSpotDao().getLatestOnce())
    }
}
