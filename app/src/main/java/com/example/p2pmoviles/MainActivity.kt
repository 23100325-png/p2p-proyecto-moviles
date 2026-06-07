package com.example.p2pmoviles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.p2pmoviles.presentation.auth.AuthViewModel
import com.example.p2pmoviles.presentation.navigation.AppNavigation
import com.example.p2pmoviles.ui.theme.P2PMovilesTheme

class MainActivity : ComponentActivity() {

    // Instanciamos el ViewModel de forma correcta usando el delegado de Android
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            P2PMovilesTheme {
                // Delegamos toda la navegación al AppNavigation
                AppNavigation(authViewModel = authViewModel)
            }
        }
    }
}
