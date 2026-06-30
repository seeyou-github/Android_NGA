package com.justwen.androidnga.ui.compose.theme

import androidx.compose.ui.graphics.Color

data class ThemePreset(
    val name: String,
    val config: ThemeConfig,
)

object ThemePresets {

    val LIGHT_PRESETS = listOf(
        ThemePreset(
            name = "暖阳",
            config = ThemeConfig(
                textColor = Color(0xFF5C3D2E),
                topBarColor = Color(0xFFD4A574),
                bgColor = Color(0xFFFFF5E6),
                accentColor = Color(0xFFD4865E),
            )
        ),
        ThemePreset(
            name = "青草",
            config = ThemeConfig(
                textColor = Color(0xFF2D4A2D),
                topBarColor = Color(0xFF6B9B6B),
                bgColor = Color(0xFFF0F7E8),
                accentColor = Color(0xFF4A8C5C),
            )
        ),
        ThemePreset(
            name = "天空",
            config = ThemeConfig(
                textColor = Color(0xFF2A3F5C),
                topBarColor = Color(0xFF6B8FBA),
                bgColor = Color(0xFFEBF3FA),
                accentColor = Color(0xFF5A7FAA),
            )
        ),
        ThemePreset(
            name = "樱花",
            config = ThemeConfig(
                textColor = Color(0xFF5C3A3A),
                topBarColor = Color(0xFFD4878A),
                bgColor = Color(0xFFFFF0F0),
                accentColor = Color(0xFFC46A70),
            )
        ),
        ThemePreset(
            name = "石墨",
            config = ThemeConfig(
                textColor = Color(0xFF333333),
                topBarColor = Color(0xFF707070),
                bgColor = Color(0xFFF2F2F2),
                accentColor = Color(0xFF555555),
            )
        ),
        ThemePreset(
            name = "丁香",
            config = ThemeConfig(
                textColor = Color(0xFF3A3350),
                topBarColor = Color(0xFF8B7BA8),
                bgColor = Color(0xFFF5F0F8),
                accentColor = Color(0xFF7A6A96),
            )
        ),
    )

    val DARK_PRESETS = listOf(
        ThemePreset(
            name = "暗夜",
            config = ThemeConfig(
                textColor = Color(0xFFD0D0D0),
                topBarColor = Color(0xFF2D3138),
                bgColor = Color(0xFF1A1D23),
                accentColor = Color(0xFF8A9BB5),
            )
        ),
        ThemePreset(
            name = "深林",
            config = ThemeConfig(
                textColor = Color(0xFFC8D0C0),
                topBarColor = Color(0xFF2A3A2A),
                bgColor = Color(0xFF1A231A),
                accentColor = Color(0xFF6B9B6B),
            )
        ),
        ThemePreset(
            name = "深海",
            config = ThemeConfig(
                textColor = Color(0xFFC0C8D5),
                topBarColor = Color(0xFF2A3040),
                bgColor = Color(0xFF1A1E28),
                accentColor = Color(0xFF6B8FBA),
            )
        ),
        ThemePreset(
            name = "酒红",
            config = ThemeConfig(
                textColor = Color(0xFFD0C0C0),
                topBarColor = Color(0xFF3A2828),
                bgColor = Color(0xFF231A1A),
                accentColor = Color(0xFFC47070),
            )
        ),
        ThemePreset(
            name = "石墨",
            config = ThemeConfig(
                textColor = Color(0xFFCCCCCC),
                topBarColor = Color(0xFF2E2E2E),
                bgColor = Color(0xFF1E1E1E),
                accentColor = Color(0xFF888888),
            )
        ),
        ThemePreset(
            name = "紫晶",
            config = ThemeConfig(
                textColor = Color(0xFFC8C0D5),
                topBarColor = Color(0xFF302A40),
                bgColor = Color(0xFF1F1A28),
                accentColor = Color(0xFF8B7BA8),
            )
        ),
    )
}
