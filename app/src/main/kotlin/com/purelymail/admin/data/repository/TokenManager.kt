package com.purelymail.admin.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "purelymail_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback for emulators/environments with Keystore anomalies
        context.getSharedPreferences("purelymail_fallback_prefs", Context.MODE_PRIVATE)
    }

    private val _apiToken = MutableStateFlow(prefs.getString(KEY_API_TOKEN, "") ?: "")
    val apiToken: StateFlow<String> = _apiToken.asStateFlow()

    private val _serverUrl = MutableStateFlow(prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL)
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _isDemoMode = MutableStateFlow(prefs.getBoolean(KEY_DEMO_MODE, false))
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_API_TOKEN, token.trim()).apply()
        _apiToken.value = token.trim()
    }

    fun saveServerUrl(url: String) {
        val cleanUrl = if (url.endsWith("/")) url.dropLast(1) else url
        prefs.edit().putString(KEY_SERVER_URL, cleanUrl).apply()
        _serverUrl.value = cleanUrl
    }

    fun setDemoMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEMO_MODE, enabled).apply()
        _isDemoMode.value = enabled
    }

    fun clearAll() {
        prefs.edit().clear().apply()
        _apiToken.value = ""
        _serverUrl.value = DEFAULT_SERVER_URL
        _isDemoMode.value = false
    }

    val hasValidToken: Boolean
        get() = _apiToken.value.isNotBlank() || _isDemoMode.value

    companion object {
        const val DEFAULT_SERVER_URL = "https://purelymail.com"
        private const val KEY_API_TOKEN = "purelymail_api_token"
        private const val KEY_SERVER_URL = "purelymail_server_url"
        private const val KEY_DEMO_MODE = "purelymail_demo_mode"
    }
}
