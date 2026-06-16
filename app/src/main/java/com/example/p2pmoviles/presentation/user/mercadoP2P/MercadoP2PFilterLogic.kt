package com.example.p2pmoviles.presentation.user.mercadoP2P

import com.example.p2pmoviles.data.model.OfertaMercado
import java.text.SimpleDateFormat
import java.util.Locale

object MercadoP2PFilterLogic {

    fun filtrarOfertas(
        ofertas: List<OfertaMercado>,
        fDesde: Long?,
        fHasta: Long?,
        tTarget: Double?,
        mMargen: Double
    ): List<OfertaMercado> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        return ofertas.filter { oferta ->
            // Filtro por Fecha
            val pasaFecha = try {
                val fechaString = oferta.fechaPublicacion.take(10)
                val fechaMillis = sdf.parse(fechaString)?.time ?: 0L

                when {
                    fDesde != null && fHasta != null -> fechaMillis in fDesde..(fHasta + 86399999)
                    fDesde != null -> fechaMillis >= fDesde
                    fHasta != null -> fechaMillis <= (fHasta + 86399999)
                    else -> true
                }
            } catch (e: Exception) {
                true
            }

            // Filtro por Tipo de Cambio
            val pasaTasa = if (tTarget != null) {
                val min = tTarget - mMargen
                val max = tTarget + mMargen
                oferta.tasaCambio in min..max
            } else true

            pasaFecha && pasaTasa
        }
    }
}
