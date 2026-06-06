package com.example.p2pmoviles.presentation.user

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
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = BinanceTextPrimary
                        )
                    }
                },
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
                                onDismiss = { mostrarDialogRetiro = false },
                                onConfirm = { idMoneda, monto ->
                                    viewModel.realizarRetiro(
                                        monedaId = idMoneda,
                                        monto = monto,
                                        onError = { mensajeError ->
                                            // Si el trigger de saldo insuficiente salta, se muestra aquí el error en rojo
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    mensajeError
                                                )
                                            }
                                        },
                                        onSuccess = {
                                            // Si pasa el trigger, avisa del éxito
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

// 🔴 VENTANA FLOTANTE 2: RETIROS
@Composable
fun DialogRetiro(
    billeteras: List<BilleteraUsuario>,
    onDismiss: () -> Unit,
    onConfirm: (Long, Double) -> Unit
) {
    var montoText by remember { mutableStateOf("") }
    var billeteraSeleccionada by remember { mutableStateOf(billeteras.firstOrNull()) }
    var menuExpandido by remember { mutableStateOf(false) }

    val disponible = billeteraSeleccionada?.saldoDisponible ?: 0.0
    val montoIngresado = montoText.toDoubleOrNull() ?: 0.0
    val esMontoValido = montoIngresado in 0.01..disponible

    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = BinanceInputBackground), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Solicitar Retiro", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Icon(Icons.Default.Close, "Cerrar", tint = BinanceTextSecondary, modifier = Modifier.clickable { onDismiss() })
                }
                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth().background(BinanceBackground, RoundedCornerShape(4.dp)).clickable { menuExpandido = true }.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(billeteraSeleccionada?.monedas?.nombre ?: "Seleccionar Moneda", color = BinanceTextPrimary)
                        Icon(Icons.Default.ArrowDropDown, null, tint = BinanceYellow)
                    }
                    DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }, modifier = Modifier.background(BinanceInputBackground)) {
                        billeteras.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b.monedas?.nombre ?: "", color = BinanceTextPrimary) },
                                onClick = { billeteraSeleccionada = b; menuExpandido = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                // Campo informativo de Saldo Disponible Exclusivo
                Text(
                    text = "Disponible para retirar: ${billeteraSeleccionada?.monedas?.simbolo ?: ""} ${String.format("%.2f", disponible)}",
                    color = BinanceYellow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = montoText,
                    onValueChange = { montoText = it },
                    label = { Text("Monto a retirar", color = BinanceTextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !esMontoValido && montoText.isNotEmpty(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = BinanceTextPrimary, unfocusedTextColor = BinanceTextPrimary, focusedBorderColor = BinanceYellow)
                )

                if (!esMontoValido && montoText.isNotEmpty()) {
                    Text("El monto excede tu saldo disponible.", color = BinanceError, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { onConfirm(billeteraSeleccionada!!.monedaId, montoIngresado) },
                    enabled = esMontoValido,
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceError),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirmar Retiro", color = Color.White)
                }
            }
        }
    }
}