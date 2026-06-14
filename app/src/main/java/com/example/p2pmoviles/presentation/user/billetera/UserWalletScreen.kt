package com.example.p2pmoviles.presentation.user.billetera

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.p2pmoviles.data.model.BilleteraUsuario
import com.example.p2pmoviles.data.model.MonedaInfo
import com.example.p2pmoviles.presentation.user.DropdownBancosCustom
import com.example.p2pmoviles.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserWalletScreen(
    userId: String,
    onBackClick: () -> Unit = {},
    viewModel: UserWalletViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var mostrarDialogRecarga by remember { mutableStateOf(false) }
    var mostrarDialogRetiro by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope() // Necesario para lanzar el snackbar de forma asíncrona

    val estaRefrescando by viewModel.estaRefrescando.collectAsState()

    LaunchedEffect(userId) {
        viewModel.inicializarUsuario(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Billetera P2P", color = BinanceTextPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BinanceBackground)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, // 🟢 NUEVO: Contenedor de mensajes
        containerColor = BinanceBackground
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {

            // 1. BOTONES SUPERIORES (Estilo Binance)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { mostrarDialogRecarga = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceSuccess),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.5f).height(48.dp)
                ) {
                    Text("Recargar", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { mostrarDialogRetiro = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceError),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.5f).height(48.dp)
                ) {
                    Text("Retirar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("MIS FONDOS DISPONIBLES Y BLOQUEADOS", color = BinanceTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            // 2. LISTA DE BILLETERAS Y MONEDAS
            when (val state = uiState) {
                is UserWalletState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BinanceYellow)
                    }
                }
                is UserWalletState.Error -> {
                    Text(state.msg, color = BinanceError, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
                is UserWalletState.Success -> {
                    val monedasGlobales by viewModel.listaMonedasGlobales.collectAsState()
                    PullToRefreshBox(
                        isRefreshing = estaRefrescando,
                        onRefresh = {
                            // Esto se ejecuta cuando el usuario jala la pantalla hacia abajo
                            viewModel.obtenerBilleteras()
                        },
                        modifier = Modifier.fillMaxSize ().weight(1f) // Ocupa el espacio restante debajo de los botones
                    ) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.billeteras) { billetera ->
                                CardBilleteraUsuarioItem(billetera)
                            }
                        }
                        // Inyección de ventanas flotantes condicionales
                        if (mostrarDialogRecarga) {
                            DialogRecarga(
                                monedasDisponibles = monedasGlobales,
                                onDismiss = { mostrarDialogRecarga = false },
                                onConfirm = { idMoneda, monto, uri ->
                                    val bytes = uri?.let {
                                        context.contentResolver.openInputStream(it)?.readBytes()
                                    }

                                    // 🟢 Modificado: Ahora le pasamos las funciones para los mensajes
                                    viewModel.realizarRecarga(
                                        monedaId = idMoneda,
                                        monto = monto,
                                        imageUri = uri,
                                        byteArray = bytes,
                                        onError = { mensajeError ->
                                            // Lanza el mensaje flotante en caso de error
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    mensajeError
                                                )
                                            }
                                        },
                                        onSuccess = { mensajeExito ->
                                            // Lanza el mensaje flotante en caso de éxito
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    mensajeExito
                                                )
                                            }
                                        }
                                    )
                                    mostrarDialogRecarga = false
                                }
                            )
                        }

                        if (mostrarDialogRetiro) {
                            DialogRetiro(
                                billeteras = state.billeteras,
                                viewModel = viewModel,
                                onDismiss = { mostrarDialogRetiro = false },
                                onConfirm = { idMoneda, monto, idCuentaBancaria ->
                                    viewModel.realizarRetiro(
                                        monedaId = idMoneda,
                                        monto = monto,
                                        cuentaBancariaId = idCuentaBancaria, // 🟢 Pasamos el ID del banco seleccionado
                                        onError = { mensajeError ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar(mensajeError)
                                            }
                                        },
                                        onSuccess = {
                                            scope.launch { snackbarHostState.showSnackbar("¡Solicitud de retiro enviada! Tus fondos han sido reservados.") }
                                            mostrarDialogRetiro = false
                                        }
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

@Composable
fun CardBilleteraUsuarioItem(billetera: BilleteraUsuario) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BinanceInputBackground),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = billetera.monedas?.rutaBandera,
                    contentDescription = "Bandera",
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(BinanceBackground),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(billetera.monedas?.nombre ?: "Divisa", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(billetera.monedas?.codigoIso ?: "---", color = BinanceTextSecondary, fontSize = 12.sp)
                }
            }

            // Muestra Dinámica del Balance Financiero
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Disp: ${billetera.monedas?.simbolo ?: ""} ${String.format("%.2f", billetera.saldoDisponible)}",
                    color = BinanceSuccess,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Bloq: ${billetera.monedas?.simbolo ?: ""} ${String.format("%.2f", billetera.saldoBloqueado)}",
                    color = BinanceTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// 🟢 VENTANA FLOTANTE 1: RECARGA (DEPÓSITOS)
@Composable
fun DialogRecarga(
    monedasDisponibles: List<MonedaInfo>, // 🟢 Ahora recibe el catálogo completo de monedas
    onDismiss: () -> Unit,
    onConfirm: (Long, Double, Uri?) -> Unit
) {
    var montoText by remember { mutableStateOf("") }
    // Dejamos seleccionada la primera moneda de la lista por defecto
    var monedaSeleccionada by remember { mutableStateOf(monedasDisponibles.firstOrNull()) }
    var menuExpandido by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = BinanceInputBackground), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Nueva Recarga", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Icon(Icons.Default.Close, "Cerrar", tint = BinanceTextSecondary, modifier = Modifier.clickable { onDismiss() })
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Menú Desplegable de Monedas
                Box(modifier = Modifier.fillMaxWidth().background(BinanceBackground, RoundedCornerShape(4.dp)).clickable { menuExpandido = true }.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        // Muestra el nombre de la moneda seleccionada
                        Text(monedaSeleccionada?.nombre ?: "Seleccionar Moneda", color = BinanceTextPrimary)
                        Icon(Icons.Default.ArrowDropDown, null, tint = BinanceYellow)
                    }
                    DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }, modifier = Modifier.background(BinanceInputBackground)) {
                        // 🟢 Recorremos el catálogo de monedas globales
                        monedasDisponibles.forEach { moneda ->
                            DropdownMenuItem(
                                text = { Text(moneda.nombre, color = BinanceTextPrimary) },
                                onClick = {
                                    monedaSeleccionada = moneda
                                    menuExpandido = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = montoText,
                    onValueChange = { montoText = it },
                    label = { Text("Monto a depositar", color = BinanceTextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = BinanceTextPrimary, unfocusedTextColor = BinanceTextPrimary, focusedBorderColor = BinanceYellow)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (imageUri == null) "🖼️ Seleccionar Comprobante" else "✓ Imagen Adjuntada", color = BinanceYellow)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val monto = montoText.toDoubleOrNull() ?: 0.0
                        // 🟢 Enviamos el id correcto de la moneda seleccionada
                        monedaSeleccionada?.id?.let { onConfirm(it, monto, imageUri) }
                    },
                    enabled = montoText.isNotEmpty() && imageUri != null,
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceSuccess),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enviar Solicitud", color = Color.White)
                }
            }
        }
    }
}


