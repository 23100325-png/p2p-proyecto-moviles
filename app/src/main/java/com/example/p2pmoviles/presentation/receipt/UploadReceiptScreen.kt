package com.example.p2pmoviles.presentation.receipt

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionmoviles.presentation.receipt.TransactionSummaryCard

private val DarkBg = Color(0xFF070B0F)
private val CardBg = Color(0xFF151A20)
private val Yellow = Color(0xFFFFC400)
private val SoftText = Color(0xFFB8BDC6)
private val Green = Color(0xFF39C56A)

@Composable
fun UploadReceiptScreen(
    onBackClick: () -> Unit
) {
    var operationNumber by remember { mutableStateOf("") }
    var receiptSent by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedFile = uri
        receiptSent = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B1117), DarkBg)
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(50.dp))

            Text(
                text = "Subir comprobante",
                color = Color.White,
                fontSize = 24.sp
            )
        }

        TransactionSummaryCard()

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Comprobante de pago",
            color = Color.White,
            fontSize = 22.sp
        )

        Text(
            text = "Puedes subir imágenes (JPG, PNG) o archivos PDF.",
            color = SoftText
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(CardBg, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = Yellow,
                    modifier = Modifier.size(45.dp)
                )

                Text(
                    text = if (selectedFile == null) "Selecciona un archivo" else "Archivo seleccionado",
                    color = Yellow,
                    fontSize = 18.sp
                )

                Text(
                    text = if (selectedFile == null) {
                        "JPG, PNG o PDF • Máx. 10 MB"
                    } else {
                        selectedFile.toString()
                    },
                    color = SoftText,
                    fontSize = 12.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(
                onClick = {
                    galleryLauncher.launch("*/*")
                },
                modifier = Modifier.width(150.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = Yellow)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Galería", color = Color.White)
            }

            Spacer(modifier = Modifier.width(20.dp))

            OutlinedButton(
                onClick = {},
                modifier = Modifier.width(150.dp),
                enabled = false
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Yellow)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cámara", color = Color.White)
            }
        }

        Text(
            text = "Número de operación (opcional)",
            color = Color.White,
            fontSize = 20.sp
        )

        Text(
            text = "Agrega el número de operación o referencia bancaria.",
            color = SoftText
        )

        OutlinedTextField(
            value = operationNumber,
            onValueChange = {
                operationNumber = it
            },
            placeholder = {
                Text("Ej. 1234567890")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = CardBg
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = Yellow)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "El comprobante quedará asociado a esta transacción.",
                        color = Color.White
                    )
                    Text(
                        text = "El otro usuario podrá visualizar el comprobante enviado.",
                        color = SoftText
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Yellow)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Tu comprobante será almacenado de forma segura para auditoría y trazabilidad.",
                color = SoftText
            )
        }

        Button(
            onClick = {
                if (selectedFile != null) {
                    receiptSent = true
                }
            },
            enabled = selectedFile != null,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Yellow,
                contentColor = Color.Black,
                disabledContainerColor = Color.DarkGray,
                disabledContentColor = SoftText
            )
        ) {
            Icon(Icons.Default.Upload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Subir comprobante")
        }

        if (receiptSent) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0D3320)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Estado de la transacción: Comprobante enviado",
                        color = Green
                    )
                }
            }
        }
    }
}