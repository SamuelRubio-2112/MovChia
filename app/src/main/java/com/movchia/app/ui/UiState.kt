package com.movchia.app.ui

/** Estado genérico de carga usado por todos los ViewModels. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

/** Convierte un [Result] en [UiState] con un mensaje amigable en caso de error. */
fun <T> Result<T>.toUiState(fallbackMessage: String = "Ocurrió un error. Intenta de nuevo."): UiState<T> =
    fold(
        onSuccess = { UiState.Success(it) },
        onFailure = { UiState.Error(it.message?.takeIf(String::isNotBlank) ?: fallbackMessage) },
    )
