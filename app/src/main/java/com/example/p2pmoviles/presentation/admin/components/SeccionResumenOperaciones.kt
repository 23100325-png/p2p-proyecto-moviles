package com.example.p2pmoviles.presentation.admin.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2pmoviles.data.model.ResumenOperaciones
import com.example.p2pmoviles.ui.theme.*

@Composable
fun SeccionResumenOperaciones(resumen: ResumenOperaciones) {
    Column {
        Text("Resumen de Operaciones (Hoy)", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = BinanceInputBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddCircle, null, tint = BinanceSuccess, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Total Recargas Aprobadas", color = BinanceTextSecondary, fontSize = 14.sp)
                    }
                    Text("${formatCurrency(resumen.totalComprasHoy)} USD", color = BinanceSuccess, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(color = BinanceTextSecondary.copy(alpha = 0.1f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RemoveCircle, null, tint = BinanceError, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Total Retiros Aprobados", color = BinanceTextSecondary, fontSize = 14.sp)
                    }
                    Text("${formatCurrency(resumen.totalVentasHoy)} USD", color = BinanceError, fontWeight = FontWeight.Bold)
                }
                
                if (resumen.totalComprasHoy == 0.0 && resumen.totalVentasHoy == 0.0) {
                    Text(
                        "No se registran operaciones aprobadas hoy.",
                        color = BinanceTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    return String.format("%.2f", amount)
}
