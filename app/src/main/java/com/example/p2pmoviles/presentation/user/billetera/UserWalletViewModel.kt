package com.example.p2pmoviles.presentation.user.billetera

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.p2pmoviles.data.SupabaseClient
import com.example.p2pmoviles.data.model.BilleteraUsuario
import com.example.p2pmoviles.data.model.CuentaBancaria // 🟢 Asegúrate de tener este import
import com.example.p2pmoviles.data.model.MonedaInfo
import com.example.p2pmoviles.data.model.SolicitudFondoInsert
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _estaRefrescando = MutableStateFlow(false)
    val estaRefrescando: StateFlow<Boolean> = _estaRefrescando

    // 🟢 NUEVO: Estado para almacenar y exponer las cuentas bancarias del usuario
    private val _cuentasBancariasUsuario = MutableStateFlow<List<CuentaBancaria>>(emptyList())
    val cuentasBancariasUsuario: StateFlow<List<CuentaBancaria>> = _cuentasBancariasUsuario.asStateFlow()

    private var usuarioLogueadoId: String = ""

    fun inicializarUsuario(id: String) {
        usuarioLogueadoId = id
        obtenerBilleteras()
        obtenerMonedasExistentes()
        obtenerCuentasBancarias() // 🟢 Cargamos las cuentas al iniciar
    }

    fun obtenerMonedasExistentes() {
        viewModelScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["monedas"]
                    .select().decodeList<MonedaInfo>()
                _listaMonedasGlobales.value = result
            } catch (e: Exception) {
                Log.e("UserWalletVM", "Error al cargar el catálogo de monedas", e)
            }
        }
    }

    // 🟢 NUEVO: Función para traer las cuentas bancarias desde Supabase
    fun obtenerCuentasBancarias() {
        if (usuarioLogueadoId.isEmpty()) return
        viewModelScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["cuentas_bancarias"]
                    .select {
                        filter { eq("usuario_id", usuarioLogueadoId) }
                    }.decodeList<CuentaBancaria>()
                _cuentasBancariasUsuario.value = result
            } catch (e: Exception) {
                Log.e("UserWalletVM", "Error al cargar cuentas bancarias", e)
            }
        }
    }

    fun obtenerBilleteras() {
        if (usuarioLogueadoId.isEmpty()) return
        viewModelScope.launch {
            _estaRefrescando.value = true
            try {
                val result = SupabaseClient.client.postgrest["billeteras"]
                    .select(columns = Columns.raw("*, monedas(*)")) {
                        filter { eq("usuario_id", usuarioLogueadoId) }
                    }.decodeList<BilleteraUsuario>()

                _uiState.value = UserWalletState.Success(result)
            } catch (e: Exception) {
                Log.e("UserWalletVM", "Error al cargar saldos", e)
                _uiState.value = UserWalletState.Error("No se pudieron cargar los saldos.")
            } finally {
                _estaRefrescando.value = false
            }
        }
    }

    fun realizarRecarga(
        monedaId: Long,
        monto: Double,
        imageUri: Uri?,
        byteArray: ByteArray?,
        onError: (String) -> Unit,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                var rutaPublicaVoucher: String? = null

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

                val nuevaSolicitud = SolicitudFondoInsert(
                    usuarioId = usuarioLogueadoId,
                    monedaId = monedaId,
                    tipoMovimiento = "RECARGA",
                    monto = monto,
                    rutaVoucher = rutaPublicaVoucher,
                    fechaSolicitud = Instant.now().toString()
                    // cuentaBancariaId = null // (Opcional o nulo para recargas si tu modelo lo maneja)
                )

                SupabaseClient.client.postgrest["movimientos_fondos"].insert(nuevaSolicitud)

                onSuccess("¡Solicitud de recarga enviada con éxito! Esperando aprobación.")
                obtenerBilleteras()

            } catch (e: Exception) {
                Log.e("UserWalletVM", "Error crítico en recarga", e)
                onError(e.localizedMessage ?: "No se pudo registrar la recarga.")
            }
        }
    }

    // 🟢 MODIFICADO: Ahora recibe cuentaBancariaId y su onSuccess maneja String para el Snackbar
    fun realizarRetiro(
        monedaId: Long,
        monto: Double,
        cuentaBancariaId: Long,
        onError: (String) -> Unit,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val nuevaSolicitud = SolicitudFondoInsert(
                    usuarioId = usuarioLogueadoId,
                    monedaId = monedaId,
                    tipoMovimiento = "RETIRO",
                    monto = monto,
                    rutaVoucher = null,
                    fechaSolicitud = Instant.now().toString(),
                    cuentaBancariaId = cuentaBancariaId // 🟢 Enviamos la cuenta destino seleccionada
                )

                SupabaseClient.client.postgrest["movimientos_fondos"].insert(nuevaSolicitud)

                onSuccess("¡Solicitud de retiro enviada! Tus fondos han sido reservados.")
                obtenerBilleteras()
            } catch (e: Exception) {
                Log.e("UserWalletVM", "Error en retiro", e)
                onError(e.localizedMessage ?: "Error al procesar el retiro")
            }
        }
    }
}