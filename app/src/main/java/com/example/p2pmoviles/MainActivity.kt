package com.example.p2pmoviles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.example.p2pmoviles.presentation.admin.AdminDashboardScreen
import com.example.p2pmoviles.presentation.auth.AuthViewModel
import com.example.p2pmoviles.presentation.auth.LoginScreen
import com.example.p2pmoviles.presentation.auth.RegistroScreen
import com.example.p2pmoviles.presentation.user.UserWalletScreen
import com.example.p2pmoviles.ui.theme.P2PMovilesTheme

class MainActivity : ComponentActivity() {

    // Instanciamos el ViewModel de forma correcta usando el delegado de Android
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            P2PMovilesTheme {
                // Control dinámico de pantallas básicas para Auth
                var pantallaActual by remember { mutableStateOf("login") }

                // OBSERVACIÓN 1: Interceptamos el botón "Atrás" del celular
                if (pantallaActual == "registro") {
                    BackHandler {
                        pantallaActual = "login" // Si está en registro, "Atrás" lo regresa al Login de forma nativa
                    }
                }

                // Enrutador condicional simplificado para los flujos prioritarios
                when (pantallaActual) {
                    "login" -> LoginScreen(
                        viewModel = authViewModel,
                        onNavigateToRegistro = {
                            pantallaActual = "registro"
                        },
                        onLoginSuccess = { esAdmin ->
                            // OBSERVACIÓN 2: Lógica de redirección lista según el Rol (Admin = 2, User = 1)
                            pantallaActual = if (esAdmin) "admin" else "billetera"
                        }
                    )

                    "registro" -> RegistroScreen(
                        viewModel = authViewModel,
                        onNavigateToLogin = {
                            pantallaActual = "login"
                        }
                    )

                    // Contenedores temporales para que tu app compile sin errores
                    // mientras solo te enfocas en el módulo de Auth.
                    "billetera" -> {
                        // Obtenemos el UID dinámico del usuario que acaba de loguearse desde tu ViewModel de Auth
                        val uidLogueado = authViewModel.usuarioActualId // Asegúrate de tener expuesto este String en tu AuthVM

                        // Inyectamos la pantalla operativa del cliente
                        UserWalletScreen(userId = uidLogueado)
                    }

                    "admin" -> {
                        // 🟢 Flujo correcto: Renderiza el Dashboard estilo Binance con toda su lógica inyectada
                        AdminDashboardScreen()
                    }
                }
            }
        }
    }
}
