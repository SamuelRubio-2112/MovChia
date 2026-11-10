package com.movchia.app.ui.screens.kids

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movchia.app.data.local.PointsLocalStore
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

data class KidsHomeData(
    val stories: List<EducationalContent>,
    val cartoons: List<EducationalContent>,
)

/** Pantalla 03_Home_Ninos: cuento del día, dibujos de seguridad y juegos. */
class KidsHomeViewModel(
    private val getContent: GetContentForAgeGroupUseCase,
    pointsStore: PointsLocalStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<KidsHomeData>>(UiState.Loading)
    val uiState: StateFlow<UiState<KidsHomeData>> = _uiState.asStateFlow()

    /** Puntos ganados en "Adivina la Señal"; se muestran en el encabezado. */
    val points: StateFlow<Int> = pointsStore.points(AgeGroup.KIDS)

    init {
        load()
    }

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val stories = getContent(AgeGroup.KIDS, ContentType.STORY)
            val cartoons = getContent(AgeGroup.KIDS, ContentType.CARTOON)
            _uiState.value = stories.mapCatching { s ->
                KidsHomeData(stories = s, cartoons = cartoons.getOrDefault(emptyList()))
            }.toUiState("No pudimos cargar el contenido. Revisa tu conexión.")
        }
    }
}
