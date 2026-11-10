package com.movchia.app.ui.screens.cms_admin

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movchia.app.data.model.Forum
import com.movchia.app.data.model.ForumPost
import com.movchia.app.domain.usecases.SaveForumUseCase
import com.movchia.app.ui.UiState
import com.movchia.app.ui.components.MovChiaTopBar
import com.movchia.app.ui.theme.ChiaRed
import java.text.DateFormat
import java.util.Date

/** Pestaña "Foros" del panel de gestión. */
@Composable
fun AdminForumsTab(
    viewModel: AdminForumsViewModel,
    onModerateForum: (Forum) -> Unit,
) {
    val forums by viewModel.forums.collectAsStateWithLifecycle()
    val editing by viewModel.editing.collectAsStateWithLifecycle()
    val editError by viewModel.editError.collectAsStateWithLifecycle()
    val saving by viewModel.saving.collectAsStateWithLifecycle()

    editing?.let { forum ->
        ForumDialog(
            forum = forum,
            error = editError,
            saving = saving,
            onChange = viewModel::updateEditing,
            onDismiss = viewModel::cancelEdit,
            onConfirm = viewModel::saveEditing,
        )
    }

    when (val s = forums) {
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
        is UiState.Success -> if (s.data.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Aún no hay foros. Usa el botón + para abrir el primer tema de opinión.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(s.data, key = { it.id }) { forum ->
                    AdminForumCard(
                        forum = forum,
                        onOpen = { onModerateForum(forum) },
                        onEdit = { viewModel.startEdit(forum) },
                        onToggleOpen = { viewModel.setOpen(forum, it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminForumCard(
    forum: Forum,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onToggleOpen: (Boolean) -> Unit,
) {
    Card(
        onClick = onOpen,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(forum.emoji, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(forum.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        if (forum.open) "Abierto · visible en la app" else "Cerrado · oculto en la app",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (forum.open) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    )
                }
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Editar foro") }
                Switch(checked = forum.open, onCheckedChange = onToggleOpen)
            }
            if (forum.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    forum.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Toca para moderar los comentarios",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ForumDialog(
    forum: Forum,
    error: String?,
    saving: Boolean,
    onChange: ((Forum) -> Forum) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(if (forum.id.isBlank()) "Nuevo foro" else "Editar foro") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = forum.emoji,
                        onValueChange = { v -> onChange { it.copy(emoji = v.take(SaveForumUseCase.MAX_EMOJI)) } },
                        label = { Text("Emoji") },
                        singleLine = true,
                        modifier = Modifier.width(88.dp),
                    )
                    OutlinedTextField(
                        value = forum.title,
                        onValueChange = { v -> onChange { it.copy(title = v.take(SaveForumUseCase.MAX_TITLE)) } },
                        label = { Text("Título") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedTextField(
                    value = forum.description,
                    onValueChange = { v -> onChange { it.copy(description = v.take(SaveForumUseCase.MAX_DESCRIPTION)) } },
                    label = { Text("Pregunta o descripción") },
                    minLines = 2,
                    maxLines = 4,
                    supportingText = { Text("${forum.description.length}/${SaveForumUseCase.MAX_DESCRIPTION}") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Abierto a comentarios", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = forum.open, onCheckedChange = { v -> onChange { it.copy(open = v) } })
                }
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            if (saving) CircularProgressIndicator(Modifier.padding(8.dp))
            else Button(onClick = onConfirm) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancelar") } },
    )
}

/** Pantalla 14_ModeracionOpiniones: comentarios de un foro, incluidos los ocultos. */
@Composable
fun ForumPostsModerationScreen(
    viewModel: ForumPostsModerationViewModel,
    title: String,
    onBack: () -> Unit,
) {
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var pendingDelete by remember { mutableStateOf<ForumPost?>(null) }

    LaunchedEffect(message) {
        message?.let { snackbar.showSnackbar(it); viewModel.consumeMessage() }
    }

    pendingDelete?.let { post ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("¿Eliminar comentario?") },
            text = { Text("Se borrará definitivamente el comentario de ${post.nickname}. Si solo quieres que no se vea, usa \"Ocultar\".") },
            confirmButton = {
                Button(
                    onClick = { viewModel.delete(post); pendingDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = ChiaRed),
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") } },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { MovChiaTopBar(title = "Moderar: $title", onBack = onBack) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = posts) {
                UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is UiState.Error -> Column(
                    Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = viewModel::load) { Text("Reintentar") }
                }
                is UiState.Success -> if (s.data.isEmpty()) {
                    Text(
                        "Este foro aún no tiene comentarios.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            val hidden = s.data.count { it.hidden }
                            Text(
                                "${s.data.size} comentarios · $hidden ocultos",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        items(s.data, key = { it.id }) { post ->
                            ModerationPostCard(
                                post = post,
                                onToggleHidden = { viewModel.toggleHidden(post) },
                                onDelete = { pendingDelete = post },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModerationPostCard(post: ForumPost, onToggleHidden: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (post.hidden) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(post.nickname, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text(
                    formatDate(post.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                if (post.hidden) {
                    Text("OCULTO", style = MaterialTheme.typography.labelSmall, color = ChiaRed, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                post.text,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (post.hidden) TextDecoration.LineThrough else null,
                color = if (post.hidden) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onToggleHidden) {
                    Icon(
                        if (post.hidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (post.hidden) "Mostrar" else "Ocultar")
                }
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ChiaRed),
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}

private fun formatDate(millis: Long): String =
    if (millis <= 0L) "" else DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(millis))
