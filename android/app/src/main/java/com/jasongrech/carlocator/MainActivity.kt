package com.jasongrech.carlocator

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jasongrech.carlocator.data.ParkingSpot
import com.jasongrech.carlocator.service.ParkingSaveService
import com.jasongrech.carlocator.ui.components.ButtonVariant
import com.jasongrech.carlocator.ui.components.ParkingBadge
import com.jasongrech.carlocator.ui.components.ParkingSpotRow
import com.jasongrech.carlocator.ui.components.SafeZonesCard
import com.jasongrech.carlocator.ui.components.SectionCard
import com.jasongrech.carlocator.ui.components.SectionEyebrow
import com.jasongrech.carlocator.ui.components.StatusDot
import com.jasongrech.carlocator.ui.components.TactileButton
import com.jasongrech.carlocator.ui.screens.LocateScreen
import com.jasongrech.carlocator.ui.theme.Accent
import com.jasongrech.carlocator.ui.theme.AccentTint
import com.jasongrech.carlocator.ui.theme.Bg
import com.jasongrech.carlocator.ui.theme.CarLocatorTheme
import com.jasongrech.carlocator.ui.theme.Mono
import com.jasongrech.carlocator.ui.theme.SurfaceRaised
import com.jasongrech.carlocator.ui.theme.TextMuted
import com.jasongrech.carlocator.ui.theme.TextPrimary
import com.jasongrech.carlocator.ui.theme.TextSecondary
import com.jasongrech.carlocator.util.LocationUtils
import com.jasongrech.carlocator.util.ReminderScheduler
import com.jasongrech.carlocator.widget.WidgetUpdater
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as CarLocatorApp
        setContent {
            CarLocatorTheme {
                CarLocatorScreen(app)
            }
        }
    }
}

private fun requiredPermissions(): List<String> {
    val perms = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        perms.add(Manifest.permission.BLUETOOTH_CONNECT)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        perms.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    return perms
}

private fun hasAllCorePermissions(context: Context): Boolean =
    requiredPermissions().all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

private fun hasBackgroundLocation(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

fun deviceNameSafe(context: Context, device: BluetoothDevice): String {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            device.address
        } else {
            device.name ?: device.address
        }
    } catch (e: SecurityException) {
        device.address
    }
}

