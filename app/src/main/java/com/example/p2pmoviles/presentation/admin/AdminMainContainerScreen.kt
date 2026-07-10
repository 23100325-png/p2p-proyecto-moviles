package com.example.p2pmoviles.presentation.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2pmoviles.ui.theme.*

@Composable
fun AdminMainContainerScreen() {
    var pestañaActiva by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = BinanceInputBackground,
                tonalElevation = 8.dp
            ) {
                // Pestaña 1: Recargas
                NavigationBarItem(
                    selected = pestañaActiva == 0,
                    onClick = { pestañaActiva = 0 },
                    label = { Text("Recargas", fontSize = 10.sp) },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = "Recargas"
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

                // Pestaña 2: Usuarios
                NavigationBarItem(
                    selected = pestañaActiva == 1,
                    onClick = { pestañaActiva = 1 },
                    label = { Text("Usuarios", fontSize = 10.sp) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Usuarios"
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

                // Pestaña 3: Disputas
                NavigationBarItem(
                    selected = pestañaActiva == 2,
                    onClick = { pestañaActiva = 2 },
                    label = { Text("Disputas", fontSize = 10.sp) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "Disputas"
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

                // Pestaña 4: Monedas
                NavigationBarItem(
                    selected = pestañaActiva == 3,
                    onClick = { pestañaActiva = 3 },
                    label = { Text("Monedas", fontSize = 10.sp) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CurrencyExchange,
                            contentDescription = "Monedas"
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (pestañaActiva) {
                0 -> AdminDashboardScreen()
                1 -> PlaceholderScreen("Gestión de Usuarios")
                2 -> PlaceholderScreen("Gestión de Disputas")
                3 -> PlaceholderScreen("Gestión de Monedas")
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = BinanceTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Próximamente disponible",
                color = BinanceTextSecondary,
                fontSize = 14.sp
            )
        }
    }
}
