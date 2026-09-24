package com.ott.tv.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.util.UUID

class DeviceIdProvider(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val deviceId: String
        get() {
            var id = prefs.getString(KEY_DEVICE_ID, null)
            if (id == null) {
                id = UUID.randomUUID().toString()
                prefs.edit { putString(KEY_DEVICE_ID, id) }
            }
            return id
        }

    companion object {
        private const val PREFS_NAME = "ott_device_prefs"
        private const val KEY_DEVICE_ID = "device_id"
    }
}
