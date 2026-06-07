package com.example.p2pmoviles.presentation.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2pmoviles.ui.theme.BinanceBackground
import com.example.p2pmoviles.ui.theme.BinanceYellow

@Composable
fun MenuScreen(
    onNavigateToWallet: () -> Unit,
    onNavigateToHome: () -> Unit, // Opción para la pantalla de búsqueda estilo Home
    onNavigateToMarket: () -> Unit,
    onNavigateToPostOffer: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToUploadReceipt: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BinanceBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Menú Principal",
            color = Color.White,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        MenuButton(text = "Mi Billetera", onClick = onNavigateToWallet)
        Spacer(modifier = Modifier.height(16.dp))

        MenuButton(text = "Mercado P2P (Filtros)", onClick = onNavigateToMarket)
        Spacer(modifier = Modifier.height(16.dp))

        MenuButton(text = "Historial de Transacciones", onClick = onNavigateToHistory)
        Spacer(modifier = Modifier.height(16.dp))

        MenuButton(text = "Subir Comprobante", onClick = onNavigateToUploadReceipt)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red.copy(alpha = 0.7f),
                contentColor = Color.White
            )
        ) {
            Text("Cerrar Sesión")
        }
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = BinanceYellow,
            contentColor = Color.Black
        )
    ) {
        Text(text, fontSize = 16.sp)
    }
}
