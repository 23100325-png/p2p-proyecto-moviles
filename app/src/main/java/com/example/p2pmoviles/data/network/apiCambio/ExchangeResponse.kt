package com.example.p2pmoviles.data.network.apiCambio

import com.google.gson.annotations.SerializedName

data class ExchangeResponse(
    @SerializedName("result") val result: String,
    @SerializedName("base_code") val baseCode: String,
    @SerializedName("conversion_rates") val conversionRates: Map<String, Double>
)