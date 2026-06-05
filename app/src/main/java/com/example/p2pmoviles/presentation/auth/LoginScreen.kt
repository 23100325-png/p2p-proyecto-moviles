package com.example.p2pmoviles.presentation.auth


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2pmoviles.ui.theme.*
import com.example.p2pmoviles.presentation.auth.AuthViewModel.LoginState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: AuthViewModel, onNavigateToRegistro: () -> Unit, onLoginSuccess: (Boolean) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val estado by viewModel.estadoLogin.collectAsState()

    // VALIDACIÓN: El botón solo se habilita si el correo es válido y la contraseña no está vacía
    val esCorreoValido = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val botonHabilitado = esCorreoValido && password.isNotBlank() && estado !is LoginState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BinanceBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Título
        Text(
            text = "Iniciar sesión",
            color = BinanceTextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtítulo
        Text(
            text = "Ingresa tus credenciales para acceder a tu cuenta",
            color = BinanceTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo: Correo Electrónico
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
            placeholder = "Ingresa tu contraseña",
            isVisible = passwordVisible,
            onVisibilityChange = { passwordVisible = !passwordVisible }
        )

        // Link: Olvidaste tu contraseña
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "¿Olvidaste tu contraseña?",
                color = BinanceYellow,
                fontSize = 14.sp,
                modifier = Modifier.clickable { viewModel.recuperarContrasena(email) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Iniciar Sesión (Dinámico)
        Button(
            onClick = { viewModel.iniciarSesion(email, password) },
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
                text = if (estado is LoginState.Loading) "Autenticando..." else "Iniciar sesión",
                color = if (botonHabilitado) BinanceBackground else BinanceTextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // feedback de errores o éxitos
        when (estado) {
            is LoginState.Error -> {
                Text(
                    text = (estado as LoginState.Error).message,
                    color = BinanceError,
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 14.sp
                )
            }
            is LoginState.Success -> {
                // Extraemos 'esAdmin' directamente usando paréntesis
                val esAdmin = (estado as LoginState.Success).esAdmin

                LaunchedEffect(Unit) {
                    onLoginSuccess(esAdmin)
                }
                Text(
                    text = "¡Sesión iniciada! Bienvenido.",
                    color = Color.Green,
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 14.sp
                )
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Divisor "o" igual a la imagen
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = BinanceTextSecondary.copy(alpha = 0.2f))
            Text(
                text = "o",
                color = BinanceTextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 14.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = BinanceTextSecondary.copy(alpha = 0.2f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Footer: ¿No tienes cuenta? Regístrate
        Row(horizontalArrangement = Arrangement.Center) {
            Text("¿No tienes cuenta? ", color = BinanceTextSecondary, fontSize = 14.sp)
            Text(
                text = "Regístrate",
                color = BinanceYellow,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToRegistro() }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Escudo inferior de protección
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = BinanceYellow,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tu información está protegida",
                color = BinanceTextSecondary,
                fontSize = 13.sp
            )
        }
    }
}