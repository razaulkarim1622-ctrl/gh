package com.example.data.repository

import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ContactEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageStatus
import com.example.data.nearby.NearbyP2PManager
import com.example.data.network.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRepository(
    private val chatDao: ChatDao,
    private val networkMonitor: NetworkMonitor,
    private val nearbyManager: NearbyP2PManager
) {
    val allConversations: Flow<List<ConversationEntity>> = chatDao.getAllConversations()
    val groupConversations: Flow<List<ConversationEntity>> = chatDao.getGroupConversations()
    val allContacts: Flow<List<ContactEntity>> = chatDao.getAllContacts()
    val blockedContacts: Flow<List<ContactEntity>> = chatDao.getBlockedContacts()
    val queuedCountFlow: Flow<Int> = chatDao.getQueuedCountFlow()

    fun getMessagesForConversation(convId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForConversation(convId)
    }

    suspend fun initializeDefaultDataIfEmpty() {
        val existing = chatDao.getAllConversations().first()
        if (existing.isNotEmpty()) return

        // Seed initial friendly contacts & conversations
        val contacts = listOf(
            ContactEntity(
                id = "c_1",
                name = "তানভীর আহমেদ",
                phoneNumber = "+880 1712 345678",
                bio = "কর্মব্যস্ত জীবন, সততাই শক্তি।",
                avatarColor = 0xFF087F5B
            ),
            ContactEntity(
                id = "c_2",
                name = "রফিকুল ইসলাম (হিসাব শাখা)",
                phoneNumber = "+880 1819 876543",
                bio = "দৈনন্দিন খাতা ও হিসাবের কাজ।",
                avatarColor = 0xFF0AA879
            ),
            ContactEntity(
                id = "c_3",
                name = "সাদিয়া সুলতানা",
                phoneNumber = "+880 1911 223344",
                bio = "Available on Jihad Chat.",
                avatarColor = 0xFF20C997
            ),
            ContactEntity(
                id = "c_4",
                name = "মাহমুদ হাসান",
                phoneNumber = "+880 1612 998877",
                bio = "কুরআন ও হাদিসের আলোকে জীবন গড়ি।",
                avatarColor = 0xFF12B886
            )
        )
        chatDao.insertContacts(contacts)

        val conversations = listOf(
            ConversationEntity(
                id = "conv_1",
                title = "তানভীর আহমেদ",
                avatarColor = 0xFF087F5B,
                avatarInitials = "তা",
                lastMessageText = "আসসালামু আলাইকুম ভাই, কেমন আছেন?",
                lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 5,
                unreadCount = 1,
                participantPhone = "+880 1712 345678"
            ),
            ConversationEntity(
                id = "conv_2",
                title = "দৈনন্দিন হিসাব গ্রুপ (MRT)",
                avatarColor = 0xFF0AA879,
                avatarInitials = "হি",
                lastMessageText = "আজকের জমা-খরচের খতিয়ান আপডেট করা হয়েছে।",
                lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 30,
                unreadCount = 0,
                isGroup = true
            ),
            ConversationEntity(
                id = "conv_3",
                title = "রফিকুল ইসলাম (হিসাব শাখা)",
                avatarColor = 0xFF20C997,
                avatarInitials = "র",
                lastMessageText = "কাল সকাল ১০টায় মিটিং হবে।",
                lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                unreadCount = 0,
                participantPhone = "+880 1819 876543"
            )
        )
        chatDao.insertConversations(conversations)

        // Seed initial messages
        val initialMessages = listOf(
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = "conv_1",
                senderId = "c_1",
                senderName = "তানভীর আহমেদ",
                recipientId = "current_user",
                text = "শুভ সকাল! জিহাদ চ্যাটে স্বাগতম।",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 15,
                deliveryStatus = MessageStatus.READ,
                isMine = false
            ),
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = "conv_1",
                senderId = "current_user",
                senderName = "আমি",
                recipientId = "c_1",
                text = "ওয়ালাইকুম আসসালাম! অ্যাপটি সত্যিই চমৎকার আর দ্রুত কাজ করছে।",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 10,
                deliveryStatus = MessageStatus.READ,
                isMine = true
            ),
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = "conv_1",
                senderId = "c_1",
                senderName = "তানভীর আহমেদ",
                recipientId = "current_user",
                text = "আসসালামু আলাইকুম ভাই, কেমন আছেন?",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 5,
                deliveryStatus = MessageStatus.READ,
                isMine = false
            ),
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = "conv_2",
                senderId = "c_2",
                senderName = "রফিকুল ইসলাম",
                recipientId = "conv_2",
                text = "আজকের জমা-খরচের খতিয়ান আপডেট করা হয়েছে। সবাই একবার দেখে নিবেন।",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 30,
                deliveryStatus = MessageStatus.READ,
                isMine = false,
                isGroup = true
            )
        )
        chatDao.insertMessages(initialMessages)
    }

    suspend fun sendMessage(
        conversationId: String,
        text: String,
        currentUserName: String,
        isGroup: Boolean = false,
        isNearbyP2P: Boolean = false,
        scope: CoroutineScope
    ) {
        val isOnline = networkMonitor.isCurrentlyConnected()
        val initialStatus = if (isNearbyP2P) {
            MessageStatus.SENT
        } else if (isOnline) {
            MessageStatus.SENT
        } else {
            MessageStatus.QUEUED_OFFLINE
        }

        val msgId = UUID.randomUUID().toString()
        val message = ChatMessageEntity(
            id = msgId,
            conversationId = conversationId,
            senderId = "current_user",
            senderName = currentUserName,
            recipientId = conversationId,
            text = text,
            timestamp = System.currentTimeMillis(),
            deliveryStatus = initialStatus,
            isMine = true,
            isGroup = isGroup,
            isNearbyP2P = isNearbyP2P
        )

        chatDao.insertMessage(message)
        chatDao.updateConversationLastMessage(conversationId, text, System.currentTimeMillis())

        if (isNearbyP2P) {
            nearbyManager.sendP2PPayload(text, scope) {
                scope.launch(Dispatchers.IO) {
                    chatDao.updateMessageStatus(msgId, MessageStatus.DELIVERED)
                }
            }
        } else if (isOnline) {
            // Simulated delivery and read receipt transitions
            scope.launch(Dispatchers.IO) {
                delay(800)
                chatDao.updateMessageStatus(msgId, MessageStatus.DELIVERED)
                delay(1200)
                chatDao.updateMessageStatus(msgId, MessageStatus.READ)

                // Optional automated conversational response for 1-to-1 chats
                if (!isGroup) {
                    delay(1500)
                    triggerAutomatedReply(conversationId, text)
                }
            }
        }
    }

    private suspend fun triggerAutomatedReply(conversationId: String, userText: String) {
        val conv = chatDao.getConversationById(conversationId) ?: return
        if (conv.isBlocked) return

        val replies = listOf(
            "ধন্যবাদ মেসেজ দেওয়ার জন্য! ইনশাআল্লাহ দ্রুত যোগাযোগ করছি।",
            "মেসেজ পেয়েছি। সবকিছু ঠিকঠাক আছে তো?",
            "দারুণ! জিহাদ চ্যাট আসলেই অনেক সুন্দর আর দ্রুত।",
            "আলহামদুলিল্লাহ, আপনার কথা নোট করে রাখলাম।"
        )
        val replyText = replies.random()

        val replyMsg = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = conv.id,
            senderName = conv.title,
            recipientId = "current_user",
            text = replyText,
            timestamp = System.currentTimeMillis(),
            deliveryStatus = MessageStatus.READ,
            isMine = false
        )
        chatDao.insertMessage(replyMsg)
        chatDao.updateConversationLastMessage(conversationId, replyText, System.currentTimeMillis())
    }

    suspend fun flushOfflineQueue(scope: CoroutineScope) {
        val queued = chatDao.getQueuedOfflineMessages()
        if (queued.isEmpty()) return

        for (msg in queued) {
            chatDao.updateMessageStatus(msg.id, MessageStatus.SENDING)
            delay(300)
            chatDao.updateMessageStatus(msg.id, MessageStatus.SENT)
            delay(500)
            chatDao.updateMessageStatus(msg.id, MessageStatus.DELIVERED)
        }
    }

    suspend fun createOrGetDirectChat(contact: ContactEntity): String {
        val convId = "conv_${contact.id}"
        val existing = chatDao.getConversationById(convId)
        if (existing == null) {
            val newConv = ConversationEntity(
                id = convId,
                title = contact.name,
                avatarColor = contact.avatarColor,
                avatarInitials = contact.name.take(2),
                lastMessageText = "চ্যাট শুরু করুন",
                lastMessageTimestamp = System.currentTimeMillis(),
                unreadCount = 0,
                participantPhone = contact.phoneNumber
            )
            chatDao.insertConversation(newConv)
        }
        return convId
    }

    suspend fun createGroup(name: String, memberNames: List<String>): String {
        val convId = "group_${UUID.randomUUID().toString().take(8)}"
        val groupConv = ConversationEntity(
            id = convId,
            title = name,
            avatarColor = 0xFF087F5B,
            avatarInitials = name.take(2),
            lastMessageText = "গ্রুপ তৈরি হয়েছে: ${memberNames.joinToString(", ")}",
            lastMessageTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            isGroup = true
        )
        chatDao.insertConversation(groupConv)

        val systemMsg = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = convId,
            senderId = "system",
            senderName = "Jihad Chat",
            recipientId = convId,
            text = "গ্রুপ তৈরি হয়েছে: $name\nসদস্য: ${memberNames.joinToString(", ")}",
            timestamp = System.currentTimeMillis(),
            deliveryStatus = MessageStatus.READ,
            isMine = false,
            isGroup = true
        )
        chatDao.insertMessage(systemMsg)
        return convId
    }

    suspend fun setBlocked(contactId: String, blocked: Boolean) {
        chatDao.setContactBlocked(contactId, blocked)
        chatDao.setConversationBlocked("conv_$contactId", blocked)
    }

    suspend fun deleteMessage(id: String) {
        chatDao.deleteMessage(id)
    }

    suspend fun deleteConversation(convId: String) {
        chatDao.deleteMessagesForConversation(convId)
        chatDao.deleteConversation(convId)
    }

    suspend fun clearAllData() {
        chatDao.clearAllMessages()
        chatDao.clearAllConversations()
    }

    suspend fun addContact(name: String, phone: String, bio: String) {
        val newContact = ContactEntity(
            id = "c_${UUID.randomUUID().toString().take(6)}",
            name = name,
            phoneNumber = phone,
            bio = if (bio.isBlank()) "Available" else bio,
            avatarColor = listOf(0xFF087F5B, 0xFF0AA879, 0xFF20C997, 0xFF12B886).random()
        )
        chatDao.insertContact(newContact)
    }
}
