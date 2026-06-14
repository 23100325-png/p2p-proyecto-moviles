package com.example.p2pmoviles.data.network.apiCambio

import com.example.p2pmoviles.data.network.apiCambio.ExchangeResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeApiService {
    // 🟢 Dejamos la ruta con variables dinámicas que se llenarán en la función
    @GET("v6/{apiKey}/latest/{base}")
    suspend fun getExchangeRates(
        @Path("apiKey") apiKey: String,
        @Path("base") baseCurrency: String
    ): ExchangeResponse
}