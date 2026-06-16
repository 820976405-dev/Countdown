package com.expiryreminder.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// 扩展语义颜色
data class ExtendedColors(
    val cardBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val divider: Color,
    val progressBackground: Color,
    val warningOrange: Color,
    val dangerRed: Color,
    val successGreen: Color,
    val infoBlue: Color,
    val warningOrangeBg: Color,
    val dangerRedBg: Color,
    val successGreenBg: Color,
    val infoBlueBg: Color,
    val inputBackground: Color,
    val inputBorder: Color,
    val placeholderText: Color,
    val chipBackground: Color,
    val sidebarBackground: Color,
    val selectedChipBackground: Color,
    val switchUncheckedTrack: Color,
    val checkboxUnchecked: Color,
    val chevronTint: Color,
    val iconBackground: Color,
)

private val LightExtendedColors = ExtendedColors(
    cardBackground = Color.White,
    textPrimary = Color(0xFF1F2329),
    textSecondary = Color(0xFF4E5969),
    textTertiary = Color(0xFF86909C),
    divider = Color(0xFFF2F3F5),
    progressBackground = Color(0xFFF2F3F5),
    warningOrange = Color(0xFFFF9500),
    dangerRed = Color(0xFFFF4D4F),
    successGreen = Color(0xFF00B42A),
    infoBlue = Color(0xFF1677FF),
    warningOrangeBg = Color(0xFFFFF7E6),
    dangerRedBg = Color(0xFFFFF1F0),
    successGreenBg = Color(0xFFF0FFF4),
    infoBlueBg = Color(0xFFF0F7FF),
    inputBackground = Color(0xFFF5F5F5),
    inputBorder = Color(0xFFE8E8E8),
    placeholderText = Color(0xFFBDBDBD),
    chipBackground = Color(0xFFF8F9FA),
    sidebarBackground = Color(0xFFF8F9FA),
    selectedChipBackground = Color(0xFFE8F5E9),
    switchUncheckedTrack = Color(0xFFD9D9D9),
    checkboxUnchecked = Color(0xFFD9D9D9),
    chevronTint = Color(0xFFC7C7CC),
    iconBackground = Color(0xFFF8F9FA),
)

private val DarkExtendedColors = ExtendedColors(
    cardBackground = Color(0xFF2A2A2A),
    textPrimary = Color(0xFFE8E8E8),
    textSecondary = Color(0xFFB0B0B0),
    textTertiary = Color(0xFF808080),
    divider = Color(0xFF3A3A3A),
    progressBackground = Color(0xFF3A3A3A),
    warningOrange = Color(0xFFFF9500),
    dangerRed = Color(0xFFFF6B6B),
    successGreen = Color(0xFF5FCF80),
    infoBlue = Color(0xFF4D9FFF),
    warningOrangeBg = Color(0xFF3D2E00),
    dangerRedBg = Color(0xFF3D1A1A),
    successGreenBg = Color(0xFF1A3D1A),
    infoBlueBg = Color(0xFF1A2A3D),
    inputBackground = Color(0xFF2A2A2A),
    inputBorder = Color(0xFF3A3A3A),
    placeholderText = Color(0xFF666666),
    chipBackground = Color(0xFF2A2A2A),
    sidebarBackground = Color(0xFF1E1E1E),
    selectedChipBackground = Color(0xFF1A3D1A),
    switchUncheckedTrack = Color(0xFF444444),
    checkboxUnchecked = Color(0xFF555555),
    chevronTint = Color(0xFF666666),
    iconBackground = Color(0xFF333333),
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5FCF80),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF81C784),
    onSecondary = Color.White,
    background = Color(0xFFF7F8FA),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F2329),
    error = Color(0xFFFF4D4F),
    onError = Color.White,
    surfaceVariant = Color(0xFFF2F3F5),
    onSurfaceVariant = Color(0xFF4E5969),
    outline = Color(0xFFC9CDD4),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5FCF80),
    onPrimary = Color(0xFF1B5E20),
    primaryContainer = Color(0xFF2E7D32),
    onPrimaryContainer = Color(0xFFE8F5E9),
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF1B5E20),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    error = Color(0xFFFF6B6B),
    onError = Color.White,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF555555),
)

private object NoRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = Color.Unspecified

    @Composable
    override fun rippleAlpha(): RippleAlpha = RippleAlpha(0.0f, 0.0f, 0.0f, 0.0f)
}

@Composable
fun ExpiryReminderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(
        LocalRippleTheme provides NoRippleTheme,
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// 便捷访问扩展颜色
object AppColors {
    val extended: ExtendedColors
        @Composable get() = LocalExtendedColors.current
}
