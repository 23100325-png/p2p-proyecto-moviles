package com.example.p2pmoviles.presentation.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.p2pmoviles.ui.theme.*

@Composable
fun MercadoP2PScreen(
    usuarioLogueadoId: String,
    mercadoViewModel: MercadoP2PViewModel = viewModel(),
    onNavegarAPublicarClick: () -> Unit // Callback para cuando decida ir al formulario
) {
    // Inicializamos el motor de búsqueda con el ID del usuario actual
    LaunchedEffect(usuarioLogueadoId) {
        mercadoViewModel.inicializar(usuarioLogueadoId)
    }

    // Estados reactivos del ViewModel
    val ofertas by  mercadoViewModel.ofertasDisponibles.collectAsState()
    val monedasFiltro by mercadoViewModel.monedasFiltro.collectAsState()
    val tengoSelected by mercadoViewModel.filtroTengo.collectAsState()
    val quieroSelected by mercadoViewModel.filtroQuiero.collectAsState()
    val cargando by mercadoViewModel.cargando.collectAsState()

    // Control de diálogos de filtros
    var mostrarDialogoTengo by remember { mutableStateOf(false) }
    var mostrarDialogoQuiero by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BinanceBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // =========================================================================
        // 🎛️ NAVEGACIÓN SECUNDARIA SUPERIOR (Al lado de Ofertar / Buscar)
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BinanceInputBackground, RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Botón 1: Buscar Ofertas (Activo por defecto en esta pantalla)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(BinanceYellow, RoundedCornerShape(6.dp)) // Fondo amarillo brillante
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Buscar ofertas",
                    color = BinanceBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Botón 2: Publicar Oferta (Nos lleva al formulario anterior al presionarlo)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavegarAPublicarClick() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Publicar oferta",
                    color = BinanceTextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =========================================================================
        // 🔍 FILTROS DINÁMICOS DE DIVISAS (Tengo / Quiero)
        // =========================================================================
        CabeceraFiltrosMercado(
            codigoTengo = tengoSelected?.codigoIso ?: "---",
            codigoQuiero = quieroSelected?.codigoIso ?: "---",
            onTengoClick = { mostrarDialogoTengo = true },
            onQuieroClick = { mostrarDialogoQuiero = true }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Subtítulo de sección
        Text(
            text = "Ofertas disponibles",
            color = BinanceTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // =========================================================================
        // 📜 LISTADO DE TARJETAS P2P EN VIVO
        // =========================================================================
        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BinanceYellow)
            }
        } else if (ofertas.isEmpty()) {
            // Estado vacío calcado de tu imagen de referencia
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = BinanceTextSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No se encontraron ofertas",
                    color = BinanceTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Intenta ajustar los filtros de búsqueda",
                    color = BinanceTextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(ofertas) { oferta ->
                    OfertaP2PCard(
                        // Extrae el nombre real del ofertante mapeado por el JOIN, o usa un fallback
                        nombreUsuario = oferta.ofertanteInfo?.nombre ?: "Usuario Anónimo",
                        calificacion = oferta.ofertanteInfo?.calificacion ?: 5.0,
                        totalOperaciones = oferta.ofertanteInfo?.totalOperaciones ?: 100,
                        banco = oferta.bancoInfo?.banco ?: "Transferencia",
                        tasaCambio = oferta.tasaCambio,
                        montoOrigen = oferta.montoOrigen,
                        codigoTengo = tengoSelected?.codigoIso ?: "",
                        codigoQuiero = quieroSelected?.codigoIso ?: "",
                        onTomarOfertaClick = {
                            // TODO: Próxima pantalla (Detalle e intención de pago)
                        }
                    )
                }
            }
        }
    }

    // Modales de Selección de Divisas de los Filtros
    if (mostrarDialogoTengo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoTengo = false },
            confirmButton = {},
            containerColor = BinanceInputBackground,
            title = { Text("Tengo en mi banco/mano:", color = BinanceTextPrimary, fontSize = 16.sp) },
            text = {
                Column {
                    monedasFiltro.forEach { mon ->
                        Text(
                            text = "${mon.codigoIso} - ${mon.nombre}",
                            color = BinanceTextPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    mercadoViewModel.aplicarFiltroTengo(mon)
                                    mostrarDialogoTengo = false
                                }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        )
    }

    if (mostrarDialogoQuiero) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoQuiero = false },
            confirmButton = {},
            containerColor = BinanceInputBackground,
            title = { Text("Quiero recibir en la app:", color = BinanceTextPrimary, fontSize = 16.sp) },
            text = {
                Column {
                    monedasFiltro.forEach { mon ->
                        Text(
                            text = "${mon.codigoIso} - ${mon.nombre}",
                            color = BinanceTextPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    mercadoViewModel.aplicarFiltroQuiero(mon)
                                    mostrarDialogoQuiero = false
                                }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        )
    }
}