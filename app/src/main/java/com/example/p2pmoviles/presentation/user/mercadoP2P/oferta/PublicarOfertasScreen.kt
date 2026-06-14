package com.example.p2pmoviles.presentation.user.mercadoP2P.oferta

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.p2pmoviles.presentation.user.DropdownMonedasCustom
import kotlinx.coroutines.launch
import com.example.p2pmoviles.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicarOfertaScreen(
    usuarioLogueadoId: String,
    //onBackClick: () -> Unit = {},
    ofertasViewModel: OfertasViewModel = viewModel()
) {
    // Inicializamos los catálogos en el ViewModel con el ID del usuario
    LaunchedEffect(usuarioLogueadoId) {
        ofertasViewModel.inicializar(usuarioLogueadoId)
    }

    // --- Lectura de Estados del ViewModel ---
    val billeterasUsuario by ofertasViewModel.billeterasUsuario.collectAsState()
    val monedasGlobales by ofertasViewModel.monedasGlobales.collectAsState()

    val tasaReferencial by ofertasViewModel.tipoCambioReferencial.collectAsState()
    val monedaTengoSelected by ofertasViewModel.monedaTengo.collectAsState()
    val monedaQuieroSelected by ofertasViewModel.monedaQuiero.collectAsState()
    LaunchedEffect(monedaTengoSelected, monedaQuieroSelected) {
        // Solo si ambas monedas ya cargaron desde Supabase, llamamos a la API de tipo de cambio
        if (monedaTengoSelected != null && monedaQuieroSelected != null) {
            ofertasViewModel.obtenerTipoCambioReal()
        }
    }

    val montoText by ofertasViewModel.montoOfertarText.collectAsState()
    val tipoCambioText by ofertasViewModel.tipoCambioText.collectAsState()
    val notasText by ofertasViewModel.notasAdicionalesText.collectAsState()
    val terminosCheck by ofertasViewModel.terminosAceptados.collectAsState()

    // --- Control de Diálogos Flotantes de Selección ---
    var mostrarDialogoTengo by remember { mutableStateOf(false) }
    var mostrarDialogoQuiero by remember { mutableStateOf(false) }

    // --- Control de Notificaciones en Pantalla (Snackbars) ---
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val montoDouble = montoText.toDoubleOrNull() ?: 0.0
    val cambioDouble = tipoCambioText.toDoubleOrNull() ?: 0.0
    val totalRecibiria = montoDouble * cambioDouble

    // --- VALIDADOR COMPLETO DEL BOTÓN (Bancos removidos) ---
    val camposCompletos = montoText.isNotEmpty() &&
            tipoCambioText.isNotEmpty() &&
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
                .verticalScroll(rememberScrollState())
        ) {
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
                    text = "Estás creando una oferta directa entre billeteras del sistema.",
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

            // Validación de saldo en vivo
            val montoIngresado = montoText.toDoubleOrNull() ?: 0.0
            val saldoDisponible = monedaTengoSelected?.saldoDisponible ?: 0.0
            val esMontoInvalido = montoIngresado > saldoDisponible

            Text(text = "Monto a ofrecer", color = BinanceTextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = montoText,
                onValueChange = { ofertasViewModel.montoOfertarText.value = it },
                modifier = Modifier.fillMaxWidth(),
                isError = esMontoInvalido,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BinanceInputBackground,
                    unfocusedContainerColor = BinanceInputBackground,
                    focusedBorderColor = BinanceYellow,
                    unfocusedBorderColor = BinanceTextSecondary.copy(alpha = 0.2f),
                    focusedTextColor = BinanceTextPrimary,
                    unfocusedTextColor = BinanceTextPrimary,
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

            // 2. Selector "QUIERO"
            DropdownMonedasCustom(
                titulo = "Quiero",
                monedaSeleccionadaNombre = monedaQuieroSelected?.nombre,
                monedaSeleccionadaCodigo = monedaQuieroSelected?.codigoIso,
                urlIcono = monedaQuieroSelected?.rutaBandera,
                onDesplegarClick = { mostrarDialogoQuiero = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Fila del Título del Precio y la Tasa Referencial de la API
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primera mitad: Título del campo
                val placeholderPrecio = "Precio por 1 ${monedaTengoSelected?.monedas?.codigoIso ?: "PEN"} (en ${monedaQuieroSelected?.codigoIso ?: "USD"})"
                Text(
                    text = placeholderPrecio,
                    color = BinanceTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )

                // Segunda mitad: Tasa Referencial (Muestra el valor o un aviso de carga)
                Text(
                    text = if (tasaReferencial != null && !tasaReferencial!!.contains("⏳") && !tasaReferencial!!.contains("❌")) {
                        tasaReferencial!!
                    } else {
                        "⏳ Conectando API..."
                    },
                    color = if (tasaReferencial?.startsWith("💡") == true) BinanceYellow else BinanceTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

// El campo OutlinedTextField se mantiene abajo ocupando todo el ancho de forma normal
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

            // 4. Campo "RECIBIRÍA AUTOMÁTICAMENTE" (Billetera Destino Interna)
            Text(text = "Monto a recibir (en tu billetera de la app)", color = BinanceTextSecondary, fontSize = 14.sp)
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

            // 5. Notas Adicionales
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
                placeholder = { Text("Ej: Intercambio automático instantáneo sin comisiones.", color = BinanceTextSecondary, fontSize = 13.sp) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Escudo de Consejos de Seguridad
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
                    Text("Seguridad de Billetera Integrada", color = BinanceYellow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Los fondos se descuentan e ingresan directamente en los saldos internos de la app de forma 100% regulada.", color = BinanceTextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón Principal
            Button(
                onClick = {
                    ofertasViewModel.publicarOferta(
                        onSuccess = { msj -> coroutineScope.launch { snackbarHostState.showSnackbar(msj) } },
                        onError = { err -> coroutineScope.launch { snackbarHostState.showSnackbar(err) } }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = camposCompletos && !esMontoInvalido,
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

            // Checkbox Obligatorio
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
    // MODALES / DIÁLOGOS DE SELECCIÓN FLOTANTES
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

    // B. Modal para elegir "Quiero"
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
}