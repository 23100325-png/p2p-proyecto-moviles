package com.example.p2pmoviles.presentation.user.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2pmoviles.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    usuarioId: String
) {
    val completedOffers by viewModel.completedOffers.collectAsState()
    val perfil by viewModel.perfil.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showAll by viewModel.showAll.collectAsState()

    // Config states
    var pushEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(usuarioId) {
        viewModel.inicializar(usuarioId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", color = BinanceTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = BinanceTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BinanceBackground)
            )
        },
        containerColor = BinanceBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // "En tiempo real" status bar
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E2329))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF0ECB81)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("En tiempo real", color = BinanceTextSecondary, fontSize = 12.sp)
                }
            }

            // --- SECCIÓN NOTIFICACIONES ---
            val displayOffers = if (showAll) completedOffers else completedOffers.take(5)
            
            SectionHeader(title = "Notificaciones", badgeCount = if (!showAll && completedOffers.size > 5) completedOffers.size else null)

            if (isLoading && completedOffers.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BinanceYellow)
                }
            }

            displayOffers.forEach { offer ->
                val isAutomatic = offer.tipoMatch == "AUTOMATICO"
                NotificationItem(
                    icon = if (isAutomatic) Icons.Default.SwapHoriz else Icons.Default.CheckCircle,
                    iconColor = if (isAutomatic) Color(0xFF9234EB) else Color(0xFF0ECB81),
                    title = if (isAutomatic) "Matching automático" else "Oferta aceptada",
                    description = "Tu oferta de ${offer.monedaInfo?.simbolo ?: ""} ${offer.montoOrigen} fue completada.",
                    time = formatFecha(offer.fechaIntercambio ?: offer.fechaPublicacion)
                )
            }

            if (!showAll && completedOffers.size > 5) {
                TextButton(
                    onClick = { viewModel.setShowAll(true) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver historial completo", color = BinanceYellow, fontSize = 14.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BinanceTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SECCIÓN CONFIGURACIÓN ---
            SectionHeader(title = "Configuración", icon = Icons.Default.Notifications)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BinanceSurface)
                    .padding(16.dp)
            ) {
                ConfigSwitchRow(
                    title = "Notificaciones push",
                    subtitle = "Recibe notificaciones en tiempo real",
                    checked = pushEnabled,
                    onCheckedChange = { pushEnabled = it }
                )
                
                Divider(color = BinanceBackground, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                ConfigSwitchRow(
                    title = "Matching automático",
                    subtitle = "Ejecutar ofertas cuando coincidan exactamente",
                    checked = perfil?.matchingAutomaticoActivo ?: false,
                    onCheckedChange = { viewModel.toggleMatchingAutomatico(it) }
                )

            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun formatFecha(fechaStr: String): String {
    return try {
        // Formato esperado: YYYY-MM-DD HH:MM
        val cleanDate = fechaStr.replace("T", " ")
        if (cleanDate.length >= 16) {
            cleanDate.substring(0, 16)
        } else {
            cleanDate
        }
    } catch (e: Exception) {
        fechaStr
    }
}


@Composable
fun SectionHeader(title: String, badgeCount: Int? = null, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(title, color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        if (badgeCount != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(BinanceYellow),
                contentAlignment = Alignment.Center
            ) {
                Text(badgeCount.toString(), color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun NotificationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    time: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = BinanceSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BinanceYellow.copy(alpha = 0.1f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("NUEVO", color = BinanceYellow, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(title, color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text(time, color = BinanceTextSecondary, fontSize = 10.sp)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(description, color = BinanceTextSecondary, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BinanceYellow))
                }
            }
        }
    }
}

@Composable
fun ConfigSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = BinanceTextSecondary, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BinanceYellow,
                uncheckedThumbColor = BinanceTextSecondary,
                uncheckedTrackColor = BinanceBackground
            )
        )
    }
}

