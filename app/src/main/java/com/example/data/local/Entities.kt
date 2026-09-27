package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val avatarUrl: String = "",
    val statusMessage: String = "Hey there! I am using SmartMsg",
    val isOnline: Boolean = false,
    val isAiAssistant: Boolean = false,
    val lastSeen: String = "Recently"
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long,
    val senderType: String, // "USER", "CONTACT", "AI"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = true,
    val messageType: String = "TEXT", // "TEXT", "VOICE", "IMAGE"
    val voiceDurationSec: Int = 0,
    val reaction: String = "" // "❤️", "👍", etc.
)

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long,
    val callType: String, // "VOICE", "VIDEO"
    val direction: String, // "INCOMING", "OUTGOING", "MISSED"
    val timestamp: Long = System.currentTimeMillis(),
    val durationSec: Int = 0
)

@Entity(tableName = "statuses")
data class StatusEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long,
    val authorName: String,
    val caption: String,
    val backgroundHex: String = "#1E40AF",
    val mediaUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isMine: Boolean = false,
    val viewsCount: Int = 0
)
