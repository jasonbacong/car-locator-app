package com.jasongrech.carlocator.ui.screens

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jasongrech.carlocator.R
import com.jasongrech.carlocator.data.ParkingSpot
import com.jasongrech.carlocator.ui.components.ButtonVariant
import com.jasongrech.carlocator.ui.components.TactileButton
import com.jasongrech.carlocator.ui.theme.Accent
import com.jasongrech.carlocator.ui.theme.Bg
import com.jasongrech.carlocator.ui.theme.Mono
import com.jasongrech.carlocator.ui.theme.Success
import com.jasongrech.carlocator.ui.theme.TextMuted
import com.jasongrech.carlocator.ui.theme.TextPrimary
import com.jasongrech.carlocator.ui.theme.TextSecondary
import com.jasongrech.carlocator.util.LocationUtils
import com.jasongrech.carlocator.util.NavIntents
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/** Close enough that more GPS polling just burns battery for no real benefit. */
private const val ARRIVAL_THRESHOLD_METERS = 15f

/**
 * Distance + a rotating arrow pointing at the saved spot — for parking garages where
 * Maps/Waze themselves lose GPS accuracy between concrete levels. Uses the device's
 * rotation sensor plus continuous location updates while this screen is on screen —
 * and stops those updates automatically once you're basically standing at the car,
 * rather than polling GPS the whole time this screen happens to be open.
 */
@SuppressLint("MissingPermission")
@Composable
fun LocateScreen(spot: ParkingSpot, onBack: () -> Unit) {
    val context = LocalContext.current

    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var azimuthDegrees by remember { mutableStateOf(0f) }
    var isTracking by remember { mutableStateOf(true) }

    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                currentLocation = result.lastLocation
            }
        }
    }

    fun startTracking() {
        if (LocationUtils.hasLocationPermission(context)) {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L).build()
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        }
        isTracking = true
    }

    fun stopTracking() {
        fusedClient.removeLocationUpdates(locationCallback)
        isTracking = false
    }

    DisposableEffect(spot.id) {
        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                azimuthDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (rotationSensor != null) {
            sensorManager.registerListener(sensorListener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        }

        startTracking()

        onDispose {
            sensorManager.unregisterListener(sensorListener)
            fusedClient.removeLocationUpdates(locationCallback)
        }
    }

    val distanceMeters = currentLocation?.let {
        LocationUtils.distanceMeters(it.latitude, it.longitude, spot.lat, spot.lng)
    }

    LaunchedEffect(distanceMeters) {
        if (isTracking && distanceMeters != null && distanceMeters <= ARRIVAL_THRESHOLD_METERS) {
            stopTracking()
        }
    }

    val arrived = !isTracking && distanceMeters != null && distanceMeters <= ARRIVAL_THRESHOLD_METERS

    val bearingToTarget = currentLocation?.let { loc ->
        val from = Location("from").apply { latitude = loc.latitude; longitude = loc.longitude }
        val target = Location("target").apply { latitude = spot.lat; longitude = spot.lng }
        from.bearingTo(target)
    } ?: 0f

    val arrowRotation = bearingToTarget - azimuthDegrees

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TactileButton(text = "Back", onClick = onBack, variant = ButtonVariant.Secondary)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            spot.address ?: "%.5f, %.5f".format(spot.lat, spot.lng),
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_up),
                contentDescription = "Direction to your car",
                tint = Accent,
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer { rotationZ = arrowRotation }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (arrived) {
            Text("You're right here", style = MaterialTheme.typography.headlineSmall, color = Success)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "GPS tracking paused to save battery — tap below if you're not actually there yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            TactileButton(text = "Keep tracking", variant = ButtonVariant.Secondary, onClick = { startTracking() })
        } else if (distanceMeters != null) {
            val distanceText = if (distanceMeters >= 1000f) {
                "%.1f km".format(distanceMeters / 1000f)
            } else {
                "%.0f m".format(distanceMeters)
            }
            Text(distanceText, style = MaterialTheme.typography.headlineSmall.copy(fontFamily = Mono), color = TextPrimary)
            Text("away, in the direction of the arrow", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        } else {
            Text("Getting your location…", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        }

        Spacer(modifier = Modifier.height(32.dp))

        TactileButton(
            text = "Open in Maps",
            onClick = { context.startActivity(NavIntents.resolvedMapsIntent(context, spot.lat, spot.lng)) },
            fullWidth = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        TactileButton(
            text = "Open in Waze",
            onClick = { context.startActivity(NavIntents.resolvedWazeIntent(context, spot.lat, spot.lng)) },
            variant = ButtonVariant.Secondary,
            fullWidth = true
        )
    }
}
