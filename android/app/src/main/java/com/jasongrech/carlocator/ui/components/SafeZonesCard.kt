package com.jasongrech.carlocator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jasongrech.carlocator.data.SafeZone
import com.jasongrech.carlocator.ui.theme.Accent
import com.jasongrech.carlocator.ui.theme.Mono
import com.jasongrech.carlocator.ui.theme.SurfaceRaised
import com.jasongrech.carlocator.ui.theme.TextMuted
import com.jasongrech.carlocator.ui.theme.TextPrimary
import com.jasongrech.carlocator.ui.theme.TextSecondary
import com.jasongrech.carlocator.util.LocationUtils
import kotlinx.coroutines.launch

/** Home zone covers the common case; this is for the other regular spots — work, gym, wherever else. */
@Composable
fun SafeZonesCard(
    zones: List<SafeZone>,
    onAdd: (name: String, lat: Double, lng: Double, radiusMeters: Float) -> Unit,
    onDelete: (Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    SectionCard {
        if (zones.isEmpty()) {
            Text(
                "No other safe zones — auto-save only skips near home.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        } else {
            zones.forEach { zone ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(zone.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(
                            "${zone.radiusMeters.toInt()} m radius",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = Mono),
                            color = TextSecondary
                        )
                    }
                    TactileButton(text = "Remove", onClick = { onDelete(zone.id) }, variant = ButtonVariant.Danger)
                }
            }
        }

        TactileButton(
            text = "Add safe zone",
            onClick = { showAddDialog = true },
            variant = ButtonVariant.Secondary,
            fullWidth = true
        )
    }

    if (showAddDialog) {
        AddZoneDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, lat, lng, radius ->
                onAdd(name, lat, lng, radius)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddZoneDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, lat: Double, lng: Double, radiusMeters: Float) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var capturedLat by remember { mutableStateOf<Double?>(null) }
    var capturedLng by remember { mutableStateOf<Double?>(null) }
    var radius by remember { mutableStateOf(150f) }
    var statusText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add safe zone", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Name, e.g. Work", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = TextSecondary,
                        cursorColor = Accent
                    )
                )
                TactileButton(
                    text = "Use current location",
                    onClick = {
                        scope.launch {
                            val location = LocationUtils.getCurrentLocation(context)
                            if (location != null) {
                                capturedLat = location.latitude
                                capturedLng = location.longitude
                                statusText = "%.5f, %.5f".format(location.latitude, location.longitude)
                            } else {
                                statusText = "Couldn't get a location fix."
                            }
                        }
                    },
                    variant = ButtonVariant.Secondary,
                    fullWidth = true
                )
                statusText?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall.copy(fontFamily = Mono), color = TextSecondary)
                }
                Text("${radius.toInt()} m radius", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Slider(
                    value = radius,
                    onValueChange = { radius = it },
                    valueRange = 50f..500f,
                    colors = SliderDefaults.colors(
                        thumbColor = Accent,
                        activeTrackColor = Accent,
                        inactiveTrackColor = SurfaceRaised
                    )
                )
            }
        },
        confirmButton = {
            TactileButton(
                text = "Add",
                enabled = name.isNotBlank() && capturedLat != null && capturedLng != null,
                onClick = {
                    val lat = capturedLat
                    val lng = capturedLng
                    if (lat != null && lng != null) {
                        onAdd(name.trim(), lat, lng, radius)
                    }
                }
            )
        },
        dismissButton = {
            TactileButton(text = "Cancel", onClick = onDismiss, variant = ButtonVariant.Secondary)
        }
    )
}
