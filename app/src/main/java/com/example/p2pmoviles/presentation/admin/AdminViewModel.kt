package com.example.p2pmoviles.presentation.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.p2pmoviles.data.SupabaseClient
import com.example.p2pmoviles.data.model.MovimientoAdmin
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AdminUIState {
    data object Loading : AdminUIState()
    data class Success(val lista: List<MovimientoAdmin>) : AdminUIState()
    data class Error(val msg: String) : AdminUIState()
}

class AdminViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUIState>(AdminUIState.Loading)
    val uiState: StateFlow<AdminUIState> = _uiState

    private val _estaRefrescando = MutableStateFlow(false)
    val estaRefrescando: StateFlow<Boolean> = _estaRefrescando

    init {
        obtenerMovimientos()
    }

    fun obtenerMovimientos() {
        viewModelScope.launch {
            _uiState.value = AdminUIState.Loading
            _estaRefrescando.value = true // 🟢 Iniciamos la animación de jalar
            try {
                // Consulta con Join Relacional nativo de Supabase
                val result = SupabaseClient.client.postgrest["movimientos_fondos"]
                    .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, monedas(*)")) {
                        filter { eq("estado", "PENDIENTE") } // Solo pendientes de revisión
                    }.decodeList<MovimientoAdmin>()

                _uiState.value = AdminUIState.Success(result)
            } catch (e: Exception) {
                Log.e("AdminVM", "Error cargando movimientos", e)
                _uiState.value = AdminUIState.Error("Error al conectar con la base de datos.")
            }finally {
                _estaRefrescando.value = false // 🟢 Apagamos la animación pase lo que pase
            }
        }
    }

    fun procesarSolicitud(movimiento: MovimientoAdmin, aprobar: Boolean) {
        viewModelScope.launch {
            try {
                // Definimos el estado según el botón presionado
                val nuevoEstado = if (aprobar) "APROBADO" else "RECHAZADO"
                val fechaActual = java.time.Instant.now().toString()

                // Una sola petición a Supabase: Cambiar estado y estampar fecha
                SupabaseClient.client.postgrest["movimientos_fondos"].update({
                    set("estado", nuevoEstado)
                    set("fecha_procesado", fechaActual)
                }) {
                    filter { eq("id", movimiento.id) }
                }

                // Volver a cargar la lista (las tarjetas procesadas desaparecerán solas)
                obtenerMovimientos()

            } catch (e: Exception) {
                Log.e("AdminVM", "Error al procesar la acción", e)
            }
        }
    }
}