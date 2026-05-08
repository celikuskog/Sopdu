package com.celik.sopdu

import android.content.SharedPreferences
import java.util.UUID

internal data class LocalIdentity(val deviceId: String, val displayName: String)

internal fun loadLocalIdentity(prefs: SharedPreferences): LocalIdentity {
    val deviceId = prefs.getString("device_id", null)
        ?: "SOPDU-${UUID.randomUUID().toString().take(8).uppercase()}".also {
            prefs.edit().putString("device_id", it).apply()
        }
    val displayName = prefs.getString("display_name", null)
        ?: deviceId.takeLast(8).also { prefs.edit().putString("display_name", it).apply() }
    return LocalIdentity(deviceId, displayName)
}
