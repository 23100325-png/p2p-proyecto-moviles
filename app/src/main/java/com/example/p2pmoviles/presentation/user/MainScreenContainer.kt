package com.example.p2pmoviles.presentation.user

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.p2pmoviles.ui.theme.*

@Composable
fun MainScreenContainer(usuarioLogueadoId: String) {
    // Estado para saber cuál pestaña está activa (0 = Billetera, 1 = P2P)
    var pestañaActiva by remember { mutableIntStateOf(0) }
    var subPantallaP2P by remember { mutableStateOf("buscar") } // "buscar" o "publicar"

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = BinanceInputBackground, // Mantiene tu fondo oscuro
                tonalElevation = 8.dp
            ) {
                // Pestaña 1: Billetera
                NavigationBarItem(
                    selected = pestañaActiva == 0,
                    onClick = { pestañaActiva = 0 },
                    label = { Text("Billetera") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Billetera"
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BinanceBackground,
                        selectedTextColor = BinanceYellow,
                        indicatorColor = BinanceYellow, // El óvalo brillante al seleccionar
                        unselectedIconColor = BinanceTextSecondary,
                        unselectedTextColor = BinanceTextSecondary
                    )
                )

                // Pestaña 2: P2P
                NavigationBarItem(
                    selected = pestañaActiva == 1,
                    onClick = { pestañaActiva = 1 },
                    label = { Text("P2P") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "P2P"
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BinanceBackground,
                        selectedTextColor = BinanceYellow,
                        indicatorColor = BinanceYellow,
                        unselectedIconColor = BinanceTextSecondary,
                        unselectedTextColor = BinanceTextSecondary
                    )
                )
            }
        },
        containerColor = BinanceBackground
    ) { paddingValues ->
        // Dibujamos dinámicamente la pantalla en el centro según la pestaña activa
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = BinanceBackground
        ) {
            when (pestañaActiva) {
                // 🟢 REEMPLAZA AQUÍ por el nombre exacto de tu pantalla de billetera existente
                0 -> UserWalletScreen(userId = usuarioLogueadoId)

                // Nuestra nueva pantalla P2P
                1 -> {
                    if (subPantallaP2P == "buscar") {
                        MercadoP2PScreen(
                            usuarioLogueadoId = usuarioLogueadoId,
                            onNavegarAPublicarClick = { subPantallaP2P = "publicar" }
                        )
                    } else {
                        // Modificamos levemente la llamada de tu pantalla anterior para que pueda regresar
                        PublicarOfertaScreen(usuarioLogueadoId = usuarioLogueadoId)
                    }
                }
            }
        }
    }
}