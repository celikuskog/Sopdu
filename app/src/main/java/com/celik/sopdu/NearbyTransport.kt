package com.celik.sopdu

import android.content.Context
import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy

internal class NearbyTransport(private val context: Context) {
    val peers = mutableStateListOf<Peer>()
    val incomingMessages = mutableStateListOf<IncomingNearbyMessage>()
    val connectionStatus = mutableStateMapOf<String, PeerConnectionStatus>()

    private lateinit var connectionsClient: ConnectionsClient
    private lateinit var payloadCallback: PayloadCallback
    private lateinit var connectionLifecycleCallback: ConnectionLifecycleCallback
    private lateinit var discoveryCallback: EndpointDiscoveryCallback

    private val serviceId = "com.celik.sopdu.SERVICE"
    private val strategy = Strategy.P2P_CLUSTER
    private var coreReady = false
    private var radioRunning = false

    fun initCore() {
        if (coreReady) return
        coreReady = true
        connectionsClient = Nearby.getConnectionsClient(context)

        payloadCallback = object : PayloadCallback() {
            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                val bytes = payload.asBytes() ?: return
                val text = bytes.toString(Charsets.UTF_8).trim()
                if (text.isNotEmpty()) {
                    incomingMessages.add(IncomingNearbyMessage(payload.id.toString(), endpointId, text, System.currentTimeMillis()))
                }
            }

            override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) = Unit
        }

        connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
                connectionStatus[endpointId] = PeerConnectionStatus.CONNECTING
                connectionsClient.acceptConnection(endpointId, payloadCallback)
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                connectionStatus[endpointId] = if (result.status.isSuccess) PeerConnectionStatus.CONNECTED else PeerConnectionStatus.FAILED
            }

            override fun onDisconnected(endpointId: String) {
                connectionStatus[endpointId] = PeerConnectionStatus.DISCONNECTED
            }
        }

        discoveryCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                if (peers.none { it.id == endpointId }) peers.add(Peer(endpointId, info.endpointName))
                connectionStatus[endpointId] = PeerConnectionStatus.CONNECTING
                connectionsClient.requestConnection(Build.MODEL ?: "Sopdu", endpointId, connectionLifecycleCallback)
            }

            override fun onEndpointLost(endpointId: String) {
                peers.removeAll { it.id == endpointId }
                connectionStatus[endpointId] = PeerConnectionStatus.DISCONNECTED
            }
        }
    }

    fun startRadioWindow() {
        if (!coreReady || radioRunning) return
        radioRunning = true
        connectionsClient.startAdvertising(
            Build.MODEL ?: "Sopdu",
            serviceId,
            connectionLifecycleCallback,
            AdvertisingOptions.Builder().setStrategy(strategy).build()
        )
        connectionsClient.startDiscovery(
            serviceId,
            discoveryCallback,
            DiscoveryOptions.Builder().setStrategy(strategy).build()
        )
    }

    fun stopRadioWindow() {
        if (!coreReady || !radioRunning) return
        radioRunning = false
        connectionsClient.stopAllEndpoints()
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
    }

    fun sendTextPayload(endpointId: String, text: String): Boolean {
        if (!coreReady || connectionStatus[endpointId] != PeerConnectionStatus.CONNECTED) return false
        return try {
            connectionsClient.sendPayload(endpointId, Payload.fromBytes(text.toByteArray(Charsets.UTF_8)))
                .addOnFailureListener { connectionStatus[endpointId] = PeerConnectionStatus.FAILED }
            true
        } catch (_: Throwable) {
            connectionStatus[endpointId] = PeerConnectionStatus.FAILED
            false
        }
    }
}
