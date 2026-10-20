package com.movchia.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/** Sesión del administrador del CMS (Firebase Auth con correo y contraseña). */
interface AuthRepository {
    val isSignedIn: Boolean
    val currentEmail: String?
    suspend fun signIn(email: String, password: String): Result<Unit>
    fun signOut()
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
}
