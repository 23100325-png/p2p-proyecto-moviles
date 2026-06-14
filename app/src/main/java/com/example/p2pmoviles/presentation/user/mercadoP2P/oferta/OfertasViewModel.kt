package com.example.p2pmoviles.presentation.user.mercadoP2P.oferta

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.p2pmoviles.data.SupabaseClient
import com.example.p2pmoviles.data.model.BilleteraUsuario
import com.example.p2pmoviles.data.model.MonedaInfo
import com.example.p2pmoviles.data.model.OfertaInsert
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class OfertasViewModel : ViewModel() {

    //val tipoCambioReferencial = MutableStateFlow<Double?>(null)
    val tipoCambioReferencial = MutableStateFlow<String?>("⏳ Conectando API...")
    private val supabase = SupabaseClient.client
    private var usuarioId: String = ""

    // --- Estados del Servidor ---
    private val _billeterasUsuario = MutableStateFlow<List<BilleteraUsuario>>(emptyList())
    val billeterasUsuario: StateFlow<List<BilleteraUsuario>> = _billeterasUsuario.asStateFlow()

    private val _monedasGlobales = MutableStateFlow<List<MonedaInfo>>(emptyList())
    val monedasGlobales: StateFlow<List<MonedaInfo>> = _monedasGlobales.asStateFlow()

    // --- Estados del Formulario (Campos dinámicos) ---
    val monedaTengo = MutableStateFlow<BilleteraUsuario?>(null)
    val monedaQuiero = MutableStateFlow<MonedaInfo?>(null)

    val montoOfertarText = MutableStateFlow("")
    val tipoCambioText = MutableStateFlow("")
    val notasAdicionalesText = MutableStateFlow("")
    val terminosAceptados = MutableStateFlow(false)

    // --- Inicialización ---
    fun inicializar(idUsuario: String) {
        this.usuarioId = idUsuario
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            try {
                // 1. Cargar todas las billeteras que el usuario "Tiene" con saldos
                val billeterasResult = supabase.postgrest["billeteras"]
                    .select(columns = Columns.Companion.raw("*, monedas(*)")) {
                        filter { eq("usuario_id", usuarioId) }
                    }.decodeList<BilleteraUsuario>()
                _billeterasUsuario.value = billeterasResult
                monedaTengo.value = billeterasResult.firstOrNull()

                // 2. Cargar todo el catálogo de monedas para la opción de "Quiero"
                val monedasResult = supabase.postgrest["monedas"]
                    .select().decodeList<MonedaInfo>()
                _monedasGlobales.value = monedasResult

                // Inicializamos "Quiero" con la primera moneda que NO sea la que ya tiene seleccionada en Tengo
                monedaQuiero.value = monedasResult.firstOrNull { it.id != monedaTengo.value?.monedaId }
            } catch (e: Exception) {
                Log.e("OfertasVM", "Error cargando catálogos del P2P", e)
            }
        }
    }

    // --- Funciones de Cambio de Moneda con Filtros Excluyentes ---
    fun seleccionarMonedaTengo(billetera: BilleteraUsuario) {
        monedaTengo.value = billetera
        // Regla: Si "Quiero" quedó igual a "Tengo", movemos "Quiero" a otra moneda disponible
        if (monedaQuiero.value?.id == billetera.monedaId) {
            monedaQuiero.value = _monedasGlobales.value.firstOrNull { it.id != billetera.monedaId }
        }
    }

    fun seleccionarMonedaQuiero(moneda: MonedaInfo) {
        monedaQuiero.value = moneda
    }

    // --- Acción Principal: Publicar la Oferta (Bancos Removidos) ---
    fun publicarOferta(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val mTengo = monedaTengo.value ?: return onError("Selecciona la moneda que tienes.")
        val mQuiero = monedaQuiero.value ?: return onError("Selecciona la moneda que quieres.")
        val monto = montoOfertarText.value.toDoubleOrNull() ?: return onError("Monto inválido.")
        val tasa = tipoCambioText.value.toDoubleOrNull() ?: return onError("Tipo de cambio inválido.")

        // Validamos la regla del saldo disponible antes de ir a Supabase
        if (monto > mTengo.saldoDisponible) {
            return onError("Saldo insuficiente. Tu saldo disponible es de ${mTengo.saldoDisponible}")
        }

        if (!terminosAceptados.value) {
            return onError("Debes aceptar los términos y condiciones.")
        }

        viewModelScope.launch {
            try {
                val nuevaOferta = OfertaInsert(
                    usuarioId = usuarioId,
                    monedaOrigenId = mTengo.monedaId,
                    monedaDestinoId = mQuiero.id,
                    montoOrigen = monto,
                    tasaCambio = tasa,
                    fechaPublicacion = Instant.now().toString()
                )

                supabase.postgrest["ofertas"].insert(nuevaOferta)

                // Limpiar formulario tras éxito
                montoOfertarText.value = ""
                tipoCambioText.value = ""
                notasAdicionalesText.value = ""
                terminosAceptados.value = false

                onSuccess("¡Oferta P2P publicada con éxito!")
            } catch (e: Exception) {
                Log.e("OfertasVM", "Error al insertar oferta en Supabase", e)
                onError("Error en el servidor: ${e.localizedMessage}")
            }
        }
    }

    fun obtenerTipoCambioReal() {
        val base = monedaTengo.value?.monedas?.codigoIso ?: return
        val destino = monedaQuiero.value?.codigoIso ?: return

        tipoCambioReferencial.value = "⏳ Conectando API..."

        if (base == destino) {
            tipoCambioReferencial.value = "💡 Ref: 1 = 1.0000"
            return
        }

        viewModelScope.launch {
            try {
                val respuesta = com.example.p2pmoviles.data.network.apiCambio.RetrofitClient.exchangeApi.getExchangeRates(
                    apiKey = com.example.p2pmoviles.data.network.apiCambio.ApiConstants.EXCHANGE_API_KEY,
                    baseCurrency = "USD"
                )

                if (respuesta.result == "success") {
                    val tasaBaseRespectoAlUsd = respuesta.conversionRates[base] ?: 1.0
                    val tasaDestinoRespectoAlUsd = respuesta.conversionRates[destino] ?: 1.0
                    val tasaCruzadaFinal = tasaDestinoRespectoAlUsd / tasaBaseRespectoAlUsd

                    // Guardamos el resultado como texto formateado
                    tipoCambioReferencial.value = "💡 Ref: 1 = ${String.format("%.4f", tasaCruzadaFinal)} $destino"
                } else {
                    tipoCambioReferencial.value = "❌ Error API: ${respuesta.result}"
                }
            } catch (e: Exception) {
                // 🟢 ¡AQUÍ ESTÁ LA MAGIA! Si la API falla, verás el motivo real escrito en tu pantalla
                tipoCambioReferencial.value = "❌ Fallo Red: ${e.localizedMessage ?: e.message}"
            }
        }
    }
}