package com.example.p2pmoviles.presentation.user.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.p2pmoviles.data.SupabaseClient
import com.example.p2pmoviles.data.model.CalificacionP2P
import com.example.p2pmoviles.data.model.CuentaBancaria
import com.example.p2pmoviles.data.model.MonedaInfo
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserUpdateBuilder
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class PerfilUsuario(
    val id: String,
    @SerialName("nombre_completo") val nombreCompleto: String,
    val nombre: String? = null,
    val email: String? = null,
    @SerialName("matching_automatico_activo") val matchingAutomaticoActivo: Boolean = true
)

class ProfileViewModel : ViewModel() {

    private val _perfil = MutableStateFlow<PerfilUsuario?>(null)
    val perfil: StateFlow<PerfilUsuario?> = _perfil.asStateFlow()

    // 🟢 NUEVO: Estado para la reputación del usuario
    private val _rating = MutableStateFlow<Pair<Double, Int>>(Pair(0.0, 0))
    val rating: StateFlow<Pair<Double, Int>> = _rating.asStateFlow()

    private val _monedas = MutableStateFlow<List<MonedaInfo>>(emptyList())
    val monedas: StateFlow<List<MonedaInfo>> = _monedas.asStateFlow()

    private val _cuentasBancarias = MutableStateFlow<List<CuentaBancaria>>(emptyList())
    val cuentasBancarias: StateFlow<List<CuentaBancaria>> = _cuentasBancarias.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    fun cargarDatos(userId: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                // 1. Cargar perfil desde la tabla 'perfiles'
                val perfilDb = SupabaseClient.client.postgrest["perfiles"]
                    .select {
                        filter { eq("id", userId) }
                    }.decodeSingleOrNull<PerfilUsuario>()
                
                // 2. El email viene de Auth
                val authUser = SupabaseClient.client.auth.currentUserOrNull()
                _perfil.value = perfilDb?.copy(email = authUser?.email)

                // 3. 🟢 CARGAR REPUTACIÓN REAL (Promedio y Conteo)
                try {
                    val calificaciones = SupabaseClient.client.postgrest["calificaciones"]
                        .select {
                            filter { eq("usuario_evaluado_id", userId) }
                        }.decodeList<CalificacionP2P>()

                    if (calificaciones.isNotEmpty()) {
                        val promedio = calificaciones.map { it.puntuacion }.average()
                        val total = calificaciones.size
                        _rating.value = Pair(promedio, total)
                    } else {
                        _rating.value = Pair(0.0, 0)
                    }
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error cargando calificaciones", e)
                }
                
                // 4. Cargar monedas para el diálogo de cuentas bancarias
                val listaMonedas = SupabaseClient.client.postgrest["monedas"]
                    .select().decodeList<MonedaInfo>()
                _monedas.value = listaMonedas

                // 5. Cargar cuentas bancarias del usuario con su información de moneda (Join)
                val listaCuentas = SupabaseClient.client.postgrest["cuentas_bancarias"]
                    .select(io.github.jan.supabase.postgrest.query.Columns.raw("*, monedas(*)")) {
                        filter { eq("usuario_id", userId) }
                    }.decodeList<CuentaBancaria>()
                _cuentasBancarias.value = listaCuentas

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al cargar datos", e)
                _mensaje.value = "Error al cargar datos: ${e.localizedMessage}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun actualizarPerfil(userId: String, nuevoNombreCompleto: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                // 1. Actualizar tabla 'perfiles'
                SupabaseClient.client.postgrest["perfiles"].update(
                    {
                        set("nombre_completo", nuevoNombreCompleto)
                    }
                ) {
                    filter { eq("id", userId) }
                }

                _mensaje.value = "Perfil actualizado correctamente."
                cargarDatos(userId)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al actualizar perfil", e)
                _mensaje.value = "Error: ${e.localizedMessage}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun agregarCuentaBancaria(cuenta: CuentaBancaria) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                SupabaseClient.client.postgrest["cuentas_bancarias"].insert(cuenta)
                _mensaje.value = "Cuenta bancaria agregada con éxito."
                _perfil.value?.id?.let { cargarDatos(it) }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al agregar cuenta", e)
                _mensaje.value = "Error al agregar cuenta: ${e.localizedMessage}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun eliminarCuentaBancaria(cuentaId: Long) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                SupabaseClient.client.postgrest["cuentas_bancarias"].delete {
                    filter { eq("id", cuentaId) }
                }
                _mensaje.value = "Cuenta eliminada."
                _perfil.value?.id?.let { cargarDatos(it) }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al eliminar cuenta", e)
                _mensaje.value = "Error al eliminar cuenta: ${e.localizedMessage}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
