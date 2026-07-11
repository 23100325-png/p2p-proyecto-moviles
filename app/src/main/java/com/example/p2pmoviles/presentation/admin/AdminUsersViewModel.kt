package com.example.p2pmoviles.presentation.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.p2pmoviles.data.SupabaseClient
import com.example.p2pmoviles.data.model.PerfilAdmin
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AdminUsersUIState {
    data object Loading : AdminUsersUIState()
    data class Success(val usuarios: List<PerfilAdmin>) : AdminUsersUIState()
    data class Error(val msg: String) : AdminUsersUIState()
}

class AdminUsersViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUsersUIState>(AdminUsersUIState.Loading)
    val uiState: StateFlow<AdminUsersUIState> = _uiState

    private val _estaRefrescando = MutableStateFlow(false)
    val estaRefrescando: StateFlow<Boolean> = _estaRefrescando

    private val _mensajeOperacion = MutableStateFlow<String?>(null)
    val mensajeOperacion: StateFlow<String?> = _mensajeOperacion

    private val _usuarioActualId = MutableStateFlow<String>("")
    val usuarioActualId: StateFlow<String> = _usuarioActualId

    private var _todosLosUsuarios = listOf<PerfilAdmin>()

    init {
        _usuarioActualId.value = SupabaseClient.client.auth.currentUserOrNull()?.id ?: ""
        obtenerTodosUsuarios()
    }

    fun obtenerTodosUsuarios() {
        viewModelScope.launch {
            _uiState.value = AdminUsersUIState.Loading
            _estaRefrescando.value = true
            try {
                val result = SupabaseClient.client.postgrest["perfiles"]
                    .select().decodeList<PerfilAdmin>()

                _todosLosUsuarios = result
                _uiState.value = AdminUsersUIState.Success(result)
            } catch (e: Exception) {
                Log.e("AdminUsersVM", "Error cargando usuarios", e)
                _uiState.value = AdminUsersUIState.Error("Error al cargar usuarios")
            } finally {
                _estaRefrescando.value = false
            }
        }
    }

    fun buscarUsuario(query: String) {
        if (query.isBlank()) {
            _uiState.value = AdminUsersUIState.Success(_todosLosUsuarios)
            return
        }
        
        val filtrados = _todosLosUsuarios.filter {
            it.nombreCompleto.contains(query, ignoreCase = true) ||
            it.email.contains(query, ignoreCase = true)
        }
        _uiState.value = AdminUsersUIState.Success(filtrados)
    }

    fun toggleActivoUsuario(usuario: PerfilAdmin, nuevoEstado: Boolean) {
        viewModelScope.launch {
            try {
                val accion = if (nuevoEstado) "desbloqueado" else "bloqueado"
                
                SupabaseClient.client.postgrest["perfiles"].update({
                    set("activo", nuevoEstado)
                }) {
                    filter { eq("id", usuario.id) }
                }

                _mensajeOperacion.value = "Usuario ${usuario.nombreCompleto} $accion exitosamente"
                obtenerTodosUsuarios()

            } catch (e: Exception) {
                Log.e("AdminUsersVM", "Error al cambiar estado de usuario", e)
                _mensajeOperacion.value = "Error al cambiar estado del usuario"
            }
        }
    }

    fun editarNombreUsuario(usuario: PerfilAdmin, nuevoNombre: String) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.postgrest["perfiles"].update({
                    set("nombre_completo", nuevoNombre)
                }) {
                    filter { eq("id", usuario.id) }
                }
                _mensajeOperacion.value = "Nombre actualizado a: $nuevoNombre"
                obtenerTodosUsuarios()
            } catch (e: Exception) {
                Log.e("AdminUsersVM", "Error editando nombre", e)
                _mensajeOperacion.value = "Error al editar nombre"
            }
        }
    }

    fun cambiarRolUsuario(usuario: PerfilAdmin, nuevoRolId: Long) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.postgrest["perfiles"].update({
                    set("rol_id", nuevoRolId)
                }) {
                    filter { eq("id", usuario.id) }
                }
                val rolStr = if (nuevoRolId == 2L) "Admin" else "Usuario"
                _mensajeOperacion.value = "Rol cambiado a $rolStr para ${usuario.nombreCompleto}"
                obtenerTodosUsuarios()
            } catch (e: Exception) {
                Log.e("AdminUsersVM", "Error cambiando rol", e)
                _mensajeOperacion.value = "Error al cambiar rol"
            }
        }
    }

    fun limpiarMensaje() {
        _mensajeOperacion.value = null
    }
}