@Composable
fun CarLocatorScreen(app: CarLocatorApp) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val carDeviceName by app.prefs.carDeviceName.collectAsState(initial = null)
    val carDeviceAddress by app.prefs.carDeviceAddress.collectAsState(initial = null)
    val home by app.prefs.homeLocation.collectAsState(initial = null)
    val radius by app.prefs.homeRadiusMeters.collectAsState(initial = 150f)
    val enabled by app.prefs.featureEnabled.collectAsState(initial = true)
    val spots by app.db.parkingSpotDao().getAll().collectAsState(initial = emptyList())
    val safeZones by app.db.safeZoneDao().getAll().collectAsState(initial = emptyList())

    var showDevicePicker by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var permissionTick by remember { mutableStateOf(0) }
    var locateTarget by remember { mutableStateOf<ParkingSpot?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionTick++ }

    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permissionTick++ }

    val corePermissionsGranted = permissionTick.let { hasAllCorePermissions(context) }
    val backgroundLocationGranted = permissionTick.let { hasBackgroundLocation(context) }
    val allReady = corePermissionsGranted && backgroundLocationGranted

    val currentLocateTarget = locateTarget
    if (currentLocateTarget != null) {
        LocateScreen(spot = currentLocateTarget, onBack = { locateTarget = null })
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(Bg)) {
        // Header — bespoke, not a Material app bar: the badge doubles as the app mark.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ParkingBadge(size = 38.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Text("CAR LOCATOR", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
            Spacer(modifier = Modifier.weight(1f))
            StatusDot(ready = allReady)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp, end = 20.dp, bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionEyebrow("Status")
                    SectionCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusDot(ready = allReady)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                if (allReady) "All set" else "Needs attention",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                        }
                        Text(
                            if (allReady)
                                "Detection is fully armed."
                            else
                                "Location, Bluetooth and notification access are needed for auto-detect to work.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        if (!corePermissionsGranted) {
                            TactileButton(
                                text = "Grant core permissions",
                                onClick = { permissionLauncher.launch(requiredPermissions().toTypedArray()) },
                                fullWidth = true
                            )
                        }
                        if (corePermissionsGranted && !backgroundLocationGranted) {
                            TactileButton(
                                text = "Allow background location",
                                onClick = {
                                    backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                },
                                variant = ButtonVariant.Secondary,
                                fullWidth = true
                            )
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionEyebrow("Parking detection")
                    SectionCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Auto-save enabled", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Switch(
                                checked = enabled,
                                onCheckedChange = { scope.launch { app.prefs.setFeatureEnabled(it) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Accent,
                                    checkedTrackColor = AccentTint,
                                    checkedBorderColor = Accent,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = SurfaceRaised,
                                    uncheckedBorderColor = SurfaceRaised
                                )
                            )
                        }
                        Text(
                            "Car device: ${carDeviceName ?: "not set"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        TactileButton(
                            text = if (carDeviceAddress == null) "Select car device" else "Change car device",
                            onClick = { showDevicePicker = true },
                            variant = ButtonVariant.Secondary,
                            fullWidth = true
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionEyebrow("Home zone")
                    SectionCard {
                        Text(
                            home?.let { "%.5f, %.5f".format(it.lat, it.lng) } ?: "not set",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = Mono),
                            color = if (home != null) TextPrimary else TextMuted
                        )
                        TactileButton(
                            text = "Use current location as home",
                            onClick = {
                                scope.launch {
                                    val location = LocationUtils.getCurrentLocation(context)
                                    if (location != null) {
                                        app.prefs.setHomeLocation(location.latitude, location.longitude)
                                        statusMessage = "Home location updated."
                                    } else {
                                        statusMessage = "Couldn't get a location fix. Check permissions/GPS."
                                    }
                                }
                            },
                            variant = ButtonVariant.Secondary,
                            fullWidth = true
                        )
                        Text(
                            "Skip saving within ${radius.toInt()} m of home",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Slider(
                            value = radius,
                            onValueChange = { scope.launch { app.prefs.setHomeRadiusMeters(it) } },
                            valueRange = 50f..500f,
                            colors = SliderDefaults.colors(
                                thumbColor = Accent,
                                activeTrackColor = Accent,
                                inactiveTrackColor = SurfaceRaised
                            )
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionEyebrow("Other safe zones")
                    SafeZonesCard(
                        zones = safeZones,
                        onAdd = { name, lat, lng, zoneRadius ->
                            scope.launch {
                                app.db.safeZoneDao().insert(
                                    com.jasongrech.carlocator.data.SafeZone(
                                        name = name, lat = lat, lng = lng, radiusMeters = zoneRadius
                                    )
                                )
                            }
                        },
                        onDelete = { id -> scope.launch { app.db.safeZoneDao().delete(id) } }
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionEyebrow("Test")
                    SectionCard {
                        TactileButton(
                            text = "Save current location now",
                            onClick = {
                                if (!hasAllCorePermissions(context)) {
                                    statusMessage = "Grant the core permissions above first — location access is required."
                                } else {
                                    ContextCompat.startForegroundService(
                                        context,
                                        Intent(context, ParkingSaveService::class.java)
                                            .putExtra(ParkingSaveService.EXTRA_FORCE, true)
                                    )
                                    statusMessage = "Requesting a GPS fix — watch for a toast or notification in a few seconds."
                                }
                            },
                            fullWidth = true
                        )
                        statusMessage?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionEyebrow("History")
                    Text(
                        "Kept for 60 days, then cleared automatically",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            if (spots.isEmpty()) {
                item {
                    SectionCard {
                        Text(
                            "No spots saved yet.",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            "Park, disconnect from the car, and this fills in — or use the test button above.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(spots) { spot ->
                    ParkingSpotRow(
                        spot = spot,
                        onDelete = {
                            scope.launch {
                                app.db.parkingSpotDao().delete(spot.id)
                                WidgetUpdater.updateAll(context)
                            }
                        },
                        onLocate = { locateTarget = spot },
                        onSaveNote = { note ->
                            scope.launch { app.db.parkingSpotDao().updateNote(spot.id, note) }
                        },
                        onScheduleReminder = { minutes ->
                            ReminderScheduler.schedule(context, minutes)
                            statusMessage = "Reminder set."
                        }
                    )
                }
            }
        }
    }

    if (showDevicePicker) {
        DevicePickerDialog(
            onDismiss = { showDevicePicker = false },
            onSelected = { device ->
                scope.launch { app.prefs.setCarDevice(device.address, deviceNameSafe(context, device)) }
                showDevicePicker = false
            }
        )
    }
}

@Composable
fun DevicePickerDialog(onDismiss: () -> Unit, onSelected: (BluetoothDevice) -> Unit) {
    val context = LocalContext.current
    val devices = remember {
        try {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                Manifest.permission.BLUETOOTH_CONNECT else Manifest.permission.BLUETOOTH
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                val manager = context.getSystemService(BluetoothManager::class.java)
                manager?.adapter?.bondedDevices?.toList() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select car Bluetooth device", color = TextPrimary) },
        text = {
            if (devices.isEmpty()) {
                Text(
                    "No paired devices found, or Bluetooth permission isn't granted yet.",
                    color = TextSecondary
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    devices.forEach { device ->
                        TactileButton(
                            text = deviceNameSafe(context, device),
                            onClick = { onSelected(device) },
                            variant = ButtonVariant.Secondary,
                            fullWidth = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            TactileButton(text = "Cancel", onClick = onDismiss, variant = ButtonVariant.Secondary)
        }
    )
}
