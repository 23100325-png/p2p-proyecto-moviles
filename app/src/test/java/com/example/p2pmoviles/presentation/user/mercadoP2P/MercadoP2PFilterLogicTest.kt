package com.example.p2pmoviles.presentation.user.mercadoP2P

import com.example.p2pmoviles.data.model.OfertaMercado
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class MercadoP2PFilterLogicTest {

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val mockOfertas = listOf(
        OfertaMercado(id = 1, usuarioId = "user1", monedaOrigenId = 1, monedaDestinoId = 2, montoOrigen = 100.0, tasaCambio = 3.5, estado = "ACTIVA", fechaPublicacion = "2024-01-01T10:00:00Z"),
        OfertaMercado(id = 2, usuarioId = "user2", monedaOrigenId = 1, monedaDestinoId = 2, montoOrigen = 200.0, tasaCambio = 3.7, estado = "ACTIVA", fechaPublicacion = "2024-01-15T10:00:00Z"),
        OfertaMercado(id = 3, usuarioId = "user3", monedaOrigenId = 1, monedaDestinoId = 2, montoOrigen = 300.0, tasaCambio = 3.9, estado = "ACTIVA", fechaPublicacion = "2024-02-01T10:00:00Z")
    )

    @Test
    fun `filtrarOfertas - filtrar por rango de fechas`() {
        val fDesde = sdf.parse("2024-01-10")?.time
        val fHasta = sdf.parse("2024-01-20")?.time

        val resultado = MercadoP2PFilterLogic.filtrarOfertas(mockOfertas, fDesde, fHasta, null, 0.0)

        assertEquals(1, resultado.size)
        assertEquals(2L, resultado[0].id)
    }

    @Test
    fun `filtrarOfertas - filtrar por tasa y margen`() {
        val tTarget = 3.7
        val mMargen = 0.1 // Rango [3.6, 3.8]

        val resultado = MercadoP2PFilterLogic.filtrarOfertas(mockOfertas, null, null, tTarget, mMargen)

        assertEquals(1, resultado.size)
        assertEquals(2L, resultado[0].id)
    }

    @Test
    fun `filtrarOfertas - sin filtros activos retorna todo`() {
        val resultado = MercadoP2PFilterLogic.filtrarOfertas(mockOfertas, null, null, null, 0.0)
        assertEquals(3, resultado.size)
    }
}
