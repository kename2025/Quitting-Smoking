package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = OxygenGreen,
    secondary = HealingCyan,
    tertiary = WarningAmber,
    background = CarbonDarkBg,
    surface = CardSlate,
    onBackground = TextWhite,
    onSurface = TextWhite,
    primaryContainer = OxygenDarkGreen,
    onPrimaryContainer = TextWhite,
    error = DopamineCoral
)

private val LightColorScheme = lightColorScheme(
    primary = OxygenDarkGreen,
    secondary = HealingCyan,
    tertiary = WarningAmber,
    background = FreshLightBg,
    surface = CardWhite,
    onBackground = TextDarkBg,
    onSurface = TextDarkBg,
    primaryContainer = OxygenGreen,
    onPrimaryContainer = CarbonDarkBg,
    error = DopamineCoral
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set default dynamicColor to false to maintain our consistent, high-fidelity Oxygen/Carbon brand!
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
