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
    @SerialName("fecha_procesado") val fechaProcesado: String? = null,
    // Aquí recibimos la relación de la tabla 'monedas' anidada
    val monedas: MonedaInfo? = null
)

@Serializable
data class PerfilAdmin(
    val id: String,
    @SerialName("nombre_completo") val nombreCompleto: String,
    @SerialName("rol_id") val rolId: Long,
    val estado: String = "Activo" // Activo, Bloqueado
)

@Serializable
data class BitacoraEntry(
    val id: Long? = null,
    val accion: String,
    val descripcion: String,
    val responsable: String,
    @SerialName("fecha_hora") val fechaHora: String
)

@Serializable
data class ResumenOperaciones(
    val totalComprasHoy: Double,
    val totalVentasHoy: Double
)
