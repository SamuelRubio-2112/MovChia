package com.movchia.app.ui.screens.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movchia.app.data.model.EducationalContent
import com.movchia.app.domain.usecases.GetContentByIdUseCase
import com.movchia.app.ui.UiState
import com.movchia.app.ui.toUiState
import com.movchia.app.utils.TextToSpeechHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Visor de una pieza de contenido. Carga el documento por id y, en el modo
 * Adultos Mayores, permite escuchar el título y el texto por voz.
 */
class ContentDetailViewModel(
    private val getContentById: GetContentByIdUseCase,
    private val tts: TextToSpeechHelper,
    private val contentId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<EducationalContent>>(UiState.Loading)
    val uiState: StateFlow<UiState<EducationalContent>> = _uiState.asStateFlow()

    /** Aviso cuando la lectura por voz no es posible; `null` si todo va bien. */
    val ttsProblem = tts.problem

    init {
        load()
    }

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            _uiState.value = getContentById(contentId).toUiState("No pudimos abrir este contenido.")
        }
    }

    fun readAloud() {
        val content = (_uiState.value as? UiState.Success)?.data ?: return
        tts.speak(listOf(content.title, content.description).filter { it.isNotBlank() }.joinToString(". "))
    }

    fun stopReading() = tts.stop()

    override fun onCleared() {
        tts.stop()
        super.onCleared()
    }
}
