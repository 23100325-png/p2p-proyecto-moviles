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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.p2pmoviles.data.model.MovimientoAdmin
import com.example.p2pmoviles.presentation.admin.components.SeccionGestionUsuarios
import com.example.p2pmoviles.presentation.admin.components.SeccionHistorialTransacciones
import com.example.p2pmoviles.presentation.admin.dialogs.DialogoConfirmacionAccion
import com.example.p2pmoviles.presentation.admin.dialogs.DialogoDetalleTransaccion
import com.example.p2pmoviles.presentation.auth.AuthViewModel
import com.example.p2pmoviles.ui.theme.BinanceInputBackground
import com.example.p2pmoviles.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    authViewModel: AuthViewModel? = null,
    onLogoutSuccess: (() -> Unit)? = null,
    adminViewModel: AdminViewModel = viewModel(),
    adminUsersViewModel: AdminUsersViewModel = viewModel(),
    adminTransactionsViewModel: AdminTransactionsViewModel = viewModel()
) {
    val uiState by adminViewModel.uiState.collectAsState()
    val estaRefrescando by adminViewModel.estaRefrescando.collectAsState()
    
    val usersUiState by adminUsersViewModel.uiState.collectAsState()
    val usersRefrescando by adminUsersViewModel.estaRefrescando.collectAsState()
    val mensajeOperacion by adminUsersViewModel.mensajeOperacion.collectAsState()
    val usuarioActualId by adminUsersViewModel.usuarioActualId.collectAsState()

    val transactionsUiState by adminTransactionsViewModel.uiState.collectAsState()
    val transactionsRefrescando by adminTransactionsViewModel.estaRefrescando.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var voucherUrlToShow by remember { mutableStateOf<String?>(null) }
    var usuarioSeleccionadoAccion by remember { mutableStateOf<Pair<com.example.p2pmoviles.data.model.PerfilAdmin, String>?>(null) }
    var transaccionSeleccionada by remember { mutableStateOf<MovimientoAprobado?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

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

    // Diálogo de confirmación para logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión", color = BinanceTextPrimary) },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?", color = BinanceTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel?.cerrarSesion {
                            onLogoutSuccess?.invoke()
                        }
                        showLogoutDialog = false
                    }
                ) {
                    Text("Cerrar sesión", color = BinanceError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = BinanceTextSecondary)
                }
            },
            containerColor = BinanceSurface
        )
    }

    // Diálogo de detalle de transacción
    transaccionSeleccionada?.let { movimiento ->
        DialogoDetalleTransaccion(
            movimiento = movimiento,
            onDismiss = { transaccionSeleccionada = null }
        )
    }

    // Diálogo de confirmación para Bloquear/Desbloquear Usuario
    usuarioSeleccionadoAccion?.let { (usuario, accion) ->
        if (accion == "BLOQUEAR" || accion == "DESBLOQUEAR") {
            DialogoConfirmacionAccion(
                usuario = usuario,
                accion = accion,
                usuarioActualId = usuarioActualId,
                onDismiss = { usuarioSeleccionadoAccion = null },
                onConfirm = {
                    val nuevoEstado = accion == "DESBLOQUEAR"
                    adminUsersViewModel.toggleActivoUsuario(usuario, nuevoEstado)
                    usuarioSeleccionadoAccion = null
                }
            )
        } else if (accion == "EDITAR" || accion == "ROLES") {
            com.example.p2pmoviles.presentation.admin.dialogs.DialogoGestionUsuario(
                usuario = usuario,
                accion = accion,
                onDismiss = { usuarioSeleccionadoAccion = null },
                onConfirm = { tipoOperacion, valor ->
                    if (tipoOperacion == "EDITAR_NOMBRE") {
                        adminUsersViewModel.editarNombreUsuario(usuario, valor)
                    } else if (tipoOperacion == "CAMBIAR_ROL") {
                        adminUsersViewModel.cambiarRolUsuario(usuario, valor.toLong())
                    }
                    usuarioSeleccionadoAccion = null
                }
            )
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
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = BinanceError
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
                val titulos = listOf("Movimientos", "Gestión de Usuarios", "Historial")
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

            // Contenido según la pestaña seleccionada
            when (selectedTab) {
                0 -> MovimientosTab(
                    uiState = uiState,
                    estaRefrescando = estaRefrescando,
                    adminViewModel = adminViewModel,
                    onVerVoucher = { voucherUrlToShow = it }
                )
                1 -> GestionUsuariosTab(
                    usersUiState = usersUiState,
                    usersRefrescando = usersRefrescando,
                    mensajeOperacion = mensajeOperacion,
                    usuarioActualId = usuarioActualId,
                    adminUsersViewModel = adminUsersViewModel,
                    onBloquearClick = { usuario ->
                        usuarioSeleccionadoAccion = usuario to "BLOQUEAR"
                    },
                    onDesbloquearClick = { usuario ->
                        usuarioSeleccionadoAccion = usuario to "DESBLOQUEAR"
                    },
                    onEditarClick = { usuario ->
                        usuarioSeleccionadoAccion = usuario to "EDITAR"
                    },
                    onCambiarRolClick = { usuario ->
                        usuarioSeleccionadoAccion = usuario to "ROLES"
                    },
                    onLimpiarMensaje = { adminUsersViewModel.limpiarMensaje() }
                )
                2 -> HistorialTransaccionesTab(
                    transactionsUiState = transactionsUiState,
                    transactionsRefrescando = transactionsRefrescando,
                    adminTransactionsViewModel = adminTransactionsViewModel,
                    onTransaccionClick = { transaccion ->
                        transaccionSeleccionada = transaccion
                    }
                )
            }
        }
    }
}

