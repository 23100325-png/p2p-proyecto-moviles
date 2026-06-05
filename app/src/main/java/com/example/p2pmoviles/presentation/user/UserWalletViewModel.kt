package com.example.p2pmoviles.presentation.user

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.p2pmoviles.data.SupabaseClient
import com.example.p2pmoviles.data.model.BilleteraUsuario
import com.example.p2pmoviles.data.model.MonedaInfo
import com.example.p2pmoviles.data.model.SolicitudFondoInsert
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant

sealed class UserWalletState {
    data object Loading : UserWalletState()
    data class Success(val billeteras: List<BilleteraUsuario>) : UserWalletState()
    data class Error(val msg: String) : UserWalletState()
}

class UserWalletViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UserWalletState>(UserWalletState.Loading)
    val uiState: StateFlow<UserWalletState> = _uiState

    private val _listaMonedasGlobales = MutableStateFlow<List<MonedaInfo>>(emptyList())
    val listaMonedasGlobales: StateFlow<List<MonedaInfo>> = _listaMonedasGlobales

    // 🟢 AGREGA ESTO EN TU USERWALLETVIEWMODEL (junto a tus otras variables de estado)
    private val _estaRefrescando = MutableStateFlow(false)
    val estaRefrescando: StateFlow<Boolean> = _estaRefrescando

    // Reemplaza esto con el ID real del usuario logueado (puedes pasarlo desde el AuthViewModel)
    private var usuarioLogueadoId: String = ""

    fun obtenerMonedasExistentes() {
        viewModelScope.launch {
            try {
                // Consultamos directamente la tabla de monedas generales
                val result = SupabaseClient.client.postgrest["monedas"]
                    .select().decodeList<MonedaInfo>()

                _listaMonedasGlobales.value = result
            } catch (e: Exception) {
                Log.e("UserWalletVM", "Error al cargar el catálogo de monedas", e)
            }
        }
    }

    fun inicializarUsuario(id: String) {
        usuarioLogueadoId = id
        obtenerBilleteras()
        obtenerMonedasExistentes()
    }

    fun obtenerBilleteras() {
        if (usuarioLogueadoId.isEmpty()) return
        viewModelScope.launch {
            // Si ya hay datos y es un refresco manual, activamos la animación superior
            _estaRefrescando.value = true
            try {
                val result = SupabaseClient.client.postgrest["billeteras"]
                    .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, monedas(*)")) {
                        filter { eq("usuario_id", usuarioLogueadoId) }
                    }.decodeList<BilleteraUsuario>()

                _uiState.value = UserWalletState.Success(result)
            } catch (e: Exception) {
                Log.e("UserWalletVM", "Error al cargar saldos", e)
                _uiState.value = UserWalletState.Error("No se pudieron cargar los saldos.")
            } finally {
                // 🟢 Al terminar la consulta (sea éxito o error), apagamos la animación de jalar
                _estaRefrescando.value = false
            }
        }
    }
    fun realizarRecarga(
        monedaId: Long,
        monto: Double,
        imageUri: Uri?,
        byteArray: ByteArray?,
        onError: (String) -> Unit,    // 🟢 ESTO ES LO QUE EL COMPILADOR DICE QUE FALTA
        onSuccess: (String) -> Unit   // 🟢 ESTO ES LO QUE EL COMPILADOR DICE QUE FALTA
    ) {
        viewModelScope.launch {
            try {
                var rutaPublicaVoucher: String? = null

                // 1. Proceso de Almacenamiento protegido
                if (byteArray != null && imageUri != null) {
                    try {
                        val nombreArchivo = "voucher_${usuarioLogueadoId}_${Instant.now().toEpochMilli()}.jpg"
                        val bucket = SupabaseClient.client.storage.from("vouchers")

                        bucket.upload(path = nombreArchivo, data = byteArray)
                        rutaPublicaVoucher = bucket.publicUrl(nombreArchivo)
                        Log.d("StorageSuccess", "Imagen subida correctamente: $rutaPublicaVoucher")
                    } catch (storageError: Exception) {
                        Log.e("StorageError", "Fallo al subir la imagen al Storage.", storageError)
                    }
                }

                // 2. Proceso de Inserción
                val nuevaSolicitud = SolicitudFondoInsert(
                    usuarioId = usuarioLogueadoId,
                    monedaId = monedaId,
                    tipoMovimiento = "RECARGA",
                    monto = monto,
                    rutaVoucher = rutaPublicaVoucher,
                    fechaSolicitud = java.time.Instant.now().toString()
                )

                SupabaseClient.client.postgrest["movimientos_fondos"].insert(nuevaSolicitud)

                // 3. Notificar éxito a la pantalla
                onSuccess("¡Solicitud de recarga enviada con éxito! Esperando aprobación.")
                obtenerBilleteras()

            } catch (e: Exception) {
                Log.e("UserWalletVM", "Error crítico en recarga", e)
                onError(e.localizedMessage ?: "No se pudo registrar la recarga.")
            }
        }
    }

    fun realizarRetiro(monedaId: Long, monto: Double, onError: (String) -> Unit, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val nuevaSolicitud = SolicitudFondoInsert(
                    usuarioId = usuarioLogueadoId,
                    monedaId = monedaId,
                    tipoMovimiento = "RETIRO",
                    monto = monto,
                    rutaVoucher = null, // Retiros no llevan voucher
                    fechaSolicitud = Instant.now().toString()
                )

                SupabaseClient.client.postgrest["movimientos_fondos"].insert(nuevaSolicitud)
                onSuccess()
                obtenerBilleteras() // Actualiza los saldos (Disponible y Bloqueado cambian por el Trigger)
            } catch (e: Exception) {
                Log.e("UserWalletVM", "Error en retiro", e)
                // Si el trigger de saldo insuficiente salta, capturamos el mensaje de error de Supabase
                onError(e.localizedMessage ?: "Error al procesar el retiro")
            }
        }
    }


}