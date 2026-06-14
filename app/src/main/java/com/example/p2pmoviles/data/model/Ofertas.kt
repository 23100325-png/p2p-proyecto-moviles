package com.example.p2pmoviles.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CuentaBancaria(
    @SerialName("id") val id: Long,
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("moneda_id") val monedaId: Long,
    @SerialName("banco") val banco: String,
    @SerialName("numero_cuenta") val numeroCuenta: String,
    @SerialName("numero_cci") val numeroCci: String?,
    @SerialName("titular_nombre") val titularNombre: String
)

@Serializable
data class OfertaInsert(
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("moneda_origen_id") val monedaOrigenId: Long,
    @SerialName("moneda_destino_id") val monedaDestinoId: Long,
    @SerialName("monto_origen") val montoOrigen: Double,
    @SerialName("tasa_cambio") val tasaCambio: Double,
    //@SerialName("cuenta_bancaria_id") val cuentaBancariaId: Long,
    @SerialName("estado") val estado: String = "PENDIENTE",
    @SerialName("fecha_publicacion") val fechaPublicacion: String
)@Serializable
data class OfertaMercado(
    @SerialName("id") val id: Long,
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("moneda_origen_id") val monedaOrigenId: Long,
    @SerialName("moneda_destino_id") val monedaDestinoId: Long,
    @SerialName("monto_origen") val montoOrigen: Double,
    @SerialName("tasa_cambio") val tasaCambio: Double,
    //@SerialName("cuenta_bancaria_id") val cuentaBancariaId: Long,
    @SerialName("estado") val estado: String,
    @SerialName("fecha_publicacion") val fechaPublicacion: String,

    // Estos campos vendrán mapeados por el JOIN de Supabase
    // Si tu estructura de perfiles maneja otros nombres, los adaptamos
    @SerialName("usuarios") val ofertanteInfo: OfertantePerfil? = null,
    //@SerialName("cuentas_bancarias") val bancoInfo: CuentaBancariaInfo? = null
)

@Serializable
data class OfertantePerfil(
    @SerialName("nombre") val nombre: String,
    @SerialName("calificacion") val calificacion: Double = 5.0,
    @SerialName("total_operaciones") val totalOperaciones: Int = 120
)

@Serializable
data class CuentaBancariaInfo(
    @SerialName("banco") val banco: String,
    @SerialName("titular_nombre") val titularNombre: String
)