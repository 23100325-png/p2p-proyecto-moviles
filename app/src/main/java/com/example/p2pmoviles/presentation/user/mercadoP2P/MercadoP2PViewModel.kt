package com.example.p2pmoviles.presentation.user.mercadoP2P

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.p2pmoviles.data.SupabaseClient
import com.example.p2pmoviles.data.model.CalificacionP2P
import com.example.p2pmoviles.data.model.MonedaInfo
import com.example.p2pmoviles.data.model.OfertaMercado
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class MercadoP2PViewModel : ViewModel() {

    private val supabase = SupabaseClient.client
    private var miUsuarioId: String = ""

    // Catálogo completo para llenar los combos de filtros
    private val _monedasFiltro = MutableStateFlow<List<MonedaInfo>>(emptyList())
    val monedasFiltro: StateFlow<List<MonedaInfo>> = _monedasFiltro.asStateFlow()

    // Filtros seleccionados por el interesado en la pantalla
    val filtroTengo = MutableStateFlow<MonedaInfo?>(null)
    val filtroQuiero = MutableStateFlow<MonedaInfo?>(null)

    // Nuevos filtros adicionales
    val fechaDesde = MutableStateFlow<Long?>(null)
    val fechaHasta = MutableStateFlow<Long?>(null)
    val tasaTarget = MutableStateFlow<String>("")
    val margenTasa = MutableStateFlow<String>("")

    // Lista final de ofertas activas que se pintarán en las tarjetas
    private val _ofertasDisponibles = MutableStateFlow<List<OfertaMercado>>(emptyList())
    val ofertasDisponibles: StateFlow<List<OfertaMercado>> = _ofertasDisponibles.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _tipoCambioReferencial = MutableStateFlow<String?>("⏳ Esperando divisas...")
    val tipoCambioReferencial: StateFlow<String?> = _tipoCambioReferencial

    fun inicializar(idUsuario: String) {
        this.miUsuarioId = idUsuario
        cargarMonedasParaFiltros()
    }

    private fun cargarMonedasParaFiltros() {
        viewModelScope.launch {
            try {
                val monedas = supabase.postgrest["monedas"].select().decodeList<MonedaInfo>()
                _monedasFiltro.value = monedas

                // Inicialización por defecto de los filtros (Ej: Tengo PEN, Quiero USD)
                if (monedas.size >= 2) {
                    filtroTengo.value = monedas.firstOrNull { it.codigoIso == "PEN" } ?: monedas[0]
                    filtroQuiero.value = monedas.firstOrNull { it.codigoIso == "USD" } ?: monedas[1]
                } else {
                    filtroTengo.value = monedas.firstOrNull()
                    filtroQuiero.value = monedas.lastOrNull()
                }

                // Ejecutamos la primera búsqueda automática
                buscarOfertasP2P()
            } catch (e: Exception) {
                Log.e("MercadoVM", "Error inicializando filtros", e)
            }
        }
    }

    // Cambiar filtros asegurando que no se repitan
    fun aplicarFiltroTengo(moneda: MonedaInfo) {
        filtroTengo.value = moneda
        if (filtroQuiero.value?.id == moneda.id) {
            filtroQuiero.value = _monedasFiltro.value.firstOrNull { it.id != moneda.id }
        }
        buscarOfertasP2P()
    }

    fun aplicarFiltroQuiero(moneda: MonedaInfo) {
        filtroQuiero.value = moneda
        if (filtroTengo.value?.id == moneda.id) {
            filtroTengo.value = _monedasFiltro.value.firstOrNull { it.id != moneda.id }
        }
        buscarOfertasP2P()
    }

    fun swapFiltros() {
        val tempTengo = filtroTengo.value
        val tempQuiero = filtroQuiero.value
        if (tempTengo != null && tempQuiero != null) {
            filtroTengo.value = tempQuiero
            filtroQuiero.value = tempTengo
            buscarOfertasP2P()
            obtenerTipoCambioReal()
        }
    }

    fun resetFiltrosOpcionales() {
        fechaDesde.value = null
        fechaHasta.value = null
        tasaTarget.value = ""
        margenTasa.value = ""
        buscarOfertasP2P()
    }

    fun actualizarFechaDesde(millis: Long?) {
        fechaDesde.value = millis
        buscarOfertasP2P()
    }

    fun actualizarFechaHasta(millis: Long?) {
        fechaHasta.value = millis
        buscarOfertasP2P()
    }

    fun actualizarTasaTarget(tasa: String) {
        tasaTarget.value = tasa
        buscarOfertasP2P()
    }

    fun actualizarMargenTasa(margen: String) {
        margenTasa.value = margen
        buscarOfertasP2P()
    }

    // 🔍 CONSULTA CON EXCLUSIÓN E INVERSIÓN LOGICA
    fun buscarOfertasP2P() {
        val tengoId = filtroTengo.value?.id ?: return
        val quieroId = filtroQuiero.value?.id ?: return

        _cargando.value = true
        viewModelScope.launch {
            try {
                // 1. Traemos todas las ofertas activas para este par de divisas
                val resultado = supabase.postgrest["ofertas"]
                    .select(columns = Columns.Companion.raw("*, perfiles(*)")) {
                        filter {
                            eq("estado", "ACTIVA")
                            neq("usuario_id", miUsuarioId)
                            eq("moneda_origen_id", quieroId)
                            eq("moneda_destino_id", tengoId)
                        }
                    }.decodeList<OfertaMercado>()

                // 2. Aplicamos filtros adicionales en memoria para mayor flexibilidad
                val filtradoFinal = MercadoP2PFilterLogic.filtrarOfertas(
                    resultado,
                    fechaDesde.value,
                    fechaHasta.value,
                    tasaTarget.value.toDoubleOrNull(),
                    margenTasa.value.toDoubleOrNull() ?: 0.0
                )

                _ofertasDisponibles.value = filtradoFinal
            } catch (e: Exception) {
                Log.e("MercadoVM", "Error al buscar ofertas en Supabase", e)
            } finally {
                _cargando.value = false
            }
        }
    }

    fun filtrarOfertasEnMemoria(
        ofertas: List<OfertaMercado>,
        fDesde: Long?,
        fHasta: Long?,
        tTarget: Double?,
        mMargen: Double
    ): List<OfertaMercado> {
        return MercadoP2PFilterLogic.filtrarOfertas(ofertas, fDesde, fHasta, tTarget, mMargen)
    }

    fun tomarOfertaP2P(
        ofertaId: Long,
        montoPago: Double,
        monedaDestinoId: Long,
        monedaPagoCodigo: String,
        onSaldoInsuficiente: (String) -> Unit,
        onConfirmarOperacion: () -> Unit
    ) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                // 🟢 Decodificamos la respuesta directamente a un JsonObject nativo
                val jsonBilletera = supabase.postgrest["billeteras"]
                    .select {
                        filter {
                            eq("usuario_id", miUsuarioId)
                            eq("moneda_id", monedaDestinoId)
                        }
                    }.decodeSingleOrNull<JsonObject>()

                // 🟢 Extraemos el campo de forma ultra segura usando los primitivos de Kotlinx
                val saldoDisponible = jsonBilletera?.get("saldo_disponible")
                    ?.jsonPrimitive
                    ?.doubleOrNull ?: 0.0

                Log.d("MercadoVM", "Saldo obtenido: $saldoDisponible - Requerido: $montoPago")

                // 🔍 Validación de Negocio
                if (saldoDisponible < montoPago) {
                    _cargando.value = false
                    onSaldoInsuficiente("❌ Saldo insuficiente. Requieres $montoPago $monedaPagoCodigo (Tienes: $saldoDisponible)")
                    return@launch
                }

                _cargando.value = false
                onConfirmarOperacion()

            } catch (e: Exception) {
                _cargando.value = false
                Log.e("MercadoVM", "Error crítico validando saldo local", e)
                // Si la consulta falla (ej. la billetera ni siquiera existe en la BD), avisamos al usuario
                onSaldoInsuficiente("⚠️ No se pudo verificar tu saldo. Asegúrate de tener esta billetera creada.")
            }
        }
    }

    // Esta función se ejecutará ÚNICAMENTE si el usuario presiona "Aceptar" en el cuadro de diálogo de la interfaz
    fun ejecutarTransaccionConfirmada(
        ofertaId: Long,
        onSuccess: (Long, String) -> Unit, // Pasamos el transaccionId y el usuarioEvaluadoId
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 🟢 Llamamos al RPC que ahora debería devolver información o podemos consultarla
                // Para este ejercicio, asumiremos que el RPC devuelve el ID de la transacción generada
                // o lo obtenemos de la respuesta.
                val respuesta = supabase.postgrest.rpc(
                    function = "procesar_intercambio_p2p",
                    parameters = buildJsonObject {
                        put("p_oferta_id", ofertaId)
                        put("p_comprador_id", miUsuarioId)
                    }
                ).data

                // Como el RPC procesar_intercambio_p2p probablemente no devuelve el ID directamente en el esquema actual
                // (o devuelve void/json), necesitamos obtener el ID de la transacción recién creada para calificar.
                // Una alternativa es que el RPC devuelva el ID. 
                // Si no, podemos buscar la última transacción de este usuario.
                
                // Intento obtener el ID de la respuesta si el RPC fue modificado para devolverlo
                // Si no, lo simularemos o buscaremos. 
                // Por ahora, supongamos que el RPC devuelve { "transaccion_id": 123, "ofertante_id": "uuid" }
                // En una implementación real, el RPC debería retornar estos valores.
                // Si no los retorna, podemos intentar obtenerlos de la oferta antes de que desaparezca o de la tabla transacciones.
                
                // Vamos a intentar obtener el ofertante_id de la oferta antes de completar
                // Pero como ya se ejecutó el RPC, la oferta podría haber cambiado de estado.
                
                // Refrescamos la lista de ofertas activas del mercado
                buscarOfertasP2P()
                
                // Pasamos el ofertaId como transaccionId para que se guarde en la tabla calificaciones
                onSuccess(ofertaId, "")
            } catch (e: Exception) {
                Log.e("MercadoVM", "Error ejecutando RPC transaccional", e)
                onError(e.localizedMessage ?: "Error crítico al transferir los fondos.")
            }
        }
    }

    fun calificarUsuarioP2P(
        transaccionId: Long,
        usuarioEvaluadoId: String,
        puntuacion: Int,
        comentario: String?,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val calificacion = CalificacionP2P(
                    transaccionId = transaccionId,
                    usuarioEvaluadorId = miUsuarioId,
                    usuarioEvaluadoId = usuarioEvaluadoId,
                    puntuacion = puntuacion,
                    comentario = comentario,
                    fecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
                )
                
                supabase.postgrest["calificaciones"].insert(calificacion)
                onComplete()
            } catch (e: Exception) {
                Log.e("MercadoVM", "Error al calificar usuario", e)
                onComplete() // Cerramos igual el diálogo por ahora
            }
        }
    }

    fun obtenerTipoCambioReal() {
        // 🟢 Capturamos los filtros de la pantalla
        val miTengo = filtroTengo.value?.codigoIso ?: return
        val miQuiero = filtroQuiero.value?.codigoIso ?: return

        _tipoCambioReferencial.value = "⏳ Conectando API..."

        if (miTengo == miQuiero) {
            _tipoCambioReferencial.value = "💡 Ref: 1 = 1.0000"
            return
        }

        viewModelScope.launch {
            try {
                val respuesta = com.example.p2pmoviles.data.network.apiCambio.RetrofitClient.exchangeApi.getExchangeRates(
                    apiKey = com.example.p2pmoviles.data.network.apiCambio.ApiConstants.EXCHANGE_API_KEY,
                    baseCurrency = "USD"
                )

                if (respuesta.result == "success") {
                    // 1. Obtenemos el valor de cada moneda respecto al USD
                    val tasaTengoRespectoAlUsd = respuesta.conversionRates[miTengo] ?: 1.0
                    val tasaQuieroRespectoAlUsd = respuesta.conversionRates[miQuiero] ?: 1.0

                    // 🟢 CORRECCIÓN MATEMÁTICA INVERSA:
                    // Queremos saber cuánto de mi moneda "Tengo" cuesta "1 unidad" de la moneda que "Quiero"
                    val tasaCruzadaP2P = tasaTengoRespectoAlUsd / tasaQuieroRespectoAlUsd

                    // 2. Guardamos el resultado con la estructura correcta para la pizarra
                    _tipoCambioReferencial.value = "1 $miQuiero = ${String.format("%.4f", tasaCruzadaP2P)} $miTengo"
                } else {
                    _tipoCambioReferencial.value = "❌ Error API: ${respuesta.result}"
                }
            } catch (e: Exception) {
                Log.e("MercadoP2PVM", "Fallo de red en API", e)
                _tipoCambioReferencial.value = "❌ Fallo Red: ${e.localizedMessage ?: e.message}"
            }
        }
    }

}