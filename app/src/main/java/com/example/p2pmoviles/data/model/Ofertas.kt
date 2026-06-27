package com.example.p2pmoviles.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OfertaInsert(
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("moneda_origen_id") val monedaOrigenId: Long,
    @SerialName("moneda_destino_id") val monedaDestinoId: Long,
    @SerialName("monto_origen") val montoOrigen: Double,
    @SerialName("tasa_cambio") val tasaCambio: Double,
    @SerialName("estado") val estado: String = "PENDIENTE",
    @SerialName("fecha_publicacion") val fechaPublicacion: String,
    @SerialName("comentario") val comentario: String? = null
)@Serializable
data class OfertaMercado(
    @SerialName("id") val id: Long,
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("moneda_origen_id") val monedaOrigenId: Long,
    @SerialName("moneda_destino_id") val monedaDestinoId: Long,
    @SerialName("monto_origen") val montoOrigen: Double,
    @SerialName("tasa_cambio") val tasaCambio: Double,
    @SerialName("estado") val estado: String,
    @SerialName("fecha_publicacion") val fechaPublicacion: String,
    @SerialName("comentario") val comentario: String? = null,
    @SerialName("comprador_id") val compradorId: String? = null,
    @SerialName("fecha_intercambio") val fechaIntercambio: String? = null,
    @SerialName("tipo_match") val tipoMatch: String? = null,

    // Relaciones
    @SerialName("perfiles") val ofertanteInfo: OfertantePerfil? = null,
    @SerialName("monedas") val monedaInfo: MonedaInfo? = null
)

@Serializable
data class OfertantePerfil(
    @SerialName("nombre_completo") val nombre: String,
    @SerialName("calificacion") val calificacion: Double = 5.0,
    @SerialName("total_operaciones") val totalOperaciones: Int = 120,
    @SerialName("matching_automatico_activo") val matchingAutomaticoActivo: Boolean = true
)
