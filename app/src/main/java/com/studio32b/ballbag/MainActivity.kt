package com.studio32b.ballbag

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.toArgb
import com.studio32b.ballbag.ui.navigation.AppNavHost
import com.studio32b.ballbag.ui.theme.ballbagTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            var darkTheme by rememberSaveable { mutableStateOf(systemDark) }
            var visible by remember { mutableStateOf(false) }
            val alpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, label = "fadeIn")
            LaunchedEffect(Unit) { visible = true }
            ballbagTheme(darkTheme = darkTheme) {
                val color = MaterialTheme.colorScheme.surfaceVariant
                SideEffect {
                    @Suppress("DEPRECATION")
                    window.statusBarColor = color.toArgb()
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.isAppearanceLightStatusBars = !darkTheme
                }
                Box(Modifier.alpha(alpha)) {
                    AppNavHost(
                        isDarkTheme = darkTheme,
                        onToggleTheme = { darkTheme = !darkTheme },
                        onExitApp = { finish() }
                    )
                }
            }
        }
    }
}
