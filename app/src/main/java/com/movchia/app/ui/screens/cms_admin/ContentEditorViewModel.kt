package com.movchia.app.ui.screens.cms_admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.data.model.ContentType
import com.movchia.app.data.model.EducationalContent
import com.movchia.app.domain.usecases.GetContentByIdUseCase
import com.movchia.app.domain.usecases.SaveContentUseCase
import com.movchia.app.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Campos del formulario del editor. Los numéricos se guardan como texto mientras se escriben. */
data class ContentForm(
    val title: String = "",
    val description: String = "",
    val type: ContentType = ContentType.NEWS,
    val mediaUrl: String = "",
    val thumbnailUrl: String = "",
    val targetGroups: Set<AgeGroup> = setOf(AgeGroup.ADULTS),
    val durationMinutes: String = "",
    val chapter: String = "",
    val published: Boolean = true,
    /** Se conserva al editar para no reordenar el contenido ya publicado. */
    val publishedAt: Long = 0L,
) {
    fun toContent(id: String) = EducationalContent(
        id = id,
        title = title,
        description = description,
        type = type,
        mediaUrl = mediaUrl,
        thumbnailUrl = thumbnailUrl,
        targetGroups = targetGroups.toList(),
        durationMinutes = durationMinutes.trim().toIntOrNull() ?: 0,
        chapter = chapter,
        published = published,
        publishedAt = publishedAt,
    )

    companion object {
        fun from(content: EducationalContent) = ContentForm(
            title = content.title,
            description = content.description,
            type = content.type,
            mediaUrl = content.mediaUrl,
            thumbnailUrl = content.thumbnailUrl,
            targetGroups = content.targetGroups.toSet(),
            durationMinutes = if (content.durationMinutes > 0) content.durationMinutes.toString() else "",
            chapter = content.chapter,
            published = content.published,
            publishedAt = content.publishedAt,
        )
    }
}

data class ContentEditorState(
    val form: ContentForm = ContentForm(),
    val saving: Boolean = false,
    val error: String? = null,
)

/** Pantalla 13_EditorContenido: crear ([contentId] vacío) o editar una pieza. */
class ContentEditorViewModel(
    private val getContentById: GetContentByIdUseCase,
    private val saveContent: SaveContentUseCase,
    val contentId: String,
) : ViewModel() {

    val isNew: Boolean get() = contentId.isBlank()

    private val _uiState = MutableStateFlow<UiState<ContentEditorState>>(UiState.Loading)
    val uiState: StateFlow<UiState<ContentEditorState>> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        if (isNew) {
            _uiState.value = UiState.Success(ContentEditorState())
            return
        }
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            _uiState.value = getContentById(contentId).fold(
                onSuccess = { UiState.Success(ContentEditorState(form = ContentForm.from(it))) },
                onFailure = { UiState.Error(it.message ?: "No pudimos cargar el contenido.") },
            )
        }
    }

    fun update(transform: (ContentForm) -> ContentForm) {
        _uiState.update { s ->
            if (s is UiState.Success) UiState.Success(s.data.copy(form = transform(s.data.form), error = null)) else s
        }
    }

    fun toggleGroup(group: AgeGroup) = update { form ->
        form.copy(targetGroups = if (group in form.targetGroups) form.targetGroups - group else form.targetGroups + group)
    }

    fun save(onSaved: () -> Unit) {
        val state = (_uiState.value as? UiState.Success)?.data ?: return
        val content = state.form.toContent(contentId)
        SaveContentUseCase.validate(content)?.let { message ->
            _uiState.value = UiState.Success(state.copy(error = message))
            return
        }
        _uiState.value = UiState.Success(state.copy(saving = true, error = null))
        viewModelScope.launch {
            saveContent(content).fold(
                onSuccess = { onSaved() },
                onFailure = { e ->
                    _uiState.value = UiState.Success(
                        state.copy(saving = false, error = e.message ?: "No se pudo guardar el contenido."),
                    )
                },
            )
        }
    }
}
