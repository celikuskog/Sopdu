package com.celik.sopdu.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "peers")
data class PeerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val lastSeenAt: Long = System.currentTimeMillis()
)