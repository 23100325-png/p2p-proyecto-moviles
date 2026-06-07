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

@Composable
fun CabeceraFiltrosMercado(
    codigoTengo: String,
    codigoQuiero: String,
    onTengoClick: () -> Unit,
    onQuieroClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BinanceInputBackground, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Selecciona tus divisas de intercambio",
            color = BinanceTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Caja Selector "Tengo"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, BinanceTextSecondary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .clickable { onTengoClick() }
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tengo", color = BinanceTextSecondary, fontSize = 11.sp)
                    Text(codigoTengo, color = BinanceYellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Ícono de cruce en medio
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Intercambio",
                tint = BinanceYellow,
                modifier = Modifier.padding(horizontal = 12.dp).size(24.dp)
            )

            // Caja Selector "Quiero"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, BinanceTextSecondary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .clickable { onQuieroClick() }
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Quiero", color = BinanceTextSecondary, fontSize = 11.sp)
                    Text(codigoQuiero, color = BinanceTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OfertaP2PCard(
    nombreUsuario: String,
    calificacion: Double,
    totalOperaciones: Int,
    banco: String,
    tasaCambio: Double,
    montoOrigen: Double,
    codigoTengo: String, // Moneda que entregará el interesado
    codigoQuiero: String, // Moneda que recibirá el interesado
    onTomarOfertaClick: () -> Unit
) {
    // 🧮 MATEMÁTICA EN VIVO: El interesado pagará = monto que ofrece el creador * tasa de cambio
    val totalAPagarInteresado = montoOrigen * tasaCambio

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(BinanceInputBackground, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        // Fila 1: Datos del Ofertante y Precio Destacado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Izquierda: Nombre y reputación
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BinanceYellow.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = nombreUsuario.take(1).uppercase(),
                        color = BinanceYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = nombreUsuario, color = BinanceTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "$calificacion ($totalOperaciones)", color = BinanceTextSecondary, fontSize = 11.sp)
                    }
                }
            }

            // Derecha: Tasa de cambio
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.2f %s", tasaCambio, codigoTengo),
                    color = BinanceTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "por 1 $codigoQuiero", color = BinanceTextSecondary, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Fila 2: Banco y Montos Financieros Calculados
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Izquierda: Detalles del método de pago e información de límites
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Banco",
                        tint = BinanceYellow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = banco, color = BinanceTextPrimary, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Disponible: $montoOrigen $codigoQuiero", color = BinanceTextSecondary, fontSize = 12.sp)
            }

            // Derecha: Botón operativo y monto total calculado
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Total a pagar:", color = BinanceTextSecondary, fontSize = 11.sp)
                Text(
                    text = String.format("%.2f %s", totalAPagarInteresado, codigoTengo),
                    color = BinanceGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = { onTomarOfertaClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = BinanceBackground),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Intercambiar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
/*
@Composable
fun CabeceraFiltrosMercado(
    codigoTengo: String,
    codigoQuiero: String,
    onTengoClick: () -> Unit,
    onQuieroClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BinanceInputBackground, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Moneda",
            color = BinanceTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
                    .border(
                        width = 1.dp,
                        color = BinanceTextSecondary.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onTengoClick() }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (codigoTengo == "USD") "🇺🇸" else "💱",
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = codigoTengo,
                            color = BinanceTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = BinanceTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = null,
                tint = BinanceYellow,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
                    .border(
                        width = 1.dp,
                        color = BinanceTextSecondary.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onQuieroClick() }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (codigoQuiero == "PEN") "🇵🇪" else "💱",
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = codigoQuiero,
                            color = BinanceTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = BinanceTextSecondary
                    )
                }
            }
        }
    }
}


 */