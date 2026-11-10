package com.movchia.app

import com.movchia.app.data.model.AgeGroup
import com.movchia.app.data.model.QuizQuestion
import com.movchia.app.ui.screens.quiz.QuizProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuizProgressTest {

    private fun question(id: String, correct: Int = 1, points: Int = 10, options: Int = 4) = QuizQuestion(
        id = id,
        question = "Pregunta $id",
        options = List(options) { "Opción $it" },
        correctIndex = correct,
        explanation = "Explicación $id",
        targetGroups = listOf(AgeGroup.KIDS, AgeGroup.ADULTS),
        points = points,
    )

    @Test
    fun `start descarta preguntas mal formadas y respeta el tamaño de ronda`() {
        val all = listOf(
            question("ok-1"),
            question("sin-opciones", options = 1),
            question("indice-fuera", correct = 7),
            question("ok-2"),
            question("ok-3"),
        )
        val progress = QuizProgress.start(all, roundSize = 2, random = Random(1))

        assertEquals(2, progress.questions.size)
        assertTrue(progress.questions.all { it.id.startsWith("ok-") })
        assertEquals(0, progress.currentIndex)
        assertFalse(progress.finished)
    }

    @Test
    fun `responder bien suma los puntos de la pregunta y cuenta el acierto`() {
        val progress = QuizProgress(listOf(question("a", correct = 2, points = 15)))
        val answered = progress.answer(2)

        assertEquals(15, answered.score)
        assertEquals(1, answered.correctAnswers)
        assertEquals(true, answered.lastAnswerCorrect)
        assertTrue(answered.answered)
    }

    @Test
    fun `responder mal no suma puntos y marca la respuesta como incorrecta`() {
        val progress = QuizProgress(listOf(question("a", correct = 2)))
        val answered = progress.answer(0)

        assertEquals(0, answered.score)
        assertEquals(0, answered.correctAnswers)
        assertEquals(false, answered.lastAnswerCorrect)
    }

    @Test
    fun `no se puede cambiar la respuesta ni elegir una opción inexistente`() {
        val progress = QuizProgress(listOf(question("a", correct = 1))).answer(1)

        assertSame(progress, progress.answer(0))
        assertSame(progress, progress.answer(9))
        assertEquals(10, progress.score)
    }

    @Test
    fun `next exige haber respondido y termina tras la última pregunta`() {
        val start = QuizProgress(listOf(question("a"), question("b")))

        assertSame(start, start.next())

        val afterFirst = start.answer(1).next()
        assertEquals(1, afterFirst.currentIndex)
        assertNull(afterFirst.selectedOption)
        assertTrue(afterFirst.isLastQuestion)

        val finished = afterFirst.answer(0).next()
        assertTrue(finished.finished)
        assertEquals(20 - 10, finished.score)
        assertEquals(0.5f, finished.accuracy)
        assertEquals(20, finished.maxScore)
        assertSame(finished, finished.next())
    }

    @Test
    fun `una lista vacía queda terminada de inmediato sin dividir por cero`() {
        val empty = QuizProgress.start(emptyList())

        assertTrue(empty.finished)
        assertEquals(0f, empty.accuracy)
        assertNull(empty.current)
    }
}
