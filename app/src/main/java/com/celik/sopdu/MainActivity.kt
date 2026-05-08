package com.celik.sopdu

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val nearbyTransport by lazy { NearbyTransport(this) }

    private val requiredPerms: Array<String> by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private val permsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (requiredPerms.all { result[it] == true }) nearbyTransport.initCore()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SopduApp(
                nearbyPeers = nearbyTransport.peers,
                incomingNearbyMessages = nearbyTransport.incomingMessages,
                connectionStatusByPeer = nearbyTransport.connectionStatus,
                onRequestInit = { ensureCoreReady() },
                onStartScanWindow = { nearbyTransport.startRadioWindow() },
                onStopScanWindow = { nearbyTransport.stopRadioWindow() },
                onSendNearbyMessage = { endpointId, text -> nearbyTransport.sendTextPayload(endpointId, text) }
            )
        }

        ensureCoreReady()
    }

    override fun onStop() {
        super.onStop()
        nearbyTransport.stopRadioWindow()
    }

    private fun ensureCoreReady() {
        if (hasAllPerms()) nearbyTransport.initCore() else permsLauncher.launch(requiredPerms)
    }

    private fun hasAllPerms(): Boolean =
        requiredPerms.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
}
