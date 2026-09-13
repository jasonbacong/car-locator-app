package com.jasongrech.carlocator.ui.components

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jasongrech.carlocator.data.CarDevice
import com.jasongrech.carlocator.deviceNameSafe
import com.jasongrech.carlocator.ui.theme.Mono
import com.jasongrech.carlocator.ui.theme.TextPrimary
import com.jasongrech.carlocator.ui.theme.TextSecondary

/** Any of these disconnecting can trigger a save — covers households with more than one car. */
@Composable
fun CarDevicesCard(
    devices: List<CarDevice>,
    onAdd: (address: String, name: String) -> Unit,
    onDelete: (Long) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    SectionCard {
        if (devices.isEmpty()) {
            Text(
                "No car devices added yet. Add your car's Bluetooth to arm auto-save.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        } else {
            devices.forEach { device ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(device.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(
                            device.address,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = Mono),
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    TactileButton(text = "Remove", onClick = { onDelete(device.id) }, variant = ButtonVariant.Danger)
                }
            }
        }

        TactileButton(
            text = "Add car device",
            onClick = { showPicker = true },
            variant = ButtonVariant.Secondary,
            fullWidth = true
        )
    }

    if (showPicker) {
        CarDevicePickerDialog(
            excludedAddresses = devices.map { it.address }.toSet(),
            onDismiss = { showPicker = false },
            onSelected = { address, name ->
                onAdd(address, name)
                showPicker = false
            }
        )
    }
}

@Composable
private fun CarDevicePickerDialog(
    excludedAddresses: Set<String>,
    onDismiss: () -> Unit,
    onSelected: (address: String, name: String) -> Unit
) {
    val context = LocalContext.current
    val devices = remember {
        try {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                Manifest.permission.BLUETOOTH_CONNECT else Manifest.permission.BLUETOOTH
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                val manager = context.getSystemService(BluetoothManager::class.java)
                manager?.adapter?.bondedDevices
                    ?.filter { it.address !in excludedAddresses }
                    ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add car device", color = TextPrimary) },
        text = {
            if (devices.isEmpty()) {
                Text(
                    "No new paired devices found, or Bluetooth permission isn't granted yet. Pair your car in Android's Bluetooth settings first.",
                    color = TextSecondary
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    devices.forEach { device ->
                        val label = deviceNameSafe(context, device)
                        TactileButton(
                            text = label,
                            onClick = { onSelected(device.address, label) },
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
