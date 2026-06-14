package com.example.p2pmoviles.presentation.admin.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.p2pmoviles.data.model.PerfilAdmin
import com.example.p2pmoviles.ui.theme.*

@Composable
fun DialogoGestionUsuario(
    usuario: PerfilAdmin,
    accion: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var valorInput by remember { mutableStateOf(if (accion == "EDITAR") usuario.nombreCompleto else "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BinanceInputBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (accion == "ROLES") "Cambiar Rol" else "Editar Usuario",
                    color = BinanceTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                if (accion == "ROLES") {
                    Text(
                        text = "Seleccione rol para ${usuario.nombreCompleto}",
                        color = BinanceTextSecondary,
                        fontSize = 14.sp
                    )
                    Row {
                        Button(
                            onClick = { onConfirm("CAMBIAR_ROL", "1") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Usuario (1)")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { onConfirm("CAMBIAR_ROL", "2") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Admin (2)")
                        }
                    }
                } else if (accion == "EDITAR") {
                    TextField(
                        value = valorInput,
                        onValueChange = { valorInput = it },
                        label = { Text("Nuevo Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onConfirm("EDITAR_NOMBRE", valorInput) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
