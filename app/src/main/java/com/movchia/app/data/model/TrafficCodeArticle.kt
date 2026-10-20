package com.movchia.app.data.model

import kotlinx.serialization.Serializable

/**
 * Artículo del Manual de Tránsito basado en el Código Nacional de Tránsito
 * (Ley 769 de 2002 y normas que la modifican).
 * Colección Firestore: `traffic_code`.
 */
@Serializable
data class TrafficCodeArticle(
    val id: String = "",
    val chapter: String = "",
    val articleNumber: String = "",
    val title: String = "",
    /** Texto normativo completo. */
    val body: String = "",
    /** Versión simplificada en lenguaje claro (modo Adultos Mayores / Niños). */
    val simplifiedBody: String = "",
    val tags: List<String> = emptyList(),
    val order: Int = 0,
)

/** Rol de un mensaje dentro del chat con el Asistente de IA. */
@Serializable
enum class MessageRole { USER, ASSISTANT }

/** Mensaje del chat con el asistente del Código Nacional de Tránsito. */
@Serializable
data class AssistantMessage(
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
)
