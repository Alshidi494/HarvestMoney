package com.harvestmoney.bounty.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryColor,
    secondary = DarkSecondaryColor,
    background = DarkBackgroundColor,
    surface = DarkSurfaceColor,
    onPrimary = DarkOnPrimaryColor,
    onSecondary = DarkOnSecondaryColor,
    onBackground = DarkOnBackgroundColor,
    onSurface = DarkOnSurfaceColor,
    error = ErrorColor
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    background = BackgroundColor,
    surface = SurfaceColor,
    onPrimary = OnPrimaryColor,
    onSecondary = OnSecondaryColor,
    onBackground = OnBackgroundColor,
    onSurface = OnSurfaceColor,
    error = ErrorColor
)

@Composable
fun HarvestMoneyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
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
