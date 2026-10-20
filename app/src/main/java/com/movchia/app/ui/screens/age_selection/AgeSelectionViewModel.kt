package com.movchia.app.ui.screens.age_selection

import androidx.lifecycle.ViewModel
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.domain.usecases.SaveAgeGroupUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AgeSelectionUiState(
    val ageInput: String = "",
    val selectedGroup: AgeGroup? = null,
    val error: String? = null,
) {
    val canContinue: Boolean get() = selectedGroup != null
}

/** Pantalla 02_IngresoEdad: el usuario escribe su edad o elige un rango. */
class AgeSelectionViewModel(
    private val saveAgeGroup: SaveAgeGroupUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgeSelectionUiState())
    val uiState: StateFlow<AgeSelectionUiState> = _uiState.asStateFlow()

    fun onAgeInputChanged(value: String) {
        val digits = value.filter(Char::isDigit).take(3)
        val age = digits.toIntOrNull()
        _uiState.update {
            it.copy(
                ageInput = digits,
                selectedGroup = age?.let(AgeGroup::fromAge),
                error = if (age != null && age > 120) "Ingresa una edad válida" else null,
            )
        }
    }

    fun onGroupSelected(group: AgeGroup) {
        _uiState.update { it.copy(selectedGroup = group, ageInput = "", error = null) }
    }

    /** Persiste la selección y devuelve el grupo elegido, o null si no hay selección válida. */
    fun confirm(): AgeGroup? {
        val state = _uiState.value
        if (state.error != null) return null
        val group = state.selectedGroup ?: return null
        saveAgeGroup(group)
        return group
    }
}
