package com.example.p2pmoviles.presentation.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.p2pmoviles.data.SupabaseClient
import com.example.p2pmoviles.data.model.MonedaInfo
import com.example.p2pmoviles.data.model.OfertaMercado

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
                // Realizamos el JOIN para jalar el nombre del usuario y datos de su cuenta bancaria
                val resultado = supabase.postgrest["ofertas"]
                    .select(columns = Columns.raw("*, perfiles(*), cuentas_bancarias(*)")) {
                        filter {
                            eq("estado", "ACTIVA")
                            // 1. Evitamos que el usuario vea sus propias publicaciones
                            neq("usuario_id", miUsuarioId)

                            // 2. Lógica Invertida:
                            // Lo que el creador vende (moneda_origen) debe ser lo que el interesado QUIERE
                            eq("moneda_origen_id", quieroId)
                            // Lo que el creador pide (moneda_destino) debe ser lo que el interesado TIENE
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
}