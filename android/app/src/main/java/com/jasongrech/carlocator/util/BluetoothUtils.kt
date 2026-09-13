package com.jasongrech.carlocator.util

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object BluetoothUtils {
    /** False whenever we genuinely can't tell (permission missing, no adapter) — never crashes. */
    fun isBluetoothEnabled(context: Context): Boolean {
        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return false

        return try {
            val manager = context.getSystemService(BluetoothManager::class.java)
            manager?.adapter?.isEnabled == true
        } catch (e: SecurityException) {
            false
        }
    }
}
