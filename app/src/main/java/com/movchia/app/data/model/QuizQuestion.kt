package com.movchia.app.data.model

import kotlinx.serialization.Serializable

/**
 * Pregunta de quiz o del módulo interactivo "Adivina la Señal".
 * Colección Firestore: `quiz_questions`.
 */
@Serializable
data class QuizQuestion(
    val id: String = "",
    val question: String = "",
    /** Imagen de la señal de tránsito asociada (opcional). */
    val imageUrl: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    /** Retroalimentación mostrada al responder. */
    val explanation: String = "",
    val targetGroups: List<AgeGroup> = emptyList(),
    val points: Int = 10,
)
