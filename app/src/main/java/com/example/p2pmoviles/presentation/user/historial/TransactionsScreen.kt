package com.example.p2pmoviles.presentation.user.historial

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.p2pmoviles.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    usuarioLogueadoId: String,
    onBackClick: () -> Unit,
    historialViewModel: HistorialOfertasViewModel = viewModel()
) {
    LaunchedEffect(usuarioLogueadoId) {
        historialViewModel.inicializar(usuarioLogueadoId)
    }

    val ofertasUsuario by historialViewModel.ofertas.collectAsState()
    val estaCargando by historialViewModel.cargando.collectAsState()
    val esModoOfertante by historialViewModel.esModoOfertante.collectAsState()
    val mensaje by historialViewModel.mensaje.collectAsState()

    var expandedStatus by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("Todos los estados") }
    var expandedSort by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf("Más recientes") }

    var startDate by remember { mutableStateOf("Desde") }
    var endDate by remember { mutableStateOf("Hasta") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()
    var startDateMillis by remember { mutableStateOf<Long?>(null) }
    var endDateMillis by remember { mutableStateOf<Long?>(null) }

    // Estado para el BottomSheet de seguimiento
    var showTrackingSheet by remember { mutableStateOf(false) }
    var selectedOfertaForTracking by remember { mutableStateOf<OfertaDb?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val ofertasFiltradas = remember(ofertasUsuario, selectedStatus, startDateMillis, endDateMillis, selectedSort) {
        val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val filtradas = ofertasUsuario.filter { oferta ->
            // 1. Filtro de Estado
            val pasaEstado = selectedStatus == "Todos los estados" || 
                           oferta.estado.equals(selectedStatus, ignoreCase = true)

            // 2. Filtro de Fecha
            val fechaOfertaMillis = try {
                val soloFecha = oferta.fechaPublicacion.take(10)
                sdfInput.parse(soloFecha)?.time
            } catch (e: Exception) {
                null
            }

            val pasaFecha = when {
                startDateMillis != null && endDateMillis != null -> {
                    fechaOfertaMillis != null && 
                    fechaOfertaMillis >= startDateMillis!! && 
                    fechaOfertaMillis <= (endDateMillis!! + 86399999)
                }
                startDateMillis != null -> {
                    fechaOfertaMillis != null && fechaOfertaMillis >= startDateMillis!!
                }
                endDateMillis != null -> {
                    fechaOfertaMillis != null && fechaOfertaMillis <= (endDateMillis!! + 86399999)
                }
                else -> true
            }

            pasaEstado && pasaFecha
        }

        // 3. Ordenamiento
        if (selectedSort == "Más recientes") {
            filtradas.sortedByDescending { it.fechaPublicacion }
        } else {
            filtradas.sortedBy { it.fechaPublicacion }
        }
    }

    Scaffold(
        containerColor = BinanceBackground // 🟢 Usando tu fondo oficial
    ) { paddingValues ->
        if (estaCargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BinanceYellow) // 🟢 Usando tu amarillo oficial
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(BinanceBackground, BinanceBackground.copy(alpha = 0.95f))
                        )
                    )
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Toolbar
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (esModoOfertante) "Mis Ofertas Publicadas" else "Compras Realizadas",
                            color = BinanceTextPrimary, // 🟢 Texto principal oficial
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Selector de modo (Ofertante vs Comprador)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BinanceSurface, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        val activeColor = BinanceYellow
                        val inactiveColor = androidx.compose.ui.graphics.Color.Transparent

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (esModoOfertante) activeColor else inactiveColor)
                                .clickable { historialViewModel.setModoOfertante(true, usuarioLogueadoId) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Soy Ofertante",
                                color = if (esModoOfertante) BinanceBackground else BinanceTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!esModoOfertante) activeColor else inactiveColor)
                                .clickable { historialViewModel.setModoOfertante(false, usuarioLogueadoId) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Soy Comprador",
                                color = if (!esModoOfertante) BinanceBackground else BinanceTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Bloque contenedor de Filtros
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BinanceSurface, RoundedCornerShape(12.dp)) // 🟢 Usando tu color de superficie/inputs
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filtrar por estado e historial",
                                color = BinanceTextSecondary, // 🟢 Texto secundario oficial
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            IconButton(
                                onClick = {
                                    startDate = "Desde"
                                    endDate = "Hasta"
                                    startDateMillis = null
                                    endDateMillis = null
                                    selectedStatus = "Todos los estados"
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.DeleteSweep,
                                    contentDescription = "Limpiar filtros",
                                    tint = BinanceYellow
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Desde", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = BinanceTextPrimary,
                                    unfocusedTextColor = BinanceTextPrimary,
                                    focusedLabelColor = BinanceYellow,
                                    unfocusedLabelColor = BinanceTextSecondary
                                ),
                                trailingIcon = {
                                    IconButton(onClick = { showStartDatePicker = true }) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = BinanceYellow)
                                    }
                                }
                            )
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Hasta", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = BinanceTextPrimary,
                                    unfocusedTextColor = BinanceTextPrimary,
                                    focusedLabelColor = BinanceYellow,
                                    unfocusedLabelColor = BinanceTextSecondary
                                ),
                                trailingIcon = {
                                    IconButton(onClick = { showEndDatePicker = true }) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = BinanceYellow)
                                    }
                                }
                            )
                        }

                        ExposedDropdownMenuBox(
                            expanded = expandedStatus,
                            onExpandedChange = { expandedStatus = !expandedStatus }
                        ) {
                            OutlinedTextField(
                                value = selectedStatus,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Estado de la oferta") },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = BinanceTextPrimary,
                                    unfocusedTextColor = BinanceTextPrimary,
                                    focusedLabelColor = BinanceYellow,
                                    unfocusedLabelColor = BinanceTextSecondary
                                ),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) }
                            )
                            ExposedDropdownMenu(
                                expanded = expandedStatus,
                                onDismissRequest = { expandedStatus = false },
                                modifier = Modifier.background(BinanceInputBackground)
                            ) {
                                listOf("Todos los estados", "ACTIVA", "COMPLETADA", "CANCELADA").forEach { state ->
                                    DropdownMenuItem(
                                        text = { Text(state, color = BinanceTextPrimary) },
                                        onClick = { selectedStatus = state; expandedStatus = false }
                                    )
                                }
                            }
                        }
                    }
                }

                // Subtítulo del listado
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Resultados de operaciones", color = BinanceTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Box {
                            Row(
                                modifier = Modifier.clickable { expandedSort = true },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedSort, color = BinanceTextSecondary, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Tune, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = expandedSort,
                                onDismissRequest = { expandedSort = false },
                                modifier = Modifier.background(BinanceSurface)
                            ) {
                                listOf("Más recientes", "Más antiguas").forEach { sortOption ->
                                    DropdownMenuItem(
                                        text = { Text(sortOption, color = BinanceTextPrimary) },
                                        onClick = {
                                            selectedSort = sortOption
                                            expandedSort = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Renderizado dinámico de filas
                if (ofertasFiltradas.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (esModoOfertante) "No se encontraron ofertas publicadas." else "No se encontraron compras realizadas.",
                                color = BinanceTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(ofertasFiltradas) { oferta ->
                        HorizontalOfertaCard(
                            oferta = oferta, 
                            esModoOfertante = esModoOfertante,
                            onClick = {
                                selectedOfertaForTracking = oferta
                                showTrackingSheet = true
                            },
                            onCancelClick = {
                                historialViewModel.cancelarOferta(oferta.id, usuarioLogueadoId)
                            }
                        )
                    }
                }

                item {
                    Text(
                        text = if (esModoOfertante) 
                            "🛡️ Esta lista contiene las transacciones en donde eres el creador principal del anuncio P2P." 
                            else "🛡️ Esta lista contiene las transacciones que aceptaste de otros usuarios en el mercado.",
                        color = BinanceTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BinanceSurface, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let { millis ->
                        startDateMillis = millis
                        startDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
                    }
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
                    endDatePickerState.selectedDateMillis?.let { millis ->
                        endDateMillis = millis
                        endDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
                    }
                    showEndDatePicker = false
                }) { Text("Aceptar", color = BinanceYellow) }
            }
        ) { DatePicker(state = endDatePickerState) }
    }

    mensaje?.let {
        AlertDialog(
            onDismissRequest = { historialViewModel.limpiarMensaje() },
            confirmButton = {
                TextButton(onClick = { historialViewModel.limpiarMensaje() }) {
                    Text("OK", color = BinanceYellow)
                }
            },
            title = { Text("Operación", color = BinanceTextPrimary) },
            text = { Text(it, color = BinanceTextSecondary) },
            containerColor = BinanceSurface
        )
    }

    // Modal Bottom Sheet para el Flujo de la Operación
    if (showTrackingSheet && selectedOfertaForTracking != null) {
        ModalBottomSheet(
            onDismissRequest = { showTrackingSheet = false },
            sheetState = sheetState,
            containerColor = BinanceSurface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = BinanceYellow) }
        ) {
            TransactionTimelineContent(
                oferta = selectedOfertaForTracking!!,
                esModoOfertante = esModoOfertante
            )
        }
    }
}

