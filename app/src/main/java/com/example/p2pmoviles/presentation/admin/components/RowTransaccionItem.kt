package com.example.p2pmoviles.presentation.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.p2pmoviles.data.model.MovimientoAdmin
import com.example.p2pmoviles.ui.theme.*

@Composable
fun RowTransaccionItem(
    movimiento: MovimientoAdmin,
    onVerVoucher: (String) -> Unit,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = BinanceInputBackground), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.8f)) {
                AsyncImage(model = movimiento.monedas?.rutaBandera, contentDescription = null, modifier = Modifier.size(28.dp).clip(CircleShape).background(BinanceBackground), contentScale = ContentScale.Crop)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(movimiento.monedas?.nombre ?: "Desconocida", color = BinanceTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(movimiento.monedas?.codigoIso ?: "---", color = BinanceTextSecondary, fontSize = 12.sp)
                }
            }
            Column(modifier = Modifier.weight(1.5f)) {
                val esRecarga = movimiento.tipoMovimiento == "RECARGA"
                Text(text = "${movimiento.monedas?.simbolo ?: ""} ${String.format("%.2f", movimiento.monto)}", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = if (esRecarga) "● Recarga" else "● Retiro", color = if (esRecarga) BinanceSuccess else BinanceError, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.Center) {
                if (movimiento.tipoMovimiento == "RECARGA" && !movimiento.rutaVoucher.isNullOrEmpty()) {
                    Row(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(BinanceBackground).clickable { onVerVoucher(movimiento.rutaVoucher!!) }.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility, null, tint = BinanceYellow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver", color = BinanceYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else { Text("--", color = BinanceTextSecondary, fontSize = 12.sp) }
            }
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onRechazar, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Close, null, tint = BinanceError, modifier = Modifier.size(20.dp)) }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onAprobar, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Check, null, tint = BinanceSuccess, modifier = Modifier.size(20.dp)) }
            }
        }
    }
}
