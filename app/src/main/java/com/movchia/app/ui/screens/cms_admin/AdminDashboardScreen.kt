package com.movchia.app.ui.screens.cms_admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.data.model.EducationalContent
import com.movchia.app.ui.UiState
import com.movchia.app.ui.components.ContentCard
import com.movchia.app.ui.components.MovChiaTopBar

/** Pantalla 12_AdminDashboard: métricas rápidas y listado de contenido. */
@Composable
fun AdminDashboardScreen(
    viewModel: CmsAdminViewModel,
    onSignedOut: () -> Unit,
    onCreateContent: () -> Unit,
) {
    val contents by viewModel.contents.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadContents() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MovChiaTopBar(title = "Panel CMS") {
                IconButton(onClick = { viewModel.signOut(); onSignedOut() }) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateContent) {
                Icon(Icons.Filled.Add, contentDescription = "Crear contenido")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = viewModel.adminEmail?.let { "Sesión: $it" } ?: "Sesión de administrador",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            when (val s = contents) {
                UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is UiState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                }
                is UiState.Success -> {
                    MetricsRow(s.data)
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(s.data, key = { it.id }) { content ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ContentCard(
                                    title = content.title,
                                    subtitle = "${content.type.name} · ${content.targetGroups.joinToString { it.displayName }}",
                                    imageUrl = content.thumbnailUrl,
                                    icon = Icons.Filled.PlayArrow,
                                    onClick = {},
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(onClick = { viewModel.deleteContent(content.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricsRow(contents: List<EducationalContent>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AgeGroup.entries.forEach { group ->
            Card(
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = contents.count { group in it.targetGroups }.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = group.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}
