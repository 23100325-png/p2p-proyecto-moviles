package com.example.p2pmoviles.presentation.user.mercadoP2P

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.p2pmoviles.data.SupabaseClient
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

    // 🔍 CONSULTA CON EXCLUSIÓN E INVERSIÓN LOGICA
    fun buscarOfertasP2P() {
        val tengoId = filtroTengo.value?.id ?: return
        val quieroId = filtroQuiero.value?.id ?: return

        _cargando.value = true
        viewModelScope.launch {
            try {
                // 🟢 Eliminamos cuentas_bancarias(*) para evitar que rompa la consulta
                val resultado = supabase.postgrest["ofertas"]
                    .select(columns = Columns.Companion.raw("*, perfiles(*)")) {
                        filter {
                            eq("estado", "ACTIVA")
                            neq("usuario_id", miUsuarioId)
                            eq("moneda_origen_id", quieroId)
                            eq("moneda_destino_id", tengoId)
                        }
                    }.decodeList<OfertaMercado>()

                _ofertasDisponibles.value = resultado
            } catch (e: Exception) {
                Log.e("MercadoVM", "Error al buscar ofertas en Supabase", e)
            } finally {
                _cargando.value = false
            }
        }
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
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 🟢 Corregido: Usamos buildJsonObject en lugar de mapOf
                supabase.postgrest.rpc(
                    function = "procesar_intercambio_p2p",
                    parameters = buildJsonObject {
                        put("p_oferta_id", ofertaId)
                        put("p_comprador_id", miUsuarioId)
                    }
                )

                // Refrescamos la lista de ofertas activas del mercado
                buscarOfertasP2P()
                onSuccess()
            } catch (e: Exception) {
                Log.e("MercadoVM", "Error ejecutando RPC transaccional", e)
                onError(e.localizedMessage ?: "Error crítico al transferir los fondos.")
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