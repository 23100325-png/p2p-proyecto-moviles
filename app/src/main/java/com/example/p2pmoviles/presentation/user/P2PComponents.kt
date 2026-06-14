package com.example.p2pmoviles.presentation.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.p2pmoviles.ui.theme.*


@Composable
fun DropdownMonedasCustom(
    titulo: String,
    monedaSeleccionadaNombre: String?,
    monedaSeleccionadaCodigo: String?,
    urlIcono: String?,
    subtextoSaldo: String? = null, // Para mostrar el saldo disponible sólo en "Tengo"
    onDesplegarClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = titulo, color = BinanceTextSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BinanceInputBackground, RoundedCornerShape(8.dp))
                .border(1.dp, BinanceTextSecondary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .clickable { onDesplegarClick() }
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!urlIcono.isNullOrEmpty()) {
                    AsyncImage(
                        model = urlIcono,
                        contentDescription = "Bandera",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = monedaSeleccionadaCodigo ?: "---",
                            color = BinanceTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = monedaSeleccionadaNombre ?: "Seleccionar divisa",
                            color = BinanceTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = BinanceYellow)
        }

        // Si tiene un saldo asignado (caso de Tengo), pintamos el aviso abajo
        if (!subtextoSaldo.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtextoSaldo, color = BinanceTextSecondary, fontSize = 12.sp)
        }
    }
}


@Composable
fun DropdownBancosCustom(
    bancoSeleccionado: String?,
    monedaCodigo: String?,
    monedaNombre: String?,
    onDesplegarClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Donde quiero recibir?", color = BinanceTextSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BinanceInputBackground, RoundedCornerShape(8.dp))
                .border(1.dp, BinanceTextSecondary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .clickable { onDesplegarClick() }
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Wallet,
                    contentDescription = "Banco",
                    tint = BinanceYellow, // 🟢 Asegúrate de usar la constante de color directa aquí
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))

                if (bancoSeleccionado != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = bancoSeleccionado, color = BinanceTextPrimary, fontSize = 15.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "($monedaCodigo - $monedaNombre)", color = BinanceTextSecondary, fontSize = 13.sp)
                    }
                } else {
                    Text(text = "Seleccionar banco de recepción", color = BinanceTextSecondary, fontSize = 14.sp)
                }
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = BinanceYellow)
        }
    }
}