package com.example.p2pmoviles.presentation.user.mercadoP2P.oferta

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.p2pmoviles.presentation.user.mercadoP2P.MercadoP2PViewModel
import com.example.p2pmoviles.data.model.OfertaMercado
import com.example.p2pmoviles.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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

    // Filtros adicionales del ViewModel
    val fechaDesde by mercadoViewModel.fechaDesde.collectAsState()
    val fechaHasta by mercadoViewModel.fechaHasta.collectAsState()
    val tasaTarget by mercadoViewModel.tasaTarget.collectAsState()
    val margenTasa by mercadoViewModel.margenTasa.collectAsState()

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
    var mostrarFiltrosAvanzados by remember { mutableStateOf(false) }

    // Date Pickers
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    // Estados para la confirmación de la transacción mediante ventanas flotantes
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var ofertaAConfirmar by remember { mutableStateOf<OfertaMercado?>(null) }

    // Estados para el diálogo de calificación
    var mostrarCalificacion by remember { mutableStateOf(false) }
    var transaccionIdParaCalificar by remember { mutableLongStateOf(0L) }
    var usuarioIdParaCalificar by remember { mutableStateOf("") }
    var puntuacionSeleccionada by remember { mutableIntStateOf(5) }
    var comentarioCalificacion by remember { mutableStateOf("") }

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
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable { mercadoViewModel.swapFiltros() }
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

            // Fila de Filtros Avanzados y Botón Limpiar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { mostrarFiltrosAvanzados = !mostrarFiltrosAvanzados }
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (mostrarFiltrosAvanzados) "Ocultar filtros" else "Filtros avanzados",
                        color = BinanceYellow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (fechaDesde != null || fechaHasta != null || tasaTarget.isNotEmpty() || margenTasa.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { mercadoViewModel.resetFiltrosOpcionales() }
                    ) {
                        Icon(Icons.Default.ClearAll, contentDescription = null, tint = BinanceError, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Limpiar", color = BinanceError, fontSize = 12.sp)
                    }
                }
            }

            if (mostrarFiltrosAvanzados) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BinanceInputBackground, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Filtros de Fecha
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(BinanceBackground, RoundedCornerShape(8.dp))
                                .clickable { showStartDatePicker = true }
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, null, tint = BinanceTextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (fechaDesde != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fechaDesde!!)) else "Desde",
                                    color = if (fechaDesde != null) BinanceTextPrimary else BinanceTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(BinanceBackground, RoundedCornerShape(8.dp))
                                .clickable { showEndDatePicker = true }
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, null, tint = BinanceTextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (fechaHasta != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fechaHasta!!)) else "Hasta",
                                    color = if (fechaHasta != null) BinanceTextPrimary else BinanceTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Filtros de Tipo de Cambio
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tasaTarget,
                            onValueChange = { mercadoViewModel.actualizarTasaTarget(it) },
                            label = { Text("Tasa buscada", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = BinanceTextPrimary,
                                unfocusedTextColor = BinanceTextPrimary,
                                focusedContainerColor = BinanceBackground,
                                unfocusedContainerColor = BinanceBackground,
                                focusedBorderColor = BinanceYellow,
                                unfocusedBorderColor = BinanceTextSecondary.copy(alpha = 0.3f),
                                focusedLabelColor = BinanceYellow,
                                unfocusedLabelColor = BinanceTextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = margenTasa,
                            onValueChange = { mercadoViewModel.actualizarMargenTasa(it) },
                            label = { Text("Margen ±", fontSize = 11.sp) },
                            modifier = Modifier.weight(0.6f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = BinanceTextPrimary,
                                unfocusedTextColor = BinanceTextPrimary,
                                focusedContainerColor = BinanceBackground,
                                unfocusedContainerColor = BinanceBackground,
                                focusedBorderColor = BinanceYellow,
                                unfocusedBorderColor = BinanceTextSecondary.copy(alpha = 0.3f),
                                focusedLabelColor = BinanceYellow,
                                unfocusedLabelColor = BinanceTextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
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
                        val fechaPublicacionFormateada = try {
                            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                            val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                            val date = parser.parse(oferta.fechaPublicacion)
                            formatter.format(date!!)
                        } catch (e: Exception) {
                            oferta.fechaPublicacion
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .background(BinanceInputBackground, RoundedCornerShape(14.dp))
                                .padding(16.dp)
                        ) {
                            // Fila superior con Info del Usuario y Fecha
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
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
                                            Text(
                                                text = String.format("%.1f (%d calificaciones)", 
                                                    oferta.ofertanteInfo?.calificacion ?: 5.0, 
                                                    oferta.ofertanteInfo?.totalOperaciones ?: 0
                                                ), 
                                                color = BinanceTextSecondary, 
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = fechaPublicacionFormateada,
                                    color = BinanceTextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Tasa de cambio de la oferta
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Precio: ", color = BinanceTextSecondary, fontSize = 12.sp)
                                Text(
                                    text = "1 $codigoQuiero = ${String.format("%.4f", oferta.tasaCambio)} $codigoTengo",
                                    color = BinanceYellow,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Recibes: ", color = BinanceTextSecondary, fontSize = 13.sp)
                                        Text(text = String.format("%s %.2f", oferta.monedaInfo?.simbolo ?: "", montoQueRecibes), color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    mercadoViewModel.actualizarFechaDesde(startDatePickerState.selectedDateMillis)
                    showStartDatePicker = false
                }) { Text("Aceptar", color = BinanceYellow) }
            }
        ) { DatePicker(state = startDatePickerState) }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    mercadoViewModel.actualizarFechaHasta(endDatePickerState.selectedDateMillis)
                    showEndDatePicker = false
                }) { Text("Aceptar", color = BinanceYellow) }
            }
        ) { DatePicker(state = endDatePickerState) }
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
            containerColor = BinanceSurface,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(BinanceYellow.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = BinanceYellow,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Confirmar Intercambio",
                        color = BinanceTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Resumen de la operación P2P",
                        color = BinanceTextSecondary,
                        fontSize = 12.sp
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // SECCIÓN 1: El Ofertante
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BinanceBackground, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BinanceYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (ofertaAConfirmar?.ofertanteInfo?.nombre ?: "U").take(1).uppercase(),
                                color = BinanceBackground,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = ofertaAConfirmar?.ofertanteInfo?.nombre ?: "Vendedor",
                                color = BinanceTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = BinanceYellow, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f (%d calificaciones)", 
                                        ofertaAConfirmar?.ofertanteInfo?.calificacion ?: 5.0, 
                                        ofertaAConfirmar?.ofertanteInfo?.totalOperaciones ?: 0
                                    ),
                                    color = BinanceTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // SECCIÓN 2: Detalles Económicos
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BinanceBackground, RoundedCornerShape(10.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailRow("Vas a pagar", String.format("%.2f %s", pagoTotal, codigoTengo), BinanceGreen)
                        HorizontalDivider(color = BinanceSurface.copy(alpha = 0.3f), thickness = 0.5.dp)
                        DetailRow("Vas a recibir", String.format("%s %.2f", ofertaAConfirmar?.monedaInfo?.simbolo ?: "", ofertaAConfirmar?.montoOrigen ?: 0.0), BinanceTextPrimary)
                        HorizontalDivider(color = BinanceSurface.copy(alpha = 0.3f), thickness = 0.5.dp)
                        DetailRow("Precio pactado", "1 $codigoQuiero = ${String.format("%.4f", ofertaAConfirmar?.tasaCambio)} $codigoTengo", BinanceYellow)
                    }

                    // SECCIÓN 3: Comentario (Si existe)
                    if (!ofertaAConfirmar?.comentario.isNullOrBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BinanceYellow.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Términos del vendedor:",
                                    color = BinanceYellow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ofertaAConfirmar!!.comentario!!,
                                color = BinanceTextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Text(
                        text = "⚠️ Al confirmar, el saldo se debitará de tu billetera y se procesará el intercambio de forma irreversible.",
                        color = BinanceTextSecondary.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacion = false
                        val ofertanteIdParaCalificar = ofertaAConfirmar?.usuarioId ?: ""
                        mercadoViewModel.ejecutarTransaccionConfirmada(
                            ofertaId = ofertaAConfirmar!!.id,
                            onSuccess = { tId, _ ->
                                coroutineScope.launch { 
                                    snackbarHostState.showSnackbar("✅ ¡Transacción completada! Saldo actualizado.") 
                                }
                                // Preparamos el diálogo de calificación
                                transaccionIdParaCalificar = tId
                                usuarioIdParaCalificar = ofertanteIdParaCalificar
                                mostrarCalificacion = true
                            },
                            onError = { error ->
                                coroutineScope.launch { snackbarHostState.showSnackbar("❌ Error: $error") }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Confirmar Compra", color = BinanceBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarConfirmacion = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar y volver", color = BinanceTextSecondary, fontSize = 14.sp)
                }
            }
        )
    }

    // 📌 DIÁLOGO DE CALIFICACIÓN
    if (mostrarCalificacion) {
        AlertDialog(
            onDismissRequest = { mostrarCalificacion = false },
            containerColor = BinanceSurface,
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("¿Deseas calificar al ofertante?", color = BinanceTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tu opinión ayuda a mantener segura la comunidad", color = BinanceTextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    // Estrellas
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..5).forEach { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index <= puntuacionSeleccionada) BinanceYellow else BinanceTextSecondary.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { puntuacionSeleccionada = index }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = comentarioCalificacion,
                        onValueChange = { comentarioCalificacion = it },
                        label = { Text("Comentarios (opcional)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BinanceTextPrimary,
                            unfocusedTextColor = BinanceTextPrimary,
                            focusedBorderColor = BinanceYellow,
                            unfocusedBorderColor = BinanceTextSecondary.copy(alpha = 0.3f),
                            focusedLabelColor = BinanceYellow,
                            unfocusedLabelColor = BinanceTextSecondary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mercadoViewModel.calificarUsuarioP2P(
                            transaccionId = transaccionIdParaCalificar,
                            usuarioEvaluadoId = usuarioIdParaCalificar,
                            puntuacion = puntuacionSeleccionada,
                            comentario = comentarioCalificacion.ifBlank { null },
                            onComplete = {
                                mostrarCalificacion = false
                                comentarioCalificacion = ""
                                puntuacionSeleccionada = 5
                                coroutineScope.launch { snackbarHostState.showSnackbar("⭐ ¡Gracias por tu calificación!") }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Calificar", color = BinanceBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarCalificacion = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ahora no", color = BinanceTextSecondary)
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = BinanceTextSecondary, fontSize = 13.sp)
        Text(text = value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
