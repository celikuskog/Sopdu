package com.celik.sopdu

data class Peer(val id: String, val name: String, val lastSeenAt: Long = 0L)
enum class Tab { CHATS, NEARBY, SETTINGS }

data class ChatMsg(val id: String, val peerId: String, val fromMe: Boolean, val text: String, val ts: Long, val deliveryStatus: MessageDeliveryStatus = MessageDeliveryStatus.RECEIVED)

enum class MessageDeliveryStatus { RECEIVED, SENT, SAVED_LOCAL, FAILED }

data class OtherDevice(val key: String, val rssi: Int, val lastSeen: Long)

data class IncomingNearbyMessage(val id: String, val peerId: String, val text: String, val ts: Long)

enum class PeerConnectionStatus { UNKNOWN, CONNECTING, CONNECTED, FAILED, DISCONNECTED }

data class RadarHit(
    val kind: String,
    val idKey: String,
    val title: String,
    val rssi: Int?
)
