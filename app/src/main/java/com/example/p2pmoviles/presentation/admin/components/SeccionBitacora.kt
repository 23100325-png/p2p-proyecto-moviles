package com.example.p2pmoviles.presentation.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2pmoviles.data.model.BitacoraEntry
import com.example.p2pmoviles.ui.theme.*

@Composable
fun SeccionBitacora(entries: List<BitacoraEntry>) {
    Column {
        Text("Bitácora de Auditoría", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("No hay registros en la bitácora.", color = BinanceTextSecondary, fontSize = 14.sp)
            }
        } else {
            entries.forEach { entry ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).background(BinanceInputBackground, CircleShape), Alignment.Center) {
                        val icon = when {
                            entry.accion.contains("Bloqueo") -> Icons.Default.Block
                            entry.accion.contains("Desbloqueo") -> Icons.Default.LockOpen
                            entry.accion.contains("Eliminación") -> Icons.Default.Delete
                            entry.accion.contains("Creación") -> Icons.Default.PersonAdd
                            entry.accion.contains("Rol") -> Icons.Default.AdminPanelSettings
                            entry.accion.contains("Edición") -> Icons.Default.Edit
                            entry.accion.contains("Fondos") -> Icons.Default.AccountBalanceWallet
                            else -> Icons.Default.History
                        }
                        val tint = when {
                            entry.accion.contains("Bloqueo") || entry.accion.contains("Eliminación") || entry.accion.contains("Rechazo") -> BinanceError
                            entry.accion.contains("Aprobación") || entry.accion.contains("Creación") || entry.accion.contains("Desbloqueo") -> BinanceSuccess
                            else -> BinanceYellow
                        }
                        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.accion, color = BinanceTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(entry.descripcion, color = BinanceTextSecondary, fontSize = 12.sp)
                        Text("Responsable: ${entry.responsable}", color = BinanceTextSecondary.copy(alpha = 0.7f), fontSize = 10.sp)
                    }
                    val hora = try { entry.fechaHora.substring(11, 16) } catch (e: Exception) { "" }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(entry.fechaHora.take(10), color = BinanceTextSecondary, fontSize = 10.sp)
                        Text(hora, color = BinanceTextSecondary, fontSize = 10.sp)
                    }
                }
                HorizontalDivider(color = BinanceTextSecondary.copy(alpha = 0.1f))
            }
        }
    }
}
