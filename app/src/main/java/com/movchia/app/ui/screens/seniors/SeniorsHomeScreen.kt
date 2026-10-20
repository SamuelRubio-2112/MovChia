package com.movchia.app.ui.screens.seniors

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.movchia.app.ui.components.LargeAccessibleButton
import com.movchia.app.ui.components.MovChiaTopBar
import com.movchia.app.ui.theme.ChiaNavy
import com.movchia.app.ui.theme.ChiaRed
import com.movchia.app.ui.theme.ChiaYellow
import com.movchia.app.ui.theme.KidsBanner
import com.movchia.app.ui.theme.Slate200
import com.movchia.app.ui.theme.Slate800
import com.movchia.app.ui.theme.White
import com.movchia.app.utils.Constants

@Composable
fun SeniorsHomeScreen(
    viewModel: SeniorsHomeViewModel,
    onOpenSimpleManual: () -> Unit,
    onOpenServicePoints: () -> Unit,
    onOpenVideos: () -> Unit,
    onOpenFaq: () -> Unit,
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MovChiaTopBar(title = "MAYORES SEGUROS") {
                Button(
                    onClick = viewModel::readScreenAloud,
                    colors = ButtonDefaults.buttonColors(containerColor = ChiaYellow, contentColor = ChiaNavy),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(end = 12.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Escuchar", style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LargeAccessibleButton(
                    text = "INICIO",
                    onClick = {},
                    leadingIcon = Icons.Filled.Home,
                    containerColor = ChiaNavy,
                    contentColor = White,
                    modifier = Modifier.weight(1f),
                )
                LargeAccessibleButton(
                    text = "EMERGENCIA ${Constants.EMERGENCY_NUMBER}",
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Constants.EMERGENCY_NUMBER}"))
                        context.startActivity(intent)
                    },
                    leadingIcon = Icons.Filled.Call,
                    containerColor = ChiaRed,
                    contentColor = White,
                    modifier = Modifier.weight(1.3f),
                    contentDescription = "Llamar a la línea de emergencia ${Constants.EMERGENCY_NUMBER}",
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // Banner de ayuda (amarillo claro con borde amarillo)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(KidsBanner)
                    .border(2.dp, ChiaYellow, MaterialTheme.shapes.small)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = Slate800, modifier = Modifier.size(28.dp))
                Text(
                    "Seleccione con un toque el botón grande para abrir la información.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate800,
                )
            }

            Spacer(Modifier.height(8.dp))

            LargeAccessibleButton(
                text = "VER MANUAL SIMPLE",
                onClick = onOpenSimpleManual,
                leadingIcon = Icons.AutoMirrored.Filled.List,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                containerColor = ChiaNavy,
                contentColor = White,
            )
            LargeAccessibleButton(
                text = "PUNTOS DE ATENCIÓN",
                onClick = onOpenServicePoints,
                leadingIcon = Icons.Filled.Phone,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                containerColor = ChiaYellow,
                contentColor = ChiaNavy,
            )
            LargeAccessibleButton(
                text = "VIDEOS EXPLICATIVOS",
                onClick = onOpenVideos,
                leadingIcon = Icons.Filled.PlayArrow,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                containerColor = ChiaNavy,
                contentColor = White,
            )
            LargeAccessibleButton(
                text = "PREGUNTAS FRECUENTES",
                onClick = onOpenFaq,
                leadingIcon = Icons.Filled.Warning,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                containerColor = Slate200,
                contentColor = ChiaNavy,
            )
        }
    }
}
