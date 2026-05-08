package com.celik.sopdu.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [Index(value = ["peerId", "ts"])]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val peerId: String,
    val fromMe: Boolean,
    val text: String,
    val ts: Long = System.currentTimeMillis(),
    val deliveryStatus: String = "RECEIVED"
)
