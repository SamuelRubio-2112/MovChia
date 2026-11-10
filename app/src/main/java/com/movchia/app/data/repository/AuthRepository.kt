package com.movchia.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.tasks.await

/** Sesión del administrador del CMS (Firebase Auth con correo y contraseña). */
interface AuthRepository {
    val isSignedIn: Boolean
    val currentEmail: String?
    suspend fun signIn(email: String, password: String): Result<Unit>
    fun signOut()

    /**
     * Garantiza una sesión (anónima si no hay otra) y devuelve el uid. Se usa
     * para firmar comentarios en los foros sin pedir datos a la persona.
     */
    suspend fun ensureSession(): Result<String>
}

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
) : AuthRepository {

    override val isSignedIn: Boolean
        get() = auth.currentUser != null

    override val currentEmail: String?
        get() = auth.currentUser?.email

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    override fun signOut() = auth.signOut()

    override suspend fun ensureSession(): Result<String> = runCatching {
        auth.currentUser?.uid ?: auth.signInAnonymously().await().user?.uid
            ?: throw IllegalStateException("No se pudo iniciar la sesión anónima.")
    }.recoverCatching { e ->
        if (e is FirebaseAuthException && e.errorCode == "ERROR_ADMIN_RESTRICTED_OPERATION") {
            throw IllegalStateException(
                "Los comentarios aún no están habilitados (falta activar el acceso anónimo en Firebase).",
            )
        }
        throw e
    }
}
