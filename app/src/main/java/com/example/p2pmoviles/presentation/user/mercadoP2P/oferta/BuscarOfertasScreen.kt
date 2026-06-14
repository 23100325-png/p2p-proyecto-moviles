package com.example.p2pmoviles.presentation.user.mercadoP2P.oferta

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2pmoviles.presentation.user.mercadoP2P.MercadoP2PViewModel
import com.example.p2pmoviles.data.model.OfertaMercado
import com.example.p2pmoviles.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun BuscarOfertasScreen(
    usuarioLogueadoId: String,
    mercadoViewModel: MercadoP2PViewModel
) {
    // Inicializamos el motor de búsqueda con el ID del usuario actual
    LaunchedEffect(usuarioLogueadoId) {
        mercadoViewModel.inicializar(usuarioLogueadoId)
    }

    // Estados reactivos mapeados del ViewModel
    val ofertas by mercadoViewModel.ofertasDisponibles.collectAsState()
    val monedasFiltro by mercadoViewModel.monedasFiltro.collectAsState()
    val tengoSelected by mercadoViewModel.filtroTengo.collectAsState()
    val quieroSelected by mercadoViewModel.filtroQuiero.collectAsState()
    val cargando by mercadoViewModel.cargando.collectAsState()

    // 🟢 Lectura del estado de la API de tipo de cambio
    val tasaReferencial by mercadoViewModel.tipoCambioReferencial.collectAsState()

    // 🟢 Sincronización automática de la API con los filtros de la interfaz
    LaunchedEffect(tengoSelected?.codigoIso, quieroSelected?.codigoIso) {
        if (tengoSelected != null && quieroSelected != null) {
            mercadoViewModel.obtenerTipoCambioReal()
        }
    }

    // Control local de los Diálogos de Filtro
    var mostrarDialogoTengo by remember { mutableStateOf(false) }
    var mostrarDialogoQuiero by remember { mutableStateOf(false) }

    // Estados para la confirmación de la transacción mediante ventanas flotantes
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var ofertaAConfirmar by remember { mutableStateOf<OfertaMercado?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val codigoTengo = tengoSelected?.codigoIso ?: "---"
    val codigoQuiero = quieroSelected?.codigoIso ?: "---"

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = BinanceBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Divisas de intercambio
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BinanceInputBackground, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Selecciona tus divisas de intercambio",
                    color = BinanceTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Caja Selector "Tengo"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(BinanceBackground, RoundedCornerShape(8.dp))
                            .clickable { mostrarDialogoTengo = true }
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tengo", color = BinanceTextSecondary, fontSize = 11.sp)
                            Text(codigoTengo, color = BinanceYellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Ícono de cruce en medio
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Intercambio",
                        tint = BinanceYellow,
                        modifier = Modifier.padding(horizontal = 12.dp).size(24.dp)
                    )

                    // Caja Selector "Quiero"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(BinanceBackground, RoundedCornerShape(8.dp))
                            .clickable { mostrarDialogoQuiero = true }
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Quiero", color = BinanceTextSecondary, fontSize = 11.sp)
                            Text(codigoQuiero, color = BinanceTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 🟢 TÍTULO SECCIÓN + COMPONENTE DE TASA REAL REFERENCIAL INTEGRADO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ofertas disponibles",
                    color = BinanceTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                if (tengoSelected != null && quieroSelected != null) {
                    Surface(
                        color = BinanceInputBackground,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val apiLista = tasaReferencial != null && !tasaReferencial!!.contains("⏳") && !tasaReferencial!!.contains("❌")
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(if (apiLista) BinanceGreen else BinanceYellow, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (apiLista) "$tasaReferencial" else tasaReferencial ?: "⏳ Conectando API...",
                                color = BinanceTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 📌 LISTADO Y ESTADOS DE CARGA
            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BinanceYellow)
                }
            } else if (ofertas.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = BinanceTextSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No se encontraron ofertas",
                        color = BinanceTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Intenta ajustar los filtros de búsqueda",
                        color = BinanceTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(ofertas) { oferta ->
                        val montoQueRecibes = oferta.montoOrigen
                        val totalAPagarInteresado = oferta.montoOrigen * oferta.tasaCambio
                        val nombreOfertante = oferta.ofertanteInfo?.nombre ?: "Usuario Anónimo"

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .background(BinanceInputBackground, RoundedCornerShape(14.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(BinanceYellow.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = nombreOfertante.take(1).uppercase(),
                                            color = BinanceYellow,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = nombreOfertante, color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "${oferta.ofertanteInfo?.calificacion ?: 5.0} (${oferta.ofertanteInfo?.totalOperaciones ?: 120} operaciones)", color = BinanceTextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = String.format("%.4f", oferta.tasaCambio),
                                        color = BinanceYellow,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(text = "Precio por 1 $codigoQuiero", color = BinanceTextSecondary, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Recibes: ", color = BinanceTextSecondary, fontSize = 13.sp)
                                        Text(text = String.format("%.2f %s", montoQueRecibes, codigoQuiero), color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Pagas: ", color = BinanceTextSecondary, fontSize = 13.sp)
                                        Text(text = String.format("%.2f %s", totalAPagarInteresado, codigoTengo), color = BinanceGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }

                                Button(
                                    onClick = {
                                        mercadoViewModel.tomarOfertaP2P(
                                            ofertaId = oferta.id,
                                            montoPago = totalAPagarInteresado,
                                            monedaDestinoId = oferta.monedaDestinoId,
                                            monedaPagoCodigo = codigoTengo,
                                            onSaldoInsuficiente = { msj ->
                                                coroutineScope.launch { snackbarHostState.showSnackbar(msj) }
                                            },
                                            onConfirmarOperacion = {
                                                ofertaAConfirmar = oferta
                                                mostrarConfirmacion = true
                                            }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = BinanceBackground),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Intercambiar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 📌 DIÁLOGOS DE FILTRADO INTERNO
    if (mostrarDialogoTengo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoTengo = false },
            confirmButton = {},
            containerColor = BinanceInputBackground,
            title = { Text("Tengo en mi billetera:", color = BinanceTextPrimary, fontSize = 16.sp) },
            text = {
                Column {
                    monedasFiltro.forEach { mon ->
                        Text(
                            text = "${mon.codigoIso} - ${mon.nombre}",
                            color = BinanceTextPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    mercadoViewModel.aplicarFiltroTengo(mon)
                                    mostrarDialogoTengo = false
                                }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        )
    }

    if (mostrarDialogoQuiero) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoQuiero = false },
            confirmButton = {},
            containerColor = BinanceInputBackground,
            title = { Text("Quiero recibir en mi billetera:", color = BinanceTextPrimary, fontSize = 16.sp) },
            text = {
                Column {
                    monedasFiltro.forEach { mon ->
                        Text(
                            text = "${mon.codigoIso} - ${mon.nombre}",
                            color = BinanceTextPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    mercadoViewModel.aplicarFiltroQuiero(mon)
                                    mostrarDialogoQuiero = false
                                }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        )
    }

    // 📌 DIÁLOGO DE CONFIRMACIÓN DE OPERACIÓN TRAS VALIDAR SALDO
    if (mostrarConfirmacion && ofertaAConfirmar != null) {
        val pagoTotal = ofertaAConfirmar!!.montoOrigen * ofertaAConfirmar!!.tasaCambio

        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            containerColor = BinanceInputBackground,
            title = { Text("Confirmar Intercambio", color = BinanceYellow, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = "¿Estás seguro de aceptar esta oferta de ${ofertaAConfirmar?.ofertanteInfo?.nombre}?", color = BinanceTextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BinanceBackground, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Entregas: ${String.format("%.2f", pagoTotal)} $codigoTengo", color = BinanceGreen, fontWeight = FontWeight.Bold)
                            Text("Recibes: ${String.format("%.2f", ofertaAConfirmar?.montoOrigen)} $codigoQuiero", color = BinanceTextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacion = false
                        mercadoViewModel.ejecutarTransaccionConfirmada(
                            ofertaId = ofertaAConfirmar!!.id,
                            onSuccess = {
                                coroutineScope.launch { snackbarHostState.showSnackbar("✅ ¡Transacción completada! Saldo actualizado.") }
                            },
                            onError = { error ->
                                coroutineScope.launch { snackbarHostState.showSnackbar("❌ Error: $error") }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow)
                ) {
                    Text("Confirmar", color = BinanceBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar", color = BinanceTextSecondary)
                }
            }
        )
    }
}