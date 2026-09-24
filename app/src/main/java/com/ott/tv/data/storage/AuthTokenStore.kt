package com.ott.tv.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.ott.tv.data.model.ActivationStatusResponse

class AuthTokenStore(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) = prefs.edit { putString(KEY_ACCESS_TOKEN, value) }

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit { putString(KEY_REFRESH_TOKEN, value) }

    var isActivated: Boolean
        get() = prefs.getBoolean(KEY_IS_ACTIVATED, false)
        set(value) = prefs.edit { putBoolean(KEY_IS_ACTIVATED, value) }

    fun saveTokens(response: ActivationStatusResponse) {
        accessToken = response.accessToken
        refreshToken = response.refreshToken
        isActivated = true
    }

    fun clear() {
        prefs.edit {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_IS_ACTIVATED)
        }
    }

    companion object {
        private const val PREFS_NAME = "ott_auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_IS_ACTIVATED = "is_activated"
    }
}
