package com.example.p2pmoviles.presentation.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.p2pmoviles.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicarOfertaScreen(
    usuarioLogueadoId: String,
    ofertasViewModel: OfertasViewModel = viewModel()
) {
    // Inicializamos los catálogos en el ViewModel con el ID del usuario
    LaunchedEffect(usuarioLogueadoId) {
        ofertasViewModel.inicializar(usuarioLogueadoId)
    }

    // --- Lectura de Estados del ViewModel ---
    val billeterasUsuario by ofertasViewModel.billeterasUsuario.collectAsState()
    val monedasGlobales by ofertasViewModel.monedasGlobales.collectAsState()

    val monedaTengoSelected by ofertasViewModel.monedaTengo.collectAsState()
    val monedaQuieroSelected by ofertasViewModel.monedaQuiero.collectAsState()

    val montoText by ofertasViewModel.montoOfertarText.collectAsState()
    val tipoCambioText by ofertasViewModel.tipoCambioText.collectAsState()
    val bancoSelected by ofertasViewModel.cuentaBancariaSeleccionada.collectAsState()
    val notasText by ofertasViewModel.notasAdicionalesText.collectAsState()
    val terminosCheck by ofertasViewModel.terminosAceptados.collectAsState()

    // --- Control de Diálogos Flotantes de Selección ---
    var mostrarDialogoTengo by remember { mutableStateOf(false) }
    var mostrarDialogoQuiero by remember { mutableStateOf(false) }
    var mostrarDialogoBanco by remember { mutableStateOf(false) }

    // --- Control de Notificaciones en Pantalla (Snackbars) ---
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val montoDouble = montoText.toDoubleOrNull() ?: 0.0
    val cambioDouble = tipoCambioText.toDoubleOrNull() ?: 0.0
    val totalRecibiria = montoDouble * cambioDouble

    // --- VALIDADOR COMPLETO DEL BOTÓN ---
    // El botón sólo se habilitará si todos los campos requeridos están llenos Y el checkbox es verdadero
    val camposCompletos = montoText.isNotEmpty() &&
            tipoCambioText.isNotEmpty() &&
            bancoSelected != null &&
            terminosCheck

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = BinanceBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()) // Permite deslizar si la pantalla es pequeña
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Título de la pantalla tal cual tu imagen
            Text(
                text = "Publicar oferta",
                color = BinanceTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Crea tu oferta de compra o venta de divisas P2P",
                color = BinanceTextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cuadro informativo decorativo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = BinanceTextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Estás creando una oferta para vender divisas.",
                    color = BinanceTextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Selector "TENGO" (Muestra saldos)
            DropdownMonedasCustom(
                titulo = "Tengo",
                monedaSeleccionadaNombre = monedaTengoSelected?.monedas?.nombre,
                monedaSeleccionadaCodigo = monedaTengoSelected?.monedas?.codigoIso,
                urlIcono = monedaTengoSelected?.monedas?.rutaBandera,
                subtextoSaldo = "Monto disponible: ${monedaTengoSelected?.saldoDisponible ?: "0.00"} ${monedaTengoSelected?.monedas?.codigoIso ?: ""}",
                onDesplegarClick = { mostrarDialogoTengo = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Calculamos en vivo si el monto ingresado supera al saldo disponible
            val montoIngresado = montoText.toDoubleOrNull() ?: 0.0
            val saldoDisponible = monedaTengoSelected?.saldoDisponible ?: 0.0
            val esMontoInvalido = montoIngresado > saldoDisponible

            Text(text = "Monto a ofrecer", color = BinanceTextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = montoText,
                onValueChange = { ofertasViewModel.montoOfertarText.value = it },
                modifier = Modifier.fillMaxWidth(),
                // 🟢 CLAVE 1: Indicamos al TextField que cambie a estado de error si se pasa del saldo
                isError = esMontoInvalido,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BinanceInputBackground,
                    unfocusedContainerColor = BinanceInputBackground,
                    focusedBorderColor = BinanceYellow,
                    unfocusedBorderColor = BinanceTextSecondary.copy(alpha = 0.2f),
                    focusedTextColor = BinanceTextPrimary,
                    unfocusedTextColor = BinanceTextPrimary,
                    // 🟢 CLAVE 2: Definimos los colores cuando ocurra un error (estilo Binance)
                    errorContainerColor = BinanceInputBackground,
                    errorBorderColor = BinanceError,
                    errorTextColor = BinanceTextPrimary,
                    errorCursorColor = BinanceError
                ),
                shape = RoundedCornerShape(8.dp),
                placeholder = { Text("0.00", color = BinanceTextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    Text(
                        text = monedaTengoSelected?.monedas?.codigoIso ?: "",
                        color = if (esMontoInvalido) BinanceError else BinanceTextSecondary,
                        modifier = Modifier.padding(end = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            )

// 🟢 CLAVE 3: Alerta dinámica debajo del cuadro si el saldo es insuficiente
            if (esMontoInvalido) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Saldo insuficiente. Máximo disponible: $saldoDisponible ${monedaTengoSelected?.monedas?.codigoIso ?: ""}",
                    color = BinanceError,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Selector "QUIERO"
            DropdownMonedasCustom(
                titulo = "Quiero",
                monedaSeleccionadaNombre = monedaQuieroSelected?.nombre,
                monedaSeleccionadaCodigo = monedaQuieroSelected?.codigoIso,
                urlIcono = monedaQuieroSelected?.rutaBandera,
                onDesplegarClick = { mostrarDialogoQuiero = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Campo del Precio (Tasa de Cambio Dinámica)
            val placeholderPrecio = "Precio por 1 ${monedaTengoSelected?.monedas?.codigoIso ?: "USD"} (en ${monedaQuieroSelected?.codigoIso ?: "PEN"})"
            Text(text = placeholderPrecio, color = BinanceTextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = tipoCambioText,
                onValueChange = { ofertasViewModel.tipoCambioText.value = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BinanceInputBackground,
                    unfocusedContainerColor = BinanceInputBackground,
                    focusedBorderColor = BinanceYellow,
                    unfocusedBorderColor = BinanceTextSecondary.copy(alpha = 0.2f),
                    focusedTextColor = BinanceTextPrimary,
                    unfocusedTextColor = BinanceTextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                placeholder = { Text("1.00", color = BinanceTextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    Text(
                        text = "${monedaQuieroSelected?.codigoIso ?: ""}/${monedaTengoSelected?.monedas?.codigoIso ?: ""}",
                        color = BinanceTextSecondary,
                        modifier = Modifier.padding(end = 8.dp),
                        fontSize = 12.sp
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Campo Gris Bloqueado "RECIBIRÍA" (Matemática Automática)
            Text(text = "Monto a recibir", color = BinanceTextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BinanceInputBackground.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .border(1.dp, BinanceTextSecondary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format("%.2f", totalRecibiria),
                        color = if (totalRecibiria > 0) BinanceGreen else BinanceTextSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = monedaQuieroSelected?.codigoIso ?: "",
                        color = BinanceTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Selector de Banco Filtrado
            DropdownBancosCustom(
                bancoSeleccionado = bancoSelected?.banco,
                monedaCodigo = monedaQuieroSelected?.codigoIso,
                monedaNombre = monedaQuieroSelected?.nombre,
                onDesplegarClick = { mostrarDialogoBanco = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Notas Adicionales (Opcional)
            Text(text = "Notas adicionales (opcional)", color = BinanceTextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = notasText,
                onValueChange = { ofertasViewModel.notasAdicionalesText.value = it },
                modifier = Modifier.fillMaxWidth().height(90.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BinanceInputBackground,
                    unfocusedContainerColor = BinanceInputBackground,
                    focusedBorderColor = BinanceYellow,
                    unfocusedBorderColor = BinanceTextSecondary.copy(alpha = 0.2f),
                    focusedTextColor = BinanceTextPrimary,
                    unfocusedTextColor = BinanceTextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                placeholder = { Text("Ej: Solo pagos desde mi banco. Nada de terceros.", color = BinanceTextSecondary, fontSize = 13.sp) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 8. Escudo de Consejos de Seguridad calcado de tu imagen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BinanceInputBackground, RoundedCornerShape(10.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Consejos de seguridad", color = BinanceYellow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("No aceptes pagos de terceros ni realices operaciones fuera de la plataforma.", color = BinanceTextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 9. Botón Principal con validación de color
            Button(
                onClick = {
                    ofertasViewModel.publicarOferta(
                        onSuccess = { msj -> coroutineScope.launch { snackbarHostState.showSnackbar(msj) } },
                        onError = { err -> coroutineScope.launch { snackbarHostState.showSnackbar(err) } }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = camposCompletos,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BinanceYellow,
                    disabledContainerColor = BinanceTextSecondary.copy(alpha = 0.3f),
                    contentColor = BinanceBackground,
                    disabledContentColor = BinanceTextSecondary
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Publicar oferta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 10. Checkbox Obligatorio de Términos y Condiciones debajo del botón
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = terminosCheck,
                    onCheckedChange = { ofertasViewModel.terminosAceptados.value = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = BinanceYellow,
                        uncheckedColor = BinanceTextSecondary,
                        checkmarkColor = BinanceBackground
                    )
                )
                Text("Al publicar, aceptas nuestros ", color = BinanceTextSecondary, fontSize = 11.sp)
                Text("Términos y Condiciones", color = BinanceYellow, fontSize = 11.sp, modifier = Modifier.clickable { /* link */ })
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // =========================================================================
    // MODALES / DIÁLOGOS DE SELECCIÓN DE SUPABASE FLOTANTES
    // =========================================================================

    // A. Modal para elegir "Tengo"
    if (mostrarDialogoTengo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoTengo = false },
            confirmButton = {},
            containerColor = BinanceInputBackground,
            title = { Text("¿Qué moneda tienes?", color = BinanceTextPrimary, fontSize = 18.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    billeterasUsuario.forEach { bill ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    ofertasViewModel.seleccionarMonedaTengo(bill)
                                    mostrarDialogoTengo = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(bill.monedas?.codigoIso ?: "", color = BinanceYellow, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp))
                            Text(bill.monedas?.nombre ?: "", color = BinanceTextPrimary, modifier = Modifier.weight(1f))
                            Text("${bill.saldoDisponible}", color = BinanceGreen)
                        }
                    }
                }
            }
        )
    }

    // B. Modal para elegir "Quiero" (Excluye la moneda de "Tengo")
    if (mostrarDialogoQuiero) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoQuiero = false },
            confirmButton = {},
            containerColor = BinanceInputBackground,
            title = { Text("¿Qué moneda quieres recibir?", color = BinanceTextPrimary, fontSize = 18.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    monedasGlobales.filter { it.id != monedaTengoSelected?.monedaId }.forEach { mon ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    ofertasViewModel.seleccionarMonedaQuiero(mon)
                                    mostrarDialogoQuiero = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(mon.codigoIso, color = BinanceYellow, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp))
                            Text(mon.nombre, color = BinanceTextPrimary)
                        }
                    }
                }
            }
        )
    }

    // C. Modal para elegir Cuentas Bancarias FILTRADAS
    if (mostrarDialogoBanco) {
        val bancosFiltrados = ofertasViewModel.obtenerCuentasFiltradas()
        AlertDialog(
            onDismissRequest = { mostrarDialogoBanco = false },
            confirmButton = {},
            containerColor = BinanceInputBackground,
            title = { Text("Selecciona tu banco de recepción", color = BinanceTextPrimary, fontSize = 18.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (bancosFiltrados.isEmpty()) {
                        Text(
                            text = "No tienes cuentas bancarias registradas para recibir ${monedaQuieroSelected?.codigoIso ?: ""}. Regístralas en tu perfil.",
                            color = BinanceError,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    } else {
                        bancosFiltrados.forEach { cuenta ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        ofertasViewModel.cuentaBancariaSeleccionada.value = cuenta
                                        mostrarDialogoBanco = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(cuenta.banco, color = BinanceTextPrimary, fontWeight = FontWeight.Bold)
                                    Text("Nº: ${cuenta.numeroCuenta}", color = BinanceTextSecondary, fontSize = 12.sp)
                                    Text("Titular: ${cuenta.titularNombre}", color = BinanceTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}