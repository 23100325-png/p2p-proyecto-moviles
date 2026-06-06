package com.example.p2pmoviles.presentation.transactions

data class Transaction(
    val type: String,
    val date: String,
    val person: String,
    val usdAmount: String,
    val penAmount: String,
    val status: String,
    val isBuy: Boolean
)