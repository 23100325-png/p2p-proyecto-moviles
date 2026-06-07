package com.example.p2pmoviles.presentation.navigation

sealed class Routes(val route: String) {
    data object Menu : Routes("menu")
    data object Home : Routes("home")


    data object Transactions : Routes("transactions")
    data object UploadReceipt : Routes("upload_receipt")

    data object Login : Routes("login")
    data object Register : Routes("register")
    data object Admin : Routes("admin")
    data object MercadoP2P : Routes("mercado_p2p")
    data object PublicarOferta : Routes("publicar_oferta")
    data object Wallet : Routes("wallet")
}