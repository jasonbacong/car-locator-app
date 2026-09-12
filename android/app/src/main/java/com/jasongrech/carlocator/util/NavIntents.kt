package com.jasongrech.carlocator.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object NavIntents {

    fun mapsIntent(lat: Double, lng: Double): Intent {
        val uri = Uri.parse("google.navigation:q=$lat,$lng&mode=d")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun mapsIntentFallback(lat: Double, lng: Double): Intent {
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun wazeIntent(lat: Double, lng: Double): Intent {
        val uri = Uri.parse("waze://?ll=$lat,$lng&navigate=yes")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.waze")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun wazeIntentFallback(lat: Double, lng: Double): Intent {
        val uri = Uri.parse("https://waze.com/ul?ll=$lat,$lng&navigate=yes")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun resolvedMapsIntent(context: Context, lat: Double, lng: Double): Intent {
        val primary = mapsIntent(lat, lng)
        return if (primary.resolveActivity(context.packageManager) != null) primary
        else mapsIntentFallback(lat, lng)
    }

    fun resolvedWazeIntent(context: Context, lat: Double, lng: Double): Intent {
        val primary = wazeIntent(lat, lng)
        return if (primary.resolveActivity(context.packageManager) != null) primary
        else wazeIntentFallback(lat, lng)
    }
}
