package com.example.p2pmoviles.presentation.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
private val DarkBg = Color(0xFF070B0F)
private val CardBg = Color(0xFF151A20)
private val Yellow = Color(0xFFFFC400)
private val SoftText = Color(0xFFB8BDC6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onBackClick: () -> Unit
) {
    val transactions = listOf(
        Transaction("Compra USDT", "23 may 2024, 10:30 a. m.", "De: Maria FX", "500.00 USD", "+ 1,910.00 PEN", "Completada", true),
        Transaction("Venta USDT", "21 may 2024, 08:15 p. m.", "A: Juan Trader", "300.00 USD", "- 1,140.00 PEN", "Completada", false),
        Transaction("Compra USDT", "19 may 2024, 02:45 p. m.", "De: CryptoCambio", "200.00 USD", "+ 760.00 PEN", "Completada", true),
        Transaction("Venta USDT", "17 may 2024, 11:10 a. m.", "A: Luis FX", "150.00 USD", "- 570.00 PEN", "Completada", false),
        Transaction("Compra USDT", "15 may 2024, 09:20 a. m.", "De: Maria FX", "250.00 USD", "+ 950.00 PEN", "En proceso", true),
        Transaction("Venta USDT", "12 may 2024, 04:50 p. m.", "A: Juan Trader", "100.00 USD", "- 380.00 PEN", "Cancelada", false),
        Transaction("Compra USDT", "10 may 2024, 07:30 p. m.", "De: CryptoCambio", "400.00 USD", "+ 1,520.00 PEN", "Completada", true)
    )

    var expandedStatus by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("Todos los estados") }

    var expandedSort by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf("Más recientes") }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    var startDate by remember { mutableStateOf("Desde") }
    var endDate by remember { mutableStateOf("Hasta") }

    var startDateMillis by remember { mutableStateOf<Long?>(null) }
    var endDateMillis by remember { mutableStateOf<Long?>(null) }

    var endDateError by remember { mutableStateOf(false) }

    val states = listOf("Todos los estados", "Completada", "En proceso", "Cancelada")
    val sortOptions = listOf("Más recientes", "Más antiguas", "Mayor monto", "Menor monto")



    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B1117), DarkBg)
                )
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(25.dp))

                Text(
                    text = "Historial de transacciones",
                    color = Color.White,
                    fontSize = 23.sp
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg, MaterialTheme.shapes.large)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Filtrar por fecha",
                    color = SoftText,
                    fontSize = 14.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Desde") },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        },
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    showStartDatePicker = true
                                }
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = "Seleccionar fecha"
                                )
                            }
                        }
                    )

                    OutlinedTextField(
                        value = endDate,
                        onValueChange = {},
                        readOnly = true,
                        enabled = startDateMillis != null,
                        isError = endDateError,
                        label = { Text("Hasta") },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        },
                        supportingText = {
                            if (endDateError) {
                                Text("La fecha debe ser mayor que la fecha inicial")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(
                                enabled = startDateMillis != null,
                                onClick = {
                                    showEndDatePicker = true
                                }
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = "Seleccionar fecha"
                                )
                            }
                        }
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = {
                        expandedStatus = !expandedStatus
                    }
                ) {
                    OutlinedTextField(
                        value = selectedStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Filtrar por estado") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expandedStatus
                            )
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = {
                            expandedStatus = false
                        }
                    ) {
                        states.forEach { state ->
                            DropdownMenuItem(
                                text = { Text(state) },
                                onClick = {
                                    selectedStatus = state
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Transacciones", color = Color.White, fontSize = 22.sp)

                Box {
                    Row(
                        modifier = Modifier.clickable {
                            expandedSort = true
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedSort, color = SoftText)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Tune, contentDescription = null, tint = Yellow)
                    }

                    DropdownMenu(
                        expanded = expandedSort,
                        onDismissRequest = {
                            expandedSort = false
                        }
                    ) {
                        sortOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedSort = option
                                    expandedSort = false
                                }
                            )
                        }
                    }
                }
            }
        }

        items(transactions) { transaction ->
            TransactionCard(transaction)
        }

        item {
            Text(
                text = "🛡️ Solo tú puedes ver tu historial de transacciones.\nLas operaciones se mantienen almacenadas para fines de auditoría.",
                color = SoftText,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg, MaterialTheme.shapes.large)
                    .padding(16.dp)
            )
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                showStartDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        startDatePickerState.selectedDateMillis?.let { millis ->

                            startDateMillis = millis
                            startDate = millis.toDateString()

                            endDateMillis = null
                            endDate = "Hasta"
                            endDateError = false
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showStartDatePicker = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                showEndDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        endDatePickerState.selectedDateMillis?.let { millis ->

                            if (startDateMillis != null && millis > startDateMillis!!) {

                                endDateMillis = millis
                                endDate = millis.toDateString()
                                endDateError = false

                                showEndDatePicker = false

                            } else {

                                endDateError = true
                            }
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEndDatePicker = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }
}

private fun Long.toDateString(): String {
    val formatter = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    )
    return formatter.format(Date(this))
}