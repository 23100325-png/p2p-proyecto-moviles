package com.example.p2pmoviles.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BinanceColorScheme = darkColorScheme(
    primary = BinanceYellow,
    background = BinanceBackground,
    surface = BinanceInputBackground,
    onPrimary = BinanceBackground,
    onBackground = BinanceTextPrimary,
    onSurface = BinanceTextPrimary,
    error = BinanceError
)

@Composable
fun P2PMovilesTheme(
    content: @Composable () -> Unit
) {
    // Eliminamos la lógica de colores dinámicos y claros para que la app
    // mantenga siempre la identidad oscura de Binance tal como en tu imagen
    MaterialTheme(
        colorScheme = BinanceColorScheme,
        typography = Typography,
        content = content
    )
}