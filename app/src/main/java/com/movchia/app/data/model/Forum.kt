package com.movchia.app.data.model

import kotlinx.serialization.Serializable

/**
 * Foro de opinión ciudadana (sección "Opiniones" de Jóvenes y Adultos).
 * Colección Firestore: `forums`. Los temas los crea el gestor desde el CMS.
 */
@Serializable
data class Forum(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    /** Emoji o texto corto que acompaña al título en la lista. */
    val emoji: String = "💬",
    val open: Boolean = true,
    val createdAt: Long = 0L,
    val postCount: Int = 0,
)

/**
 * Comentario dentro de un foro. Subcolección `forums/{forumId}/posts`.
 * Moderación POSTERIOR: se publica de inmediato (`hidden = false`) y el
 * gestor puede ocultarlo o borrarlo después.
 */
@Serializable
data class ForumPost(
    val id: String = "",
    val forumId: String = "",
    /** uid de Firebase Auth (sesión anónima) del autor. */
    val authorId: String = "",
    val nickname: String = "",
    val text: String = "",
    val createdAt: Long = 0L,
    val hidden: Boolean = false,
)
