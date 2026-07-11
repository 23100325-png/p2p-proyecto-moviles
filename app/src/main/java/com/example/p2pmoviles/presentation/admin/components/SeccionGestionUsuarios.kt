package com.example.p2pmoviles.presentation.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onDesbloquearClick: (PerfilAdmin) -> Unit,
    onEditarClick: (PerfilAdmin) -> Unit,
    onCambiarRolClick: (PerfilAdmin) -> Unit,
    onSearch: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Gestión de Usuarios",
                color = BinanceTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            
            Badge(containerColor = BinanceYellow.copy(alpha = 0.1f)) {
                Text(
                    "${usuarios.size} usuarios",
                    color = BinanceYellow,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 10.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        // Barra de Búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                onSearch(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Buscar por nombre o email", color = BinanceTextSecondary, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BinanceTextSecondary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { 
                        searchQuery = "" 
                        onSearch("")
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = BinanceTextSecondary)
                    }
                }
            },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BinanceYellow,
                unfocusedBorderColor = BinanceInputBackground,
                focusedContainerColor = BinanceInputBackground,
                unfocusedContainerColor = BinanceInputBackground,
                cursorColor = BinanceYellow
            ),
            singleLine = true
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
                        onDesbloquearClick = onDesbloquearClick,
                        onEditarClick = onEditarClick,
                        onCambiarRolClick = onCambiarRolClick
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
    onDesbloquearClick: (PerfilAdmin) -> Unit,
    onEditarClick: (PerfilAdmin) -> Unit,
    onCambiarRolClick: (PerfilAdmin) -> Unit
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
                    usuario.nombreCompleto.ifEmpty { "Sin nombre" },
                    color = BinanceTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    usuario.email,
                    color = BinanceTextSecondary,
                    fontSize = 12.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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

            Spacer(modifier = Modifier.width(8.dp))

            // Botones de acción
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Botón Editar
                IconButton(
                    onClick = { onEditarClick(usuario) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = BinanceYellow,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Botón Cambiar Rol
                IconButton(
                    onClick = { onCambiarRolClick(usuario) },
                    modifier = Modifier.size(32.dp),
                    enabled = usuario.id != usuarioActualId
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Cambiar Rol",
                        tint = BinanceSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (usuario.activo) {
                    IconButton(
                        onClick = { onBloquearClick(usuario) },
                        modifier = Modifier.size(32.dp),
                        enabled = usuario.id != usuarioActualId
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Bloquear",
                            tint = BinanceError,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = { onDesbloquearClick(usuario) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.LockOpen,
                            contentDescription = "Desbloquear",
                            tint = BinanceSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

