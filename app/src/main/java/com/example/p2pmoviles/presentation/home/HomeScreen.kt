package com.example.p2pmoviles.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.p2pmoviles.presentation.user.mercadoP2P.MercadoP2PViewModel
import com.example.p2pmoviles.ui.theme.BinanceBackground
import com.example.p2pmoviles.ui.theme.BinanceTextPrimary
import com.example.p2pmoviles.ui.theme.BinanceTextSecondary
import com.example.p2pmoviles.ui.theme.BinanceYellow

private val DarkBg = Color(0xFF070B0F)
private val SoftText = Color(0xFFB8BDC6)
private val Yellow = Color(0xFFFFC400)

@Composable
fun HomeScreen(
    usuarioLogueadoId: String,
    onBackClick: () -> Unit,
    mercadoViewModel: MercadoP2PViewModel = viewModel()
) {
    // Inicializamos el motor de búsqueda con el ID del usuario actual
    LaunchedEffect(usuarioLogueadoId) {
        mercadoViewModel.inicializar(usuarioLogueadoId)
    }

    val ofertas by mercadoViewModel.ofertasDisponibles.collectAsState()
    val cargando by mercadoViewModel.cargando.collectAsState()
    val tengoSelected by mercadoViewModel.filtroTengo.collectAsState()
    val quieroSelected by mercadoViewModel.filtroQuiero.collectAsState()

    var expandedSort by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf("Tipo de cambio") }

    val sortOptions = listOf(
        "Tipo de cambio",
        "Puntuación del vendedor",
        "Fecha de publicación"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B1117), DarkBg)
                )
            )
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Text(
                    text = "Buscar ofertas",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            SearchBox(mercadoViewModel)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ofertas disponibles",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Box {
                    Row(
                        modifier = Modifier.clickable {
                            expandedSort = true
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedSort,
                            color = SoftText,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Yellow
                        )
                    }

                    DropdownMenu(
                        expanded = expandedSort,
                        onDismissRequest = { expandedSort = false }
                    ) {
                        sortOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedSort = option
                                    expandedSort = false
                                }
                            )
                        }
                    }
                }
            }
        }

        if (cargando) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Yellow)
                }
            }
        } else if (ofertas.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = SoftText.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No se encontraron ofertas",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Intenta ajustar los filtros de búsqueda",
                        color = SoftText,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(ofertas) { oferta ->
                OfferCard(
                    oferta = oferta,
                    codigoTengo = tengoSelected?.codigoIso ?: "",
                    codigoQuiero = quieroSelected?.codigoIso ?: ""
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
