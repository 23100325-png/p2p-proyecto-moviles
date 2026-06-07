package com.example.p2pmoviles.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.p2pmoviles.data.model.BitacoraEntry
import com.example.p2pmoviles.data.model.MovimientoAdmin
import com.example.p2pmoviles.data.model.PerfilAdmin
import com.example.p2pmoviles.data.model.ResumenOperaciones
import com.example.p2pmoviles.ui.theme.*
//hola
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel = viewModel()
) {
    val uiState by adminViewModel.uiState.collectAsState()
    val estaRefrescando by adminViewModel.estaRefrescando.collectAsState()
    val mensajeExito by adminViewModel.mensajeExito.collectAsState()
    val mensajeError by adminViewModel.mensajeError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var voucherUrlToShow by remember { mutableStateOf<String?>(null) }
    var verTodosUsuarios by remember { mutableStateOf(false) }

    LaunchedEffect(mensajeExito, mensajeError) {
        mensajeExito?.let {
            snackbarHostState.showSnackbar(it)
            adminViewModel.limpiarMensaje()
        }
        mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            adminViewModel.limpiarMensaje()
        }
    }

    // Estados para diálogos de gestión
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var usuarioSeleccionado by remember { mutableStateOf<PerfilAdmin?>(null) }
    var accionSeleccionada by remember { mutableStateOf<String?>(null) }
    var valorInput by remember { mutableStateOf("") }

    // Diálogo para Crear Usuario
    if (mostrarDialogoCrear) {
        var nombre by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var rolId by remember { mutableLongStateOf(1L) }

        Dialog(onDismissRequest = { mostrarDialogoCrear = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BinanceInputBackground),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BinanceTextSecondary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Crear Nuevo Usuario",
                        color = BinanceYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre Completo", color = BinanceTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BinanceTextPrimary,
                            unfocusedTextColor = BinanceTextPrimary,
                            focusedBorderColor = BinanceYellow,
                            unfocusedBorderColor = BinanceTextSecondary.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = BinanceTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BinanceTextPrimary,
                            unfocusedTextColor = BinanceTextPrimary,
                            focusedBorderColor = BinanceYellow,
                            unfocusedBorderColor = BinanceTextSecondary.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña", color = BinanceTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = BinanceTextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BinanceTextPrimary,
                            unfocusedTextColor = BinanceTextPrimary,
                            focusedBorderColor = BinanceYellow,
                            unfocusedBorderColor = BinanceTextSecondary.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    Column {
                        Text("Rol del Usuario", color = BinanceTextSecondary, fontSize = 14.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = rolId == 1L,
                                onClick = { rolId = 1L },
                                label = { Text("Usuario") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BinanceYellow.copy(alpha = 0.2f),
                                    selectedLabelColor = BinanceYellow
                                )
                            )
                            FilterChip(
                                selected = rolId == 2L,
                                onClick = { rolId = 2L },
                                label = { Text("Admin") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BinanceYellow.copy(alpha = 0.2f),
                                    selectedLabelColor = BinanceYellow
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (nombre.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                                adminViewModel.crearUsuarioManual(nombre, email, password, rolId)
                                mostrarDialogoCrear = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Crear Usuario", color = BinanceBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Diálogo para Editar/Roles (Acciones genéricas con input)
    accionSeleccionada?.let { accion ->
        val user = usuarioSeleccionado ?: return@let
        Dialog(onDismissRequest = { accionSeleccionada = null }) {
            Card(colors = CardDefaults.cardColors(containerColor = BinanceInputBackground), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if(accion == "ROLES") "Cambiar Rol" else "Editar Usuario", color = BinanceTextPrimary, fontWeight = FontWeight.Bold)
                    
                    if (accion == "ROLES") {
                        Text("Seleccione rol para ${user.nombreCompleto}", color = BinanceTextSecondary, fontSize = 14.sp)
                        Row {
                            Button(onClick = { adminViewModel.gestionarUsuario(user, "CAMBIAR_ROL", "1"); accionSeleccionada = null }, modifier = Modifier.weight(1f)) { Text("Usuario (1)") }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { adminViewModel.gestionarUsuario(user, "CAMBIAR_ROL", "2"); accionSeleccionada = null }, modifier = Modifier.weight(1f)) { Text("Admin (2)") }
                        }
                    } else if (accion == "EDITAR") {
                        TextField(value = valorInput, onValueChange = { valorInput = it }, label = { Text("Nuevo Nombre") }, modifier = Modifier.fillMaxWidth())
                        Button(onClick = { adminViewModel.gestionarUsuario(user, "EDITAR_NOMBRE", valorInput); accionSeleccionada = null }, modifier = Modifier.fillMaxWidth()) { Text("Guardar") }
                    }
                }
            }
        }
    }

    // Diálogo para ver Voucher
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
                        Text("Comprobante de Pago", color = BinanceTextPrimary, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { voucherUrlToShow = null }) {
                            Icon(Icons.Default.Close, "Cerrar", tint = BinanceError)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = url,
                        contentDescription = "Voucher",
                        modifier = Modifier.fillMaxWidth().height(350.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Panel Administrativo",
                        color = BinanceTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BinanceBackground)
            )
        },
        bottomBar = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().background(BinanceInputBackground).padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Panel protegido. Acceso con permisos ADM.", color = BinanceTextSecondary, fontSize = 11.sp)
                    Text("Configurar Roles", color = BinanceYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { })
                }
                NavigationBar(containerColor = BinanceBackground, tonalElevation = 0.dp) {
                    NavigationBarItem(icon = { Icon(Icons.Default.Home, null) }, selected = true, onClick = {}, colors = NavigationBarItemDefaults.colors(selectedIconColor = BinanceYellow, unselectedIconColor = BinanceTextSecondary, indicatorColor = Color.Transparent))
                    NavigationBarItem(icon = { Icon(Icons.Default.AccountBalanceWallet, null) }, selected = false, onClick = {}, colors = NavigationBarItemDefaults.colors(unselectedIconColor = BinanceTextSecondary))
                    NavigationBarItem(icon = { Icon(Icons.Default.Language, null) }, selected = false, onClick = {}, colors = NavigationBarItemDefaults.colors(unselectedIconColor = BinanceTextSecondary))
                    NavigationBarItem(icon = { Icon(Icons.Default.Chat, null) }, selected = false, onClick = {}, colors = NavigationBarItemDefaults.colors(unselectedIconColor = BinanceTextSecondary))
                    NavigationBarItem(icon = { Box(modifier = Modifier.size(24.dp).background(BinanceError, CircleShape)) }, selected = false, onClick = {}, colors = NavigationBarItemDefaults.colors(unselectedIconColor = BinanceTextSecondary))
                }
            }
        },
        containerColor = BinanceBackground
    ) { paddingValues ->
        val scrollState = rememberScrollState()

        when (val state = uiState) {
            is AdminUIState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = BinanceYellow) }
            is AdminUIState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(state.msg, color = BinanceError) }
            is AdminUIState.Success -> {
                PullToRefreshBox(
                    isRefreshing = estaRefrescando,
                    onRefresh = { adminViewModel.obtenerDatosCompletos() },
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        SeccionIndicadores(state.usuariosActivos, state.operacionesPendientes)
                        SeccionGestionUsuarios(
                            usuarios = state.listaUsuarios,
                            verTodos = verTodosUsuarios,
                            onVerTodosToggle = { verTodosUsuarios = !verTodosUsuarios },
                            onAccion = { user, action ->
                                when (action) {
                                    "EDITAR" -> {
                                        usuarioSeleccionado = user
                                        valorInput = user.nombreCompleto
                                        accionSeleccionada = "EDITAR"
                                    }
                                    "ROLES" -> {
                                        usuarioSeleccionado = user
                                        accionSeleccionada = "ROLES"
                                    }
                                    "BLOQUEAR", "DESBLOQUEAR", "ELIMINAR" -> {
                                        adminViewModel.gestionarUsuario(user, action)
                                    }
                                }
                            },
                            onCrearClick = { mostrarDialogoCrear = true }
                        )
                        SeccionBitacora(state.bitacora)
                        SeccionResumenOperaciones(state.resumen)
                        
                        if (state.listaMovimientos.isNotEmpty()) {
                            Text("Solicitudes Pendientes", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            state.listaMovimientos.forEach { mov ->
                                RowTransaccionItem(
                                    movimiento = mov,
                                    onVerVoucher = { url -> voucherUrlToShow = url },
                                    onAprobar = { adminViewModel.procesarSolicitud(mov, true) },
                                    onRechazar = { adminViewModel.procesarSolicitud(mov, false) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeccionIndicadores(activos: Int, pendientes: Int) {
    Column {
        Text("Indicadores Clave", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Card(colors = CardDefaults.cardColors(containerColor = BinanceInputBackground), shape = RoundedCornerShape(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Usuarios Activos", color = BinanceTextSecondary, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(formatNumber(activos), color = BinanceTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowUpward, null, tint = BinanceSuccess, modifier = Modifier.size(16.dp))
                    }
                }
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(BinanceTextSecondary.copy(alpha = 0.2f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Estado del Sistema", color = BinanceTextSecondary, fontSize = 12.sp)
                    Box(modifier = Modifier.size(24.dp).background(BinanceSuccess.copy(alpha = 0.2f), CircleShape).padding(4.dp)) {
                        Box(modifier = Modifier.fillMaxSize().background(BinanceSuccess, CircleShape))
                    }
                }
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(BinanceTextSecondary.copy(alpha = 0.2f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Operaciones Pendientes", color = BinanceTextSecondary, fontSize = 12.sp)
                    Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(BinanceYellow).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text(pendientes.toString(), color = BinanceBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SeccionGestionUsuarios(
    usuarios: List<PerfilAdmin>,
    verTodos: Boolean,
    onVerTodosToggle: () -> Unit,
    onCrearClick: () -> Unit,
    onAccion: (PerfilAdmin, String) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Gestión de Usuarios", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row {
                Text("+ Crear Usuario", color = BinanceYellow, fontSize = 12.sp, modifier = Modifier.clickable { onCrearClick() })
                Spacer(modifier = Modifier.width(12.dp))
                Text(if (verTodos) "Ver menos" else "Ver Todos", color = BinanceYellow, fontSize = 12.sp, modifier = Modifier.clickable { onVerTodosToggle() })
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        if (usuarios.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("No hay usuarios disponibles.", color = BinanceTextSecondary, fontSize = 14.sp)
            }
        } else {
            val listaAMostrar = if (verTodos) usuarios else usuarios.take(3)
            listaAMostrar.forEach { user ->
                Card(colors = CardDefaults.cardColors(containerColor = BinanceInputBackground), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(BinanceSuccess.copy(alpha = 0.2f), CircleShape), Alignment.Center) { Icon(Icons.Default.Person, null, tint = BinanceSuccess) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.nombreCompleto, color = BinanceTextPrimary, fontWeight = FontWeight.Bold)
                            Text(user.estado, color = if (user.estado == "Activo") BinanceSuccess else BinanceError, fontSize = 12.sp)
                        }
                        if (user.estado == "Bloqueado") {
                            Button(onClick = { onAccion(user, "DESBLOQUEAR") }, colors = ButtonDefaults.buttonColors(containerColor = BinanceBackground), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                                Text("Desbloquear", color = BinanceTextPrimary, fontSize = 10.sp)
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                BotonAccionMini("Editar", BinanceSuccess) { onAccion(user, "EDITAR") }
                                BotonAccionMini("Bloquear", BinanceError) { onAccion(user, "BLOQUEAR") }
                                BotonAccionMini("Eliminar", BinanceError) { onAccion(user, "ELIMINAR") }
                                BotonAccionMini("Roles", BinanceYellow) { onAccion(user, "ROLES") }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun BotonAccionMini(text: String, color: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent, border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)), shape = RoundedCornerShape(4.dp)) {
        Text(text, color = color, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
    }
}

@Composable
fun SeccionBitacora(entries: List<BitacoraEntry>) {
    Column {
        Text("Bitácora de Auditoría", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("No hay registros en la bitácora.", color = BinanceTextSecondary, fontSize = 14.sp)
            }
        } else {
            entries.forEach { entry ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).background(BinanceInputBackground, CircleShape), Alignment.Center) {
                        val icon = when {
                            entry.accion.contains("Bloqueo") -> Icons.Default.Block
                            entry.accion.contains("Desbloqueo") -> Icons.Default.LockOpen
                            entry.accion.contains("Eliminación") -> Icons.Default.Delete
                            entry.accion.contains("Creación") -> Icons.Default.PersonAdd
                            entry.accion.contains("Rol") -> Icons.Default.AdminPanelSettings
                            entry.accion.contains("Edición") -> Icons.Default.Edit
                            entry.accion.contains("Fondos") -> Icons.Default.AccountBalanceWallet
                            else -> Icons.Default.History
                        }
                        val tint = when {
                            entry.accion.contains("Bloqueo") || entry.accion.contains("Eliminación") || entry.accion.contains("Rechazo") -> BinanceError
                            entry.accion.contains("Aprobación") || entry.accion.contains("Creación") || entry.accion.contains("Desbloqueo") -> BinanceSuccess
                            else -> BinanceYellow
                        }
                        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.accion, color = BinanceTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(entry.descripcion, color = BinanceTextSecondary, fontSize = 12.sp)
                        Text("Responsable: ${entry.responsable}", color = BinanceTextSecondary.copy(alpha = 0.7f), fontSize = 10.sp)
                    }
                    val hora = try { entry.fechaHora.substring(11, 16) } catch (e: Exception) { "" }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(entry.fechaHora.take(10), color = BinanceTextSecondary, fontSize = 10.sp)
                        Text(hora, color = BinanceTextSecondary, fontSize = 10.sp)
                    }
                }
                HorizontalDivider(color = BinanceTextSecondary.copy(alpha = 0.1f))
            }
        }
    }
}

@Composable
fun SeccionResumenOperaciones(resumen: ResumenOperaciones) {
    Column {
        Text("Resumen de Operaciones (Hoy)", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = BinanceInputBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddCircle, null, tint = BinanceSuccess, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Total Recargas Aprobadas", color = BinanceTextSecondary, fontSize = 14.sp)
                    }
                    Text("${formatCurrency(resumen.totalComprasHoy)} USD", color = BinanceSuccess, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(color = BinanceTextSecondary.copy(alpha = 0.1f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RemoveCircle, null, tint = BinanceError, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Total Retiros Aprobados", color = BinanceTextSecondary, fontSize = 14.sp)
                    }
                    Text("${formatCurrency(resumen.totalVentasHoy)} USD", color = BinanceError, fontWeight = FontWeight.Bold)
                }
                
                if (resumen.totalComprasHoy == 0.0 && resumen.totalVentasHoy == 0.0) {
                    Text(
                        "No se registran operaciones aprobadas hoy.",
                        color = BinanceTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
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
    Card(colors = CardDefaults.cardColors(containerColor = BinanceInputBackground), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.8f)) {
                AsyncImage(model = movimiento.monedas?.rutaBandera, contentDescription = null, modifier = Modifier.size(28.dp).clip(CircleShape).background(BinanceBackground), contentScale = ContentScale.Crop)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(movimiento.monedas?.nombre ?: "Desconocida", color = BinanceTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(movimiento.monedas?.codigoIso ?: "---", color = BinanceTextSecondary, fontSize = 12.sp)
                }
            }
            Column(modifier = Modifier.weight(1.5f)) {
                val esRecarga = movimiento.tipoMovimiento == "RECARGA"
                Text(text = "${movimiento.monedas?.simbolo ?: ""} ${String.format("%.2f", movimiento.monto)}", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = if (esRecarga) "● Recarga" else "● Retiro", color = if (esRecarga) BinanceSuccess else BinanceError, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.Center) {
                if (movimiento.tipoMovimiento == "RECARGA" && !movimiento.rutaVoucher.isNullOrEmpty()) {
                    Row(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(BinanceBackground).clickable { onVerVoucher(movimiento.rutaVoucher) }.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility, null, tint = BinanceYellow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver", color = BinanceYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else { Text("--", color = BinanceTextSecondary, fontSize = 12.sp) }
            }
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onRechazar, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Close, null, tint = BinanceError, modifier = Modifier.size(20.dp)) }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onAprobar, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Check, null, tint = BinanceSuccess, modifier = Modifier.size(20.dp)) }
            }
        }
    }
}

fun formatCurrency(amount: Double): String {
    return String.format("%.2f", amount)
}

fun formatNumber(num: Int): String {
    return String.format("%,d", num).replace(',', '.')
}
