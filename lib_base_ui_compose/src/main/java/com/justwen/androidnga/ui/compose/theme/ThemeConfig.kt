package com.justwen.androidnga.ui.compose.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

data class ThemeConfig(
    val textColor: Color = Color(0xFF1C1B1F),
    val topBarColor: Color = Color(0xFF591804),
    val bgColor: Color = Color(0xFFFFF8E7),
    val accentColor: Color = Color(0xFF591804),
) {
    companion object {
        private const val KEY_TEXT_COLOR = "theme_text_color"
        private const val KEY_TOP_BAR_COLOR = "theme_top_bar_color"
        private const val KEY_BG_COLOR = "theme_bg_color"
        private const val KEY_ACCENT_COLOR = "theme_accent_color"
        private val DEFAULT_LIGHT = ThemePresets.LIGHT_PRESETS[0].config
        private val DEFAULT_DARK = ThemePresets.DARK_PRESETS[0].config

        fun load(isNight: Boolean): ThemeConfig {
            val default = if (isNight) DEFAULT_DARK else DEFAULT_LIGHT
            val prefs = gov.anzong.androidnga.base.util.PreferenceUtils.getDefaultPreferences()
            val text = prefs.getString(KEY_TEXT_COLOR, null)?.let { parseColorOrNull(it) } ?: default.textColor
            val topBar = prefs.getString(KEY_TOP_BAR_COLOR, null)?.let { parseColorOrNull(it) } ?: default.topBarColor
            val bg = prefs.getString(KEY_BG_COLOR, null)?.let { parseColorOrNull(it) } ?: default.bgColor
            val accent = prefs.getString(KEY_ACCENT_COLOR, null)?.let { parseColorOrNull(it) } ?: default.accentColor
            return ThemeConfig(textColor = text, topBarColor = topBar, bgColor = bg, accentColor = accent)
        }

        private fun parseColorOrNull(hex: String): Color? {
            return try {
                Color(android.graphics.Color.parseColor(hex))
            } catch (_: Exception) { null }
        }

        fun save(config: ThemeConfig) {
            val edit = gov.anzong.androidnga.base.util.PreferenceUtils.edit()
            edit.putString(KEY_TEXT_COLOR, colorToHex(config.textColor))
            edit.putString(KEY_TOP_BAR_COLOR, colorToHex(config.topBarColor))
            edit.putString(KEY_BG_COLOR, colorToHex(config.bgColor))
            edit.putString(KEY_ACCENT_COLOR, colorToHex(config.accentColor))
            edit.commit()
        }

        private fun colorToHex(color: Color): String {
            val argb = color.toArgb()
            return String.format("#%08X", argb)
        }

        fun resetToPreset(preset: ThemePreset) {
            save(preset.config)
        }
    }
}
