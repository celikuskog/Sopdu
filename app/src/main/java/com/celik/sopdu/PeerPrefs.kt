package com.celik.sopdu

import android.content.SharedPreferences

internal fun loadLocalNicknames(prefs: SharedPreferences): Map<String, String> =
    prefs.all.filterKeys { it.startsWith("nickname_") }
        .mapNotNull { (key, value) -> (value as? String)?.let { key.removePrefix("nickname_") to it } }
        .toMap()

internal fun saveLocalNickname(prefs: SharedPreferences, peerId: String, nickname: String): Map<String, String> {
    val editor = prefs.edit()
    if (nickname.isBlank()) editor.remove("nickname_$peerId") else editor.putString("nickname_$peerId", nickname)
    editor.apply()
    return loadLocalNicknames(prefs)
}

internal fun loadAcceptedPeers(prefs: SharedPreferences): Set<String> =
    prefs.getStringSet("accepted_peers", emptySet()).orEmpty()

internal fun saveAcceptedPeer(prefs: SharedPreferences, peerId: String): Set<String> {
    val next = loadAcceptedPeers(prefs).plus(peerId)
    prefs.edit().putStringSet("accepted_peers", next).apply()
    return next
}

internal fun loadBlockedPeers(prefs: SharedPreferences): Set<String> =
    prefs.getStringSet("blocked_peers", emptySet()).orEmpty()

internal fun saveBlockedPeer(prefs: SharedPreferences, peerId: String, name: String): Set<String> {
    val next = loadBlockedPeers(prefs).plus(peerId)
    prefs.edit()
        .putStringSet("blocked_peers", next)
        .putString("blocked_peer_$peerId", name.ifBlank { peerId.take(6).uppercase() })
        .apply()
    return next
}

internal fun removeBlockedPeer(prefs: SharedPreferences, peerId: String): Set<String> {
    val next = loadBlockedPeers(prefs).minus(peerId)
    prefs.edit().putStringSet("blocked_peers", next).remove("blocked_peer_$peerId").apply()
    return next
}

internal fun loadBlockedPeerNames(prefs: SharedPreferences): Map<String, String> =
    loadBlockedPeers(prefs).associateWith { peerId ->
        prefs.getString("blocked_peer_$peerId", null) ?: peerId.take(6).uppercase()
    }

internal fun loadPendingPeers(prefs: SharedPreferences): Map<String, String> =
    prefs.all.filterKeys { it.startsWith("pending_peer_") }
        .mapNotNull { (key, value) -> (value as? String)?.let { key.removePrefix("pending_peer_") to it } }
        .toMap()

internal fun savePendingPeer(prefs: SharedPreferences, peerId: String, name: String): Map<String, String> {
    prefs.edit().putString("pending_peer_$peerId", name).apply()
    return loadPendingPeers(prefs)
}

internal fun removePendingPeer(prefs: SharedPreferences, peerId: String): Map<String, String> {
    prefs.edit().remove("pending_peer_$peerId").apply()
    return loadPendingPeers(prefs)
}

internal fun seedDebugPendingPeer(prefs: SharedPreferences): Map<String, String> {
    if (prefs.getBoolean("debug_pending_seeded", false)) return loadPendingPeers(prefs)
    prefs.edit()
        .putString("pending_peer_DEBUG_REQUEST", "K7M2")
        .putBoolean("debug_pending_seeded", true)
        .apply()
    return loadPendingPeers(prefs)
}

internal fun loadRejectedPeers(prefs: SharedPreferences): Map<String, String> =
    prefs.all.filterKeys { it.startsWith("rejected_peer_") }
        .mapNotNull { (key, value) -> (value as? String)?.let { key.removePrefix("rejected_peer_") to it } }
        .toMap()

internal fun saveRejectedPeer(prefs: SharedPreferences, peerId: String, name: String): Map<String, String> {
    prefs.edit().putString("rejected_peer_$peerId", name.ifBlank { peerId.take(6).uppercase() }).apply()
    return loadRejectedPeers(prefs)
}

internal fun removeRejectedPeer(prefs: SharedPreferences, peerId: String): Map<String, String> {
    prefs.edit().remove("rejected_peer_$peerId").apply()
    return loadRejectedPeers(prefs)
}

internal fun seedDebugHiddenPeers(prefs: SharedPreferences): Pair<Map<String, String>, Set<String>> {
    if (prefs.getBoolean("debug_hidden_seeded", false)) return loadRejectedPeers(prefs) to loadBlockedPeers(prefs)
    val blocked = loadBlockedPeers(prefs).plus("DEBUG_BLOCKED")
    prefs.edit()
        .putString("rejected_peer_DEBUG_REJECTED", "R8Q4")
        .putStringSet("blocked_peers", blocked)
        .putString("blocked_peer_DEBUG_BLOCKED", "B2L9")
        .putBoolean("debug_hidden_seeded", true)
        .apply()
    return loadRejectedPeers(prefs) to loadBlockedPeers(prefs)
}
