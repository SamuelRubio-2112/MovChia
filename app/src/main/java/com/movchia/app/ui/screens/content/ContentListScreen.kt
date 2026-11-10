package com.movchia.app.ui.screens.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movchia.app.data.model.EducationalContent
import com.movchia.app.ui.UiState
import com.movchia.app.ui.components.ContentCard
import com.movchia.app.ui.components.MovChiaTopBar

/**
 * Pantalla de listado de contenido. Reutilizada por Noticias, Videos,
 * Cuentos, Dibujos y Preguntas frecuentes; el tipo lo decide la ruta.
 */
@Composable
fun ContentListScreen(
    viewModel: ContentListViewModel,
    title: String,
    onBack: () -> Unit,
    onOpenContent: (EducationalContent) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { MovChiaTopBar(title = title, onBack = onBack) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                is UiState.Error -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        s.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = viewModel::load) { Text("Reintentar") }
                }

                is UiState.Success -> if (s.data.isEmpty()) {
                    Text(
                        text = "Aún no hay contenido publicado en esta sección.",
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
                        items(s.data, key = { it.id }) { content ->
                            ContentCard(
                                title = content.title,
                                subtitle = content.description.takeIf { it.isNotBlank() },
                                imageUrl = content.thumbnailUrl,
                                icon = content.type.icon(),
                                onClick = { onOpenContent(content) },
                            )
                        }
                    }
                }
            }
        }
    }
}
