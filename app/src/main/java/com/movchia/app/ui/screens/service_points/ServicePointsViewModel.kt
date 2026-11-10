package com.movchia.app.ui.screens.service_points

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movchia.app.data.model.ServicePoint
import com.movchia.app.domain.usecases.GetServicePointByIdUseCase
import com.movchia.app.domain.usecases.GetServicePointsUseCase
import com.movchia.app.ui.UiState
import com.movchia.app.ui.toUiState
import com.movchia.app.utils.TextToSpeechHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Listado "Sedes y Puntos" (adultos) / "Puntos de atención" (mayores). */
class ServicePointsViewModel(
    private val getServicePoints: GetServicePointsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<ServicePoint>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<ServicePoint>>> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            _uiState.value = getServicePoints().toUiState("No pudimos cargar las sedes. Revisa tu conexión.")
        }
    }
}

/** Detalle de una sede con lectura por voz para Adultos Mayores. */
class ServicePointDetailViewModel(
    private val getServicePointById: GetServicePointByIdUseCase,
    private val tts: TextToSpeechHelper,
    private val id: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ServicePoint>>(UiState.Loading)
    val uiState: StateFlow<UiState<ServicePoint>> = _uiState.asStateFlow()
    val ttsProblem = tts.problem

    init {
        load()
    }

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            _uiState.value = getServicePointById(id).toUiState("No pudimos cargar esta sede.")
        }
    }

    fun readAloud() {
        val point = (_uiState.value as? UiState.Success)?.data ?: return
        tts.speak(
            buildString {
                append(point.name).append(". ")
                if (point.address.isNotBlank()) append("Dirección: ").append(point.address).append(". ")
                if (point.schedule.isNotBlank()) append("Horario: ").append(point.schedule).append(". ")
                if (point.phone.isNotBlank()) append("Teléfono: ").append(point.phone).append(". ")
                append("Toque el botón Cómo llegar para abrir el mapa.")
            },
        )
    }

    override fun onCleared() {
        tts.stop()
        super.onCleared()
    }
}
