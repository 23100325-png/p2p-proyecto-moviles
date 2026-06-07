package com.example.p2pmoviles.presentation.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.p2pmoviles.data.PerfilSimplificado
import com.example.p2pmoviles.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthViewModel : ViewModel() {

    private val _estadoRegistro = MutableStateFlow<RegistroState>(RegistroState.Idle)
    val estadoRegistro: StateFlow<RegistroState> = _estadoRegistro.asStateFlow()

    private val _estadoLogin = MutableStateFlow<LoginState>(LoginState.Idle)
    val estadoLogin: StateFlow<LoginState> = _estadoLogin.asStateFlow()


    fun registrarUsuario(nombre: String, email: String, pass: String, confirmPass: String) {
        if (nombre.isBlank() || email.isBlank() || pass.isBlank()) {
            _estadoRegistro.value = RegistroState.Error("Completa todos los campos.")
            return
        }
        _estadoRegistro.value = RegistroState.Loading
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = pass
                    // OBSERVACIÓN 5: Recomendable inyectar el nombre en los metadatos de Auth para sincronizar perfiles
                    data = buildJsonObject {
                        put("nombre_completo", nombre)
                    }
                }
                _estadoRegistro.value = RegistroState.Success
            } catch (e: Exception) {
                _estadoRegistro.value = RegistroState.Error(e.localizedMessage ?: "Error al registrar")
            }
        }
    }

    var usuarioActualId by mutableStateOf("")
        private set

    fun iniciarSesion(email: String, pass: String) {
        // .trim() evita que un espacio en blanco al final del correo rompa el login
        val emailLimpio = email.trim()

        _estadoLogin.value = LoginState.Loading
        viewModelScope.launch {
            try {
                // 1. Autenticación en Supabase Auth
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = emailLimpio
                    this.password = pass
                }

                // 2. Obtener la sesión del usuario que acaba de ingresar
                val usuarioActual = SupabaseClient.client.auth.currentUserOrNull()
                if (usuarioActual != null) {
                    Log.d("LoginSuccess", "UID de Auth encontrado: ${usuarioActual.id}")
                    usuarioActualId = usuarioActual.id
                    // 3. Buscar el rol en la tabla perfiles
                    val respuestaPerfil = SupabaseClient.client.postgrest["perfiles"]
                        .select {
                            filter { eq("id", usuarioActual.id) }
                        }.decodeSingleOrNull<PerfilSimplificado>()

                    if (respuestaPerfil != null) {
                        // 4. Validación de Roles para separar pantallas
                        // ID 2 -> Admin (ADM) / ID 1 -> Usuario (USU)
                        val esAdmin = respuestaPerfil.rol_id == 2L
                        Log.d("LoginSuccess", "Perfil encontrado. ¿Es Admin?: $esAdmin. Rol ID: ${respuestaPerfil.rol_id}")

                        _estadoLogin.value = LoginState.Success(esAdmin = esAdmin)
                    } else {
                        // Si llega aquí, el UID existe en Auth pero la consulta no trajo filas de la tabla
                        Log.e("LoginError", "La tabla perfiles no tiene ningún registro con el ID: ${usuarioActual.id}")
                        _estadoLogin.value = LoginState.Error("El perfil de usuario no existe en la base de datos.")
                    }
                } else {
                    _estadoLogin.value = LoginState.Error("No se pudo obtener el usuario de la sesión actual.")
                }
            } catch (e: Exception) {
                Log.e("LoginError", "Error crítico en el flujo: ", e)
                val mensaje = when {
                    e.message?.contains("Invalid login credentials", ignoreCase = true) == true -> "Correo o contraseña incorrectos."
                    e.message?.contains("Serializer", ignoreCase = true) == true -> "Error al mapear los datos del perfil (Serialización)."
                    else -> e.localizedMessage ?: "Error desconocido al iniciar sesión"
                }
                _estadoLogin.value = LoginState.Error(mensaje)
            }
        }
    }

    fun recuperarContrasena(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _estadoLogin.value = LoginState.Error("Ingresa un correo válido primero.")
            return
        }
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.resetPasswordForEmail(email)
                _estadoLogin.value = LoginState.Error("Correo de recuperación enviado (Revisa tu bandeja).")
            } catch (e: Exception) {
                _estadoLogin.value = LoginState.Error(e.localizedMessage ?: "Error al enviar correo")
            }
        }
    }

    fun cerrarSesion(onResultado: () -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Destruye el token en los servidores de Supabase
                SupabaseClient.client.auth.signOut()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error al destruir sesión en Supabase", e)
            } finally {
                // 2. PASE LO QUE PASE (así falle internet), limpiamos la memoria local
                usuarioActualId = ""
                _estadoLogin.value = LoginState.Idle

                // 3. 🟢 LE AVISAMOS A LA NAVEGACIÓN QUE YA PUEDE PROSEGUIR
                onResultado()
            }
        }
    }


// OBSERVACIÓN 4: Clases selladas actualizadas a 'data object' estándar moderno de Kotlin
sealed class RegistroState {
    data object Idle : RegistroState()
    data object Loading : RegistroState()
    data object Success : RegistroState()
    data class Error(val message: String) : RegistroState()
}

sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data class Success(val esAdmin: Boolean) : LoginState()
    data class Error(val message: String) : LoginState()
}
}