// 🔴 VENTANA FLOTANTE 2: RETIROS CON SELECCIÓN DE BANCO
@Composable
fun DialogRetiro(
    billeteras: List<BilleteraUsuario>,
    viewModel: UserWalletViewModel, // Inyectamos el ViewModel para leer o filtrar las cuentas registradas
    onDismiss: () -> Unit,
    onConfirm: (Long, Double, Long) -> Unit // Ahora retorna: MonedaId, Monto, CuentaBancariaId
) {
    var montoText by remember { mutableStateOf("") }
    var billeteraSeleccionada by remember { mutableStateOf(billeteras.firstOrNull()) }
    var menuExpandidoMoneda by remember { mutableStateOf(false) }

    // Control del Modal secundario de Bancos
    var mostrarModalBancos by remember { mutableStateOf(false) }

    // Obtenemos las cuentas bancarias del usuario directamente desde el estado del ViewModel
    // Nota: Asegúrate de tener expuesto un StateFlow con las cuentas del usuario en tu UserWalletViewModel
    val todasLasCuentas by viewModel.cuentasBancariasUsuario.collectAsState(initial = emptyList())

    // Filtramos en tiempo real para que solo aparezcan cuentas que coincidan con la moneda elegida
    val bancosFiltrados = todasLasCuentas.filter { it.monedaId == billeteraSeleccionada?.monedaId }
    var cuentaBancariaSeleccionada by remember { mutableStateOf<com.example.p2pmoviles.data.model.CuentaBancaria?>(null) }

    // Reiniciar banco seleccionado si el usuario cambia de moneda para evitar inconsistencias
    LaunchedEffect(billeteraSeleccionada) {
        cuentaBancariaSeleccionada = null
    }

    val disponible = billeteraSeleccionada?.saldoDisponible ?: 0.0
    val montoIngresado = montoText.toDoubleOrNull() ?: 0.0
    val esMontoValido = montoIngresado in 0.01..disponible

    // El botón final solo se activa si el monto es válido Y seleccionó un banco receptor
    val formularioListo = esMontoValido && cuentaBancariaSeleccionada != null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BinanceInputBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Solicitar Retiro", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Icon(Icons.Default.Close, "Cerrar", tint = BinanceTextSecondary, modifier = Modifier.clickable { onDismiss() })
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Moneda
                Text(text = "Moneda a retirar", color = BinanceTextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth().background(BinanceBackground, RoundedCornerShape(4.dp)).clickable { menuExpandidoMoneda = true }.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(billeteraSeleccionada?.monedas?.nombre ?: "Seleccionar Moneda", color = BinanceTextPrimary)
                        Icon(Icons.Default.ArrowDropDown, null, tint = BinanceYellow)
                    }
                    DropdownMenu(expanded = menuExpandidoMoneda, onDismissRequest = { menuExpandidoMoneda = false }, modifier = Modifier.background(BinanceInputBackground)) {
                        billeteras.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b.monedas?.nombre ?: "", color = BinanceTextPrimary) },
                                onClick = { billeteraSeleccionada = b; menuExpandidoMoneda = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Campo informativo de Saldo Disponible
                Text(
                    text = "Disponible para retirar: ${billeteraSeleccionada?.monedas?.simbolo ?: ""} ${String.format("%.2f", disponible)}",
                    color = BinanceYellow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Entrada del Monto numérico
                OutlinedTextField(
                    value = montoText,
                    onValueChange = { montoText = it },
                    label = { Text("Monto a retirar", color = BinanceTextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !esMontoValido && montoText.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = BinanceTextPrimary, unfocusedTextColor = BinanceTextPrimary, focusedBorderColor = BinanceYellow)
                )

                if (!esMontoValido && montoText.isNotEmpty()) {
                    Text("El monto excede tu saldo disponible.", color = BinanceError, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🟢 INTEGRACIÓN DEL DROPDOWN DE BANCOS CUSTOM
                DropdownBancosCustom(
                    bancoSeleccionado = cuentaBancariaSeleccionada?.banco,
                    monedaCodigo = billeteraSeleccionada?.monedas?.codigoIso,
                    monedaNombre = billeteraSeleccionada?.monedas?.nombre,
                    onDesplegarClick = { mostrarModalBancos = true }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de Confirmación
                Button(
                    onClick = {
                        if (billeteraSeleccionada != null && cuentaBancariaSeleccionada != null) {
                            onConfirm(billeteraSeleccionada!!.monedaId, montoIngresado, cuentaBancariaSeleccionada!!.id)
                        }
                    },
                    enabled = formularioListo,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BinanceError,
                        disabledContainerColor = BinanceTextSecondary.copy(alpha = 0.3f),
                        contentColor = Color.White,
                        disabledContentColor = BinanceTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirmar Retiro", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Modal Flotante Secundario para listar las cuentas bancarias filtradas por moneda
    if (mostrarModalBancos) {
        AlertDialog(
            onDismissRequest = { mostrarModalBancos = false },
            confirmButton = {},
            containerColor = BinanceInputBackground,
            title = { Text("Selecciona tu cuenta bancaria", color = BinanceTextPrimary, fontSize = 16.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (bancosFiltrados.isEmpty()) {
                        Text(
                            text = "No tienes cuentas bancarias registradas en ${billeteraSeleccionada?.monedas?.codigoIso ?: ""}. Agrégalas en tu configuración de perfil.",
                            color = BinanceError,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        bancosFiltrados.forEach { cuenta ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        cuentaBancariaSeleccionada = cuenta
                                        mostrarModalBancos = false
                                    }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(text = cuenta.banco, color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Nº: ${cuenta.numeroCuenta}", color = BinanceTextSecondary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Divider(color = BinanceTextSecondary.copy(alpha = 0.1f))
                            }
                        }
                    }
                }
            }
        )
    }
}