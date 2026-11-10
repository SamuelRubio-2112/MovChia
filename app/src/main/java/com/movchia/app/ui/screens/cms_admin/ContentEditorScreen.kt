package com.movchia.app.ui.screens.cms_admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.data.model.ContentType
import com.movchia.app.domain.usecases.SaveContentUseCase
import com.movchia.app.ui.UiState
import com.movchia.app.ui.components.LargeAccessibleButton
import com.movchia.app.ui.components.MovChiaTopBar
import com.movchia.app.ui.screens.content.displayName

/** Pantalla 13_EditorContenido. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContentEditorScreen(
    viewModel: ContentEditorViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MovChiaTopBar(title = if (viewModel.isNew) "Nuevo contenido" else "Editar contenido", onBack = onBack)
        },
        bottomBar = {
            val s = state as? UiState.Success ?: return@Scaffold
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp) {
                Box(Modifier.navigationBarsPadding().imePadding().padding(16.dp)) {
                    if (s.data.saving) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    } else {
                        LargeAccessibleButton(
                            text = if (s.data.form.published) "GUARDAR Y PUBLICAR" else "GUARDAR BORRADOR",
                            onClick = { viewModel.save(onSaved) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        when (val s = state) {
            UiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is UiState.Error -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = viewModel::load) { Text("Reintentar") }
                }
            }
            is UiState.Success -> EditorForm(
                state = s.data,
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditorForm(
    state: ContentEditorState,
    viewModel: ContentEditorViewModel,
    modifier: Modifier = Modifier,
) {
    val form = state.form
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.error != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    state.error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }

        SectionLabel("Tipo de contenido")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ContentType.entries.forEach { type ->
                FilterChip(
                    selected = form.type == type,
                    onClick = { viewModel.update { it.copy(type = type) } },
                    label = { Text(type.displayName()) },
                )
            }
        }

        SectionLabel("Grupos de edad")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AgeGroup.entries.forEach { group ->
                FilterChip(
                    selected = group in form.targetGroups,
                    onClick = { viewModel.toggleGroup(group) },
                    label = { Text(group.displayName) },
                )
            }
        }

        OutlinedTextField(
            value = form.title,
            onValueChange = { v -> viewModel.update { it.copy(title = v.take(SaveContentUseCase.MAX_TITLE)) } },
            label = { Text("Título") },
            singleLine = true,
            supportingText = { Text("${form.title.length}/${SaveContentUseCase.MAX_TITLE}") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.description,
            onValueChange = { v -> viewModel.update { it.copy(description = v.take(SaveContentUseCase.MAX_DESCRIPTION)) } },
            label = { Text(if (form.type == ContentType.STORY || form.type == ContentType.NEWS || form.type == ContentType.FAQ) "Texto" else "Descripción") },
            minLines = 4,
            maxLines = 12,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.mediaUrl,
            onValueChange = { v -> viewModel.update { it.copy(mediaUrl = v) } },
            label = { Text("Enlace del video o imagen") },
            placeholder = { Text("https://www.youtube.com/watch?v=…") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            supportingText = { Text("YouTube, MP4/HLS o imagen. Para videos se recomienda YouTube (sin costo de almacenamiento).") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.thumbnailUrl,
            onValueChange = { v -> viewModel.update { it.copy(thumbnailUrl = v) } },
            label = { Text("Enlace de la miniatura (opcional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = form.durationMinutes,
                onValueChange = { v -> viewModel.update { it.copy(durationMinutes = v.filter(Char::isDigit).take(3)) } },
                label = { Text("Duración (min)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = form.chapter,
                onValueChange = { v -> viewModel.update { it.copy(chapter = v.take(40)) } },
                label = { Text("Capítulo o sección") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Publicado", style = MaterialTheme.typography.titleSmall)
                Text(
                    if (form.published) "Visible en la app para los grupos elegidos."
                    else "Borrador: solo lo ve el personal en este panel.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = form.published, onCheckedChange = { v -> viewModel.update { it.copy(published = v) } })
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
}
