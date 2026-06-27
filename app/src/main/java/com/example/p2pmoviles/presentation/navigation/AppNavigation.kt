package com.example.p2pmoviles.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.p2pmoviles.presentation.admin.AdminDashboardScreen
import com.example.p2pmoviles.presentation.auth.AuthViewModel
import com.example.p2pmoviles.presentation.auth.LoginScreen
import com.example.p2pmoviles.presentation.auth.RegistroScreen
import com.example.p2pmoviles.presentation.home.HomeScreen
import com.example.p2pmoviles.presentation.main.MainContainerScreen
import com.example.p2pmoviles.presentation.receipt.UploadReceiptScreen
import com.example.p2pmoviles.presentation.user.historial.TransactionsScreen
import com.example.p2pmoviles.presentation.user.mercadoP2P.MercadoP2PScreen
import com.example.p2pmoviles.presentation.user.mercadoP2P.oferta.PublicarOfertaScreen
import com.example.p2pmoviles.presentation.user.billetera.UserWalletScreen

import com.example.p2pmoviles.presentation.user.notifications.NotificationsScreen

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val userId = authViewModel.usuarioActualId

    NavHost(
        navController = navController,
        startDestination = Routes.Login.route
    ) {
        // --- FLUJO DE AUTENTICACIÓN ---
        composable(Routes.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegistro = {
                    navController.navigate(Routes.Register.route)
                },
                onLoginSuccess = { esAdmin ->
                    if (esAdmin) {
                        navController.navigate(Routes.Admin.route) {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate("main_container") {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.Register.route) {
            RegistroScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // --- MENÚ PRINCIPAL ---
        composable("main_container") {
            MainContainerScreen(
                authViewModel = authViewModel,
                onLogoutSuccess = {
                    // Cuando la corrutina limpie el ID y el token, lo saca al Login limpiando el árbol
                    navController.navigate(Routes.Login.route) {
                        popUpTo("main_container") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToNotifications = {
                    navController.navigate(Routes.Notifications.route)
                }
            )
        }
        // --- PANTALLAS DE USUARIO ---
        composable(Routes.Notifications.route) {
            NotificationsScreen(
                usuarioId = userId,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Routes.Wallet.route) {
            UserWalletScreen(
                userId = userId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.Home.route) {
            HomeScreen(
                usuarioLogueadoId = userId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.MercadoP2P.route) {
            MercadoP2PScreen(
                usuarioLogueadoId = userId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.PublicarOferta.route) {
            PublicarOfertaScreen(
                usuarioLogueadoId = userId,
                //onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.Transactions.route) {
            TransactionsScreen(
                usuarioLogueadoId = userId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.UploadReceipt.route) {
            UploadReceiptScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- PANTALLA DE ADMIN ---
        composable(Routes.Admin.route) {
            AdminDashboardScreen()
        }
    }
}
