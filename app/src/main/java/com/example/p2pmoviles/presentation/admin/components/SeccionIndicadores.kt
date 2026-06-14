package com.example.p2pmoviles.presentation.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2pmoviles.ui.theme.*

@Composable
fun SeccionIndicadores(activos: Int, pendientes: Int) {
    Column {
        Text("Indicadores Clave", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Card(colors = CardDefaults.cardColors(containerColor = BinanceInputBackground), shape = RoundedCornerShape(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Usuarios Activos", color = BinanceTextSecondary, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(formatNumber(activos), color = BinanceTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowUpward, null, tint = BinanceSuccess, modifier = Modifier.size(16.dp))
                    }
                }
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(BinanceTextSecondary.copy(alpha = 0.2f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Estado del Sistema", color = BinanceTextSecondary, fontSize = 12.sp)
                    Box(modifier = Modifier.size(24.dp).background(BinanceSuccess.copy(alpha = 0.2f), CircleShape).padding(4.dp)) {
                        Box(modifier = Modifier.fillMaxSize().background(BinanceSuccess, CircleShape))
                    }
                }
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(BinanceTextSecondary.copy(alpha = 0.2f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Operaciones Pendientes", color = BinanceTextSecondary, fontSize = 12.sp)
                    Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(BinanceYellow).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text(pendientes.toString(), color = BinanceBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun formatNumber(num: Int): String {
    return String.format("%,d", num).replace(',', '.')
}
