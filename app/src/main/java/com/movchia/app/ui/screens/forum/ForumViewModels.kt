package com.movchia.app.ui.screens.forum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movchia.app.data.local.UserProfileLocalStore
import com.movchia.app.data.model.Forum
import com.movchia.app.data.model.ForumPost
import com.movchia.app.domain.usecases.GetForumPostsUseCase
import com.movchia.app.domain.usecases.GetForumsUseCase
import com.movchia.app.domain.usecases.PublishForumPostUseCase
import com.movchia.app.ui.UiState
import com.movchia.app.ui.toUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Lista de foros abiertos ("Opiniones"). */
class ForumsViewModel(
    private val getForums: GetForumsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Forum>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Forum>>> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            _uiState.value = getForums().toUiState("No pudimos cargar los foros. Revisa tu conexión.")
        }
    }
}

/** Estado del detalle: comentarios + redacción. */
data class ForumDetailState(
    val posts: List<ForumPost> = emptyList(),
    val draft: String = "",
    val sending: Boolean = false,
    /** Mensaje de error del envío (no de la carga). */
    val sendError: String? = null,
)

class ForumDetailViewModel(
    private val getPosts: GetForumPostsUseCase,
    private val publishPost: PublishForumPostUseCase,
    private val profile: UserProfileLocalStore,
    val forumId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ForumDetailState>>(UiState.Loading)
    val uiState: StateFlow<UiState<ForumDetailState>> = _uiState.asStateFlow()

    val nickname: StateFlow<String> = profile.nickname

    init {
        load()
    }

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            _uiState.value = getPosts(forumId)
                .map { ForumDetailState(posts = it) }
                .toUiState("No pudimos cargar los comentarios. Revisa tu conexión.")
        }
    }

    fun onDraftChange(value: String) = updateState { it.copy(draft = value.take(MAX_TEXT), sendError = null) }

    fun saveNickname(value: String) = profile.saveNickname(value)

    fun send() {
        val state = (_uiState.value as? UiState.Success)?.data ?: return
        val text = state.draft.trim()
        if (text.length < MIN_TEXT) {
            updateState { it.copy(sendError = "Escribe al menos $MIN_TEXT caracteres.") }
            return
        }
        if (nickname.value.isBlank()) {
            updateState { it.copy(sendError = "Elige un apodo para firmar tu comentario.") }
            return
        }
        updateState { it.copy(sending = true, sendError = null) }
        viewModelScope.launch {
            publishPost(forumId = forumId, nickname = nickname.value, text = text)
                .onSuccess { post ->
                    // Moderación posterior: el comentario aparece de inmediato.
                    updateState { it.copy(posts = listOf(post) + it.posts, draft = "", sending = false) }
                }
                .onFailure { e ->
                    updateState {
                        it.copy(sending = false, sendError = e.message ?: "No pudimos publicar tu comentario.")
                    }
                }
        }
    }

    private inline fun updateState(transform: (ForumDetailState) -> ForumDetailState) {
        _uiState.update { s -> if (s is UiState.Success) UiState.Success(transform(s.data)) else s }
    }

    companion object {
        const val MIN_TEXT = 5
        const val MAX_TEXT = 500
    }
}