@Composable
fun TransactionTimelineContent(oferta: OfertaDb, esModoOfertante: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Seguimiento de Operación",
            color = BinanceTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Hito 1: Publicación (Siempre completado)
        TimelineItem(
            title = "Oferta Publicada",
            subtitle = "Publicada por: ${oferta.perfilOfertante?.nombreCompleto ?: "Tú"}",
            date = formatTimelineDate(oferta.fechaPublicacion),
            isCompleted = true,
            isLast = false
        )

        // Hito 2: Toma de oferta
        val fueTomada = oferta.compradorId != null
        TimelineItem(
            title = if (fueTomada) "Oferta Tomada" else "Esperando Comprador",
            subtitle = if (fueTomada) {
                "Tomada por: ${oferta.perfilComprador?.nombreCompleto ?: "Tú"}"
            } else {
                "Aún no ha sido aceptada por ningún usuario"
            },
            date = if (fueTomada && oferta.estado != "ACTIVA") formatTimelineDate(oferta.fechaIntercambio ?: "") else if (fueTomada) "En proceso" else "Pendiente",
            isCompleted = fueTomada,
            isLast = oferta.estado == "ACTIVA" && !fueTomada
        )

        // Hito 3: Finalización o Cancelación
        if (oferta.estado != "ACTIVA") {
            val esCancelada = oferta.estado == "CANCELADA"
            TimelineItem(
                title = if (esCancelada) "Operación Cancelada" else "Operación Completada",
                subtitle = when {
                    esCancelada && esModoOfertante -> "Cancelaste la oferta"
                    esCancelada -> "La oferta fue cancelada por el vendedor"
                    else -> "El intercambio se realizó con éxito"
                },
                date = formatTimelineDate(oferta.fechaIntercambio ?: ""),
                isCompleted = true,
                isLast = true,
                pointColor = if (esCancelada) BinanceError else BinanceGreen
            )
        } else if (fueTomada) {
            // Si fue tomada pero sigue activa (esperando confirmación final)
            TimelineItem(
                title = "En Proceso de Pago",
                subtitle = "Esperando la confirmación de la transferencia",
                date = "En espera...",
                isCompleted = false,
                isLast = true
            )
        }
    }
}

