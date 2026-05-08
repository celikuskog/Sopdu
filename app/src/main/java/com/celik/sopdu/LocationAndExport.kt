package com.celik.sopdu

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.celik.sopdu.data.MessageEntity
import com.celik.sopdu.data.PeerEntity
import org.json.JSONArray
import org.json.JSONObject

internal fun getLastKnownCoordinates(context: Context): Pair<Double, Double>? {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!fine && !coarse) return null
    return try {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = manager.getProviders(true)
        val last = providers.mapNotNull { provider ->
            runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
        }.maxByOrNull(Location::getTime)
        last?.let { it.latitude to it.longitude }
    } catch (_: Throwable) {
        null
    }
}

internal fun buildPeerInfoText(
    deviceId: String,
    displayName: String,
    additionalName: String,
    batteryPercent: Int?,
    latitude: Double?,
    longitude: Double?,
    timestamp: String,
    distressStatus: String
): String = listOf(
    "deviceId: $deviceId",
    "displayName: $displayName",
    "additionalName: ${additionalName.ifBlank { "not added" }}",
    "batteryPercent: ${batteryPercent?.toString() ?: "not shared"}",
    "latitude: ${latitude?.toString() ?: "not shared"}",
    "longitude: ${longitude?.toString() ?: "not shared"}",
    "timestamp: $timestamp",
    "distressStatus: $distressStatus"
).joinToString("\n")

internal fun buildChatsExportJson(
    identity: LocalIdentity,
    peers: List<PeerEntity>,
    messages: List<MessageEntity>,
    nicknames: Map<String, String>
): String {
    val root = JSONObject()
        .put("format", "sopdu.chats")
        .put("version", 1)
        .put("exportedAt", formatStatusTimestamp(System.currentTimeMillis()))
        .put("deviceId", identity.deviceId)
        .put("displayName", identity.displayName)

    val peersJson = JSONArray()
    peers.forEach { peer ->
        peersJson.put(
            JSONObject()
                .put("id", peer.id)
                .put("displayName", peer.name)
                .put("additionalName", nicknames[peer.id].orEmpty())
                .put("lastSeenAt", peer.lastSeenAt)
        )
    }

    val messagesJson = JSONArray()
    messages.forEach { message ->
        messagesJson.put(
            JSONObject()
                .put("id", message.id)
                .put("peerId", message.peerId)
                .put("fromMe", message.fromMe)
                .put("text", message.text)
                .put("timestamp", message.ts)
                .put("deliveryStatus", message.deliveryStatus)
        )
    }

    return root.put("peers", peersJson).put("messages", messagesJson).toString(2)
}
