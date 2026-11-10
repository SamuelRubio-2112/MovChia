package com.movchia.app.ui.screens.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.data.model.ContentType
import com.movchia.app.data.model.EducationalContent
import com.movchia.app.domain.usecases.GetContentForAgeGroupUseCase
import com.movchia.app.ui.UiState
import com.movchia.app.ui.toUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Listado de contenido de un [ContentType] para el grupo de edad activo
 * (Noticias, Videos explicativos, Cuentos, Preguntas frecuentes, etc.).
 */
class ContentListViewModel(
    private val getContent: GetContentForAgeGroupUseCase,
    val group: AgeGroup,
    val type: ContentType,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<EducationalContent>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<EducationalContent>>> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            _uiState.value = getContent(group, type)
                .toUiState("No pudimos cargar el contenido. Revisa tu conexión.")
        }
    }
}
