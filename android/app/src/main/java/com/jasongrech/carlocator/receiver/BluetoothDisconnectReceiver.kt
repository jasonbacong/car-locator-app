package com.jasongrech.carlocator.receiver

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.jasongrech.carlocator.CarLocatorApp
import com.jasongrech.carlocator.service.ParkingSaveService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Registered in the manifest. ACL_DISCONNECTED is exempt from the Android 8+
 * implicit-broadcast background limits, so this fires even when the app has
 * no running process — as long as the app hasn't been force-stopped and has
 * been launched at least once since install.
 */
class BluetoothDisconnectReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != BluetoothDevice.ACTION_ACL_DISCONNECTED) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }
        val address = try {
            device?.address
        } catch (e: SecurityException) {
            null
        } ?: return

        val pendingResult = goAsync()
        val app = context.applicationContext as CarLocatorApp

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val enabled = app.prefs.featureEnabled.first()
                val savedDevices = app.db.carDeviceDao().getAllOnce()
                val isKnownCar = savedDevices.any { it.address.equals(address, ignoreCase = true) }
                if (enabled && isKnownCar) {
                    ContextCompat.startForegroundService(
                        context, Intent(context, ParkingSaveService::class.java)
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
