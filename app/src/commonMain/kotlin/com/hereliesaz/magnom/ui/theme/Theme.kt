package com.hereliesaz.magnom.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.hereliesaz.magnom.resources.Res
import com.hereliesaz.magnom.resources.public_pixel
import org.jetbrains.compose.resources.Font

private val Amber = Color(0xFFF0A641)
private val AmberDeep = Color(0xFFB3560A)
private val Signal = Color(0xFF3FC3CF)

private val DarkColors = darkColorScheme(
    primary = Amber,
    onPrimary = Color(0xFF201400),
    secondary = Signal,
    background = Color(0xFF0E1218),
    onBackground = Color(0xFFE7EBF1),
    surface = Color(0xFF161C26),
    onSurface = Color(0xFFE7EBF1),
    surfaceVariant = Color(0xFF1C2431),
    onSurfaceVariant = Color(0xFF93A0B2),
    error = Color(0xFFFF7367),
    outline = Color(0xFF36434F),
)

private val LightColors = lightColorScheme(
    primary = AmberDeep,
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF0D7A8C),
    background = Color(0xFFE9EBEF),
    onBackground = Color(0xFF141A24),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF141A24),
    surfaceVariant = Color(0xFFF4F6F9),
    onSurfaceVariant = Color(0xFF5B6675),
    error = Color(0xFFB42318),
    outline = Color(0xFFB9C2CE),
)

@Composable
fun MagNomTheme(content: @Composable () -> Unit) {
    val pixel = FontFamily(Font(Res.font.public_pixel))
    val base = Typography()
    val typography = base.copy(
        headlineSmall = base.headlineSmall.copy(fontFamily = pixel),
        titleLarge = base.titleLarge.copy(fontFamily = pixel),
        titleMedium = base.titleMedium.copy(fontFamily = pixel),
    )
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = typography,
        content = content,
    )
}
