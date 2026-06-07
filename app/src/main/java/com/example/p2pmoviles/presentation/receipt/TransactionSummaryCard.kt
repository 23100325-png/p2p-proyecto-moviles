package com.example.aplicacionmoviles.presentation.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CardBg = Color(0xFF151A20)
private val Yellow = Color(0xFFFFC400)
private val SoftText = Color(0xFFB8BDC6)

@Composable
fun TransactionSummaryCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, MaterialTheme.shapes.large)
            .padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Transacción", color = SoftText)
            Text("TXN-245678", color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🇺🇸 USD", color = Color.White)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Yellow)
                Spacer(modifier = Modifier.width(16.dp))
                Text("🇵🇪 PEN", color = Color.White)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("Monto", color = SoftText)
            Text("500.00 USD", color = Color.White)
            Text("1,910.00 PEN", color = SoftText)
        }
    }
}