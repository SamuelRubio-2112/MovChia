package com.movchia.app.ui.components

import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.movchia.app.R
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.ui.theme.ChiaBlue
import com.movchia.app.ui.theme.ChiaNavy
import com.movchia.app.ui.theme.ChiaTeal
import com.movchia.app.ui.theme.LocalHighContrast
import com.movchia.app.ui.theme.MovChiaTheme
import com.movchia.app.ui.theme.White

/**
 * Origen del video que reproducirá el componente. La reproducción real
 * (ExoPlayer/Media3) se integrará más adelante; por ahora solo se representa.
 */
sealed interface VideoSource {
    /** Video alojado en Cloud Storage, YouTube u otra URL pública. */
    data class Url(val url: String, val thumbnailUrl: String = "") : VideoSource

    /** Video empaquetado en `res/raw`. */
    data class LocalRes(@param:RawRes val resId: Int) : VideoSource

    /** Aún no hay video asignado (estado por defecto del CMS). */
    data object None : VideoSource
}

/**
 * Espacio reservado para un Video Educativo o Campaña (secciones "Campaña del
 * Mes", "Dibujos de Seguridad" y "Videos Explicativos" del Figma).
 *
 * - Relación 16:9 con esquinas redondeadas.
 * - Si el origen es una URL con miniatura, la carga con Coil.
 * - Degradado oscuro inferior con título y etiqueta descriptiva.
 * - Botón circular de reproducción en el centro.
 *
 * @param source origen del video (URL, recurso local o ninguno).
 * @param title título mostrado sobre el degradado (ej. "Campaña: Respeta al Peatón").
 * @param onPlayClick acción al tocar el componente o el ícono de reproducir.
 */
@Composable
fun VideoPlaceholderComponent(
    source: VideoSource,
    modifier: Modifier = Modifier,
    title: String? = null,
    label: String = stringResource(
        if (source is VideoSource.None) R.string.video_placeholder_label else R.string.video_tap_to_play,
    ),
    onPlayClick: () -> Unit = {},
) {
    val highContrast = LocalHighContrast.current
    val playDescription = stringResource(R.string.cd_play_video)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(MaterialTheme.shapes.medium)
            .clickable(role = Role.Button, onClickLabel = playDescription, onClick = onPlayClick),
        shape = MaterialTheme.shapes.medium,
        color = ChiaNavy,
        tonalElevation = 2.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo: miniatura remota o degradado institucional.
            val thumbnail = (source as? VideoSource.Url)?.thumbnailUrl.orEmpty()
            if (thumbnail.isNotBlank()) {
                AsyncImage(
                    model = thumbnail,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(listOf(ChiaBlue, ChiaNavy, ChiaTeal.copy(alpha = 0.6f))),
                        ),
                )
            }

            // Degradado inferior para legibilidad del texto.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, ChiaNavy.copy(alpha = 0.85f)),
                            startY = 120f,
                        ),
                    ),
            )

            // Botón de reproducción.
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.18f))
                    .border(2.dp, White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = playDescription,
                    tint = White,
                    modifier = Modifier.size(40.dp),
                )
            }

            // Título y etiqueta.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                if (!title.isNullOrBlank()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (source is VideoSource.None) MaterialTheme.colorScheme.tertiary else ChiaTeal),
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = White.copy(alpha = if (highContrast) 1f else 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun VideoPlaceholderPreview() {
    MovChiaTheme(ageGroup = AgeGroup.ADULTS) {
        VideoPlaceholderComponent(
            source = VideoSource.None,
            title = "Campaña: Respeta al Peatón",
            modifier = Modifier.padding(16.dp),
        )
    }
}
