package com.movchia.app.domain.usecases

import com.movchia.app.data.model.ContentType
import com.movchia.app.data.model.EducationalContent
import com.movchia.app.data.model.Forum
import com.movchia.app.data.model.ForumPost
import com.movchia.app.data.model.StaffRole
import com.movchia.app.data.repository.AuthRepository
import com.movchia.app.data.repository.ContentRepository
import com.movchia.app.data.repository.ForumRepository
import com.movchia.app.data.session.StaffSession
import com.movchia.app.data.session.StaffSessionStore

/**
 * Casos de uso del panel de gestión (CMS). Todos requieren una sesión de
 * personal verificada ([StaffSessionStore]); las reglas de Firestore hacen la
 * misma comprobación del lado del servidor.
 */

/** Inicia sesión con correo y contraseña y verifica que la cuenta tenga rol de personal. */
class SignInAdminUseCase(
    private val auth: AuthRepository,
    private val sessions: StaffSessionStore,
    /** En modo demo devuelve un rol sin tocar Firebase; en producción, `null`. */
    private val demoRole: () -> StaffRole? = { null },
) {
    suspend operator fun invoke(email: String, password: String): Result<StaffSession> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Correo y contraseña son obligatorios"))
        }
        demoRole()?.let { role ->
            return Result.success(StaffSession(uid = "demo-staff", email = email.trim(), role = role).also(sessions::set))
        }
        auth.signIn(email, password).getOrElse { return Result.failure(it) }
        return verifyRole()
    }

    /**
     * Reanuda la sesión que Firebase Auth recuerda entre arranques. Devuelve
     * `null` si no hay sesión de personal.
     */
    suspend fun restore(): Result<StaffSession?> {
        sessions.current?.let { return Result.success(it) }
        demoRole()?.let { return Result.success(null) }
        if (!auth.isStaffSignedIn) return Result.success(null)
        return verifyRole()
    }

    private suspend fun verifyRole(): Result<StaffSession> {
        val role = auth.fetchRole().getOrElse { return Result.failure(it) }
        val uid = auth.currentUid
        val email = auth.currentEmail
        if (role == null || uid == null || email == null) {
            auth.signOut()
            return Result.failure(IllegalStateException(NO_ROLE_MESSAGE))
        }
        return Result.success(StaffSession(uid, email, role).also(sessions::set))
    }

    companion object {
        const val NO_ROLE_MESSAGE = "Esta cuenta no tiene permisos en el panel de gestión. Pide a un administrador que te asigne un rol."
    }
}

class SignOutAdminUseCase(
    private val auth: AuthRepository,
    private val sessions: StaffSessionStore,
) {
    operator fun invoke() {
        sessions.clear()
        if (auth.isStaffSignedIn) auth.signOut()
    }
}

/** Todo el contenido, publicado o no, del más reciente al más antiguo. */
class GetAllContentsUseCase(private val repository: ContentRepository) {
    suspend operator fun invoke(): Result<List<EducationalContent>> = repository.getAllContents()
}

/** Crea o actualiza una pieza de contenido tras validarla. */
class SaveContentUseCase(
    private val repository: ContentRepository,
    private val now: () -> Long = System::currentTimeMillis,
) {
    suspend operator fun invoke(content: EducationalContent): Result<EducationalContent> {
        val clean = normalize(content)
        validate(clean)?.let { return Result.failure(IllegalArgumentException(it)) }
        return repository.upsertContent(clean).map { clean }
    }

    private fun normalize(content: EducationalContent) = content.copy(
        title = content.title.trim(),
        description = content.description.trim(),
        mediaUrl = content.mediaUrl.trim(),
        thumbnailUrl = content.thumbnailUrl.trim(),
        chapter = content.chapter.trim(),
        targetGroups = content.targetGroups.distinct(),
        durationMinutes = content.durationMinutes.coerceAtLeast(0),
        publishedAt = if (content.publishedAt > 0L) content.publishedAt else now(),
    )

    companion object {
        const val MIN_TITLE = 3
        const val MAX_TITLE = 120
        const val MAX_DESCRIPTION = 5000

        private val mediaRequired = setOf(ContentType.VIDEO, ContentType.CARTOON, ContentType.GAME)

        /** Devuelve el mensaje de error o `null` si el contenido es válido. */
        fun validate(content: EducationalContent): String? = when {
            content.title.trim().length < MIN_TITLE -> "El título debe tener al menos $MIN_TITLE caracteres."
            content.title.trim().length > MAX_TITLE -> "El título no puede superar $MAX_TITLE caracteres."
            content.description.length > MAX_DESCRIPTION -> "La descripción no puede superar $MAX_DESCRIPTION caracteres."
            content.targetGroups.isEmpty() -> "Elige al menos un grupo de edad."
            content.type in mediaRequired && content.mediaUrl.isBlank() ->
                "Este tipo de contenido necesita el enlace del video o archivo."
            content.description.isBlank() && content.mediaUrl.isBlank() ->
                "Escribe una descripción o agrega un enlace de video o imagen."
            !content.mediaUrl.isValidUrlOrBlank() -> "El enlace del video o imagen no es válido."
            !content.thumbnailUrl.isValidUrlOrBlank() -> "El enlace de la miniatura no es válido."
            content.durationMinutes < 0 -> "La duración no puede ser negativa."
            else -> null
        }

        private fun String.isValidUrlOrBlank(): Boolean {
            val url = trim()
            if (url.isEmpty()) return true
            return url.startsWith("https://") || url.startsWith("http://") || url.startsWith("file:///android_asset/")
        }
    }
}

