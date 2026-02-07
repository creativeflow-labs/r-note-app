package com.rnote.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SagePrimary,
    onPrimary = SurfaceWhite,
    primaryContainer = SagePrimaryLight,
    onPrimaryContainer = TextPrimary,
    secondary = AccentWarm,
    onSecondary = TextPrimary,
    secondaryContainer = AccentWarm.copy(alpha = 0.2f),
    onSecondaryContainer = TextPrimary,
    background = CloudDancer,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    outlineVariant = DividerColor.copy(alpha = 0.5f),
    error = AccentCoral,
    onError = SurfaceWhite
)

@Composable
fun RNoteTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CloudDancer.toArgb()
            window.navigationBarColor = CloudDancer.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = RNoteTypography,
        content = content
    )
}
