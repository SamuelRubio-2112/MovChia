package com.movchia.app.ui.screens.service_points

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.data.model.ServicePoint
import com.movchia.app.ui.UiState
import com.movchia.app.ui.components.LargeAccessibleButton
import com.movchia.app.ui.components.MovChiaTopBar
import com.movchia.app.ui.theme.ChiaNavy
import com.movchia.app.ui.theme.ChiaYellow
import com.movchia.app.ui.theme.LocalAgeGroup
import com.movchia.app.ui.theme.White

/**
 * Listado de sedes. Reutilizado por "Sedes y Puntos" (adultos) y
 * "Puntos de atención" (mayores): el tema de alto contraste y la tipografía
 * grande llegan solos desde [com.movchia.app.ui.theme.MovChiaTheme].
 */
@Composable
fun ServicePointsListScreen(
    viewModel: ServicePointsViewModel,
    title: String,
    onBack: () -> Unit,
    onOpenPoint: (ServicePoint) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { MovChiaTopBar(title = title, onBack = onBack) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is UiState.Error -> ErrorBlock(s.message, onRetry = viewModel::load, Modifier.align(Alignment.Center))
                is UiState.Success -> if (s.data.isEmpty()) {
                    Text(
                        text = "Aún no hay sedes registradas.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(s.data, key = { it.id }) { point ->
                            ServicePointCard(point, onClick = { onOpenPoint(point) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServicePointCard(point: ServicePoint, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Filled.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(point.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                if (point.address.isNotBlank()) {
                    Text(
                        point.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Detalle de la sede con "Cómo llegar" (Google Maps por Intent) y "Llamar". */
@Composable
fun ServicePointDetailScreen(
    viewModel: ServicePointDetailViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ttsProblem by viewModel.ttsProblem.collectAsStateWithLifecycle()
    val isSenior = LocalAgeGroup.current == AgeGroup.SENIORS
    val point = (state as? UiState.Success)?.data

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { MovChiaTopBar(title = "Sede", onBack = onBack) },
        bottomBar = {
            if (point != null) {
                Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp, shadowElevation = 8.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        LargeAccessibleButton(
                            text = "CÓMO LLEGAR",
                            onClick = { openInMaps(context, point) },
                            leadingIcon = Icons.Filled.Place,
                            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Abrir Google Maps con la ruta hasta ${point.name}",
                        )
                        if (point.phone.isNotBlank()) {
                            LargeAccessibleButton(
                                text = "LLAMAR",
                                onClick = {
                                    val digits = point.phone.filter { it.isDigit() || it == '+' }
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits")))
                                },
                                leadingIcon = Icons.Filled.Call,
                                containerColor = if (isSenior) ChiaYellow else MaterialTheme.colorScheme.secondary,
                                contentColor = if (isSenior) ChiaNavy else White,
                                contentDescription = "Llamar al ${point.phone}",
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is UiState.Error -> ErrorBlock(s.message, onRetry = viewModel::load, Modifier.align(Alignment.Center))
                is UiState.Success -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        s.data.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    if (isSenior) {
                        Button(
                            onClick = viewModel::readAloud,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = ChiaYellow,
                                contentColor = ChiaNavy,
                            ),
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.size(8.dp))
                            Text("Escuchar", style = MaterialTheme.typography.labelLarge)
                        }
                        if (ttsProblem != null) {
                            Text(ttsProblem!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    InfoRow(Icons.Filled.Place, "Dirección", s.data.address)
                    InfoRow(Icons.Filled.Schedule, "Horario", s.data.schedule)
                    InfoRow(Icons.Filled.Call, "Teléfono", s.data.phone)
                    InfoRow(Icons.Filled.Email, "Correo", s.data.email)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    if (value.isBlank()) return
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}

/**
 * Abre Google Maps (o la app de mapas disponible) con la sede. Si hay
 * coordenadas se usan; si no, se busca por dirección. Sin API key ni mapa embebido.
 */
private fun openInMaps(context: android.content.Context, point: ServicePoint) {
    val label = Uri.encode(point.name)
    val uri = if (point.latitude != 0.0 && point.longitude != 0.0) {
        Uri.parse("geo:${point.latitude},${point.longitude}?q=${point.latitude},${point.longitude}($label)")
    } else {
        Uri.parse("geo:0,0?q=${Uri.encode("${point.address}, Chía, Cundinamarca")}")
    }
    val mapsIntent = Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps")
    try {
        context.startActivity(mapsIntent)
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (_: ActivityNotFoundException) {
            val web = "https://www.google.com/maps/search/?api=1&query=" +
                Uri.encode("${point.address}, Chía, Cundinamarca")
            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(web))) }
                .onFailure { Toast.makeText(context, "No hay una app de mapas disponible.", Toast.LENGTH_LONG).show() }
        }
    }
}
