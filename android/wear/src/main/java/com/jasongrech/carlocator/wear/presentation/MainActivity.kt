package com.jasongrech.carlocator.wear.presentation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable

private const val PARKING_SPOT_PATH = "/parking_spot"

data class SyncedSpot(
    val lat: Double,
    val lng: Double,
    val address: String?,
    val timestamp: Long
)

/**
 * Mirrors the phone's most recently saved parking spot, pushed over the Wear OS
 * Data Layer by WearSyncer on the phone. Reads the current item on launch and
 * subscribes for live updates while visible, so this stays correct even if the
 * spot changes (new save, undo, manual delete) while the watch screen is open.
 */
class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {

    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val mainHandler = Handler(Looper.getMainLooper())
    private val spotState = mutableStateOf<SyncedSpot?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp(spot = spotState.value, onOpenMaps = ::openMaps)
        }
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(this)
        loadLatest()
    }

    override fun onPause() {
        dataClient.removeListener(this)
        super.onPause()
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == PARKING_SPOT_PATH) {
                applyDataMap(DataMapItem.fromDataItem(event.dataItem).dataMap)
            }
        }
        dataEvents.release()
    }

    private fun loadLatest() {
        dataClient.dataItems.addOnSuccessListener { buffer ->
            val item = (0 until buffer.count)
                .map { buffer[it] }
                .firstOrNull { it.uri.path == PARKING_SPOT_PATH }
            item?.let { applyDataMap(DataMapItem.fromDataItem(it).dataMap) }
            buffer.release()
        }
    }

    private fun applyDataMap(map: DataMap) {
        val updated = if (map.getBoolean("hasSpot", false)) {
            SyncedSpot(
                lat = map.getDouble("lat"),
                lng = map.getDouble("lng"),
                address = map.getString("address")?.ifBlank { null },
                timestamp = map.getLong("timestamp")
            )
        } else null
        mainHandler.post { spotState.value = updated }
    }

    private fun openMaps(spot: SyncedSpot) {
        val uri = Uri.parse("geo:${spot.lat},${spot.lng}?q=${spot.lat},${spot.lng}")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No maps app on this watch", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun WearApp(spot: SyncedSpot?, onOpenMaps: (SyncedSpot) -> Unit) {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (spot == null) {
                Text(
                    text = "No parking spot saved yet",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.body2
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = spot.address ?: "%.5f, %.5f".format(spot.lat, spot.lng),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.title3
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = timeAgo(spot.timestamp), style = MaterialTheme.typography.caption1)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = { onOpenMaps(spot) }, colors = ButtonDefaults.primaryButtonColors()) {
                        Text("Navigate")
                    }
                }
            }
        }
    }
}

private fun timeAgo(timestamp: Long): String {
    val minutes = (System.currentTimeMillis() - timestamp) / 60000
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes min ago"
        minutes < 60 * 24 -> "${minutes / 60} hr ago"
        else -> "${minutes / (60 * 24)} d ago"
    }
}
