package com.movchia.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.movchia.app.data.model.StaffRole
import com.movchia.app.data.remote.FirestoreCollections
import kotlinx.coroutines.tasks.await

/**
 * Sesiones de Firebase Auth. Hay dos tipos:
 *  - anónima: la crea [ensureSession] para firmar comentarios en los foros;
 *  - de personal (correo y contraseña): la usa el panel de gestión. El rol se
 *    lee de Firestore (`users/{uid}.role`) con [fetchRole].
 */
interface AuthRepository {
    val isSignedIn: Boolean
    val currentUid: String?
    val currentEmail: String?

    /** `true` si la sesión actual es de correo y contraseña (no anónima). */
    val isStaffSignedIn: Boolean

    suspend fun signIn(email: String, password: String): Result<Unit>
    fun signOut()

    /**
     * Garantiza una sesión (anónima si no hay otra) y devuelve el uid. Se usa
     * para firmar comentarios en los foros sin pedir datos a la persona.
     */
    suspend fun ensureSession(): Result<String>

    /** Rol del usuario actual según `users/{uid}`; `null` si no tiene documento o rol válido. */
    suspend fun fetchRole(): Result<StaffRole?>
}

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
) : AuthRepository {

    override val isSignedIn: Boolean
        get() = auth.currentUser != null

    override val currentUid: String?
        get() = auth.currentUser?.uid

    override val currentEmail: String?
        get() = auth.currentUser?.email

    override val isStaffSignedIn: Boolean
        get() = auth.currentUser?.let { !it.isAnonymous && !it.email.isNullOrBlank() } ?: false

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        Unit
    }.recoverCatching { e -> throw IllegalStateException(friendlySignInMessage(e), e) }

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

    override suspend fun fetchRole(): Result<StaffRole?> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching null
        val doc = db.collection(FirestoreCollections.USERS).document(uid).get().await()
        StaffRole.fromFirestore(doc.getString(FirestoreCollections.FIELD_ROLE))
    }

    private fun friendlySignInMessage(e: Throwable): String = when ((e as? FirebaseAuthException)?.errorCode) {
        "ERROR_INVALID_EMAIL" -> "El correo no tiene un formato válido."
        "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL", "ERROR_INVALID_LOGIN_CREDENTIALS" ->
            "Correo o contraseña incorrectos."
        "ERROR_USER_DISABLED" -> "Esta cuenta está deshabilitada."
        "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Espera unos minutos e inténtalo de nuevo."
        "ERROR_NETWORK_REQUEST_FAILED" -> "Sin conexión. Revisa tu red e inténtalo de nuevo."
        else -> e.message ?: "No fue posible iniciar sesión."
    }
}
