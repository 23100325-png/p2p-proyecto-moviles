package com.example.p2pmoviles.presentation.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.p2pmoviles.data.SupabaseClient
import com.example.p2pmoviles.data.model.MonedaInfo
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovimientoAprobado(
    val id: Long,
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("moneda_id") val monedaId: Long,
    @SerialName("tipo_movimiento") val tipoMovimiento: String,
    val monto: Double,
    @SerialName("ruta_voucher") val rutaVoucher: String?,
    val estado: String,
    @SerialName("fecha_solicitud") val fechaSolicitud: String,
    @SerialName("fecha_procesado") val fechaProcesado: String?,
    @SerialName("cuenta_bancaria_id") val cuentaBancariaId: Long?,
    // Relaciones
    val monedas: MonedaInfo? = null,
    @SerialName("perfiles") val perfilUsuario: PerfilTransaccion? = null
)

@Serializable
data class PerfilTransaccion(
    @SerialName("nombre_completo") val nombreCompleto: String
)

sealed class AdminTransactionsUIState {
    data object Loading : AdminTransactionsUIState()
    data class Success(val movimientos: List<MovimientoAprobado>) : AdminTransactionsUIState()
    data class Error(val msg: String) : AdminTransactionsUIState()
}

class AdminTransactionsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AdminTransactionsUIState>(AdminTransactionsUIState.Loading)
    val uiState: StateFlow<AdminTransactionsUIState> = _uiState

    private val _estaRefrescando = MutableStateFlow(false)
    val estaRefrescando: StateFlow<Boolean> = _estaRefrescando

    init {
        obtenerTransacciones()
    }

    fun obtenerTransacciones() {
        viewModelScope.launch {
            _uiState.value = AdminTransactionsUIState.Loading
            _estaRefrescando.value = true
            try {
                val result = SupabaseClient.client.postgrest["movimientos_fondos"]
                    .select(columns = Columns.raw("*, monedas(*), perfiles(nombre_completo)")) {
                        filter { eq("tipo_movimiento", "RECARGA") }
                        filter { eq("estado", "APROBADO") }
                        order("fecha_procesado", Order.DESCENDING)
                    }.decodeList<MovimientoAprobado>()

                _uiState.value = AdminTransactionsUIState.Success(result)
            } catch (e: Exception) {
                Log.e("AdminTransactionsVM", "Error cargando transacciones", e)
                _uiState.value = AdminTransactionsUIState.Error("Error al cargar transacciones")
            } finally {
                _estaRefrescando.value = false
            }
        }
    }

    fun filtrarPorMoneda(monedaId: Long) {
        viewModelScope.launch {
            try {
                val movimientos = (uiState.value as? AdminTransactionsUIState.Success)?.movimientos ?: return@launch
                val filtrados = movimientos.filter { it.monedaId == monedaId }
                _uiState.value = AdminTransactionsUIState.Success(filtrados)
            } catch (e: Exception) {
                Log.e("AdminTransactionsVM", "Error filtrando por moneda", e)
            }
        }
    }

    fun filtrarPorFecha(fechaDesde: Long?, fechaHasta: Long?) {
        viewModelScope.launch {
            try {
                val movimientos = (uiState.value as? AdminTransactionsUIState.Success)?.movimientos ?: return@launch
                val sdfInput = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())

                val filtrados = movimientos.filter { mov ->
                    try {
                        val fecha = sdfInput.parse(mov.fechaProcesado ?: mov.fechaSolicitud)?.time ?: return@filter true
                        
                        when {
                            fechaDesde != null && fechaHasta != null -> {
                                fecha >= fechaDesde && fecha <= fechaHasta + 86399999
                            }
                            fechaDesde != null -> fecha >= fechaDesde
                            fechaHasta != null -> fecha <= fechaHasta + 86399999
                            else -> true
                        }
                    } catch (e: Exception) {
                        true
                    }
                }
                _uiState.value = AdminTransactionsUIState.Success(filtrados)
            } catch (e: Exception) {
                Log.e("AdminTransactionsVM", "Error filtrando por fecha", e)
            }
        }
    }

    fun buscarTransaccion(query: String) {
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    obtenerTransacciones()
                    return@launch
                }

                val movimientos = (uiState.value as? AdminTransactionsUIState.Success)?.movimientos ?: return@launch
                val filtrados = movimientos.filter { 
                    it.id.toString().contains(query) || 
                    it.perfilUsuario?.nombreCompleto?.contains(query, ignoreCase = true) == true ||
                    it.usuarioId.contains(query, ignoreCase = true)
                }
                _uiState.value = AdminTransactionsUIState.Success(filtrados)
            } catch (e: Exception) {
                Log.e("AdminTransactionsVM", "Error buscando transacción", e)
            }
        }
    }

    fun ordenarPorFecha(ascendente: Boolean = false) {
        viewModelScope.launch {
            try {
                val movimientos = (uiState.value as? AdminTransactionsUIState.Success)?.movimientos ?: return@launch
                val ordenados = if (ascendente) {
                    movimientos.sortedBy { it.fechaProcesado ?: it.fechaSolicitud }
                } else {
                    movimientos.sortedByDescending { it.fechaProcesado ?: it.fechaSolicitud }
                }
                _uiState.value = AdminTransactionsUIState.Success(ordenados)
            } catch (e: Exception) {
                Log.e("AdminTransactionsVM", "Error ordenando", e)
            }
        }
    }

    fun ordenarPorMonto(ascendente: Boolean = false) {
        viewModelScope.launch {
            try {
                val movimientos = (uiState.value as? AdminTransactionsUIState.Success)?.movimientos ?: return@launch
                val ordenados = if (ascendente) {
                    movimientos.sortedBy { it.monto }
                } else {
                    movimientos.sortedByDescending { it.monto }
                }
                _uiState.value = AdminTransactionsUIState.Success(ordenados)
            } catch (e: Exception) {
                Log.e("AdminTransactionsVM", "Error ordenando", e)
            }
        }
    }
}
