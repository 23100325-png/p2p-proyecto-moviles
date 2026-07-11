package com.example.p2pmoviles.presentation.admin.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.p2pmoviles.presentation.admin.MovimientoAprobado
import com.example.p2pmoviles.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DialogoDetalleTransaccion(
    movimiento: MovimientoAprobado,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BinanceInputBackground),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Encabezado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Detalle de Transacción",
                        color = BinanceTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = BinanceError
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ID de Transacción
                DetalleField(
                    label = "ID Transacción",
                    value = "#${movimiento.id}"
                )

                // Usuario
                DetalleField(
                    label = "Usuario",
                    value = movimiento.perfilUsuario?.nombreCompleto ?: movimiento.usuarioId
                )

                // Moneda
                DetalleField(
                    label = "Moneda",
                    value = "${movimiento.monedas?.nombre ?: "Desconocida"} (${movimiento.monedas?.codigoIso ?: "---"})"
                )

                // Monto
                DetalleField(
                    label = "Monto",
                    value = "${movimiento.monedas?.simbolo ?: ""} ${String.format("%.2f", movimiento.monto)}",
                    valueColor = BinanceSuccess
                )

                // Tipo de Movimiento
                DetalleField(
                    label = "Tipo de Movimiento",
                    value = movimiento.tipoMovimiento
                )

                // Estado
                DetalleField(
                    label = "Estado",
                    value = movimiento.estado,
                    valueColor = when (movimiento.estado.uppercase()) {
                        "APROBADO" -> BinanceSuccess
                        "RECHAZADO" -> BinanceError
                        else -> BinanceYellow
                    }
                )

                // Fecha Solicitud
                DetalleField(
                    label = "Fecha Solicitud",
                    value = formatearFecha(movimiento.fechaSolicitud)
                )

                // Fecha Procesado
                if (!movimiento.fechaProcesado.isNullOrBlank()) {
                    DetalleField(
                        label = "Fecha Aprobación",
                        value = formatearFecha(movimiento.fechaProcesado)
                    )
                }

                // Voucher
                if (!movimiento.rutaVoucher.isNullOrBlank()) {
                    DetalleField(
                        label = "Comprobante",
                        value = "Disponible"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceBackground)
                ) {
                    Text("Cerrar", color = BinanceTextPrimary)
                }
            }
        }
    }
}

@Composable
fun DetalleField(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = BinanceTextPrimary
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            color = BinanceTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

private fun formatearFecha(fechaIso: String?): String {
    if (fechaIso.isNullOrBlank()) return "No disponible"
    
    return try {
        val sdfInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val sdfOutput = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val fecha = sdfInput.parse(fechaIso) ?: return fechaIso
        sdfOutput.format(fecha)
    } catch (e: Exception) {
        try {
            val partes = fechaIso.take(10).split("-")
            if (partes.size == 3) {
                "${partes[2]}/${partes[1]}/${partes[0]}"
            } else {
                fechaIso
            }
        } catch (e: Exception) {
            fechaIso
        }
    }
}
