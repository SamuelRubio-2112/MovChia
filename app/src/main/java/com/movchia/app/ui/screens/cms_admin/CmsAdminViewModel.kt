package com.movchia.app.ui.screens.cms_admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movchia.app.data.model.ContentType
import com.movchia.app.data.model.EducationalContent
import com.movchia.app.data.repository.AuthRepository
import com.movchia.app.data.session.StaffSession
import com.movchia.app.data.session.StaffSessionStore
import com.movchia.app.domain.usecases.DeleteContentUseCase
import com.movchia.app.domain.usecases.GetAllContentsUseCase
import com.movchia.app.domain.usecases.SignInAdminUseCase
import com.movchia.app.domain.usecases.SignOutAdminUseCase
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
    /** Correo de la sesión que Firebase recuerda y se está verificando. */
    val restoringEmail: String? = null,
)

/** Pantalla 07_LoginAdmin: correo + contraseña y verificación del rol. */
class AdminLoginViewModel(
    private val signInAdmin: SignInAdminUseCase,
    private val authRepository: AuthRepository,
    val demoMode: StateFlow<Boolean>,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminLoginUiState())
    val uiState: StateFlow<AdminLoginUiState> = _uiState.asStateFlow()

    private var restoreAttempted = false

    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value, error = null) }

    /** Reanuda la sesión de personal recordada por Firebase, si existe. Se llama una sola vez. */
    fun restoreSession(onSuccess: () -> Unit) {
        if (restoreAttempted) return
        restoreAttempted = true
        if (!authRepository.isStaffSignedIn) return
        _uiState.update { it.copy(isLoading = true, restoringEmail = authRepository.currentEmail) }
        viewModelScope.launch {
            signInAdmin.restore().fold(
                onSuccess = { session ->
                    _uiState.update { it.copy(isLoading = false, restoringEmail = null) }
                    if (session != null) onSuccess()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, restoringEmail = null, error = e.message) }
                },
            )
        }
    }

    fun signIn(onSuccess: () -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            signInAdmin(state.email, state.password).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, password = "") }
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "No fue posible iniciar sesión") }
                },
            )
        }
    }
}

/** Pantalla 12_AdminDashboard: listado de todo el contenido (publicado y borradores). */
class AdminDashboardViewModel(
    private val getAllContents: GetAllContentsUseCase,
    private val deleteContent: DeleteContentUseCase,
    private val signOutAdmin: SignOutAdminUseCase,
    sessions: StaffSessionStore,
) : ViewModel() {

    val session: StateFlow<StaffSession?> = sessions.session

    private val _contents = MutableStateFlow<UiState<List<EducationalContent>>>(UiState.Loading)
    val contents: StateFlow<UiState<List<EducationalContent>>> = _contents.asStateFlow()

    private val _filter = MutableStateFlow<ContentType?>(null)
    val filter: StateFlow<ContentType?> = _filter.asStateFlow()

    /** Mensaje puntual para un Snackbar (se consume con [consumeMessage]). */
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun load() {
        _contents.value = UiState.Loading
        viewModelScope.launch {
            _contents.value = getAllContents().toUiState("No pudimos cargar el contenido del panel.")
        }
    }

    fun setFilter(type: ContentType?) {
        _filter.value = type
    }

    fun delete(content: EducationalContent) {
        viewModelScope.launch {
            deleteContent(content.id).fold(
                onSuccess = {
                    _contents.update { s ->
                        if (s is UiState.Success) UiState.Success(s.data.filterNot { it.id == content.id }) else s
                    }
                    _message.value = "\"${content.title}\" eliminado."
                },
                onFailure = { e -> _message.value = e.message ?: "No se pudo eliminar el contenido." },
            )
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun signOut() = signOutAdmin()
}
