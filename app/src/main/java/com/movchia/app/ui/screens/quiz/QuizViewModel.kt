package com.movchia.app.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movchia.app.data.local.PointsLocalStore
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.data.model.QuizQuestion
import com.movchia.app.domain.usecases.GetQuizQuestionsUseCase
import com.movchia.app.ui.UiState
import com.movchia.app.ui.toUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Quiz reutilizable ("Adivina la Señal" en Niños, "Quiz de Tránsito" en
 * Jóvenes y Adultos). Las preguntas se descargan una sola vez por pantalla;
 * "Jugar de nuevo" rebaraja las ya cargadas sin volver a consultar Firestore.
 */
class QuizViewModel(
    private val getQuestions: GetQuizQuestionsUseCase,
    private val pointsStore: PointsLocalStore,
    val group: AgeGroup,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<QuizProgress>>(UiState.Loading)
    val uiState: StateFlow<UiState<QuizProgress>> = _uiState.asStateFlow()

    /** Puntos acumulados históricamente por este grupo (se muestran en resultados). */
    val totalPoints: StateFlow<Int> = pointsStore.points(group)

    private var loadedQuestions: List<QuizQuestion> = emptyList()

    init {
        load()
    }

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            _uiState.value = getQuestions(group)
                .onSuccess { loadedQuestions = it }
                .map { QuizProgress.start(it) }
                .toUiState("No pudimos cargar las preguntas. Revisa tu conexión.")
        }
    }

    fun selectOption(index: Int) = updateProgress { it.answer(index) }

    fun next() = updateProgress { progress ->
        val advanced = progress.next()
        if (advanced.finished && !progress.finished) pointsStore.add(group, advanced.score)
        advanced
    }

    fun restart() {
        _uiState.value = UiState.Success(QuizProgress.start(loadedQuestions))
    }

    private inline fun updateProgress(transform: (QuizProgress) -> QuizProgress) {
        _uiState.update { state ->
            if (state is UiState.Success) UiState.Success(transform(state.data)) else state
        }
    }
}