@Composable
fun TimelineItem(
    title: String,
    subtitle: String,
    date: String,
    isCompleted: Boolean,
    isLast: Boolean,
    pointColor: androidx.compose.ui.graphics.Color = BinanceGreen
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            // El punto del hito
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(
                        if (isCompleted) pointColor else BinanceTextSecondary.copy(alpha = 0.3f),
                        CircleShape
                    )
            )
            
            // La línea vertical
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(if (isCompleted) pointColor else BinanceTextSecondary.copy(alpha = 0.2f))
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(start = 12.dp, bottom = 32.dp)
                .weight(1f)
        ) {
            Text(
                text = title,
                color = if (isCompleted) BinanceTextPrimary else BinanceTextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = BinanceTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        Text(
            text = date,
            color = BinanceTextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

fun formatTimelineDate(dateStr: String, showTime: Boolean = true): String {
    if (dateStr.isBlank()) return "---"
    return try {
        // Supabase ISO format: 2024-05-18T16:30:00.123456+00:00
        val hasTime = dateStr.contains("T")
        val cleanDate = if (hasTime) dateStr.take(19) else dateStr.take(10)
        
        val inputPattern = if (hasTime) "yyyy-MM-dd'T'HH:mm:ss" else "yyyy-MM-dd"
        // Si no tiene tiempo originalmente, no mostramos 00:00 aunque showTime sea true
        val outputPattern = if (hasTime && showTime) "dd/MM/yyyy HH:mm" else "dd/MM/yyyy"
        
        val input = SimpleDateFormat(inputPattern, Locale.getDefault())
        val output = SimpleDateFormat(outputPattern, Locale.getDefault())
        
        val date = input.parse(cleanDate)
        if (date != null) output.format(date) else dateStr
    } catch (e: Exception) {
        if (showTime) dateStr.take(16).replace("T", " ") else dateStr.take(10)
    }
}

// ==========================================================
// COMPONENTE VISUAL DE LA TARJETA ADAPTADO A TU PALETA
// ==========================================================
// ==========================================================
// COMPONENTE VISUAL DE LA TARJETA ADAPTADO A TU PALETA
// ==========================================================
@Composable
fun HorizontalOfertaCard(
    oferta: OfertaDb, 
    esModoOfertante: Boolean,
    onClick: () -> Unit = {},
    onCancelClick: () -> Unit = {}
) {
    val esActiva = oferta.estado == "ACTIVA"
    val esCompletada = oferta.estado == "COMPLETADA"
    val esCancelada = oferta.estado == "CANCELADA"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = BinanceSurface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // FILA SUPERIOR: Fecha y Estado de la publicación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val fechaFormateada = formatTimelineDate(oferta.fechaPublicacion, showTime = false)
                
                Text(text = "Operación: $fechaFormateada", color = BinanceTextSecondary, fontSize = 11.sp)

                val statusColor = when {
                    esActiva -> BinanceYellow
                    esCompletada -> BinanceGreen
                    else -> BinanceTextSecondary // Para CANCELADA
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = oferta.estado,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // FILA CENTRAL RE-ESTRUCTURADA: "Vendí" -> "Recibí"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bloque Izquierdo: Lo que diste (Entregaste / Pagaste)
                Column {
                    Text(text = if (esModoOfertante) "Entregaste" else "Pagaste", color = BinanceTextSecondary, fontSize = 11.sp)
                    Text(
                        text = if (esModoOfertante) oferta.textoVendi else oferta.textoPague,
                        color = BinanceTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = (if (esModoOfertante) oferta.monedaOrigen?.nombre else oferta.monedaDestino?.nombre) ?: "",
                        color = BinanceTextSecondary,
                        fontSize = 10.sp
                    )
                }

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = BinanceYellow,
                    modifier = Modifier.size(20.dp)
                )

                // Bloque Derecho: Lo que recibes
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Recibiste", color = BinanceTextSecondary, fontSize = 11.sp)
                    Text(
                        text = if (esModoOfertante) oferta.textoRecibi else oferta.textoRecibiComoComprador,
                        color = if (esCancelada) BinanceTextPrimary else BinanceSuccess,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = (if (esModoOfertante) oferta.monedaDestino?.nombre else oferta.monedaOrigen?.nombre) ?: "",
                        color = BinanceTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            // Tipo de Cambio
            val isoOrigen = oferta.monedaOrigen?.codigoIso ?: ""
            val isoDestino = oferta.monedaDestino?.codigoIso ?: ""
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BinanceBackground, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "Tipo de cambio: 1 $isoOrigen = ${String.format("%.4f", oferta.tasaCambio)} $isoDestino",
                    color = BinanceTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // SECCIÓN INFERIOR: Detalles de la Contraparte y Fecha Intercambio
            if (esCompletada) {
                HorizontalDivider(color = BinanceBackground, thickness = 1.dp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (esModoOfertante) "Comprador:" else "Vendedor:", color = BinanceTextSecondary, fontSize = 11.sp)
                        Text(
                            text = (if (esModoOfertante) oferta.perfilComprador?.nombreCompleto else oferta.perfilOfertante?.nombreCompleto) ?: "Desconocido",
                            color = BinanceYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    oferta.fechaIntercambio?.let { fechaInt ->
                        val fechaIntFormateada = formatTimelineDate(fechaInt)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Finalizado el:", color = BinanceTextSecondary, fontSize = 11.sp)
                            Text(
                                text = fechaIntFormateada,
                                color = BinanceTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Comentario (si existe)
            if (!oferta.comentario.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = "💬 ${oferta.comentario}",
                        color = BinanceTextSecondary,
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            // BOTÓN DE CANCELAR (Solo para modo ofertante y estado ACTIVA)
            if (esModoOfertante && esActiva) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onCancelClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BinanceError.copy(alpha = 0.1f),
                        contentColor = BinanceError
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancelar Oferta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
