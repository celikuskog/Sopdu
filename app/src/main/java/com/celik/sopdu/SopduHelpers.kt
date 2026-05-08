package com.celik.sopdu

internal fun fakeStableBatteryForPeer(peerId: String): Int {
    val value = kotlin.math.abs(peerId.hashCode() % 86) + 12
    return value.coerceIn(1, 99)
}
