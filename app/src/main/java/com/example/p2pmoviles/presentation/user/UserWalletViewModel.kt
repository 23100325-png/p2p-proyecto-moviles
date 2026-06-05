package com.example.p2pmoviles.presentation.user

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.p2pmoviles.data.SupabaseClient
import com.example.p2pmoviles.data.model.BilleteraUsuario
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

    // Reemplaza esto con el ID real del usuario logueado (puedes pasarlo desde el AuthViewModel)
    private var usuarioLogueadoId: String = ""

    fun inicializarUsuario(id: String) {
        usuarioLogueadoId = id
        obtenerBilleteras()
    }

    fun obtenerBilleteras() {
        if (usuarioLogueadoId.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = UserWalletState.Loading
            try {
                // Consulta relacional: Trae las billeteras del usuario con su respectiva moneda
                val result = SupabaseClient.client.postgrest["billeteras"]
                    .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, monedas(*)")) {
                        filter { eq("usuario_id", usuarioLogueadoId) }
                    }.decodeList<BilleteraUsuario>()

                _uiState.value = UserWalletState.Success(result)
            } catch (e: Exception) {
                Log.e("UserWalletVM", "Error al cargar saldos", e)
                _uiState.value = UserWalletState.Error("No se pudieron cargar los saldos.")
            }
        }
    }

    // 🟢 FUNCIÓN CLAVE: Sube la imagen al Storage y luego guarda el registro en la tabla
    fun realizarRecarga(monedaId: Long, monto: Double, imageUri: Uri?, byteArray: ByteArray?) {
        viewModelScope.launch {
            try {
                var rutaPublicaVoucher: String? = null

                // 1. Si el usuario seleccionó una imagen, la subimos al Storage de Supabase
                if (byteArray != null && imageUri != null) {
                    val nombreArchivo = "voucher_${usuarioLogueadoId}_${Instant.now().toEpochMilli()}.jpg"

                    // Asegúrate de tener creado un bucket llamado "vouchers" público en tu Storage de Supabase
                    val bucket = SupabaseClient.client.storage.from("vouchers")
                    bucket.upload(path = nombreArchivo, data = byteArray)

                    // Obtenemos la URL pública del archivo subido
                    rutaPublicaVoucher = bucket.publicUrl(nombreArchivo)
                }

                // 2. Insertamos la fila en movimientos_fondos
                val nuevaSolicitud = SolicitudFondoInsert(
                    usuarioId = usuarioLogueadoId,
                    monedaId = monedaId,
                    tipoMovimiento = "RECARGA",
                    monto = monto,
                    rutaVoucher = rutaPublicaVoucher,
                    fechaSolicitud = Instant.now().toString()
                )

                SupabaseClient.client.postgrest["movimientos_fondos"].insert(nuevaSolicitud)

                // 3. Recargamos la UI para ver cualquier cambio
                obtenerBilleteras()
            } catch (e: Exception) {
                Log.e("UserWalletVM", "Error en recarga", e)
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