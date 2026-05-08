package com.celik.sopdu

import com.celik.sopdu.data.MessageEntity
import com.celik.sopdu.data.PeerEntity
import com.celik.sopdu.data.SopduDao
import java.util.UUID

internal suspend fun seedDebugChatsIfNeeded(dao: SopduDao, existingPeers: List<PeerEntity>) {
    dao.upsertPeer(PeerEntity(id = "TEST_PEER", name = "TEST PEER"))
    if (existingPeers.none { it.id == "DEBUG_BLOCKED" }) {
        dao.upsertPeer(PeerEntity(id = "DEBUG_BLOCKED", name = "B2L9", lastSeenAt = System.currentTimeMillis() - 41 * 60_000L))
    }

    val now = System.currentTimeMillis()
    val codes = listOf("A3F9", "F0E4", "B7C2", "9C7B", "D1A8")
    codes.forEachIndexed { idx, code ->
        val id = "TEST_0${idx + 1}"
        if (existingPeers.none { it.id == id }) {
            dao.upsertPeer(PeerEntity(id, code, now - (idx + 1) * 11 * 60_000L))
            dao.insertMessage(MessageEntity(UUID.randomUUID().toString(), id, false, "STATUS? ANYONE THERE?", now - (idx + 1) * 60_000L, "RECEIVED"))
            dao.insertMessage(MessageEntity(UUID.randomUUID().toString(), id, true, "COPY. STAY CALM.", now - (idx + 1) * 58_000L, "SENT"))
        }
    }
}