class DeleteContentUseCase(private val repository: ContentRepository) {
    suspend operator fun invoke(id: String): Result<Unit> {
        if (id.isBlank()) return Result.failure(IllegalArgumentException("Contenido no especificado"))
        return repository.deleteContent(id)
    }
}

/** Todos los foros (abiertos y cerrados) para el panel. */
class GetAllForumsUseCase(private val repository: ForumRepository) {
    suspend operator fun invoke(): Result<List<Forum>> = repository.getAllForums()
}

/** Crea o edita un foro. Un foro cerrado deja de verse y de recibir comentarios. */
class SaveForumUseCase(
    private val repository: ForumRepository,
    private val now: () -> Long = System::currentTimeMillis,
) {
    suspend operator fun invoke(forum: Forum): Result<Forum> {
        val clean = forum.copy(
            title = forum.title.trim(),
            description = forum.description.trim(),
            emoji = forum.emoji.trim().ifBlank { DEFAULT_EMOJI },
            createdAt = if (forum.createdAt > 0L) forum.createdAt else now(),
        )
        validate(clean)?.let { return Result.failure(IllegalArgumentException(it)) }
        return repository.upsertForum(clean)
    }

    companion object {
        const val MIN_TITLE = 3
        const val MAX_TITLE = 60
        const val MAX_DESCRIPTION = 200
        const val MAX_EMOJI = 4
        const val DEFAULT_EMOJI = "💬"

        fun validate(forum: Forum): String? = when {
            forum.title.trim().length < MIN_TITLE -> "El título debe tener al menos $MIN_TITLE caracteres."
            forum.title.trim().length > MAX_TITLE -> "El título no puede superar $MAX_TITLE caracteres."
            forum.description.length > MAX_DESCRIPTION -> "La descripción no puede superar $MAX_DESCRIPTION caracteres."
            forum.emoji.length > MAX_EMOJI -> "El emoji es demasiado largo."
            else -> null
        }
    }
}

/** Comentarios de un foro incluyendo los ocultos (solo personal). */
class GetForumPostsForModerationUseCase(private val repository: ForumRepository) {
    suspend operator fun invoke(forumId: String): Result<List<ForumPost>> {
        if (forumId.isBlank()) return Result.failure(IllegalArgumentException("Foro no especificado"))
        return repository.getAllPosts(forumId)
    }
}

class SetPostHiddenUseCase(private val repository: ForumRepository) {
    suspend operator fun invoke(forumId: String, postId: String, hidden: Boolean): Result<Unit> {
        if (forumId.isBlank() || postId.isBlank()) return Result.failure(IllegalArgumentException("Comentario no especificado"))
        return repository.setPostHidden(forumId, postId, hidden)
    }
}

class DeleteForumPostUseCase(private val repository: ForumRepository) {
    suspend operator fun invoke(forumId: String, postId: String): Result<Unit> {
        if (forumId.isBlank() || postId.isBlank()) return Result.failure(IllegalArgumentException("Comentario no especificado"))
        return repository.deletePost(forumId, postId)
    }
}
