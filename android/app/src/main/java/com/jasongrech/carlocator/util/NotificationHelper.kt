package com.jasongrech.carlocator.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jasongrech.carlocator.R
import com.jasongrech.carlocator.data.ParkingSpot
import com.jasongrech.carlocator.receiver.UndoSaveReceiver

object NotificationHelper {
    const val CHANNEL_SAVING = "saving_spot"
    const val CHANNEL_SAVED = "saved_spot"
    const val savingNotificationId = 1001
    const val savedNotificationId = 1002

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_SAVING, "Saving parking spot", NotificationManager.IMPORTANCE_LOW)
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_SAVED, "Parking spot saved", NotificationManager.IMPORTANCE_HIGH)
        )
    }

    fun savingNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, CHANNEL_SAVING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Car Locator")
            .setContentText("Saving your parking spot…")
            .setOngoing(true)
            .build()
    }

    fun showSavedNotification(context: Context, spot: ParkingSpot) {
        ensureChannels(context)

        val mapsIntent = NavIntents.resolvedMapsIntent(context, spot.lat, spot.lng)
        val wazeIntent = NavIntents.resolvedWazeIntent(context, spot.lat, spot.lng)

        val requestBase = (spot.id % 100_000).toInt() * 10

        val mapsPending = PendingIntent.getActivity(
            context, requestBase + 1, mapsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val wazePending = PendingIntent.getActivity(
            context, requestBase + 2, wazeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val undoIntent = Intent(context, UndoSaveReceiver::class.java).apply {
            putExtra(UndoSaveReceiver.EXTRA_SPOT_ID, spot.id)
            putExtra(UndoSaveReceiver.EXTRA_NOTIFICATION_ID, savedNotificationId)
        }
        val undoPending = PendingIntent.getBroadcast(
            context, requestBase + 3, undoIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = spot.address ?: "%.5f, %.5f".format(spot.lat, spot.lng)

        val notification = NotificationCompat.Builder(context, CHANNEL_SAVED)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Parking spot saved")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(0, "Open in Maps", mapsPending)
            .addAction(0, "Open in Waze", wazePending)
            .addAction(0, "Undo", undoPending)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.notify(savedNotificationId, notification)
    }
}
