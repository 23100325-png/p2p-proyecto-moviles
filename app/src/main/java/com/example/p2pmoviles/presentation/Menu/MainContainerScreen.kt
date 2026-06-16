package com.example.p2pmoviles.presentation.Menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarItem(val title: String, val icon: ImageVector) {
    object Billetera : BottomBarItem("Billetera", Icons.Default.AccountBalanceWallet)
    object MercadoP2P : BottomBarItem("Mercado P2P", Icons.Default.Storefront)
    object Historial : BottomBarItem("Historial", Icons.Default.History)
    //object Perfil : BottomBarItem("Mi perfil", Icons.Default.Person)
    object CerrarSesion : BottomBarItem("Salir", Icons.Default.ExitToApp)
}