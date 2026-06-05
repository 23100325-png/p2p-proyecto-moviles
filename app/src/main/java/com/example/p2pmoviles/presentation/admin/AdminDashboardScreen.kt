package com.example.p2pmoviles.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage // Importar Coil para renderizar las URLs de las banderas
import com.example.p2pmoviles.data.model.MovimientoAdmin
import com.example.p2pmoviles.ui.theme.BinanceInputBackground
import com.example.p2pmoviles.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel = viewModel()
) {
    val uiState by adminViewModel.uiState.collectAsState()
    val estaRefrescando by adminViewModel.estaRefrescando.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var voucherUrlToShow by remember { mutableStateOf<String?>(null) }

    // Diálogo flotante para visualizar el voucher a pantalla completa
    voucherUrlToShow?.let { url ->
        Dialog(onDismissRequest = { voucherUrlToShow = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BinanceInputBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Comprobante de Pago",
                            color = BinanceTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { voucherUrlToShow = null }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = BinanceError
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = url,
                        contentDescription = "Voucher Completo",
                        modifier = Modifier.fillMaxWidth().height(350.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(8.dp).background(BinanceYellow, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Binance Admin Panel",
                            color = BinanceYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BinanceBackground)
            )
        },
        containerColor = BinanceBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            // Estructura de los Estados en el Estado Exitoso
            val movimientosCompletos = (uiState as? AdminUIState.Success)?.lista ?: emptyList()
            val listaFiltrada = when (selectedTab) {
                1 -> movimientosCompletos.filter { it.tipoMovimiento == "RECARGA" }
                2 -> movimientosCompletos.filter { it.tipoMovimiento == "RETIRO" }
                else -> movimientosCompletos
            }

            // Tabs superiores estilo Binance Exchange
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BinanceBackground,
                contentColor = BinanceYellow,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = BinanceYellow
                    )
                }
            ) {
                val titulos = listOf(
                    "Todos (${movimientosCompletos.size})",
                    "Recarga (${movimientosCompletos.count { it.tipoMovimiento == "RECARGA" }})",
                    "Retiros (${movimientosCompletos.count { it.tipoMovimiento == "RETIRO" }})"
                )
                titulos.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = FontWeight.Medium,
                                color = if (selectedTab == index) BinanceTextPrimary else BinanceTextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SOLICITUDES PENDIENTES",
                color = BinanceTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Cuerpo principal del Panel de Control
            when (val state = uiState) {
                is AdminUIState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BinanceYellow)
                    }
                }

                is AdminUIState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.msg, color = BinanceError, textAlign = TextAlign.Center)
                    }
                }

                is AdminUIState.Success -> {
                    // 🟢 2. ENVOLVEMOS EL CONTROL DE LA LISTA EN EL PULLTOREFRESHBOX
                    PullToRefreshBox(
                        isRefreshing = estaRefrescando,
                        onRefresh = {
                            // 🟢 Esto llama a tu función de Supabase del ViewModel para actualizar
                            adminViewModel.obtenerMovimientos()
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (listaFiltrada.isEmpty()) {
                            // Si la lista está vacía, igual permitimos jalar hacia abajo metiendo el texto en un contenedor con scroll
                            Box(
                                modifier = Modifier.fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No hay transacciones pendientes en esta sección.",
                                    color = BinanceTextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            // Tu LazyColumn original intacta
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(listaFiltrada) { mov ->
                                    RowTransaccionItem(
                                        movimiento = mov,
                                        onVerVoucher = { url -> voucherUrlToShow = url },
                                        onAprobar = {
                                            adminViewModel.procesarSolicitud(
                                                mov,
                                                aprobar = true
                                            )
                                        },
                                        onRechazar = {
                                            adminViewModel.procesarSolicitud(
                                                mov,
                                                aprobar = false
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun RowTransaccionItem(
    movimiento: MovimientoAdmin,
    onVerVoucher: (String) -> Unit,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BinanceInputBackground),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Bandera y datos de la moneda
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.8f)) {
                AsyncImage(
                    model = movimiento.monedas?.rutaBandera,
                    contentDescription = "Bandera",
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(BinanceBackground),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(movimiento.monedas?.nombre ?: "Desconocida", color = BinanceTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(movimiento.monedas?.codigoIso ?: "---", color = BinanceTextSecondary, fontSize = 12.sp)
                }
            }

            // 2. Monto y Tipo (Depósito/Retiro)
            Column(modifier = Modifier.weight(1.5f), horizontalAlignment = Alignment.Start) {
                val esRecarga = movimiento.tipoMovimiento == "RECARGA"
                Text(
                    text = "${movimiento.monedas?.simbolo ?: ""} ${String.format("%.2f", movimiento.monto)}",
                    color = BinanceTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = if (esRecarga) "● Recarga" else "● Retiro",
                    color = if (esRecarga) BinanceSuccess else BinanceError,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 3. Comprobante (Voucher)
            Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.Center) {
                if (movimiento.tipoMovimiento == "RECARGA" && !movimiento.rutaVoucher.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BinanceBackground)
                            .clickable { onVerVoucher(movimiento.rutaVoucher) }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = "Ver", tint = BinanceYellow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver", color = BinanceYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("--", color = BinanceTextSecondary, fontSize = 12.sp)
                }
            }

            // 4. Acciones (Botones aprobar / rechazar)
            Row(
                modifier = Modifier.weight(1f), // 🟢 Aumentamos el peso a 1f para dar espacio real y evitar colapsos
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🟢 BOTÓN RECHAZAR: Ícono limpio sin fondo circular pesado
                IconButton(
                    onClick = onRechazar,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Rechazar",
                        tint = BinanceError, // Mantiene tu color rojo de error
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp)) // 🟢 ESPACIO CLAVE: Separación física segura entre ambos comandos

                // 🟢 BOTÓN APROBAR: Ícono limpio sin fondo circular pesado
                IconButton(
                    onClick = onAprobar,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Aprobar",
                        tint = BinanceSuccess, // Mantiene tu color verde de éxito
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}