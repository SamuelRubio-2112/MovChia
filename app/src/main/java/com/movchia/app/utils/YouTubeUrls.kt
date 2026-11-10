package com.movchia.app.utils

/**
 * Reconoce enlaces de YouTube y extrae el identificador del video.
 * YouTube no permite reproducir sus videos con ExoPlayer; por eso el
 * reproductor usa el iframe oficial cuando el enlace es de YouTube.
 */
object YouTubeUrls {

    private val patterns = listOf(
        // youtube.com/watch?v=ID, /embed/ID, /shorts/ID, /live/ID, /v/ID (con o sin m. / www. / -nocookie)
        Regex("""(?:youtube\.com|youtube-nocookie\.com)/(?:watch\?(?:[^#]*&)?v=|embed/|shorts/|live/|v/)([A-Za-z0-9_-]{11})"""),
        // youtu.be/ID
        Regex("""youtu\.be/([A-Za-z0-9_-]{11})"""),
    )

    /** Devuelve el id de 11 caracteres del video, o `null` si [url] no es de YouTube. */
    fun extractVideoId(url: String): String? {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return null
        return patterns.firstNotNullOfOrNull { it.find(trimmed)?.groupValues?.get(1) }
    }

    fun isYouTubeUrl(url: String): Boolean = extractVideoId(url) != null

    /**
     * Origen con el que el WebView carga el iframe. YouTube exige un Referer
     * válido (errores 152/153 si falta); no necesita existir como sitio.
     */
    const val EMBED_ORIGIN = "https://movchia.app"

    /** URL del reproductor embebido (sin videos relacionados de otros canales). */
    fun embedUrl(videoId: String): String =
        "https://www.youtube.com/embed/$videoId?playsinline=1&rel=0&modestbranding=1&enablejsapi=1&origin=$EMBED_ORIGIN"

    /** URL para abrir el video en la app de YouTube o el navegador. */
    fun watchUrl(videoId: String): String = "https://www.youtube.com/watch?v=$videoId"
}
