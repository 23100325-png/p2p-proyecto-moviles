package com.example.p2pmoviles.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class BitacoraEntry(
    val id: Long? = null,
    val accion: String,
    val descripcion: String,
    val responsable: String,
    @SerialName("fecha_hora") val fechaHora: String
)
