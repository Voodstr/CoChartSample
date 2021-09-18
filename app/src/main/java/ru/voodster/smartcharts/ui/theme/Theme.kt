package ru.voodster.smartcharts.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = dark_primaryLightColor,
    primaryVariant = dark_primaryColor,
    secondary = dark_secondaryLightColor,
    secondaryVariant = dark_secondaryColor,
    background = dark_primaryDarkColor,
    surface = dark_secondaryDarkColor,
    onPrimary = dark_primaryTextColor,
    onSecondary = dark_secondaryTextColor,
    onBackground = dark_primaryTextColor,
    onSurface = dark_secondaryTextColor
)

private val LightColorPalette = lightColors(
    primary = primaryLightColor,
    primaryVariant = primaryColor,
    secondary = secondaryLightColor,
    secondaryVariant = secondaryColor,
    background = primaryDarkColor,
    surface = secondaryDarkColor,
    onPrimary = primaryTextColor,
    onSecondary = secondaryTextColor,
    onBackground = primaryTextColor,
    onSurface = secondaryTextColor
)

@Composable
fun SmartChartsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}