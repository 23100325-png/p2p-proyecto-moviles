package com.example.p2pmoviles.presentation.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2pmoviles.data.model.PerfilAdmin
import com.example.p2pmoviles.ui.theme.*

@Composable
fun SeccionGestionUsuarios(
    usuarios: List<PerfilAdmin>,
    usuarioActualId: String,
    onBloquearClick: (PerfilAdmin) -> Unit,
    onDesbloquearClick: (PerfilAdmin) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Gestión de Usuarios",
            color = BinanceTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (usuarios.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay usuarios registrados.",
                    color = BinanceTextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(usuarios) { usuario ->
                    RowUsuarioItem(
                        usuario = usuario,
                        usuarioActualId = usuarioActualId,
                        onBloquearClick = onBloquearClick,
                        onDesbloquearClick = onDesbloquearClick
                    )
                }
            }
        }
    }
}

@Composable
fun RowUsuarioItem(
    usuario: PerfilAdmin,
    usuarioActualId: String,
    onBloquearClick: (PerfilAdmin) -> Unit,
    onDesbloquearClick: (PerfilAdmin) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BinanceInputBackground),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Información del usuario
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    usuario.email,
                    color = BinanceTextSecondary,
                    fontSize = 12.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Badge(
                        containerColor = if (usuario.rolId == 2L) BinanceYellow else BinanceSuccess,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text(
                            if (usuario.rolId == 2L) "Admin" else "Usuario",
                            color = BinanceBackground,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Badge(
                        containerColor = if (usuario.activo) BinanceSuccess else BinanceError,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text(
                            if (usuario.activo) "Activo" else "Bloqueado",
                            color = BinanceBackground,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Botones de acción
            if (usuario.activo) {
                Button(
                    onClick = { onBloquearClick(usuario) },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceError),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    enabled = usuario.id != usuarioActualId
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Bloquear",
                        tint = BinanceBackground,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Bloquear",
                        color = BinanceBackground,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = { onDesbloquearClick(usuario) },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceSuccess),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.LockOpen,
                        contentDescription = "Desbloquear",
                        tint = BinanceBackground,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Desbloquear",
                        color = BinanceBackground,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
