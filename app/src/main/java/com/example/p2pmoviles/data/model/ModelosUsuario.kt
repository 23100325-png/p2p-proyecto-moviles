package com.example.p2pmoviles.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class BilleteraUsuario(
    val id: Long,
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("moneda_id") val monedaId: Long,
    @SerialName("saldo_disponible") val saldoDisponible: Double,
    @SerialName("saldo_bloqueado") val saldoBloqueado: Double,
    // Relación anidada con la tabla 'monedas'
    val monedas: MonedaInfo? = null
)

@Serializable
data class SolicitudFondoInsert(
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("moneda_id") val monedaId: Long,
    @SerialName("tipo_movimiento") val tipoMovimiento: String,
    @SerialName("monto") val monto: Double,
    @SerialName("ruta_voucher") val rutaVoucher: String?,
    @SerialName("fecha_solicitud") val fechaSolicitud: String,
    @SerialName("cuenta_bancaria_id") val cuentaBancariaId: Long? = null // 👈 AGREGA ESTO SI FALTA
)