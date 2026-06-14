package com.example.p2pmoviles.presentation.user.mercadoP2P

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.p2pmoviles.presentation.user.mercadoP2P.oferta.BuscarOfertasScreen
import com.example.p2pmoviles.presentation.user.mercadoP2P.oferta.PublicarOfertaScreen
import com.example.p2pmoviles.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MercadoP2PScreen(
    usuarioLogueadoId: String,
    onBackClick: () -> Unit = {},
    mercadoViewModel: MercadoP2PViewModel = viewModel()
) {
    var subPestañaActiva by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mercado P2P", color = BinanceTextPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BinanceBackground)
            )
        },
        containerColor = BinanceBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BinanceBackground)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Selector superior de operaciones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BinanceInputBackground, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (subPestañaActiva == 0) BinanceYellow else Color.Transparent, RoundedCornerShape(6.dp))
                        .clickable { subPestañaActiva = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Buscar ofertas",
                        color = if (subPestañaActiva == 0) BinanceBackground else BinanceTextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (subPestañaActiva == 1) BinanceYellow else Color.Transparent, RoundedCornerShape(6.dp))
                        .clickable { subPestañaActiva = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Publicar oferta",
                        color = if (subPestañaActiva == 1) BinanceBackground else BinanceTextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Espacio central dinámico
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (subPestañaActiva) {
                    0 -> {
                        // 🟢 Invocamos la pantalla unificada de búsqueda
                        BuscarOfertasScreen(
                            usuarioLogueadoId = usuarioLogueadoId,
                            mercadoViewModel = mercadoViewModel
                        )
                    }
                    1 -> {
                        PublicarOfertaScreen(
                            usuarioLogueadoId = usuarioLogueadoId,
                            //onBackClick = { subPestañaActiva = 0 }
                        )
                    }
                }
            }
        }
    }
}