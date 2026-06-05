package com.example.p2pmoviles.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2pmoviles.ui.theme.*
import com.example.p2pmoviles.presentation.auth.AuthViewModel.RegistroState

@Composable
fun RegistroScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val estado by viewModel.estadoRegistro.collectAsState()

    // --- LÓGICA DE VALIDACIONES DINÁMICAS ---
    // Usamos remember para evitar que los patrones regex se recompilen innecesariamente con cada tecla presionada
    val esCorreoValido = remember(email) {
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    val cumpleLongitud = password.length >= 8
    val sonContrasenasIguales = password.isNotEmpty() && password == confirmPassword
    val mostrarCuadroRequisitos = !cumpleLongitud || !sonContrasenasIguales

    val botonHabilitado = nombre.isNotBlank() && esCorreoValido && cumpleLongitud && sonContrasenasIguales && estado !is RegistroState.Loading

    // OBSERVACIÓN 1: Añadimos verticalScroll para evitar desbordamientos en pantallas pequeñas
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BinanceBackground)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Crear cuenta",
            color = BinanceTextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Regístrate para acceder al sistema de\nintercambio de divisas P2P",
            color = BinanceTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo: Nombre
        CustomTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = "Nombre completo",
            placeholder = "Ingresa tu nombre completo",
            icon = Icons.Default.Person
        )

        // Campo: Correo
        CustomTextField(
            value = email,
            onValueChange = { email = it },
            label = "Correo electrónico",
            placeholder = "Ingresa tu correo electrónico",
            icon = Icons.Default.Email
        )

        // Campo: Contraseña
        CustomPasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            placeholder = "Mínimo 8 caracteres",
            isVisible = passwordVisible,
            onVisibilityChange = { passwordVisible = !passwordVisible }
        )

        // Campo: Confirmar Contraseña
        CustomPasswordField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirmar contraseña",
            placeholder = "Confirma tu contraseña",
            isVisible = confirmPasswordVisible,
            onVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible }
        )

        // Caja de requisitos (Dinámica)
        if (mostrarCuadroRequisitos) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BinanceYellow.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .background(BinanceYellow.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = BinanceYellow,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Requisitos de contraseña:", color = BinanceYellow, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                        Text(
                            text = if (cumpleLongitud) "✓ Mínimo 8 caracteres" else "✗ Mínimo 8 caracteres",
                            color = if (cumpleLongitud) Color.Green else BinanceTextSecondary,
                            fontSize = 12.sp
                        )

                        Text(
                            text = if (sonContrasenasIguales) "✓ Las contraseñas coinciden" else "✗ Las contraseñas deben ser iguales",
                            color = if (sonContrasenasIguales) Color.Green else BinanceTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Registrarme
        Button(
            onClick = { viewModel.registrarUsuario(nombre, email, password, confirmPassword) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BinanceYellow,
                disabledContainerColor = BinanceInputBackground
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = botonHabilitado
        ) {
            Text(
                text = if (estado is RegistroState.Loading) "Procesando..." else "Registrarme",
                color = if (botonHabilitado) BinanceBackground else BinanceTextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Manejo de Mensajes (Éxito o Error)
        when (estado) {
            is RegistroState.Error -> {
                Text(
                    text = (estado as RegistroState.Error).message,
                    color = BinanceError,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
            is RegistroState.Success -> {
                Text(
                    text = "¡Cuenta creada con éxito! Ya puedes iniciar sesión.",
                    color = Color.Green,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
                // OBSERVACIÓN 2: Efecto lanzado para navegar automáticamente al tener éxito
                LaunchedEffect(Unit) {
                    onNavigateToLogin()
                }
            }
            else -> {}
        }

        // OBSERVACIÓN 3: Se cambió Modifier.weight(1f) por un Spacer adaptativo
        Spacer(modifier = Modifier.height(32.dp))

        // Footer
        Row(horizontalArrangement = Arrangement.Center) {
            Text("¿Ya tienes cuenta? ", color = BinanceTextSecondary, fontSize = 14.sp)
            Text(
                text = "Inicia sesión",
                color = BinanceYellow,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = BinanceTextSecondary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Tu información está protegida", color = BinanceTextSecondary, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// --- COMPONENTES REUTILIZABLES ---

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(text = label, color = BinanceTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = BinanceTextSecondary) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = BinanceTextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BinanceInputBackground,
                unfocusedContainerColor = BinanceInputBackground,
                focusedBorderColor = BinanceYellow,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = BinanceTextPrimary,
                unfocusedTextColor = BinanceTextPrimary
            ),
            singleLine = true
        )
    }
}

@Composable
fun CustomPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isVisible: Boolean,
    onVisibilityChange: () -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(text = label, color = BinanceTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = BinanceTextSecondary) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BinanceTextSecondary) },
            trailingIcon = {
                val image = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = onVisibilityChange) {
                    Icon(image, contentDescription = null, tint = BinanceTextSecondary)
                }
            },
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BinanceInputBackground,
                unfocusedContainerColor = BinanceInputBackground,
                focusedBorderColor = BinanceYellow,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = BinanceTextPrimary,
                unfocusedTextColor = BinanceTextPrimary
            ),
            singleLine = true
        )
    }
}
