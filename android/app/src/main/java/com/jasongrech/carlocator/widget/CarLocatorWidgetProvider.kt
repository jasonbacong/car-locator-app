package com.jasongrech.carlocator.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.view.View
import android.widget.RemoteViews
import com.jasongrech.carlocator.CarLocatorApp
import com.jasongrech.carlocator.MainActivity
import com.jasongrech.carlocator.R
import com.jasongrech.carlocator.data.ParkingSpot
import com.jasongrech.carlocator.util.NavIntents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CarLocatorWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        val app = context.applicationContext as CarLocatorApp
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val spot = app.db.parkingSpotDao().getLatestOnce()
                val views = buildViews(context, spot)
                appWidgetIds.forEach { id -> appWidgetManager.updateAppWidget(id, views) }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        fun buildViews(context: Context, spot: ParkingSpot?): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_car_locator)

            if (spot == null) {
                views.setTextViewText(R.id.widget_title, "No spot saved")
                views.setTextViewText(R.id.widget_subtitle, "Park and disconnect to save one")
                views.setViewVisibility(R.id.widget_actions, View.GONE)
            } else {
                views.setTextViewText(
                    R.id.widget_title,
                    spot.address ?: "%.5f, %.5f".format(spot.lat, spot.lng)
                )
                val relativeTime = DateUtils.getRelativeTimeSpanString(
                    spot.timestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
                )
                views.setTextViewText(R.id.widget_subtitle, relativeTime)
                views.setViewVisibility(R.id.widget_actions, View.VISIBLE)

                views.setOnClickPendingIntent(
                    R.id.widget_maps_button,
                    PendingIntent.getActivity(
                        context, 101, NavIntents.resolvedMapsIntent(context, spot.lat, spot.lng),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                views.setOnClickPendingIntent(
                    R.id.widget_waze_button,
                    PendingIntent.getActivity(
                        context, 102, NavIntents.resolvedWazeIntent(context, spot.lat, spot.lng),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }

            views.setOnClickPendingIntent(
                R.id.widget_root,
                PendingIntent.getActivity(
                    context, 100, Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            return views
        }
    }
}
