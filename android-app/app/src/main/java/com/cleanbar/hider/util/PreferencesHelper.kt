package com.cleanbar.hider.util

import android.content.Context
import android.content.SharedPreferences

object PreferencesHelper {

    private const val PREFS_NAME = "cleanbar_prefs"
    private const val KEY_PERSISTENT_MODE = "key_persistent_mode"
    private const val KEY_TARGET_HIDDEN = "key_target_hidden"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isPersistentModeEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_PERSISTENT_MODE, false)
    }

    fun setPersistentModeEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_PERSISTENT_MODE, enabled).apply()
    }

    fun isTargetHidden(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_TARGET_HIDDEN, false)
    }

    fun setTargetHidden(context: Context, isHidden: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_TARGET_HIDDEN, isHidden).apply()
    }
}
