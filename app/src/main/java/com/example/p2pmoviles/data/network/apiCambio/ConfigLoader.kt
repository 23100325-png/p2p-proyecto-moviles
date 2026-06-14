package com.example.p2pmoviles.data.network.apiCambio

import android.content.Context
import android.util.Log
import java.util.Properties

object ConfigLoader {
    private const val TAG = "ConfigLoader"

    // Función que lee directamente el archivo local.properties
    fun getExchangeApiKey(context: Context): String {
        val properties = Properties()
        return try {
            // Buscamos el archivo local.properties en el directorio raíz o assets del sistema
            // Para entornos de desarrollo en Android, la forma más infalible si Gradle da problemas
            // es leerlo a través de un archivo espejo o directamente desde el entorno.
            // Para asegurar que funcione sin dependencias de Gradle, lee la propiedad del sistema:
            System.getProperty("EXCHANGE_RATE_API_KEY") ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo leer la API Key", e)
            ""
        }
    }
}