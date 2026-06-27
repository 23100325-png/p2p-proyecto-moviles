package com.example.p2pmoviles.presentation.user.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.p2pmoviles.data.model.CuentaBancaria
import com.example.p2pmoviles.data.model.MonedaInfo
import com.example.p2pmoviles.presentation.auth.AuthViewModel
import com.example.p2pmoviles.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onLogoutSuccess: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val userId = authViewModel.usuarioActualId
    val perfil by profileViewModel.perfil.collectAsState()
    val rating by profileViewModel.rating.collectAsState()
    val mensaje by profileViewModel.mensaje.collectAsState()
    val cargando by profileViewModel.cargando.collectAsState()
    val monedas by profileViewModel.monedas.collectAsState()
    val cuentasBancarias by profileViewModel.cuentasBancarias.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var accountToDelete by remember { mutableStateOf<CuentaBancaria?>(null) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            profileViewModel.cargarDatos(userId)
        }
    }

    Scaffold(
        containerColor = BinanceBackground,
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = BinanceTextPrimary, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(
                            badge = { Badge(containerColor = BinanceYellow) { Text("3") } }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = BinanceTextPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BinanceBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAccountDialog = true },
                containerColor = BinanceYellow,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.AddCard, contentDescription = "Agregar Cuenta")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = BinanceYellow
            )

            // 🟢 NUEVO: Cuadro de Calificación real debajo de la foto
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(BinanceSurface, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = BinanceYellow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val rawRating = rating.first
                val totalResenas = rating.second
                val ratingFormateado = if (totalResenas > 0) String.format(java.util.Locale.US, "%.1f", rawRating) else "0.0"
                
                Text(
                    text = "$ratingFormateado ($totalResenas reseñas)",
                    color = BinanceTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (perfil != null) {
                ProfileInfoCard(
                    title = "Nombre Completo",
                    value = perfil!!.nombreCompleto,
                    icon = Icons.Default.Badge
                )
                ProfileInfoCard(
                    title = "Email",
                    value = perfil!!.email ?: "No asignado",
                    icon = Icons.Default.Email
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceSurface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Editar Perfil", color = BinanceYellow)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- SECCIÓN DE CUENTAS BANCARIAS ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Mis Cuentas Bancarias",
                        color = BinanceTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showAddAccountDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Agregar", tint = BinanceYellow)
                    }
                }

                if (cuentasBancarias.isEmpty()) {
                    Text(
                        "No tienes cuentas registradas.",
                        color = BinanceTextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    cuentasBancarias.forEach { cuenta ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = BinanceSurface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cuenta.banco,
                                        color = BinanceTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${cuenta.monedas?.nombre ?: "Cargando..."} - ${cuenta.numeroCuenta}",
                                        color = BinanceTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                                IconButton(onClick = { accountToDelete = cuenta }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = Color(0xFFD32F2F)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        authViewModel.cerrarSesion {
                            onLogoutSuccess()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar Sesión", color = Color.White)
                }
            } else if (cargando) {
                CircularProgressIndicator(color = BinanceYellow)
            }
        }
    }

    if (showEditDialog && perfil != null) {
        EditProfileDialog(
            perfil = perfil!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { nc ->
                profileViewModel.actualizarPerfil(userId, nc)
                showEditDialog = false
            }
        )
    }

    if (showAddAccountDialog) {
        AddBankAccountDialog(
            userId = userId,
            monedas = monedas,
            onDismiss = { showAddAccountDialog = false },
            onConfirm = { nuevaCuenta ->
                profileViewModel.agregarCuentaBancaria(nuevaCuenta)
                showAddAccountDialog = false
            }
        )
    }

    accountToDelete?.let { cuenta ->
        AlertDialog(
            onDismissRequest = { accountToDelete = null },
            title = { Text("Confirmar eliminación", color = BinanceTextPrimary) },
            text = { Text("¿Estás seguro de que deseas eliminar la cuenta en ${cuenta.monedas?.nombre ?: "esta moneda"} del banco ${cuenta.banco} (${cuenta.numeroCuenta})?", color = BinanceTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        cuenta.id?.let { profileViewModel.eliminarCuentaBancaria(it) }
                        accountToDelete = null
                    }
                ) {
                    Text("Eliminar", color = Color(0xFFD32F2F))
                }
            },
            dismissButton = {
                TextButton(onClick = { accountToDelete = null }) {
                    Text("Cancelar", color = BinanceTextSecondary)
                }
            },
            containerColor = BinanceSurface
        )
    }

    mensaje?.let {
        AlertDialog(
            onDismissRequest = { profileViewModel.limpiarMensaje() },
            confirmButton = {
                TextButton(onClick = { profileViewModel.limpiarMensaje() }) {
                    Text("OK", color = BinanceYellow)
                }
            },
            title = { Text("Mensaje", color = BinanceTextPrimary) },
            text = { Text(it, color = BinanceTextSecondary) },
            containerColor = BinanceSurface
        )
    }
}

@Composable
fun ProfileInfoCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = BinanceSurface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, color = BinanceTextSecondary, fontSize = 12.sp)
                Text(text = value, color = BinanceTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    perfil: PerfilUsuario,
    onDismiss: () -> Unit,
    onConfirm: (nombreCompleto: String) -> Unit
) {
    var nc by remember { mutableStateOf(perfil.nombreCompleto) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Perfil", color = BinanceTextPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = nc,
                    onValueChange = { nc = it },
                    label = { Text("Nombre Completo") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BinanceYellow,
                        unfocusedBorderColor = BinanceTextSecondary,
                        focusedLabelColor = BinanceYellow,
                        unfocusedLabelColor = BinanceTextSecondary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(nc) }) {
                Text("Guardar", color = BinanceYellow)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = BinanceTextSecondary)
            }
        },
        containerColor = BinanceSurface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBankAccountDialog(
    userId: String,
    monedas: List<MonedaInfo>,
    onDismiss: () -> Unit,
    onConfirm: (CuentaBancaria) -> Unit
) {
    var banco by remember { mutableStateOf("") }
    var numeroCuenta by remember { mutableStateOf("") }
    var numeroCci by remember { mutableStateOf("") }
    var titularNombre by remember { mutableStateOf("") }
    var monedaSeleccionada by remember { mutableStateOf<MonedaInfo?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = BinanceSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Agregar Cuenta Bancaria",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BinanceYellow
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Dropdown para Moneda
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BinanceTextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BinanceTextSecondary)
                    ) {
                        Text(monedaSeleccionada?.nombre ?: "Seleccionar Moneda")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(BinanceSurface)
                    ) {
                        monedas.forEach { moneda ->
                            DropdownMenuItem(
                                text = { Text(moneda.nombre, color = Color.White) },
                                onClick = {
                                    monedaSeleccionada = moneda
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ProfileTextField(value = banco, onValueChange = { banco = it }, label = "Banco")
                ProfileTextField(
                    value = numeroCuenta,
                    onValueChange = { if (it.all { char -> char.isDigit() }) numeroCuenta = it },
                    label = "Número de Cuenta",
                    keyboardType = KeyboardType.Number
                )
                ProfileTextField(
                    value = numeroCci,
                    onValueChange = { if (it.all { char -> char.isDigit() }) numeroCci = it },
                    label = "Número CCI",
                    keyboardType = KeyboardType.Number
                )
                ProfileTextField(value = titularNombre, onValueChange = { titularNombre = it }, label = "Titular")

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = BinanceTextSecondary)
                    }
                    val isFormValid = monedaSeleccionada != null && 
                                    banco.isNotBlank() && 
                                    numeroCuenta.isNotBlank() && 
                                    numeroCci.isNotBlank() && 
                                    titularNombre.isNotBlank()
                                    
                    Button(
                        onClick = {
                            if (isFormValid) {
                                onConfirm(
                                    CuentaBancaria(
                                        usuarioId = userId,
                                        monedaId = monedaSeleccionada!!.id,
                                        banco = banco,
                                        numeroCuenta = numeroCuenta,
                                        numeroCci = numeroCci,
                                        titularNombre = titularNombre
                                    )
                                )
                            }
                        },
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BinanceYellow,
                            disabledContainerColor = BinanceYellow.copy(alpha = 0.3f)
                        )
                    ) {
                        Text("Agregar", color = if (isFormValid) Color.Black else Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = BinanceYellow,
            unfocusedBorderColor = BinanceTextSecondary,
            focusedLabelColor = BinanceYellow,
            unfocusedLabelColor = BinanceTextSecondary
        )
    )
}
