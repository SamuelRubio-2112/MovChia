package com.movchia.app.ui.screens.cms_admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movchia.app.data.model.Forum
import com.movchia.app.data.model.ForumPost
import com.movchia.app.domain.usecases.DeleteForumPostUseCase
import com.movchia.app.domain.usecases.GetAllForumsUseCase
import com.movchia.app.domain.usecases.GetForumPostsForModerationUseCase
import com.movchia.app.domain.usecases.SaveForumUseCase
import com.movchia.app.domain.usecases.SetPostHiddenUseCase
import com.movchia.app.ui.UiState
import com.movchia.app.ui.toUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Pestaña "Foros" del panel: lista completa, crear, editar, abrir/cerrar. */
class AdminForumsViewModel(
    private val getAllForums: GetAllForumsUseCase,
    private val saveForum: SaveForumUseCase,
) : ViewModel() {

    private val _forums = MutableStateFlow<UiState<List<Forum>>>(UiState.Loading)
    val forums: StateFlow<UiState<List<Forum>>> = _forums.asStateFlow()

    /** Foro en edición en el diálogo (`Forum()` vacío = crear). `null` = diálogo cerrado. */
    private val _editing = MutableStateFlow<Forum?>(null)
    val editing: StateFlow<Forum?> = _editing.asStateFlow()

    private val _editError = MutableStateFlow<String?>(null)
    val editError: StateFlow<String?> = _editError.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun load() {
        _forums.value = UiState.Loading
        viewModelScope.launch {
            _forums.value = getAllForums().toUiState("No pudimos cargar los foros.")
        }
    }

    fun startCreate() {
        _editError.value = null
        _editing.value = Forum()
    }

    fun startEdit(forum: Forum) {
        _editError.value = null
        _editing.value = forum
    }

    fun cancelEdit() {
        _editing.value = null
        _editError.value = null
    }

    fun updateEditing(transform: (Forum) -> Forum) {
        _editing.update { it?.let(transform) }
        _editError.value = null
    }

    fun saveEditing() {
        val forum = _editing.value ?: return
        SaveForumUseCase.validate(forum.copy(title = forum.title.trim()))?.let { _editError.value = it; return }
        _saving.value = true
        viewModelScope.launch {
            saveForum(forum).fold(
                onSuccess = { saved ->
                    replace(saved)
                    _editing.value = null
                    _message.value = if (forum.id.isBlank()) "Foro creado." else "Foro actualizado."
                },
                onFailure = { e -> _editError.value = e.message ?: "No se pudo guardar el foro." },
            )
            _saving.value = false
        }
    }

    /** Abre o cierra un foro sin pasar por el diálogo. */
    fun setOpen(forum: Forum, open: Boolean) {
        viewModelScope.launch {
            saveForum(forum.copy(open = open)).fold(
                onSuccess = { saved ->
                    replace(saved)
                    _message.value = if (open) "Foro abierto: vuelve a verse en la app." else "Foro cerrado: ya no se ve en la app."
                },
                onFailure = { e -> _message.value = e.message ?: "No se pudo actualizar el foro." },
            )
        }
    }

    private fun replace(saved: Forum) {
        _forums.update { s ->
            if (s !is UiState.Success) return@update s
            val list = s.data.toMutableList()
            val i = list.indexOfFirst { it.id == saved.id }
            if (i >= 0) list[i] = saved else list.add(0, saved)
            UiState.Success(list.sortedByDescending { it.createdAt })
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}

/** Moderación posterior de los comentarios de un foro (ocultar, mostrar, eliminar). */
class ForumPostsModerationViewModel(
    private val getPosts: GetForumPostsForModerationUseCase,
    private val setPostHidden: SetPostHiddenUseCase,
    private val deletePost: DeleteForumPostUseCase,
    val forumId: String,
) : ViewModel() {

    private val _posts = MutableStateFlow<UiState<List<ForumPost>>>(UiState.Loading)
    val posts: StateFlow<UiState<List<ForumPost>>> = _posts.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        load()
    }

    fun load() {
        _posts.value = UiState.Loading
        viewModelScope.launch {
            _posts.value = getPosts(forumId).toUiState("No pudimos cargar los comentarios.")
        }
    }

    fun toggleHidden(post: ForumPost) {
        val hidden = !post.hidden
        viewModelScope.launch {
            setPostHidden(forumId, post.id, hidden).fold(
                onSuccess = {
                    _posts.update { s ->
                        if (s is UiState.Success) UiState.Success(s.data.map { if (it.id == post.id) it.copy(hidden = hidden) else it })
                        else s
                    }
                    _message.value = if (hidden) "Comentario oculto para la ciudadanía." else "Comentario visible de nuevo."
                },
                onFailure = { e -> _message.value = e.message ?: "No se pudo actualizar el comentario." },
            )
        }
    }

    fun delete(post: ForumPost) {
        viewModelScope.launch {
            deletePost(forumId, post.id).fold(
                onSuccess = {
                    _posts.update { s ->
                        if (s is UiState.Success) UiState.Success(s.data.filterNot { it.id == post.id }) else s
                    }
                    _message.value = "Comentario eliminado."
                },
                onFailure = { e -> _message.value = e.message ?: "No se pudo eliminar el comentario." },
            )
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
