package com.movchia.app.ui.screens.adults

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

data class AdultsHomeData(
    val news: List<EducationalContent>,
    val campaign: EducationalContent?,
)

/** Pantalla 04_Home_JovenesAdultos: noticias, trámites y campaña del mes. */
class AdultsHomeViewModel(
    private val getContent: GetContentForAgeGroupUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<AdultsHomeData>>(UiState.Loading)
    val uiState: StateFlow<UiState<AdultsHomeData>> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val news = getContent(AgeGroup.ADULTS, ContentType.NEWS)
            val campaigns = getContent(AgeGroup.ADULTS, ContentType.CAMPAIGN)
            _uiState.value = news.mapCatching { n ->
                AdultsHomeData(news = n, campaign = campaigns.getOrNull()?.firstOrNull())
            }.toUiState("No pudimos cargar las noticias. Revisa tu conexión.")
        }
    }
}
