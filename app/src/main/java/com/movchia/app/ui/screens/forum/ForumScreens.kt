package com.movchia.app.ui.screens.forum

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movchia.app.data.local.UserProfileLocalStore
import com.movchia.app.data.model.Forum
import com.movchia.app.data.model.ForumPost
import com.movchia.app.ui.UiState
import com.movchia.app.ui.components.MovChiaTopBar
import com.movchia.app.ui.theme.ChiaBlue
import com.movchia.app.ui.theme.ChiaSkyBackground
import com.movchia.app.ui.theme.ChiaTeal
import com.movchia.app.ui.theme.White
import java.text.DateFormat
import java.util.Date

/** Lista de foros ("Opiniones" en Jóvenes y Adultos). */
@Composable
fun ForumsScreen(
    viewModel: ForumsViewModel,
    onBack: () -> Unit,
    onOpenForum: (Forum) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { MovChiaTopBar(title = "Opiniones", onBack = onBack) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is UiState.Error -> ErrorBlock(s.message, viewModel::load, Modifier.align(Alignment.Center))
                is UiState.Success -> if (s.data.isEmpty()) {
                    Text(
                        "Aún no hay foros abiertos. ¡Pronto podrás opinar!",
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
                        item {
                            Text(
                                "Cuéntanos qué piensas sobre la movilidad en Chía. Tus comentarios ayudan a la Secretaría a mejorar.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }
                        items(s.data, key = { it.id }) { forum ->
                            ForumCard(forum, onClick = { onOpenForum(forum) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ForumCard(forum: Forum, onClick: () -> Unit) {
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
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(ChiaSkyBackground),
                contentAlignment = Alignment.Center,
            ) {
                Text(forum.emoji, style = MaterialTheme.typography.titleLarge)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(forum.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                if (forum.description.isNotBlank()) {
                    Text(
                        forum.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Detalle de un foro: comentarios (más recientes primero) y caja para opinar. */
@Composable
fun ForumDetailScreen(
    viewModel: ForumDetailViewModel,
    title: String,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val nickname by viewModel.nickname.collectAsStateWithLifecycle()
    var askNickname by remember { mutableStateOf(false) }

    if (askNickname) {
        NicknameDialog(
            current = nickname,
            onDismiss = { askNickname = false },
            onConfirm = { viewModel.saveNickname(it); askNickname = false },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MovChiaTopBar(title = title, onBack = onBack) {
                TextButton(onClick = { askNickname = true }) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(nickname.ifBlank { "Apodo" }, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
        bottomBar = {
            val s = state as? UiState.Success ?: return@Scaffold
            Composer(
                draft = s.data.draft,
                sending = s.data.sending,
                error = s.data.sendError,
                onDraftChange = viewModel::onDraftChange,
                onSend = { if (nickname.isBlank()) askNickname = true else viewModel.send() },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is UiState.Error -> ErrorBlock(s.message, viewModel::load, Modifier.align(Alignment.Center))
                is UiState.Success -> if (s.data.posts.isEmpty()) {
                    Text(
                        "Sé la primera persona en opinar en este foro.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(s.data.posts, key = { it.id }) { post -> PostCard(post, mine = post.nickname == nickname) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostCard(post: ForumPost, mine: Boolean) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (mine) ChiaSkyBackground else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(if (mine) ChiaBlue else ChiaTeal),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        post.nickname.take(1).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = White,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        post.nickname + if (mine) " (tú)" else "",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        formatDate(post.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(post.text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun Composer(
    draft: String,
    sending: Boolean,
    error: String?,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp, shadowElevation = 8.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            if (error != null) {
                Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(4.dp))
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe tu opinión…") },
                    maxLines = 4,
                    enabled = !sending,
                    shape = MaterialTheme.shapes.medium,
                    supportingText = {
                        Text("${draft.length}/${ForumDetailViewModel.MAX_TEXT}", style = MaterialTheme.typography.labelSmall)
                    },
                )
                if (sending) {
                    CircularProgressIndicator(Modifier.size(40.dp).padding(bottom = 22.dp))
                } else {
                    FilledIconButton(
                        onClick = onSend,
                        modifier = Modifier.padding(bottom = 22.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Publicar comentario")
                    }
                }
            }
        }
    }
}

@Composable
private fun NicknameDialog(current: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var value by remember { mutableStateOf(current) }
    val valid = UserProfileLocalStore.isValidNickname(value)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Cómo quieres firmar?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Elige un apodo para tus comentarios. No pedimos nombre, correo ni documento.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it.take(UserProfileLocalStore.NICKNAME_MAX) },
                    singleLine = true,
                    placeholder = { Text("Ej. Ciclista de Chía") },
                    supportingText = {
                        Text("Entre ${UserProfileLocalStore.NICKNAME_MIN} y ${UserProfileLocalStore.NICKNAME_MAX} caracteres")
                    },
                )
            }
        },
        confirmButton = { Button(onClick = { onConfirm(value) }, enabled = valid) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}

private fun formatDate(millis: Long): String =
    if (millis <= 0L) "" else DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(millis))
