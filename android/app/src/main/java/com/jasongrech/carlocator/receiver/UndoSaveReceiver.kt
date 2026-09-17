package com.jasongrech.carlocator.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jasongrech.carlocator.CarLocatorApp
import com.jasongrech.carlocator.util.WearSyncer
import com.jasongrech.carlocator.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Handles the "Undo" action on the saved-spot notification. */
class UndoSaveReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_SPOT_ID = "spot_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val spotId = intent.getLongExtra(EXTRA_SPOT_ID, -1L)
        if (spotId == -1L) return

        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if (notificationId != -1) {
            context.getSystemService(NotificationManager::class.java)?.cancel(notificationId)
        }

        val pendingResult = goAsync()
        val app = context.applicationContext as CarLocatorApp
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.db.parkingSpotDao().delete(spotId)
                WidgetUpdater.updateAll(context)
                WearSyncer.pushLatest(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
