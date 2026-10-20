package com.movchia.app.ui.screens.traffic_code

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movchia.app.data.model.AssistantMessage
import com.movchia.app.data.model.MessageRole
import com.movchia.app.data.model.TrafficCodeArticle
import com.movchia.app.domain.usecases.AskTrafficAssistantUseCase
import com.movchia.app.domain.usecases.GetTrafficCodeArticlesUseCase
import com.movchia.app.ui.UiState
import com.movchia.app.ui.toUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssistantUiState(
    val messages: List<AssistantMessage> = emptyList(),
    val input: String = "",
    val isThinking: Boolean = false,
    val error: String? = null,
)

/**
 * Pantalla 09_ManualTransito + Agente IA. Dos pestañas:
 * - Manual: listado y búsqueda de artículos del Código Nacional de Tránsito.
 * - Asistente: chat con el agente de IA.
 *
 * @param simplified si es true (modo Adultos Mayores) se muestra `simplifiedBody`.
 */
class TrafficCodeViewModel(
    private val getArticles: GetTrafficCodeArticlesUseCase,
    private val askAssistant: AskTrafficAssistantUseCase,
    val simplified: Boolean = false,
) : ViewModel() {

    private val _articles = MutableStateFlow<UiState<List<TrafficCodeArticle>>>(UiState.Loading)
    val articles: StateFlow<UiState<List<TrafficCodeArticle>>> = _articles.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _assistant = MutableStateFlow(AssistantUiState())
    val assistant: StateFlow<AssistantUiState> = _assistant.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadArticles()
    }

    fun loadArticles(query: String = _searchQuery.value) {
        _articles.value = UiState.Loading
        viewModelScope.launch {
            _articles.value = getArticles(query).toUiState("No pudimos cargar el manual.")
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350) // debounce
            loadArticles(query)
        }
    }

    fun onAssistantInputChanged(value: String) {
        _assistant.update { it.copy(input = value, error = null) }
    }

    fun sendQuestion() {
        val question = _assistant.value.input.trim()
        if (question.isEmpty() || _assistant.value.isThinking) return

        val userMessage = AssistantMessage(role = MessageRole.USER, text = question)
        _assistant.update {
            it.copy(messages = it.messages + userMessage, input = "", isThinking = true, error = null)
        }

        viewModelScope.launch {
            askAssistant(question, _assistant.value.messages).fold(
                onSuccess = { answer ->
                    _assistant.update {
                        it.copy(
                            messages = it.messages + AssistantMessage(MessageRole.ASSISTANT, answer),
                            isThinking = false,
                        )
                    }
                },
                onFailure = { e ->
                    _assistant.update {
                        it.copy(isThinking = false, error = e.message ?: "El asistente no está disponible.")
                    }
                },
            )
        }
    }
}
