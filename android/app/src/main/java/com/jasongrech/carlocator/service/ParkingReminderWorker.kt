package com.jasongrech.carlocator.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.jasongrech.carlocator.MainActivity
import com.jasongrech.carlocator.R
import com.jasongrech.carlocator.util.NotificationHelper

/** Fired by [com.jasongrech.carlocator.util.ReminderScheduler] after the requested delay. */
class ParkingReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val REMINDER_NOTIFICATION_ID = 2001
    }

    override suspend fun doWork(): Result {
        NotificationHelper.ensureChannels(applicationContext)

        val openAppPending = PendingIntent.getActivity(
            applicationContext, 0, Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_SAVED)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Parking reminder")
            .setContentText("Check your meter or time limit — you set this reminder from Car Locator.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppPending)
            .build()

        applicationContext.getSystemService(NotificationManager::class.java)
            ?.notify(REMINDER_NOTIFICATION_ID, notification)

        return Result.success()
    }
}
