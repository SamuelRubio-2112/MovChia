package com.movchia.app.ui.screens.cms_admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.data.model.ContentType
import com.movchia.app.data.model.EducationalContent
import com.movchia.app.data.model.Forum
import com.movchia.app.ui.UiState
import com.movchia.app.ui.components.ContentCard
import com.movchia.app.ui.components.MovChiaTopBar
import com.movchia.app.ui.screens.content.displayName
import com.movchia.app.ui.screens.content.icon
import com.movchia.app.ui.theme.ChiaRed

private enum class AdminTab(val label: String) { CONTENTS("Contenidos"), FORUMS("Foros") }

/**
 * Pantalla 12_AdminDashboard: panel de gestión con dos pestañas.
 *  - Contenidos: todo lo publicado y en borrador, con filtro por tipo, edición y borrado.
 *  - Foros: temas de opinión, abrir/cerrar, editar y moderar comentarios.
 */
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    forumsViewModel: AdminForumsViewModel,
    onSignedOut: () -> Unit,
    onCreateContent: () -> Unit,
    onEditContent: (EducationalContent) -> Unit,
    onPreviewContent: (EducationalContent) -> Unit,
    onModerateForum: (Forum) -> Unit,
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val forumMessage by forumsViewModel.message.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(AdminTab.CONTENTS) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.load()
        forumsViewModel.load()
    }
    LaunchedEffect(message) {
        message?.let { snackbar.showSnackbar(it); viewModel.consumeMessage() }
    }
    LaunchedEffect(forumMessage) {
        forumMessage?.let { snackbar.showSnackbar(it); forumsViewModel.consumeMessage() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            MovChiaTopBar(title = "Panel de gestión") {
                IconButton(onClick = { viewModel.signOut(); onSignedOut() }) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == AdminTab.CONTENTS,
                    onClick = { tab = AdminTab.CONTENTS },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text(AdminTab.CONTENTS.label) },
                )
                NavigationBarItem(
                    selected = tab == AdminTab.FORUMS,
                    onClick = { tab = AdminTab.FORUMS },
                    icon = { Icon(Icons.Filled.Forum, contentDescription = null) },
                    label = { Text(AdminTab.FORUMS.label) },
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (tab) {
                        AdminTab.CONTENTS -> onCreateContent()
                        AdminTab.FORUMS -> forumsViewModel.startCreate()
                    }
                },
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = if (tab == AdminTab.CONTENTS) "Nuevo contenido" else "Nuevo foro",
                )
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = session?.let { "${it.role.displayName} · ${it.email}" } ?: "Sesión de personal",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            when (tab) {
                AdminTab.CONTENTS -> ContentsTab(viewModel, onEditContent, onPreviewContent)
                AdminTab.FORUMS -> AdminForumsTab(forumsViewModel, onModerateForum)
            }
        }
    }
}

@Composable
private fun ContentsTab(
    viewModel: AdminDashboardViewModel,
    onEditContent: (EducationalContent) -> Unit,
    onPreviewContent: (EducationalContent) -> Unit,
) {
    val contents by viewModel.contents.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<EducationalContent?>(null) }

    pendingDelete?.let { content ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("¿Eliminar contenido?") },
            text = { Text("\"${content.title}\" desaparecerá de la app para todos los grupos. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.delete(content); pendingDelete = null },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = ChiaRed),
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") } },
        )
    }

    when (val s = contents) {
        UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is UiState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(s.message, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Button(onClick = viewModel::load) { Text("Reintentar") }
            }
        }
        is UiState.Success -> {
            MetricsRow(s.data)
            FilterRow(selected = filter, onSelect = viewModel::setFilter)
            val visible = s.data.filter { filter == null || it.type == filter }
            if (visible.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (s.data.isEmpty()) "Aún no hay contenido. Usa el botón + para crear el primero."
                        else "No hay contenido de este tipo.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                return
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(visible, key = { it.id }) { content ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ContentCard(
                            title = content.title,
                            subtitle = buildString {
                                append(content.type.displayName())
                                append(" · ")
                                append(content.targetGroups.joinToString { it.displayName })
                                if (!content.published) append(" · BORRADOR")
                            },
                            imageUrl = content.thumbnailUrl,
                            icon = content.type.icon(),
                            onClick = { onPreviewContent(content) },
                            modifier = Modifier.weight(1f),
                            accentColor = if (content.published) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                        )
                        Column {
                            IconButton(onClick = { onEditContent(content) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { pendingDelete = content }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = ChiaRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(selected: ContentType?, onSelect: (ContentType?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("Todos") })
        ContentType.entries.forEach { type ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelect(if (selected == type) null else type) },
                label = { Text(type.displayName()) },
            )
        }
    }
}

@Composable
private fun MetricsRow(contents: List<EducationalContent>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
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
                        text = contents.count { group in it.targetGroups && it.published }.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
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
