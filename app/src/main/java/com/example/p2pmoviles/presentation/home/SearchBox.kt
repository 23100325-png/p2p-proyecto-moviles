package com.example.p2pmoviles.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2pmoviles.presentation.user.mercadoP2P.MercadoP2PViewModel

private val CardBg = Color(0xFF151A20)
private val Yellow = Color(0xFFFFC400)
private val SoftText = Color(0xFFB8BDC6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBox(viewModel: MercadoP2PViewModel) {
    val monedas by viewModel.monedasFiltro.collectAsState()
    val tengoSelected by viewModel.filtroTengo.collectAsState()
    val quieroSelected by viewModel.filtroQuiero.collectAsState()

    var expandedTengo by remember { mutableStateOf(false) }
    var expandedQuiero by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, MaterialTheme.shapes.large)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Monedas", color = Color.White, fontWeight = FontWeight.SemiBold)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Dropdown para "Tengo"
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = tengoSelected?.codigoIso ?: "---",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tengo en banco") },
                    modifier = Modifier.fillMaxWidth().clickable { expandedTengo = true },
                    enabled = false, // Lo manejamos con el clic del Box o el modificador
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.White,
                        disabledBorderColor = SoftText,
                        disabledLabelColor = SoftText
                    )
                )
                Box(modifier = Modifier.matchParentSize().clickable { expandedTengo = true })
                
                DropdownMenu(expanded = expandedTengo, onDismissRequest = { expandedTengo = false }) {
                    monedas.forEach { mon ->
                        DropdownMenuItem(
                            text = { Text(mon.codigoIso) },
                            onClick = {
                                viewModel.aplicarFiltroTengo(mon)
                                expandedTengo = false
                            }
                        )
                    }
                }
            }

            // Dropdown para "Quiero"
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = quieroSelected?.codigoIso ?: "---",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Quiero recibir") },
                    modifier = Modifier.fillMaxWidth().clickable { expandedQuiero = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.White,
                        disabledBorderColor = SoftText,
                        disabledLabelColor = SoftText
                    )
                )
                Box(modifier = Modifier.matchParentSize().clickable { expandedQuiero = true })

                DropdownMenu(expanded = expandedQuiero, onDismissRequest = { expandedQuiero = false }) {
                    monedas.forEach { mon ->
                        DropdownMenuItem(
                            text = { Text(mon.codigoIso) },
                            onClick = {
                                viewModel.aplicarFiltroQuiero(mon)
                                expandedQuiero = false
                            }
                        )
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.buscarOfertasP2P() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Yellow,
                contentColor = Color.Black
            )
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refrescar Ofertas", fontWeight = FontWeight.Bold)
        }
    }
}