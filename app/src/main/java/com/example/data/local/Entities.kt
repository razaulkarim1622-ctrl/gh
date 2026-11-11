package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageStatus {
    QUEUED_OFFLINE,
    SENDING,
    SENT,
    DELIVERED,
    READ
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val recipientId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val deliveryStatus: MessageStatus = MessageStatus.SENT,
    val isMine: Boolean = true,
    val isGroup: Boolean = false,
    val isNearbyP2P: Boolean = false
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val avatarColor: Long = 0xFF087F5B,
    val avatarInitials: String = "",
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isGroup: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isBlocked: Boolean = false,
    val isNearbyP2P: Boolean = false,
    val isOnline: Boolean = true,
    val participantPhone: String = ""
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phoneNumber: String,
    val bio: String = "Available",
    val avatarColor: Long = 0xFF0AA879,
    val isRegistered: Boolean = true,
    val isBlocked: Boolean = false
)
