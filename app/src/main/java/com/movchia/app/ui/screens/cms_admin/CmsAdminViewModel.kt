package com.movchia.app.ui.screens.cms_admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.data.model.EducationalContent
import com.movchia.app.data.repository.AuthRepository
import com.movchia.app.data.repository.ContentRepository
import com.movchia.app.domain.usecases.SignInAdminUseCase
import com.movchia.app.ui.UiState
import com.movchia.app.ui.toUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminLoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

/**
 * Pantallas 07_LoginAdmin, 12_AdminDashboard, 13_EditorContenido y
 * 14_ModeracionOpiniones. Base para el CMS de la Secretaría de Movilidad.
 */
class CmsAdminViewModel(
    private val signInAdmin: SignInAdminUseCase,
    private val authRepository: AuthRepository,
    private val contentRepository: ContentRepository,
) : ViewModel() {

    private val _login = MutableStateFlow(AdminLoginUiState())
    val login: StateFlow<AdminLoginUiState> = _login.asStateFlow()

    private val _contents = MutableStateFlow<UiState<List<EducationalContent>>>(UiState.Loading)
    val contents: StateFlow<UiState<List<EducationalContent>>> = _contents.asStateFlow()

    val isSignedIn: Boolean get() = authRepository.isSignedIn
    val adminEmail: String? get() = authRepository.currentEmail

    fun onEmailChanged(value: String) = _login.update { it.copy(email = value, error = null) }
    fun onPasswordChanged(value: String) = _login.update { it.copy(password = value, error = null) }

    fun signIn(onSuccess: () -> Unit) {
        val state = _login.value
        _login.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            signInAdmin(state.email, state.password).fold(
                onSuccess = {
                    _login.update { it.copy(isLoading = false, password = "") }
                    onSuccess()
                },
                onFailure = { e ->
                    _login.update { it.copy(isLoading = false, error = e.message ?: "No fue posible iniciar sesión") }
                },
            )
        }
    }

    fun signOut() = authRepository.signOut()

    /** Carga todo el contenido publicado (todos los grupos) para el dashboard. */
    fun loadContents() {
        _contents.value = UiState.Loading
        viewModelScope.launch {
            val all = AgeGroup.entries.map { contentRepository.getContentFor(it) }
            val failure = all.firstOrNull { it.isFailure }
            _contents.value = if (failure != null) {
                failure.toUiState("No pudimos cargar el contenido del CMS.")
            } else {
                UiState.Success(all.flatMap { it.getOrDefault(emptyList()) }.distinctBy { it.id })
            }
        }
    }

    fun deleteContent(id: String) {
        viewModelScope.launch {
            contentRepository.deleteContent(id)
            loadContents()
        }
    }
}
