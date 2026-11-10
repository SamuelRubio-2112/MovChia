package com.movchia.app.ui.screens.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.data.model.ContentType
import com.movchia.app.data.model.EducationalContent
import com.movchia.app.ui.UiState
import com.movchia.app.ui.components.MovChiaTopBar
import com.movchia.app.ui.components.VideoPlayer
import com.movchia.app.ui.theme.ChiaNavy
import com.movchia.app.ui.theme.ChiaYellow
import com.movchia.app.ui.theme.LocalAgeGroup
import com.movchia.app.utils.YouTubeUrls
import java.text.DateFormat
import java.util.Date

/**
 * Visor de contenido (noticia, cuento, video, campaña, FAQ). Adapta el medio
 * al tipo de dato: reproductor para video (YouTube o MP4), imagen de
 * portada para texto. En modo Adultos Mayores agrega el botón "Escuchar".
 */
@Composable
fun ContentDetailScreen(
    viewModel: ContentDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ttsProblem by viewModel.ttsProblem.collectAsStateWithLifecycle()
    val isSenior = LocalAgeGroup.current == AgeGroup.SENIORS

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val content = (state as? UiState.Success)?.data
            MovChiaTopBar(title = content?.type?.displayName() ?: "Contenido", onBack = onBack) {
                if (isSenior && content != null) {
                    Button(
                        onClick = viewModel::readAloud,
                        colors = ButtonDefaults.buttonColors(containerColor = ChiaYellow, contentColor = ChiaNavy),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(end = 12.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Escuchar", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        },
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

                is UiState.Success -> ContentBody(content = s.data, ttsProblem = ttsProblem)
            }
        }
    }
}

@Composable
private fun ContentBody(content: EducationalContent, ttsProblem: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (content.mediaKind()) {
            MediaKind.VIDEO -> VideoPlayer(url = content.mediaUrl)
            MediaKind.IMAGE -> AsyncImage(
                model = content.thumbnailUrl.ifBlank { content.mediaUrl },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            MediaKind.NONE -> Unit
        }

        // Etiqueta de tipo + fecha de publicación.
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(content.type.icon(), contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Text(
                text = content.type.displayName().uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            if (content.publishedAt > 0L) {
                Text(
                    text = "· " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(content.publishedAt)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text = content.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        if (ttsProblem != null) {
            Text(
                text = ttsProblem,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        if (content.description.isNotBlank()) {
            Text(
                text = content.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        if (content.durationMinutes > 0) {
            Text(
                text = "Duración: ${content.durationMinutes} min",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ---------------------------------------------------------------------------
// Utilidades de presentación compartidas por el listado y el visor
// ---------------------------------------------------------------------------

internal enum class MediaKind { VIDEO, IMAGE, NONE }

private val videoTypes = setOf(ContentType.VIDEO, ContentType.CARTOON, ContentType.CAMPAIGN)
private val videoExtensions = listOf(".mp4", ".m3u8", ".webm", ".mkv", ".mov")

/** Decide qué medio mostrar según el tipo de contenido y las URLs disponibles. */
internal fun EducationalContent.mediaKind(): MediaKind {
    val url = mediaUrl.trim()
    val lower = url.lowercase().substringBefore('?')
    val looksLikeVideo = url.isNotEmpty() &&
        (YouTubeUrls.isYouTubeUrl(url) || type in videoTypes || videoExtensions.any { lower.endsWith(it) })
    return when {
        looksLikeVideo -> MediaKind.VIDEO
        thumbnailUrl.isNotBlank() || url.isNotEmpty() -> MediaKind.IMAGE
        else -> MediaKind.NONE
    }
}

internal fun ContentType.displayName(): String = when (this) {
    ContentType.VIDEO -> "Video"
    ContentType.STORY -> "Cuento"
    ContentType.CARTOON -> "Dibujos de seguridad"
    ContentType.GAME -> "Juego"
    ContentType.NEWS -> "Noticia"
    ContentType.CAMPAIGN -> "Campaña"
    ContentType.FAQ -> "Pregunta frecuente"
}

internal fun ContentType.icon(): ImageVector = when (this) {
    ContentType.VIDEO -> Icons.Filled.PlayArrow
    ContentType.STORY -> Icons.AutoMirrored.Filled.MenuBook
    ContentType.CARTOON -> Icons.Filled.Movie
    ContentType.GAME -> Icons.Filled.SportsEsports
    ContentType.NEWS -> Icons.Filled.Newspaper
    ContentType.CAMPAIGN -> Icons.Filled.Campaign
    ContentType.FAQ -> Icons.Filled.QuestionAnswer
}
