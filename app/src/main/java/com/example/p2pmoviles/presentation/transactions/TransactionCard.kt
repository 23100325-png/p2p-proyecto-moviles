package com.example.p2pmoviles.presentation.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CardBg = Color(0xFF151A20)
private val Yellow = Color(0xFFFFC400)
private val Green = Color(0xFF39C56A)
private val Red = Color(0xFFFF554A)
private val SoftText = Color(0xFFB8BDC6)

@Composable
fun TransactionCard(transaction: Transaction) {
    val statusColor = when (transaction.status) {
        "Completada" -> Green
        "En proceso" -> Yellow
        "Cancelada" -> Color.Gray
        else -> SoftText
    }

    val circleColor = when (transaction.status) {
        "Cancelada" -> Red
        "En proceso" -> Color.Gray
        else -> if (transaction.isBuy) Green else Yellow
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, MaterialTheme.shapes.large)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(55.dp)
                .background(circleColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    transaction.status == "Cancelada" -> Icons.Default.Close
                    transaction.status == "En proceso" -> Icons.Default.Schedule
                    transaction.isBuy -> Icons.Default.ArrowDownward
                    else -> Icons.Default.ArrowUpward
                },
                contentDescription = null,
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.type, color = Color.White)
            Text(transaction.date, color = SoftText)
            Spacer(modifier = Modifier.height(8.dp))
            Text(transaction.person, color = SoftText)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(transaction.usdAmount, color = Color.White)
            Text(
                text = transaction.penAmount,
                color = if (transaction.penAmount.startsWith("+")) Green else Red
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(transaction.status, color = statusColor)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = SoftText
        )
    }
}