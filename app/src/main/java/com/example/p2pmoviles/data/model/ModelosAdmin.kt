package com.example.p2pmoviles.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class MonedaInfo(
    val id: Long,
    @SerialName("codigo_iso") val codigoIso: String,
    val nombre: String,
    val simbolo: String,
    @SerialName("ruta_bandera") val rutaBandera: String
)

@Serializable
data class MovimientoAdmin(
    val id: Long,
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("moneda_id") val monedaId: Long,
    @SerialName("tipo_movimiento") val tipoMovimiento: String, // "RECARGA" o "RETIRO"
    val monto: Double,
    @SerialName("ruta_voucher") val rutaVoucher: String?,
    val estado: String, // "PENDIENTE", "APROBADO", "RECHAZADO"
    @SerialName("fecha_solicitud") val fechaSolicitud: String,
    // Aquí recibimos la relación de la tabla 'monedas' anidada
    val monedas: MonedaInfo? = null
)

@Serializable
data class PerfilAdmin(
    val id: String,
    @SerialName("nombre_completo") val nombreCompleto: String,
    val estado: String,
    @SerialName("rol_id") val rolId: Int
)

@Serializable
data class ResumenOperaciones(
    val totalComprasHoy: Double,
    val totalVentasHoy: Double
)
