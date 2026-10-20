package com.movchia.app.data.model

import kotlinx.serialization.Serializable

/** Tipo de pieza educativa publicada desde el CMS. */
@Serializable
enum class ContentType {
    VIDEO,      // Video educativo o campaña
    STORY,      // Cuento del día (niños)
    CARTOON,    // Dibujos de seguridad (niños)
    GAME,       // Juego interactivo (niños)
    NEWS,       // Noticia local (adultos)
    CAMPAIGN,   // Campaña del mes (adultos)
    FAQ,        // Preguntas frecuentes (adultos mayores)
}

/**
 * Contenido educativo almacenado en Firestore (colección `contents`).
 * Todos los campos tienen valor por defecto para que Firestore pueda
 * deserializar el documento con el constructor vacío.
 */
@Serializable
data class EducationalContent(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: ContentType = ContentType.VIDEO,
    /** URL del video/imagen en Cloud Storage o en un servicio externo. */
    val mediaUrl: String = "",
    val thumbnailUrl: String = "",
    /** Grupos de edad a los que va dirigido el contenido. */
    val targetGroups: List<AgeGroup> = emptyList(),
    val durationMinutes: Int = 0,
    val chapter: String = "",
    val published: Boolean = true,
    val publishedAt: Long = 0L,
)
