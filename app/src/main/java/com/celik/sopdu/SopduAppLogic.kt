package com.celik.sopdu

import com.celik.sopdu.data.MessageEntity

private const val MAX_INCOMING_MESSAGE_CHARS = 1200

internal fun incomingPeerName(peerId: String, nearbyPeers: List<Peer>): String =
    nearbyPeers.find { it.id == peerId }?.name?.uppercase() ?: peerId.take(6).uppercase()

internal fun shouldIgnoreIncomingPeer(
    peerId: String,
    identity: LocalIdentity,
    blockedPeers: Set<String>,
    rejectedPeers: Map<String, String>
): Boolean = peerId == identity.deviceId || blockedPeers.contains(peerId) || rejectedPeers.containsKey(peerId)

internal fun cleanIncomingText(text: String): String? {
    val trimmed = text.trim()
    if (trimmed.isBlank()) return null
    return trimmed.take(MAX_INCOMING_MESSAGE_CHARS)
}

internal fun messageEntityToChatMsg(entity: MessageEntity): ChatMsg {
    val fallback = if (entity.fromMe) MessageDeliveryStatus.SAVED_LOCAL else MessageDeliveryStatus.RECEIVED
    val status = runCatching { MessageDeliveryStatus.valueOf(entity.deliveryStatus) }.getOrDefault(fallback)
    return ChatMsg(entity.id, entity.peerId, entity.fromMe, entity.text, entity.ts, status)
}

internal fun outgoingMessageStatus(sentToTransport: Boolean, peer: Peer): MessageDeliveryStatus =
    if (sentToTransport || peer.id == "TEST_PEER") MessageDeliveryStatus.SENT else MessageDeliveryStatus.SAVED_LOCAL
