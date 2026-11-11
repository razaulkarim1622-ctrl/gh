package com.example.ui.navigation

sealed class AppScreen {
    object Splash : AppScreen()
    object Welcome : AppScreen()
    object Login : AppScreen()
    data class Otp(val phoneNumber: String) : AppScreen()
    data class Register(val phoneNumber: String) : AppScreen()
    object Home : AppScreen()
    data class ChatDetail(
        val conversationId: String,
        val title: String,
        val isGroup: Boolean = false,
        val isNearby: Boolean = false
    ) : AppScreen()
    object Contacts : AppScreen()
    object NewChat : AppScreen()
    data class GroupChat(val conversationId: String, val title: String) : AppScreen()
    object Profile : AppScreen()
    object Settings : AppScreen()
    object Privacy : AppScreen()
    object NearbyChat : AppScreen()
}
