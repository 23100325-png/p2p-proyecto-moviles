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

import com.example.p2pmoviles.data.model.BitacoraEntry
import com.example.p2pmoviles.data.model.PerfilAdmin
import com.example.p2pmoviles.data.model.ResumenOperaciones
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.async
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email

sealed class AdminUIState {
    data object Loading : AdminUIState()
    data class Success(
        val listaMovimientos: List<MovimientoAdmin>,
        val listaUsuarios: List<PerfilAdmin>,
        val bitacora: List<BitacoraEntry>,
        val resumen: ResumenOperaciones,
        val usuariosActivos: Int,
        val operacionesPendientes: Int
    ) : AdminUIState()
    data class Error(val msg: String) : AdminUIState()
}

class AdminViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUIState>(AdminUIState.Loading)
    val uiState: StateFlow<AdminUIState> = _uiState

    private val _estaRefrescando = MutableStateFlow(false)
    val estaRefrescando: StateFlow<Boolean> = _estaRefrescando

    private val _mensajeExito = MutableStateFlow<String?>(null)
    val mensajeExito: StateFlow<String?> = _mensajeExito

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError

    fun limpiarMensaje() {
        _mensajeExito.value = null
        _mensajeError.value = null
    }

    init {
        obtenerDatosCompletos()
    }

    fun obtenerDatosCompletos() {
        viewModelScope.launch {
            _estaRefrescando.value = true
            try {
                val movsDeferred = async {
                    try {
                        SupabaseClient.client.postgrest["movimientos_fondos"]
                            .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, monedas(*)")) {
                                filter { eq("estado", "PENDIENTE") }
                            }.decodeList<MovimientoAdmin>()
                    } catch (e: Exception) {
                        Log.e("AdminVM", "Error cargando movimientos (posible tabla faltante)", e)
                        emptyList<MovimientoAdmin>()
                    }
                }

                val usersDeferred = async {
                    try {
                        SupabaseClient.client.postgrest["perfiles"]
                            .select().decodeList<PerfilAdmin>()
                    } catch (e: Exception) {
                        Log.e("AdminVM", "Error cargando perfiles (posible tabla faltante)", e)
                        emptyList<PerfilAdmin>()
                    }
                }

                val bitacoraDeferred = async {
                    try {
                        SupabaseClient.client.postgrest["bitacora_admin"]
                            .select {
                                order("fecha_hora", Order.DESCENDING)
                                limit(15)
                            }.decodeList<BitacoraEntry>()
                    } catch (e: Exception) {
                        Log.e("AdminVM", "Error cargando bitácora", e)
                        emptyList<BitacoraEntry>()
                    }
                }

                // Cálculo de resumen de hoy
                val resumenDeferred = async {
                    try {
                        // Obtenemos el inicio del día actual en la zona horaria del sistema
                        // y lo convertimos a formato ISO UTC para la base de datos
                        val inicioDia = java.time.LocalDate.now()
                            .atStartOfDay(java.time.ZoneId.systemDefault())
                            .toInstant()
                            .toString()

                        val movsHoy = SupabaseClient.client.postgrest["movimientos_fondos"]
                            .select {
                                filter {
                                    eq("estado", "APROBADO")
                                    gte("fecha_procesado", inicioDia)
                                }
                            }.decodeList<MovimientoAdmin>()
                        
                        val compras = movsHoy.filter { it.tipoMovimiento == "RECARGA" }.sumOf { it.monto }
                        val ventas = movsHoy.filter { it.tipoMovimiento == "RETIRO" }.sumOf { it.monto }
                        ResumenOperaciones(compras, ventas)
                    } catch (e: Exception) {
                        Log.e("AdminVM", "Error calculando resumen", e)
                        ResumenOperaciones(0.0, 0.0)
                    }
                }

                val movs = movsDeferred.await()
                val users = usersDeferred.await()
                val bitacora = bitacoraDeferred.await()
                val resumen = resumenDeferred.await()

                _uiState.value = AdminUIState.Success(
                    listaMovimientos = movs,
                    listaUsuarios = users,
                    bitacora = bitacora,
                    resumen = resumen,
                    usuariosActivos = users.count { it.estado == "Activo" },
                    operacionesPendientes = movs.size
                )
            } catch (e: Exception) {
                Log.e("AdminVM", "Error general en obtenerDatosCompletos", e)
                _uiState.value = AdminUIState.Error("Error crítico al procesar datos.")
            } finally {
                _estaRefrescando.value = false
            }
        }
    }

    fun registrarEnBitacora(accion: String, descripcion: String) {
        viewModelScope.launch {
            try {
                // Intentar obtener el nombre o email del administrador actual
                val user = SupabaseClient.client.auth.currentUserOrNull()
                val responsable = user?.email?.split("@")?.get(0)?.replaceFirstChar { it.uppercase() } 
                    ?: "Admin"

                val entry = BitacoraEntry(
                    accion = accion,
                    descripcion = descripcion,
                    responsable = responsable,
                    fechaHora = java.time.Instant.now().toString()
                )
                SupabaseClient.client.postgrest["bitacora_admin"].insert(entry)
            } catch (e: Exception) {
                Log.e("AdminVM", "Error bitácora", e)
            }
        }
    }

    fun gestionarUsuario(usuario: PerfilAdmin, accion: String, nuevoValor: String = "") {
        viewModelScope.launch {
            try {
                when (accion) {
                    "BLOQUEAR" -> {
                        SupabaseClient.client.postgrest["perfiles"].update({
                            set("estado", "Bloqueado")
                        }) { filter { eq("id", usuario.id) } }
                        registrarEnBitacora("Bloqueo de Usuario", usuario.nombreCompleto)
                    }
                    "DESBLOQUEAR" -> {
                        SupabaseClient.client.postgrest["perfiles"].update({
                            set("estado", "Activo")
                        }) { filter { eq("id", usuario.id) } }
                        registrarEnBitacora("Desbloqueo de Usuario", usuario.nombreCompleto)
                    }
                    "ELIMINAR" -> {
                        SupabaseClient.client.postgrest["perfiles"].delete {
                            filter { eq("id", usuario.id) }
                        }
                        registrarEnBitacora("Eliminación de Usuario", usuario.nombreCompleto)
                    }
                    "CAMBIAR_ROL" -> {
                        val nuevoRol = nuevoValor.toLongOrNull() ?: 1L
                        SupabaseClient.client.postgrest["perfiles"].update({
                            set("rol_id", nuevoRol)
                        }) { filter { eq("id", usuario.id) } }
                        registrarEnBitacora("Cambio de Rol", "${usuario.nombreCompleto} -> Rol ID $nuevoRol")
                    }
                    "EDITAR_NOMBRE" -> {
                        SupabaseClient.client.postgrest["perfiles"].update({
                            set("nombre_completo", nuevoValor)
                        }) { filter { eq("id", usuario.id) } }
                        registrarEnBitacora("Edición de Perfil", "Nuevo nombre: $nuevoValor")
                    }
                }
                obtenerDatosCompletos()
            } catch (e: Exception) {
                Log.e("AdminVM", "Error gestión usuario: $accion", e)
            }
        }
    }

    fun crearUsuarioManual(nombre: String, email: String, contrasena: String, rolId: Long) {
        viewModelScope.launch {
            try {
                // 1. Crear el usuario en Supabase Auth
                val authResult = SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = contrasena
                }

                // En Supabase kt 2.5.x signUpWith retorna un objeto que contiene el 'user'
                val user = authResult
                val userId = user?.id ?: throw Exception("No se pudo obtener el ID del nuevo usuario")

                // 2. Crear el perfil asociado en la tabla perfiles
                val nuevoPerfil = PerfilAdmin(
                    id = userId,
                    nombreCompleto = nombre,
                    rolId = rolId,
                    estado = "Activo"
                )
                SupabaseClient.client.postgrest["perfiles"].insert(nuevoPerfil)

                registrarEnBitacora("Creación de Usuario", "Usuario: $nombre (Email: $email)")
                _mensajeExito.value = "Usuario '$nombre' creado con éxito"
                obtenerDatosCompletos()
            } catch (e: Exception) {
                Log.e("AdminVM", "Error al crear usuario", e)
                _mensajeError.value = "Error al crear usuario: ${e.localizedMessage ?: "desconocido"}"
            }
        }
    }

    fun procesarSolicitud(movimiento: MovimientoAdmin, aprobar: Boolean) {
        viewModelScope.launch {
            try {
                val nuevoEstado = if (aprobar) "APROBADO" else "RECHAZADO"
                val fechaActual = java.time.Instant.now().toString()

                SupabaseClient.client.postgrest["movimientos_fondos"].update({
                    set("estado", nuevoEstado)
                    set("fecha_procesado", fechaActual)
                }) {
                    filter { eq("id", movimiento.id) }
                }

                registrarEnBitacora(
                    if (aprobar) "Aprobación de Fondos" else "Rechazo de Fondos",
                    "Monto: ${movimiento.monto}"
                )

                obtenerDatosCompletos()
            } catch (e: Exception) {
                Log.e("AdminVM", "Error al procesar la acción", e)
            }
        }
    }
}
