package com.example.p2pmoviles.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2pmoviles.data.model.OfertaMercado

private val CardBg = Color(0xFF151A20)
private val Yellow = Color(0xFFFFC400)
private val SoftText = Color(0xFFB8BDC6)

@Composable
fun OfferCard(
    oferta: OfertaMercado,
    codigoTengo: String,
    codigoQuiero: String
) {
    val nombreUsuario = oferta.ofertanteInfo?.nombre ?: "Usuario"
    val inicial = nombreUsuario.take(1).uppercase()
    val rating = "${oferta.ofertanteInfo?.calificacion ?: 5.0} (${oferta.ofertanteInfo?.totalOperaciones ?: 100})"
    //val paymentMethod = oferta.bancoInfo?.banco ?: "Transferencia"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, MaterialTheme.shapes.large)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(Yellow, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(inicial, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(nombreUsuario, color = Color.White, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Yellow, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(rating, color = SoftText, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Método de pago", color = SoftText, fontSize = 11.sp)
            //Text(paymentMethod, color = Color.White, fontSize = 13.sp)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("${oferta.tasaCambio} $codigoTengo", color = Color.White, fontWeight = FontWeight.Bold)
            Text("por 1 $codigoQuiero", color = Yellow, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Monto", color = SoftText, fontSize = 11.sp)
            Text("${oferta.monedaInfo?.simbolo ?: ""} ${oferta.montoOrigen}", color = Color.White, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = SoftText
        )
    }
}
