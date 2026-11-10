package com.movchia.app.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.movchia.app.ui.theme.ChiaNavy
import com.movchia.app.utils.YouTubeUrls

/**
 * Reproductor de video del visor de contenido.
 *
 * - Enlaces de YouTube: iframe oficial dentro de un WebView (YouTube no
 *   permite reproducir sus streams con ExoPlayer). Incluye un botón para abrir
 *   el video en la app de YouTube.
 * - Cualquier otra URL (MP4 en Cloud Storage, HLS, etc.): Media3 / ExoPlayer.
 *
 * Relación 16:9 y esquinas redondeadas, igual que [VideoPlaceholderComponent].
 */
@Composable
fun VideoPlayer(url: String, modifier: Modifier = Modifier) {
    val youtubeId = remember(url) { YouTubeUrls.extractVideoId(url) }
    if (youtubeId != null) {
        YouTubeEmbedPlayer(videoId = youtubeId, modifier = modifier)
    } else {
        ExoVideoPlayer(url = url, modifier = modifier)
    }
}

@Composable
private fun ExoVideoPlayer(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var error by remember(url) { mutableStateOf<String?>(null) }
    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            addListener(object : Player.Listener {
                override fun onPlayerError(e: PlaybackException) {
                    Log.w("MovChiaVideo", "Error de reproducción ($url): ${e.errorCodeName}", e)
                    error = when (e.errorCode) {
                        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
                        -> "No hay conexión a internet. Conéctese e intente de nuevo."
                        PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
                        PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
                        -> "El video ya no está disponible en el servidor."
                        else -> "No fue posible reproducir el video."
                    }
                }
            })
            prepare()
        }
    }

    // Pausar al salir de primer plano (llamada entrante, cambio de app) y liberar al salir de la pantalla.
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { player.pause() }
    DisposableEffect(player) { onDispose { player.release() } }

    Column(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(MaterialTheme.shapes.medium)
                .background(ChiaNavy),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = true
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
            },
            update = { view -> view.player = player },
        )
        error?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
            )
            TextButton(
                onClick = {
                    error = null
                    player.setMediaItem(MediaItem.fromUri(url))
                    player.prepare()
                    player.play()
                },
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Text("  Reintentar", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun YouTubeEmbedPlayer(videoId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val html = remember(videoId) {
        """
        <!DOCTYPE html>
        <html>
          <head>
            <meta name="viewport" content="width=device-width, initial-scale=1" />
            <style>html,body{margin:0;padding:0;height:100%;background:#000;overflow:hidden}
            iframe{position:absolute;top:0;left:0;width:100%;height:100%;border:0}</style>
          </head>
          <body>
            <iframe src="${YouTubeUrls.embedUrl(videoId)}"
              allow="autoplay; encrypted-media; picture-in-picture" allowfullscreen></iframe>
          </body>
        </html>
        """.trimIndent()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(MaterialTheme.shapes.medium)
                .background(ChiaNavy),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = true
                    // YouTube bloquea el reproductor embebido cuando detecta un WebView
                    // de app ("; wv" en el user agent) sin un origen válido.
                    settings.userAgentString = settings.userAgentString
                        .replace("; wv", "")
                        .replace(Regex("""Version/\d+\.\d+ """), "")
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()
                    // El baseUrl actúa como Referer; sin él YouTube responde "Video no disponible" (152/153).
                    loadDataWithBaseURL(YouTubeUrls.EMBED_ORIGIN, html, "text/html", "utf-8", null)
                }
            },
            onRelease = { webView ->
                webView.stopLoading()
                webView.destroy()
            },
        )
        TextButton(
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(YouTubeUrls.watchUrl(videoId))))
            },
        ) {
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
            Text("  Abrir en YouTube", style = MaterialTheme.typography.labelLarge)
        }
    }
}
