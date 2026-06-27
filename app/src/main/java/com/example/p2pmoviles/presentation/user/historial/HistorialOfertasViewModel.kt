package com.example.p2pmoviles.presentation.user.historial

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.p2pmoviles.data.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MonedaRelacion(
    val id: Long,
    @SerialName("codigo_iso") val codigoIso: String,
    val simbolo: String,
    val nombre: String
)

@Serializable
data class PerfilRelacion(
    @SerialName("nombre_completo") val nombreCompleto: String
)

@Serializable
data class PerfilMapa(
    val id: String,
    @SerialName("nombre_completo") val nombreCompleto: String
) {
    fun toPerfilRelacion() = PerfilRelacion(nombreCompleto)
}

@Serializable
data class OfertaDb(
    val id: Long,
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("moneda_origen_id") val monedaOrigenId: Long,
    @SerialName("moneda_destino_id") val monedaDestinoId: Long,
    @SerialName("monto_origen") val montoOrigen: Double,
    @SerialName("tasa_cambio") val tasaCambio: Double,
    val estado: String,
    @SerialName("fecha_publicacion") val fechaPublicacion: String,
    val comentario: String? = null,
    @SerialName("comprador_id") val compradorId: String? = null,
    @SerialName("fecha_intercambio") val fechaIntercambio: String? = null,
    // Relaciones anidadas
    @SerialName("moneda_origen") val monedaOrigen: MonedaRelacion? = null,
    @SerialName("moneda_destino") val monedaDestino: MonedaRelacion? = null,
    @SerialName("perfil_comprador") val perfilComprador: PerfilRelacion? = null,
    @SerialName("perfil_ofertante") val perfilOfertante: PerfilRelacion? = null
) {
    // 🟢 CAMPOS CALCULADOS DINÁMICOS (Vista como Ofertante)
    val textoVendi: String get() = "${monedaOrigen?.simbolo ?: ""} ${String.format("%.2f", montoOrigen)}"
    val textoRecibi: String get() = "${monedaDestino?.simbolo ?: ""} ${String.format("%.2f", montoOrigen * tasaCambio)}"

    // 🟢 CAMPOS CALCULADOS DINÁMICOS (Vista como Comprador)
    val textoPague: String get() = "${monedaDestino?.simbolo ?: ""} ${String.format("%.2f", montoOrigen * tasaCambio)}"
    val textoRecibiComoComprador: String get() = "${monedaOrigen?.simbolo ?: ""} ${String.format("%.2f", montoOrigen)}"
}

class HistorialOfertasViewModel : ViewModel() {

    private val _ofertas = MutableStateFlow<List<OfertaDb>>(emptyList())
    val ofertas: StateFlow<List<OfertaDb>> = _ofertas.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _esModoOfertante = MutableStateFlow(true)
    val esModoOfertante: StateFlow<Boolean> = _esModoOfertante.asStateFlow()

    // 🟢 NUEVOS ESTADOS PARA FILTRO DE FECHAS
    private val _fechaDesde = MutableStateFlow<Long?>(null)
    val fechaDesde: StateFlow<Long?> = _fechaDesde.asStateFlow()

    private val _fechaHasta = MutableStateFlow<Long?>(null)
    val fechaHasta: StateFlow<Long?> = _fechaHasta.asStateFlow()

    fun actualizarFechaDesde(millis: Long?) {
        _fechaDesde.value = millis
        // Si la nueva fecha 'desde' es mayor que la 'hasta' actual, reseteamos 'hasta'
        if (millis != null && _fechaHasta.value != null && millis > _fechaHasta.value!!) {
            _fechaHasta.value = null
        }
    }

    fun actualizarFechaHasta(millis: Long?) {
        // Validación: Solo permitimos que 'hasta' sea mayor o igual a 'desde'
        val desde = _fechaDesde.value
        if (desde != null && millis != null && millis < desde) {
            _mensaje.value = "La fecha 'Hasta' no puede ser anterior a 'Desde'."
            return
        }
        _fechaHasta.value = millis
    }

    fun limpiarFiltrosFechas() {
        _fechaDesde.value = null
        _fechaHasta.value = null
    }

    fun setModoOfertante(esOfertante: Boolean, usuarioId: String) {
        _esModoOfertante.value = esOfertante
        cargarOfertasDelUsuario(usuarioId)
    }

    fun inicializar(usuarioLogueadoId: String) {
        cargarOfertasDelUsuario(usuarioLogueadoId)
    }

    private fun cargarOfertasDelUsuario(usuarioId: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val modoOfertante = _esModoOfertante.value

                val resultado = SupabaseClient.client.postgrest["ofertas"]
                    .select(Columns.raw("*, moneda_origen:monedas!moneda_origen_id(*), moneda_destino:monedas!moneda_destino_id(*)")) {
                        filter {
                            if (modoOfertante) {
                                eq("usuario_id", usuarioId)
                            } else {
                                eq("comprador_id", usuarioId)
                            }
                        }
                    }.decodeList<OfertaDb>()
                
                // Obtener perfiles adicionales que Supabase no une automáticamente
                val idsParaPerfiles = if (modoOfertante) {
                    resultado.mapNotNull { it.compradorId }.distinct()
                } else {
                    resultado.map { it.usuarioId }.distinct()
                }
                
                val perfilesMap = if (idsParaPerfiles.isNotEmpty()) {
                    try {
                        SupabaseClient.client.postgrest["perfiles"]
                            .select(Columns.raw("id, nombre_completo")) {
                                filter {
                                    isIn("id", idsParaPerfiles)
                                }
                            }.decodeList<PerfilMapa>().associateBy { it.id }
                    } catch (e: Exception) {
                        Log.e("HistorialVM", "Error al traer perfiles agrupados", e)
                        emptyMap()
                    }
                } else {
                    emptyMap()
                }

                val ofertasFinales = resultado.map { oferta ->
                    if (modoOfertante) {
                        oferta.copy(perfilComprador = perfilesMap[oferta.compradorId]?.toPerfilRelacion())
                    } else {
                        oferta.copy(perfilOfertante = perfilesMap[oferta.usuarioId]?.toPerfilRelacion())
                    }
                }

                _ofertas.value = ofertasFinales

            } catch (e: Exception) {
                Log.e("HistorialVM", "Error al traer ofertas de la base de datos", e)
            } finally {
                _cargando.value = false
            }
        }
    }

    fun cancelarOferta(ofertaId: Long, usuarioId: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                SupabaseClient.client.postgrest["ofertas"].update(
                    {
                        set("estado", "CANCELADA")
                    }
                ) {
                    filter {
                        eq("id", ofertaId)
                        eq("estado", "ACTIVA")
                    }
                }
                _mensaje.value = "Oferta cancelada con éxito."
                cargarOfertasDelUsuario(usuarioId)
            } catch (e: Exception) {
                Log.e("HistorialVM", "Error al cancelar oferta", e)
                _mensaje.value = "Error al cancelar: ${e.localizedMessage}"
            } finally {
                _cargando.value = false
            }
        }
    }

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
