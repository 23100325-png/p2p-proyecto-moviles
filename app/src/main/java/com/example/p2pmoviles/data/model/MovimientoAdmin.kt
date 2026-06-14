package com.example.p2pmoviles.data

import kotlinx.serialization.Serializable

@Serializable
data class PerfilSimplificado(
    val id: String,              // El ID que vincula con Auth (UUID)
    val nombre_completo: String, // El nombre del usuario
    val rol_id: Long             // 🟢 Nombre exacto de tu columna en Supabase
)

