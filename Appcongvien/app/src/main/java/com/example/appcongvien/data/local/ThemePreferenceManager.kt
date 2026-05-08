package com.example.appcongvien.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.appcongvien.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferenceManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(readThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        if (_themeMode.value == mode) return
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    private fun readThemeMode(): ThemeMode {
        val rawMode = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return ThemeMode.entries.firstOrNull { it.name == rawMode } ?: ThemeMode.SYSTEM
    }

    companion object {
        private const val PREFS_NAME = "park_adventure_ui_prefs"
        private const val KEY_THEME_MODE = "theme_mode"

        @Volatile
        private var INSTANCE: ThemePreferenceManager? = null

        fun getInstance(context: Context): ThemePreferenceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemePreferenceManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
