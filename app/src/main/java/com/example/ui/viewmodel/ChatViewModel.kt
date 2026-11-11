package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ContactEntity
import com.example.data.local.ConversationEntity
import com.example.data.nearby.NearbyConnectionState
import com.example.data.nearby.NearbyDevice
import com.example.data.nearby.NearbyP2PManager
import com.example.data.network.NetworkMonitor
import com.example.data.preferences.UserPreferences
import com.example.data.repository.ChatRepository
import com.example.ui.navigation.AppScreen
import com.example.util.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    val preferences = UserPreferences(application)
    val networkMonitor = NetworkMonitor(application)
    val nearbyManager = NearbyP2PManager(application)
    private val database = ChatDatabase.getInstance(application)
    val repository = ChatRepository(database.chatDao(), networkMonitor, nearbyManager)

    // Navigation BackStack
    private val backStack = mutableListOf<AppScreen>()
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Splash)
    val currentScreen = _currentScreen.asStateFlow()

    // Preferences & Settings
    val language: StateFlow<AppLanguage> = preferences.language
    val isDarkTheme: StateFlow<Boolean> = preferences.isDarkTheme
    val isLoggedIn: StateFlow<Boolean> = preferences.isLoggedIn
    val userDisplayName: StateFlow<String> = preferences.displayName
    val userPhone: StateFlow<String> = preferences.phoneNumber
    val userBio: StateFlow<String> = preferences.bio

    // Data streams
    val conversations: StateFlow<List<ConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contacts: StateFlow<List<ContactEntity>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedContacts: StateFlow<List<ContactEntity>> = repository.blockedContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val queuedCount: StateFlow<Int> = repository.queuedCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnlineFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isSimulatedOffline: StateFlow<Boolean> = networkMonitor.isSimulatedOffline

    val nearbyDevices: StateFlow<List<NearbyDevice>> = nearbyManager.discoveredDevices
    val nearbyState: StateFlow<NearbyConnectionState> = nearbyManager.connectionState

    // Notification toast / alert message for in-app feedback
    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage = _userFeedbackMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
        }

        // Auto-flush queued offline messages when coming back online
        viewModelScope.launch {
            networkMonitor.isOnlineFlow.collect { online ->
                if (online) {
                    repository.flushOfflineQueue(viewModelScope)
                }
            }
        }

        // Listen for incoming peer P2P messages
        viewModelScope.launch {
            nearbyManager.incomingP2PMessages.collect { pair ->
                pair?.let { (peerId, text) ->
                    val peerDevice = nearbyDevices.value.find { it.id == peerId }
                    val peerName = peerDevice?.name ?: "Nearby P2P Peer"
                    repository.sendMessage(
                        conversationId = "nearby_p2p_session",
                        text = text,
                        currentUserName = peerName,
                        isGroup = false,
                        isNearbyP2P = true,
                        scope = viewModelScope
                    )
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            backStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun popBackStack(): Boolean {
        return if (backStack.isNotEmpty()) {
            val prev = backStack.removeAt(backStack.size - 1)
            _currentScreen.value = prev
            true
        } else {
            false
        }
    }

    fun getMessagesForConversation(convId: String): Flow<List<ChatMessageEntity>> {
        return repository.getMessagesForConversation(convId)
    }

    fun sendMessage(
        conversationId: String,
        text: String,
        isGroup: Boolean = false,
        isNearbyP2P: Boolean = false
    ) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = conversationId,
                text = text.trim(),
                currentUserName = userDisplayName.value,
                isGroup = isGroup,
                isNearbyP2P = isNearbyP2P,
                scope = viewModelScope
            )
        }
    }

    fun toggleSimulateOffline() {
        networkMonitor.toggleSimulateOffline()
        if (networkMonitor.isCurrentlyConnected()) {
            viewModelScope.launch {
                repository.flushOfflineQueue(viewModelScope)
            }
            showFeedback("ইন্টারনেট সচল হয়েছে - কিউভুক্ত মেসেজ পাঠানো হচ্ছে")
        } else {
            showFeedback("অফলাইন মোড সক্রিয় - মেসেজ লোকাল কিউতে থাকবে")
        }
    }

    fun flushOfflineQueueNow() {
        viewModelScope.launch {
            repository.flushOfflineQueue(viewModelScope)
            showFeedback("সব অফলাইন মেসেজ সিঙ্ক করা হয়েছে")
        }
    }

    fun showFeedback(msg: String) {
        _userFeedbackMessage.value = msg
    }

    fun clearFeedback() {
        _userFeedbackMessage.value = null
    }

    fun startNearbyDiscovery() {
        nearbyManager.startDiscovery(viewModelScope)
    }

    fun stopNearbyDiscovery() {
        nearbyManager.stopDiscovery()
    }

    fun connectNearbyDevice(device: NearbyDevice) {
        nearbyManager.connectToDevice(device, viewModelScope)
        showFeedback("${device.name} এর সাথে P2P কানেক্টেড!")
    }

    fun disconnectNearby() {
        nearbyManager.disconnect()
        showFeedback("P2P ডিসকানেক্ট করা হয়েছে")
    }

    fun createGroup(name: String, memberNames: List<String>) {
        viewModelScope.launch {
            val convId = repository.createGroup(name, memberNames)
            navigateTo(AppScreen.GroupChat(convId, name))
        }
    }

    fun startChatWithContact(contact: ContactEntity) {
        viewModelScope.launch {
            val convId = repository.createOrGetDirectChat(contact)
            navigateTo(AppScreen.ChatDetail(convId, contact.name, isGroup = false))
        }
    }

    fun blockUser(contactId: String) {
        viewModelScope.launch {
            repository.setBlocked(contactId, true)
            showFeedback("ব্যবহারকারীকে ব্লক করা হয়েছে")
        }
    }

    fun unblockUser(contactId: String) {
        viewModelScope.launch {
            repository.setBlocked(contactId, false)
            showFeedback("ব্যবহারকারীকে আনব্লক করা হয়েছে")
        }
    }

    fun deleteMessage(id: String) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }

    fun deleteConversation(convId: String) {
        viewModelScope.launch {
            repository.deleteConversation(convId)
            popBackStack()
        }
    }

    fun addContact(name: String, phone: String, bio: String) {
        viewModelScope.launch {
            repository.addContact(name, phone, bio)
            showFeedback("নতুন কন্টাক্ট যুক্ত হয়েছে")
        }
    }

    fun setLanguage(lang: AppLanguage) {
        preferences.setLanguage(lang)
    }

    fun setDarkTheme(dark: Boolean) {
        preferences.setDarkTheme(dark)
    }

    fun setReadReceipts(enabled: Boolean) {
        preferences.setReadReceipts(enabled)
    }

    fun setLastSeen(enabled: Boolean) {
        preferences.setLastSeen(enabled)
    }

    fun setSecurityLock(enabled: Boolean) {
        preferences.setSecurityLock(enabled)
    }

    fun updateProfile(name: String, bio: String) {
        preferences.setProfile(name, userPhone.value, bio)
        showFeedback("প্রোফাইল আপডেট হয়েছে")
    }

    fun completeRegistration(name: String, bio: String, phone: String) {
        preferences.setProfile(name, phone, bio)
        preferences.setLoggedIn(true)
        navigateTo(AppScreen.Home)
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            showFeedback("সব লোকাল ডেটা মুছে ফেলা হয়েছে")
        }
    }
}
