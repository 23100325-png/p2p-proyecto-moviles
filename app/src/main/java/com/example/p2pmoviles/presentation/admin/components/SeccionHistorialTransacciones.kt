package com.example.p2pmoviles.presentation.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2pmoviles.presentation.admin.MovimientoAprobado
import com.example.p2pmoviles.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun SeccionHistorialTransacciones(
    movimientos: List<MovimientoAprobado>,
    onMovimientoClick: (MovimientoAprobado) -> Unit,
    onSearch: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Historial de Transacciones Aprobadas",
            color = BinanceTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        // Barra de Búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                onSearch(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Buscar por ID o Usuario", color = BinanceTextSecondary, fontSize = 14.sp) },
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

        Spacer(modifier = Modifier.height(8.dp))

        if (movimientos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay transacciones aprobadas.",
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
                items(movimientos) { movimiento ->
                    RowMovimientoItem(
                        movimiento = movimiento,
                        onMovimientoClick = onMovimientoClick
                    )
                }
            }
        }
    }
}

@Composable
fun RowMovimientoItem(
    movimiento: MovimientoAprobado,
    onMovimientoClick: (MovimientoAprobado) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BinanceInputBackground),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMovimientoClick(movimiento) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // ID y Usuario
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Badge(
                        containerColor = BinanceYellow.copy(alpha = 0.2f),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text(
                            "#${movimiento.id}",
                            color = BinanceYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Usuario y Moneda
                Text(
                    movimiento.perfilUsuario?.nombreCompleto ?: movimiento.usuarioId,
                    color = BinanceTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Badge(
                        containerColor = BinanceSuccess.copy(alpha = 0.2f),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text(
                            movimiento.monedas?.codigoIso ?: "---",
                            color = BinanceSuccess,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        "${movimiento.monedas?.simbolo ?: ""} ${String.format("%.2f", movimiento.monto)}",
                        color = BinanceTextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Fecha
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Aprobado: ${formatearFecha(movimiento.fechaProcesado ?: movimiento.fechaSolicitud)}",
                    color = BinanceTextSecondary,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Ver detalle",
                tint = BinanceTextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun formatearFecha(fechaIso: String): String {
    return try {
        val sdfInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val sdfOutput = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fecha = sdfInput.parse(fechaIso) ?: return fechaIso
        sdfOutput.format(fecha)
    } catch (e: Exception) {
        // Si no es ISO, intentar otro formato
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
