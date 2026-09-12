package com.jasongrech.carlocator.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.jasongrech.carlocator.CarLocatorApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Call after any change that could affect the widget's latest-spot display: save, undo, delete. */
object WidgetUpdater {
    fun updateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, CarLocatorWidgetProvider::class.java))
        if (ids.isEmpty()) return

        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            val app = appContext as CarLocatorApp
            val spot = app.db.parkingSpotDao().getLatestOnce()
            val views = CarLocatorWidgetProvider.buildViews(appContext, spot)
            ids.forEach { manager.updateAppWidget(it, views) }
        }
    }
}
