package com.justwen.androidnga.ui.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alibaba.android.arouter.launcher.ARouter
import com.justwen.androidnga.base.service.api.IThemeManagerService

private fun buildColors(cfg: ThemeConfig): Colors {
    val onPrimary = cfg.topBarColor.readableOn(cfg.topBarColor)
    val onBackground = cfg.textColor
    val onSurface = cfg.textColor
    val surface = cfg.bgColor.let {
        val f = 0.03f
        Color(
            (it.red * (1 - f) + f).coerceAtMost(1f),
            (it.green * (1 - f) + f).coerceAtMost(1f),
            (it.blue * (1 - f) + f).coerceAtMost(1f),
            1f
        )
    }
    val isDark = cfg.bgColor.luminance() < 0.5f

    val base = if (isDark) darkColors() else lightColors()
    return base.copy(
        primary = cfg.topBarColor,
        primaryVariant = cfg.accentColor,
        secondary = cfg.accentColor,
        background = cfg.bgColor,
        surface = surface,
        onPrimary = onPrimary,
        onSecondary = cfg.textColor,
        onBackground = onBackground,
        onSurface = onSurface,
    )
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    customConfig: ThemeConfig? = null,
    content: @Composable () -> Unit
) {
    val themeManager = ARouter.getInstance().build(IThemeManagerService.ROUTER_PATH).navigation() as? IThemeManagerService
    val isNight = darkTheme || (themeManager?.isNightMode() == true)
    val cfg = customConfig ?: ThemeConfig.load(isNight)
    val colors = buildColors(cfg)

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
