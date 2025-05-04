package com.example.appestudos.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

class ThemeManager {
    var isDarkMode by mutableStateOf(false)
        private set

    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
    }
}

val LocalThemeManager = staticCompositionLocalOf { ThemeManager() }

@Composable
fun AppTheme(
    darkTheme: Boolean = LocalThemeManager.current.isDarkMode,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColors()
    } else {
        lightColors()
    }

    androidx.compose.material.MaterialTheme(
        colors = colors,
        content = content
    )
}

private fun darkColors() = androidx.compose.material.darkColors(
    primary = Color(0xFF01380b),
    primaryVariant = Color(0xFF01380b),
    secondary = Color(0xFF01380b),
    secondaryVariant = Color(0xFF01380b),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    error = Color(0xFFCF6679),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black
)

private fun lightColors() = androidx.compose.material.lightColors(
    primary = Color(0xFF339158),
    primaryVariant = Color(0xFF339158),
    secondary = Color(0xFF339158),
    secondaryVariant = Color(0xFF339158),
    background = Color.White,
    surface = Color.White,
    error = Color(0xFFB00020),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White
) 