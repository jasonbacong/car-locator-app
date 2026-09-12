package com.jasongrech.carlocator.util

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jasongrech.carlocator.service.ParkingReminderWorker
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    fun schedule(context: Context, minutes: Long) {
        val request = OneTimeWorkRequestBuilder<ParkingReminderWorker>()
            .setInitialDelay(minutes, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
