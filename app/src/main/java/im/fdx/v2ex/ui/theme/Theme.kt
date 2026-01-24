package im.fdx.v2ex.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.runtime.produceState
import im.fdx.v2ex.pref
import im.fdx.v2ex.utils.Keys
import android.content.SharedPreferences

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Accent,
    background = NightBackground,
    surface = NightSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Primary
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Accent,
    background = Background,
    surface = Surface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color(0xDE000000), // 87% Black to match traditional view
    onSurfaceVariant = Primary
)

@Composable
fun V2ExTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    var textSizeMode by remember { 
        mutableIntStateOf(pref.getString(Keys.PREF_TEXT_SIZE, "0")?.toInt() ?: 0) 
    }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == Keys.PREF_TEXT_SIZE) {
                textSizeMode = p.getString(key, "0")?.toInt() ?: 0
            }
        }
        pref.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            pref.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context).copy(
                background = Color.White,
                surface = Color.White,
                onBackground = Color.Black,
                onSurface = Color.Black
            )
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val fontScale = when (textSizeMode) {
        1 -> 0.875f
        2 -> 1.0f
        3 -> 1.125f
        4 -> 1.25f
        else -> 1.0f
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypography(fontScale),
        content = content
    )
}

