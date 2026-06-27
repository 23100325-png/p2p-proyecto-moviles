package com.example.p2pmoviles.presentation.user.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.p2pmoviles.data.SupabaseClient
import com.example.p2pmoviles.data.model.OfertaMercado
import com.example.p2pmoviles.presentation.user.profile.PerfilUsuario
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {
    private val supabase = SupabaseClient.client
    private var usuarioId: String = ""

    private val _completedOffers = MutableStateFlow<List<OfertaMercado>>(emptyList())
    val completedOffers: StateFlow<List<OfertaMercado>> = _completedOffers.asStateFlow()

    private val _perfil = MutableStateFlow<PerfilUsuario?>(null)
    val perfil: StateFlow<PerfilUsuario?> = _perfil.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showAll = MutableStateFlow(false)
    val showAll: StateFlow<Boolean> = _showAll.asStateFlow()

    private val _selectedOffer = MutableStateFlow<OfertaMercado?>(null)
    val selectedOffer: StateFlow<OfertaMercado?> = _selectedOffer.asStateFlow()

    fun inicializar(idUsuario: String) {
        if (idUsuario.isEmpty()) return
        // 🟢 CORRECCIÓN 2: Evita bucles infinitos de recarga si el ID es el mismo
        if (idUsuario == this.usuarioId && _completedOffers.value.isNotEmpty()) return

        this.usuarioId = idUsuario
        fetchCompletedOffers()
        fetchPerfil()
    }

    fun fetchCompletedOffers() {
        if (usuarioId.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Traemos las ofertas completadas donde el usuario participó (como creador o comprador)
                val result = supabase.postgrest["ofertas"]
                    .select(Columns.raw("*, perfiles!usuario_id(*), monedas!moneda_origen_id(*)")) {
                        filter {
                            eq("estado", "COMPLETADA")
                            or {
                                eq("usuario_id", usuarioId)
                                eq("comprador_id", usuarioId)
                            }
                        }
                        order("fecha_intercambio", Order.DESCENDING)
                    }.decodeList<OfertaMercado>()
                    .filter { offer ->
                        // 🟢 CORRECCIÓN: En matching automático se generan dos filas en la BD (una por cada oferta matcheada).
                        // Para evitar duplicados y no mostrar la oferta de la contraparte como propia,
                        // solo mostramos la fila donde el usuario es el creador (usuario_id).
                        if (offer.tipoMatch == "AUTOMATICO") {
                            offer.usuarioId == usuarioId
                        } else {
                            // En matching manual (una sola fila), se muestra siempre ya que el usuario es parte activa.
                            true
                        }
                    }

                _completedOffers.value = result
                Log.d("NotificationsVM", "Ofertas cargadas con éxito: ${result.size}")
            } catch (e: Exception) {
                Log.e("NotificationsVM", "Error al traer ofertas: ${e.message}", e)
                _completedOffers.value = emptyList() // Asegura limpiar la lista si hay error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchPerfil() {
        if (usuarioId.isEmpty()) return
        viewModelScope.launch {
            try {
                val result = supabase.postgrest["perfiles"]
                    .select {
                        filter { eq("id", usuarioId) }
                    }.decodeSingleOrNull<PerfilUsuario>()
                _perfil.value = result
            } catch (e: Exception) {
                Log.e("NotificationsVM", "Error fetching profile", e)
            }
        }
    }

    fun toggleMatchingAutomatico(activo: Boolean) {
        if (usuarioId.isEmpty()) return

        // Estrategia optimista para que el Switch de la Screen responda al instante
        val perfilPrevio = _perfil.value
        _perfil.value = _perfil.value?.copy(matchingAutomaticoActivo = activo)

        viewModelScope.launch {
            try {
                supabase.postgrest["perfiles"].update(
                    mapOf("matching_automatico_activo" to activo)
                ) {
                    filter { eq("id", usuarioId) }
                }
            } catch (e: Exception) {
                Log.e("NotificationsVM", "Error updating matching auto", e)
                _perfil.value = perfilPrevio // Si falla la red, revierte el Switch
            }
        }
    }

    fun setShowAll(value: Boolean) {
        _showAll.value = value
    }

    fun setSelectedOffer(offer: OfertaMercado?) {
        _selectedOffer.value = offer
    }
}