package com.celik.sopdu.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SopduDao {

    // Chats list
    @Query("SELECT * FROM peers ORDER BY lastSeenAt DESC")
    fun observePeers(): Flow<List<PeerEntity>>

    @Query("SELECT * FROM peers ORDER BY lastSeenAt DESC")
    suspend fun getAllPeers(): List<PeerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPeer(peer: PeerEntity)

    @Query("DELETE FROM peers")
    suspend fun deleteAllPeers()

    @Query("DELETE FROM peers WHERE id = :peerId")
    suspend fun deletePeer(peerId: String)

    // Messages
    @Query("SELECT * FROM messages WHERE peerId = :peerId ORDER BY ts ASC")
    fun observeMessages(peerId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY ts ASC")
    suspend fun getAllMessages(): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: MessageEntity)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    @Query("DELETE FROM messages WHERE peerId = :peerId")
    suspend fun deleteMessagesForPeer(peerId: String)

    @Query("DELETE FROM messages WHERE ts < :olderThanTs")
    suspend fun deleteMessagesOlderThan(olderThanTs: Long)
}
