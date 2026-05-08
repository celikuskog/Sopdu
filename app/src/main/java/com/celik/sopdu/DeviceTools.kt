package com.celik.sopdu

import android.annotation.SuppressLint
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.ContextCompat

internal fun openBluetoothSettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

internal fun openTetheringSettings(context: Context) {
    listOf(
        Intent("android.settings.TETHER_SETTINGS"),
        Intent(Settings.ACTION_WIRELESS_SETTINGS),
        Intent(Settings.ACTION_SETTINGS)
    ).forEach { intent ->
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        } catch (_: Throwable) {
        }
    }
}

internal fun openDialer(context: Context) {
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

internal fun isBluetoothEnabled(): Boolean =
    BluetoothAdapter.getDefaultAdapter()?.isEnabled == true

internal fun isTetheringOnBestEffort(context: Context): Boolean {
    return try {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val method = cm.javaClass.getDeclaredMethod("getTetheredIfaces")
        method.isAccessible = true
        (method.invoke(cm) as? Array<*>)?.isNotEmpty() == true
    } catch (_: Throwable) {
        false
    }
}

internal class BleRadarScanner(private val context: Context) {
    val otherDevices = mutableStateListOf<OtherDevice>()

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val scanner: BluetoothLeScanner? = adapter?.bluetoothLeScanner
    private var scanCallback: ScanCallback? = null

    @SuppressLint("MissingPermission")
    fun start() {
        if (scanner == null || scanCallback != null || !hasBleScanPermission()) return
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val now = System.currentTimeMillis()
                val address = result.device?.address ?: return
                val key = address.hashCode().toUInt().toString(16).uppercase().take(6)
                val index = otherDevices.indexOfFirst { it.key == key }
                if (index >= 0) {
                    otherDevices[index] = otherDevices[index].copy(rssi = result.rssi, lastSeen = now)
                } else {
                    otherDevices.add(OtherDevice(key, result.rssi, now))
                }
            }
        }
        scanCallback = callback
        try {
            scanner.startScan(null, ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), callback)
        } catch (_: Throwable) {
            scanCallback = null
        }
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        val callback = scanCallback
        if (callback != null && scanner != null && hasBleScanPermission()) {
            try {
                scanner.stopScan(callback)
            } catch (_: Throwable) {
            }
        }
        scanCallback = null
    }

    fun clear() {
        otherDevices.clear()
    }

    private fun hasBleScanPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
}
