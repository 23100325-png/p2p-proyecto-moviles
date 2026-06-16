package com.example.p2pmoviles.presentation.main // 🟢 Ajusta a tu paquete real

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.p2pmoviles.presentation.Menu.BottomBarItem
import com.example.p2pmoviles.presentation.auth.AuthViewModel
import com.example.p2pmoviles.presentation.user.historial.TransactionsScreen
import com.example.p2pmoviles.presentation.user.mercadoP2P.MercadoP2PScreen
import com.example.p2pmoviles.presentation.user.billetera.UserWalletScreen
import com.example.p2pmoviles.ui.theme.*

@Composable
fun MainContainerScreen(
    authViewModel: AuthViewModel,
    onLogoutSuccess: () -> Unit // Callback para cuando el LoginScreen tome el control definitivo
) {
    val userId = authViewModel.usuarioActualId

    // Estado para saber qué pestaña está viendo el usuario actualmente (Inicia en Billetera)
    var itemSeleccionado by remember { mutableStateOf<BottomBarItem>(BottomBarItem.Billetera) }

    // Lista ordenada de los botones inferiores
    val listaItems = listOf(
        BottomBarItem.Billetera,
        BottomBarItem.MercadoP2P,
        BottomBarItem.Historial,
        BottomBarItem.CerrarSesion
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = BinanceSurface, // Color gris oscuro para la barra inferior
                tonalElevation = 8.dp
            ) {
                listaItems.forEach { item ->
                    val esActivo = itemSeleccionado == item

                    NavigationBarItem(
                        selected = esActivo,
                        onClick = {
                            if (item is BottomBarItem.CerrarSesion) {
                                // 🔴 Si toca salir, ejecuta la limpieza asíncrona del primer clic
                                authViewModel.cerrarSesion {
                                    onLogoutSuccess()
                                }
                            } else {
                                // Cambia la pantalla central inmediatamente
                                itemSeleccionado = item
                            }
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
                            indicatorColor = BinanceBackground // El óvalo selector detrás del ícono
                        )
                    )
                }
            }
        },
        containerColor = BinanceBackground
    ) { paddingValores ->
        // 🔲 El espacio restante de la pantalla cambia dinámicamente aquí
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores) // 🚨 EVITA que tus pantallas se metan debajo de la barra inferior
        ) {
            when (itemSeleccionado) {
                is BottomBarItem.Billetera -> {
                    UserWalletScreen(
                        userId = userId,
                        onBackClick = { /* Ya no lo necesitas porque estás en pestañas raíz */ }
                    )
                }
                is BottomBarItem.MercadoP2P -> {
                    MercadoP2PScreen(
                        usuarioLogueadoId = userId,
                        onBackClick = { /* Opcional */ }
                    )
                }
                is BottomBarItem.Historial -> {
                    TransactionsScreen(
                        usuarioLogueadoId = userId,
                        onBackClick = { /* Opcional */ }
                    )
                }
                is BottomBarItem.CerrarSesion -> {
                    // Carga visual mientras se procesa la salida de Supabase
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BinanceYellow)
                    }
                }
            }
        }
    }
}