@Composable
fun MovimientosTab(
    uiState: AdminUIState,
    estaRefrescando: Boolean,
    adminViewModel: AdminViewModel,
    onVerVoucher: (String) -> Unit
) {
    val movimientosCompletos = (uiState as? AdminUIState.Success)?.lista ?: emptyList()
    val listaFiltrada = movimientosCompletos

    Text(
        text = "SOLICITUDES PENDIENTES",
        color = BinanceTextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))

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
            PullToRefreshBox(
                isRefreshing = estaRefrescando,
                onRefresh = { adminViewModel.obtenerMovimientos() },
                modifier = Modifier.fillMaxSize()
            ) {
                if (listaFiltrada.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No hay transacciones pendientes.",
                            color = BinanceTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(listaFiltrada) { mov ->
                            RowTransaccionItem(
                                movimiento = mov,
                                onVerVoucher = { url -> onVerVoucher(url) },
                                onAprobar = {
                                    adminViewModel.procesarSolicitud(mov, aprobar = true)
                                },
                                onRechazar = {
                                    adminViewModel.procesarSolicitud(mov, aprobar = false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GestionUsuariosTab(
    usersUiState: AdminUsersUIState,
    usersRefrescando: Boolean,
    mensajeOperacion: String?,
    usuarioActualId: String,
    adminUsersViewModel: AdminUsersViewModel,
    onBloquearClick: (com.example.p2pmoviles.data.model.PerfilAdmin) -> Unit,
    onDesbloquearClick: (com.example.p2pmoviles.data.model.PerfilAdmin) -> Unit,
    onEditarClick: (com.example.p2pmoviles.data.model.PerfilAdmin) -> Unit,
    onCambiarRolClick: (com.example.p2pmoviles.data.model.PerfilAdmin) -> Unit,
    onLimpiarMensaje: () -> Unit
) {
    when (val state = usersUiState) {
        is AdminUsersUIState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BinanceYellow)
            }
        }

        is AdminUsersUIState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.msg, color = BinanceError, textAlign = TextAlign.Center)
            }
        }

        is AdminUsersUIState.Success -> {
            PullToRefreshBox(
                isRefreshing = usersRefrescando,
                onRefresh = { adminUsersViewModel.obtenerTodosUsuarios() },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Mostrar banner de operación exitosa
                    mensajeOperacion?.let {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BinanceSuccess.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable { onLimpiarMensaje() }
                        ) {
                            Text(
                                it,
                                color = BinanceSuccess,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    SeccionGestionUsuarios(
                        usuarios = state.usuarios,
                        usuarioActualId = usuarioActualId,
                        onBloquearClick = onBloquearClick,
                        onDesbloquearClick = onDesbloquearClick,
                        onEditarClick = onEditarClick,
                        onCambiarRolClick = onCambiarRolClick,
                        onSearch = { query -> adminUsersViewModel.buscarUsuario(query) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistorialTransaccionesTab(
    transactionsUiState: AdminTransactionsUIState,
    transactionsRefrescando: Boolean,
    adminTransactionsViewModel: AdminTransactionsViewModel,
    onTransaccionClick: (MovimientoAprobado) -> Unit
) {
    when (val state = transactionsUiState) {
        is AdminTransactionsUIState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BinanceYellow)
            }
        }

        is AdminTransactionsUIState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.msg, color = BinanceError, textAlign = TextAlign.Center)
            }
        }

        is AdminTransactionsUIState.Success -> {
            PullToRefreshBox(
                isRefreshing = transactionsRefrescando,
                onRefresh = { adminTransactionsViewModel.obtenerTransacciones() },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    SeccionHistorialTransacciones(
                        movimientos = state.movimientos,
                        onMovimientoClick = onTransaccionClick,
                        onSearch = { query -> adminTransactionsViewModel.buscarTransaccion(query) }
                    )
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