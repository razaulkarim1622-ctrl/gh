package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.util.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("jihad_chat_prefs", Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false))
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _phoneNumber = MutableStateFlow(prefs.getString("phone_number", "+8801711111111") ?: "+8801711111111")
    val phoneNumber = _phoneNumber.asStateFlow()

    private val _displayName = MutableStateFlow(prefs.getString("display_name", "Jihad User") ?: "Jihad User")
    val displayName = _displayName.asStateFlow()

    private val _bio = MutableStateFlow(prefs.getString("bio", "আসসালামু আলাইকুম! জিহাদ চ্যাটে স্বাগতম।") ?: "আসসালামু আলাইকুম! জিহাদ চ্যাটে স্বাগতম।")
    val bio = _bio.asStateFlow()

    private val _language = MutableStateFlow(
        if (prefs.getString("lang", "bn") == "en") AppLanguage.ENGLISH else AppLanguage.BENGALI
    )
    val language = _language.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", false))
    val isDarkTheme = _isDarkTheme.asStateFlow()

    private val _readReceipts = MutableStateFlow(prefs.getBoolean("read_receipts", true))
    val readReceipts = _readReceipts.asStateFlow()

    private val _lastSeen = MutableStateFlow(prefs.getBoolean("last_seen", true))
    val lastSeen = _lastSeen.asStateFlow()

    private val _securityLock = MutableStateFlow(prefs.getBoolean("security_lock", false))
    val securityLock = _securityLock.asStateFlow()

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean("is_logged_in", loggedIn).apply()
        _isLoggedIn.value = loggedIn
    }

    fun setProfile(name: String, phone: String, userBio: String) {
        prefs.edit()
            .putString("display_name", name)
            .putString("phone_number", phone)
            .putString("bio", userBio)
            .putBoolean("is_logged_in", true)
            .apply()
        _displayName.value = name
        _phoneNumber.value = phone
        _bio.value = userBio
        _isLoggedIn.value = true
    }

    fun setLanguage(lang: AppLanguage) {
        prefs.edit().putString("lang", lang.code).apply()
        _language.value = lang
    }

    fun setDarkTheme(dark: Boolean) {
        prefs.edit().putBoolean("is_dark_theme", dark).apply()
        _isDarkTheme.value = dark
    }

    fun setReadReceipts(enabled: Boolean) {
        prefs.edit().putBoolean("read_receipts", enabled).apply()
        _readReceipts.value = enabled
    }

    fun setLastSeen(enabled: Boolean) {
        prefs.edit().putBoolean("last_seen", enabled).apply()
        _lastSeen.value = enabled
    }

    fun setSecurityLock(enabled: Boolean) {
        prefs.edit().putBoolean("security_lock", enabled).apply()
        _securityLock.value = enabled
    }
}
