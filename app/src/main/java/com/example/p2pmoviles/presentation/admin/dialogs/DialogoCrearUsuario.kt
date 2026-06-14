package com.example.p2pmoviles.presentation.admin.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.p2pmoviles.ui.theme.*

@Composable
fun DialogoCrearUsuario(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Long) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rolId by remember { mutableLongStateOf(1L) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BinanceInputBackground),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BinanceTextSecondary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Crear Nuevo Usuario",
                    color = BinanceYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre Completo", color = BinanceTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BinanceTextPrimary,
                        unfocusedTextColor = BinanceTextPrimary,
                        focusedBorderColor = BinanceYellow,
                        unfocusedBorderColor = BinanceTextSecondary.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = BinanceTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BinanceTextPrimary,
                        unfocusedTextColor = BinanceTextPrimary,
                        focusedBorderColor = BinanceYellow,
                        unfocusedBorderColor = BinanceTextSecondary.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña", color = BinanceTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = BinanceTextSecondary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BinanceTextPrimary,
                        unfocusedTextColor = BinanceTextPrimary,
                        focusedBorderColor = BinanceYellow,
                        unfocusedBorderColor = BinanceTextSecondary.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )

                Column {
                    Text("Rol del Usuario", color = BinanceTextSecondary, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = rolId == 1L,
                            onClick = { rolId = 1L },
                            label = { Text("Usuario") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BinanceYellow.copy(alpha = 0.2f),
                                selectedLabelColor = BinanceYellow
                            )
                        )
                        FilterChip(
                            selected = rolId == 2L,
                            onClick = { rolId = 2L },
                            label = { Text("Admin") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BinanceYellow.copy(alpha = 0.2f),
                                selectedLabelColor = BinanceYellow
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (nombre.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                            onConfirm(nombre, email, password, rolId)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Crear Usuario", color = BinanceBackground, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
