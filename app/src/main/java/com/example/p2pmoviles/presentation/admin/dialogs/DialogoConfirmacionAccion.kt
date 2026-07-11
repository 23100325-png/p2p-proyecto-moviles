package com.example.p2pmoviles.presentation.admin.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.p2pmoviles.data.model.PerfilAdmin
import com.example.p2pmoviles.ui.theme.*

@Composable
fun DialogoConfirmacionAccion(
    usuario: PerfilAdmin,
    accion: String,
    usuarioActualId: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val esAutoBloqueo = usuario.id == usuarioActualId && accion == "BLOQUEAR"
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BinanceInputBackground),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                Text(
                    text = when (accion) {
                        "BLOQUEAR" -> "Bloquear Usuario"
                        "DESBLOQUEAR" -> "Desbloquear Usuario"
                        else -> "Confirmar Acción"
                    },
                    color = BinanceTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                // Mensaje de confirmación
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when {
                            esAutoBloqueo -> "No puedes bloquearte a ti mismo"
                            accion == "BLOQUEAR" -> "¿Estás seguro de que deseas bloquear a ${usuario.nombreCompleto}?"
                            accion == "DESBLOQUEAR" -> "¿Estás seguro de que deseas desbloquear a ${usuario.nombreCompleto}?"
                            else -> "¿Confirmar acción para ${usuario.nombreCompleto}?"
                        },
                        color = BinanceTextSecondary,
                        fontSize = 14.sp
                    )
                    
                    if (esAutoBloqueo) {
                        Text(
                            text = "Los administradores no pueden bloquearse a sí mismos.",
                            color = BinanceError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else if (accion == "BLOQUEAR") {
                        Text(
                            text = "El usuario no podrá iniciar sesión.",
                            color = BinanceYellow,
                            fontSize = 12.sp
                        )
                    }
                }

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BinanceBackground)
                    ) {
                        Text("Cancelar", color = BinanceTextPrimary)
                    }

                    if (!esAutoBloqueo) {
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (accion == "BLOQUEAR") BinanceError else BinanceSuccess
                            )
                        ) {
                            Text(
                                when (accion) {
                                    "BLOQUEAR" -> "Bloquear"
                                    "DESBLOQUEAR" -> "Desbloquear"
                                    else -> "Confirmar"
                                },
                                color = BinanceTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
