package com.movchia.app.ui.screens.traffic_code

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movchia.app.data.model.AssistantMessage
import com.movchia.app.data.model.MessageRole
import com.movchia.app.data.model.TrafficCodeArticle
import com.movchia.app.ui.UiState
import com.movchia.app.ui.components.MovChiaTopBar
import com.movchia.app.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrafficCodeScreen(
    viewModel: TrafficCodeViewModel,
    onBack: () -> Unit,
    initialTab: Int = 0,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                MovChiaTopBar(
                    title = if (viewModel.simplified) "Manual Simple" else "Manual de Tránsito",
                    onBack = onBack,
                )
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Manual") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Asistente IA") })
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> ManualTab(viewModel)
                else -> AssistantTab(viewModel)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Pestaña Manual
// ---------------------------------------------------------------------------

@Composable
private fun ManualTab(viewModel: TrafficCodeViewModel) {
    val articles by viewModel.articles.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Buscar artículo, señal o palabra clave") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
        )
        Text(
            text = Constants.TRAFFIC_CODE_LAW,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))

        when (val s = articles) {
            UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is UiState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(s.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
            }
            is UiState.Success -> {
                if (s.data.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Aún no hay artículos publicados. El equipo de la Secretaría los cargará desde el CMS.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(s.data, key = { it.id }) { article ->
                            ArticleCard(article, simplified = viewModel.simplified)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleCard(article: TrafficCodeArticle, simplified: Boolean) {
    Card(shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${article.chapter} · Art. ${article.articleNumber}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(4.dp))
            Text(article.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (simplified && article.simplifiedBody.isNotBlank()) article.simplifiedBody else article.body,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Pestaña Asistente IA
// ---------------------------------------------------------------------------

@Composable
private fun AssistantTab(viewModel: TrafficCodeViewModel) {
    val state by viewModel.assistant.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            reverseLayout = true,
        ) {
            if (state.isThinking) {
                item { ChatBubble(AssistantMessage(MessageRole.ASSISTANT, "Consultando el Código de Tránsito…")) }
            }
            items(state.messages.asReversed()) { message -> ChatBubble(message) }
            if (state.messages.isEmpty()) {
                item {
                    Text(
                        "Pregúntame sobre señales, límites de velocidad, documentos obligatorios o cualquier norma de tránsito.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        state.error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = state.input,
                onValueChange = viewModel::onAssistantInputChanged,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe tu pregunta…") },
                shape = MaterialTheme.shapes.large,
                maxLines = 3,
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = viewModel::sendQuestion, enabled = state.input.isNotBlank() && !state.isThinking) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar pregunta")
            }
        }
    }
}

@Composable
private fun ChatBubble(message: AssistantMessage) {
    val isUser = message.role == MessageRole.USER
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(bubbleColor)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(message.text, style = MaterialTheme.typography.bodyLarge, color = textColor)
        }
    }
}
