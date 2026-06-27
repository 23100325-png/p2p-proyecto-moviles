package com.example.p2pmoviles.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.p2pmoviles.presentation.Menu.BottomBarItem
import com.example.p2pmoviles.presentation.auth.AuthViewModel
import com.example.p2pmoviles.presentation.user.historial.TransactionsScreen
import com.example.p2pmoviles.presentation.user.mercadoP2P.MercadoP2PScreen
import com.example.p2pmoviles.presentation.user.billetera.UserWalletScreen
import com.example.p2pmoviles.presentation.user.profile.ProfileScreen
import com.example.p2pmoviles.ui.theme.*

@Composable
fun MainContainerScreen(
    authViewModel: AuthViewModel,
    onLogoutSuccess: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val userId = authViewModel.usuarioActualId

    // Usamos rememberSaveable con el título (String) para que el estado persista
    // incluso cuando navegamos a otras pantallas y regresamos.
    var selectedTabTitle by rememberSaveable { mutableStateOf(BottomBarItem.Billetera.title) }

    // Obtenemos el objeto BottomBarItem correspondiente al título guardado
    val itemSeleccionado = remember(selectedTabTitle) {
        when (selectedTabTitle) {
            BottomBarItem.MercadoP2P.title -> BottomBarItem.MercadoP2P
            BottomBarItem.Historial.title -> BottomBarItem.Historial
            BottomBarItem.Perfil.title -> BottomBarItem.Perfil
            else -> BottomBarItem.Billetera
        }
    }

    val listaItems = listOf(
        BottomBarItem.Billetera,
        BottomBarItem.MercadoP2P,
        BottomBarItem.Historial,
        BottomBarItem.Perfil
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = BinanceSurface,
                tonalElevation = 8.dp
            ) {
                listaItems.forEach { item ->
                    val esActivo = itemSeleccionado == item

                    NavigationBarItem(
                        selected = esActivo,
                        onClick = {
                            selectedTabTitle = item.title
                        },
                        label = {
                            Text(
                                text = item.title,
                                color = if (esActivo) BinanceYellow else BinanceTextSecondary
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (esActivo) BinanceYellow else BinanceTextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = BinanceBackground
                        )
                    )
                }
            }
        },
        containerColor = BinanceBackground
    ) { paddingValores ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
        ) {
            when (itemSeleccionado) {
                is BottomBarItem.Billetera -> {
                    UserWalletScreen(
                        userId = userId,
                        onBackClick = { /* Pestaña raíz */ }
                    )
                }
                is BottomBarItem.MercadoP2P -> {
                    MercadoP2PScreen(
                        usuarioLogueadoId = userId,
                        onBackClick = { /* Pestaña raíz */ }
                    )
                }
                is BottomBarItem.Historial -> {
                    TransactionsScreen(
                        usuarioLogueadoId = userId,
                        onBackClick = { /* Pestaña raíz */ }
                    )
                }
                is BottomBarItem.Perfil -> {
                    ProfileScreen(
                        authViewModel = authViewModel,
                        onLogoutSuccess = onLogoutSuccess,
                        onNavigateToNotifications = onNavigateToNotifications
                    )
                }
            }
        }
    }
}
