package com.example.p2pmoviles.presentation.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2pmoviles.data.model.PerfilAdmin
import com.example.p2pmoviles.ui.theme.*

@Composable
fun SeccionGestionUsuarios(
    usuarios: List<PerfilAdmin>,
    verTodos: Boolean,
    onVerTodosToggle: () -> Unit,
    onCrearClick: () -> Unit,
    onAccion: (PerfilAdmin, String) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Gestión de Usuarios", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row {
                Text("+ Crear Usuario", color = BinanceYellow, fontSize = 12.sp, modifier = Modifier.clickable { onCrearClick() })
                Spacer(modifier = Modifier.width(12.dp))
                Text(if (verTodos) "Ver Menos" else "Ver Todos", color = BinanceYellow, fontSize = 12.sp, modifier = Modifier.clickable { onVerTodosToggle() })
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        if (usuarios.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("No hay usuarios disponibles.", color = BinanceTextSecondary, fontSize = 14.sp)
            }
        } else {
            val listaAMostrar = if (verTodos) usuarios else usuarios.take(3)
            listaAMostrar.forEach { user ->
                Card(colors = CardDefaults.cardColors(containerColor = BinanceInputBackground), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(BinanceSuccess.copy(alpha = 0.2f), CircleShape), Alignment.Center) { Icon(Icons.Default.Person, null, tint = BinanceSuccess) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.nombreCompleto, color = BinanceTextPrimary, fontWeight = FontWeight.Bold)
                            Text(user.estado, color = if (user.estado == "Activo") BinanceSuccess else BinanceError, fontSize = 12.sp)
                        }
                        if (user.estado == "Bloqueado") {
                            Button(onClick = { onAccion(user, "DESBLOQUEAR") }, colors = ButtonDefaults.buttonColors(containerColor = BinanceBackground), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                                Text("Desbloquear", color = BinanceTextPrimary, fontSize = 10.sp)
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                BotonAccionMini("Editar", BinanceSuccess) { onAccion(user, "EDITAR") }
                                BotonAccionMini("Bloquear", BinanceError) { onAccion(user, "BLOQUEAR") }
                                BotonAccionMini("Eliminar", BinanceError) { onAccion(user, "ELIMINAR") }
                                BotonAccionMini("Roles", BinanceYellow) { onAccion(user, "ROLES") }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun BotonAccionMini(text: String, color: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent, border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)), shape = RoundedCornerShape(4.dp)) {
        Text(text, color = color, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
    }
}
