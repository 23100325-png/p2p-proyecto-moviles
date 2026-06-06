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
import com.example.p2pmoviles.presentation.menu.MenuScreen
import com.example.p2pmoviles.presentation.receipt.UploadReceiptScreen
import com.example.p2pmoviles.presentation.transactions.TransactionsScreen
import com.example.p2pmoviles.presentation.user.MercadoP2PScreen
import com.example.p2pmoviles.presentation.user.PublicarOfertaScreen
import com.example.p2pmoviles.presentation.user.UserWalletScreen

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
                        navController.navigate(Routes.Menu.route) {
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
        composable(Routes.Menu.route) {
            MenuScreen(
                onNavigateToWallet = { navController.navigate(Routes.Wallet.route) },
                onNavigateToHome = { navController.navigate(Routes.Home.route) },
                onNavigateToMarket = { navController.navigate(Routes.MercadoP2P.route) },
                onNavigateToPostOffer = { navController.navigate(Routes.PublicarOferta.route) },
                onNavigateToHistory = { navController.navigate(Routes.Transactions.route) },
                onNavigateToUploadReceipt = { navController.navigate(Routes.UploadReceipt.route) },
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        // --- PANTALLAS DE USUARIO ---
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
                onBackClick = { navController.popBackStack() },
                onNavegarAPublicarClick = {
                    navController.navigate(Routes.PublicarOferta.route)
                }
            )
        }

        composable(Routes.PublicarOferta.route) {
            PublicarOfertaScreen(
                usuarioLogueadoId = userId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.Transactions.route) {
            TransactionsScreen(
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
