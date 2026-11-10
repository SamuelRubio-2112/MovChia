package com.movchia.app

import com.movchia.app.utils.YouTubeUrls
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class YouTubeUrlsTest {

    @Test
    fun extractsIdFromCommonFormats() {
        val expected = "dQw4w9WgXcQ"
        listOf(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtube.com/watch?v=dQw4w9WgXcQ&t=30s",
            "https://www.youtube.com/watch?feature=share&v=dQw4w9WgXcQ",
            "https://m.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ?si=abc",
            "https://www.youtube.com/embed/dQw4w9WgXcQ",
            "https://www.youtube.com/shorts/dQw4w9WgXcQ",
            "https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ",
            "  https://www.youtube.com/watch?v=dQw4w9WgXcQ  ",
        ).forEach { url ->
            assertEquals("No se reconoció: $url", expected, YouTubeUrls.extractVideoId(url))
        }
    }

    @Test
    fun rejectsNonYouTubeUrls() {
        assertNull(YouTubeUrls.extractVideoId(""))
        assertNull(YouTubeUrls.extractVideoId("https://firebasestorage.googleapis.com/v0/b/x/o/video.mp4"))
        assertNull(YouTubeUrls.extractVideoId("https://vimeo.com/123456"))
        assertFalse(YouTubeUrls.isYouTubeUrl("https://example.com/youtube.com/watch?v=short"))
    }

    @Test
    fun buildsEmbedAndWatchUrls() {
        assertEquals("https://www.youtube.com/watch?v=abc", YouTubeUrls.watchUrl("abc"))
        assertEquals(true, YouTubeUrls.embedUrl("abc").startsWith("https://www.youtube.com/embed/abc?"))
    }
}
