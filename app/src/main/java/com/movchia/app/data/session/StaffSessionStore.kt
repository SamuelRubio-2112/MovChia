package com.movchia.app.data.session

import com.movchia.app.data.model.StaffRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Sesión verificada de un miembro del personal (correo + rol). */
data class StaffSession(
    val uid: String,
    val email: String,
    val role: StaffRole,
)

/**
 * Sesión del panel de gestión en memoria. Firebase Auth recuerda al usuario
 * entre arranques, pero el rol se vuelve a leer de Firestore en cada inicio
 * (ver `SignInAdminUseCase.restore`), así que aquí no se persiste nada.
 */
class StaffSessionStore {
    private val _session = MutableStateFlow<StaffSession?>(null)
    val session: StateFlow<StaffSession?> = _session.asStateFlow()

    val current: StaffSession? get() = _session.value

    fun set(session: StaffSession) {
        _session.value = session
    }

    fun clear() {
        _session.value = null
    }
}
