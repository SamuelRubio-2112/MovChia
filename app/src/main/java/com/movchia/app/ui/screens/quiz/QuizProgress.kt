package com.movchia.app.ui.screens.quiz

import com.movchia.app.data.model.QuizQuestion
import kotlin.random.Random

/**
 * Estado inmutable de una ronda de quiz. Toda la lógica de puntaje vive aquí
 * (sin Android ni corrutinas) para poder probarla con JUnit.
 *
 * Flujo: [answer] marca la opción elegida y suma puntos si acierta;
 * [next] pasa a la siguiente pregunta. Cuando [currentIndex] supera la última
 * pregunta, [finished] es `true` y se muestran los resultados.
 */
data class QuizProgress(
    val questions: List<QuizQuestion>,
    val currentIndex: Int = 0,
    val selectedOption: Int? = null,
    val score: Int = 0,
    val correctAnswers: Int = 0,
) {
    val finished: Boolean get() = currentIndex >= questions.size
    val current: QuizQuestion? get() = questions.getOrNull(currentIndex)
    val answered: Boolean get() = selectedOption != null
    val isLastQuestion: Boolean get() = currentIndex == questions.lastIndex

    /** Puntos máximos posibles en esta ronda. */
    val maxScore: Int get() = questions.sumOf { it.points }

    /** `true` si la opción elegida en la pregunta actual es la correcta; `null` si aún no responde. */
    val lastAnswerCorrect: Boolean? get() = selectedOption?.let { it == current?.correctIndex }

    /** Fracción de aciertos sobre el total de preguntas (0..1). */
    val accuracy: Float get() = if (questions.isEmpty()) 0f else correctAnswers.toFloat() / questions.size

    fun answer(optionIndex: Int): QuizProgress {
        val question = current ?: return this
        if (answered || optionIndex !in question.options.indices) return this
        val correct = optionIndex == question.correctIndex
        return copy(
            selectedOption = optionIndex,
            score = score + if (correct) question.points else 0,
            correctAnswers = correctAnswers + if (correct) 1 else 0,
        )
    }

    fun next(): QuizProgress =
        if (!answered || finished) this else copy(currentIndex = currentIndex + 1, selectedOption = null)

    companion object {
        /** Máximo de preguntas por ronda para que el quiz no se haga eterno. */
        const val DEFAULT_ROUND_SIZE = 10

        /**
         * Arma una ronda: descarta preguntas mal formadas en el CMS (menos de dos
         * opciones o índice correcto fuera de rango), baraja y recorta a [roundSize].
         */
        fun start(
            all: List<QuizQuestion>,
            roundSize: Int = DEFAULT_ROUND_SIZE,
            random: Random = Random.Default,
        ): QuizProgress = QuizProgress(
            questions = all
                .filter { it.options.size >= 2 && it.correctIndex in it.options.indices }
                .shuffled(random)
                .take(roundSize),
        )
    }
}
