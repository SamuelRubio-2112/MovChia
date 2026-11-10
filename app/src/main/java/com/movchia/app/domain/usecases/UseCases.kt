package com.movchia.app.domain.usecases

import com.movchia.app.data.local.AgeGroupLocalStore
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.data.model.AssistantMessage
import com.movchia.app.data.model.ContentType
import com.movchia.app.data.model.EducationalContent
import com.movchia.app.data.model.Forum
import com.movchia.app.data.model.ForumPost
import com.movchia.app.data.model.QuizQuestion
import com.movchia.app.data.model.ServicePoint
import com.movchia.app.data.model.TrafficCodeArticle
import com.movchia.app.data.repository.AuthRepository
import com.movchia.app.data.repository.ContentRepository
import com.movchia.app.data.repository.ForumRepository
import com.movchia.app.data.repository.TrafficCodeRepository

/**
 * Casos de uso de la capa de dominio. Cada uno encapsula una sola acción de
 * negocio y expone `operator fun invoke` para usarse como función.
 */

class SaveAgeGroupUseCase(private val store: AgeGroupLocalStore) {
    operator fun invoke(age: Int): AgeGroup = AgeGroup.fromAge(age).also(store::save)
    operator fun invoke(group: AgeGroup) = store.save(group)
}

class GetContentForAgeGroupUseCase(private val repository: ContentRepository) {
    suspend operator fun invoke(
        group: AgeGroup,
        type: ContentType? = null,
    ): Result<List<EducationalContent>> = repository.getContentFor(group, type)
}

class GetContentByIdUseCase(private val repository: ContentRepository) {
    suspend operator fun invoke(id: String): Result<EducationalContent> {
        if (id.isBlank()) return Result.failure(IllegalArgumentException("Contenido no especificado"))
        return repository.getContentById(id)
    }
}

class GetQuizQuestionsUseCase(private val repository: ContentRepository) {
    suspend operator fun invoke(group: AgeGroup): Result<List<QuizQuestion>> =
        repository.getQuizQuestions(group)
}

class GetServicePointsUseCase(private val repository: ContentRepository) {
    suspend operator fun invoke(): Result<List<ServicePoint>> = repository.getServicePoints()
}

class GetServicePointByIdUseCase(private val repository: ContentRepository) {
    suspend operator fun invoke(id: String): Result<ServicePoint> {
        if (id.isBlank()) return Result.failure(IllegalArgumentException("Sede no especificada"))
        return repository.getServicePointById(id)
    }
}

class GetTrafficCodeArticlesUseCase(private val repository: TrafficCodeRepository) {
    suspend operator fun invoke(query: String = ""): Result<List<TrafficCodeArticle>> =
        if (query.isBlank()) repository.getArticles() else repository.searchArticles(query)
}

class AskTrafficAssistantUseCase(private val repository: TrafficCodeRepository) {
    suspend operator fun invoke(
        question: String,
        history: List<AssistantMessage>,
    ): Result<String> {
        if (question.isBlank()) return Result.failure(IllegalArgumentException("La pregunta está vacía"))
        return repository.askAssistant(question.trim(), history)
    }
}

class GetForumsUseCase(private val repository: ForumRepository) {
    suspend operator fun invoke(): Result<List<Forum>> = repository.getForums()
}

class GetForumPostsUseCase(private val repository: ForumRepository) {
    suspend operator fun invoke(forumId: String): Result<List<ForumPost>> {
        if (forumId.isBlank()) return Result.failure(IllegalArgumentException("Foro no especificado"))
        return repository.getPosts(forumId)
    }
}

/** Publica un comentario firmándolo con la sesión anónima del dispositivo. */
class PublishForumPostUseCase(
    private val forums: ForumRepository,
    /** Devuelve el uid con el que se firma; en modo demo no toca Firebase. */
    private val ensureSession: suspend () -> Result<String>,
) {
    suspend operator fun invoke(forumId: String, nickname: String, text: String): Result<ForumPost> {
        val clean = text.trim()
        if (clean.length < 5) return Result.failure(IllegalArgumentException("El comentario es muy corto."))
        if (clean.length > 500) return Result.failure(IllegalArgumentException("Máximo 500 caracteres."))
        if (nickname.isBlank()) return Result.failure(IllegalArgumentException("Falta el apodo."))
        val uid = ensureSession().getOrElse { return Result.failure(it) }
        return forums.publishPost(
            ForumPost(
                forumId = forumId,
                authorId = uid,
                nickname = nickname.trim(),
                text = clean,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }
}

class SignInAdminUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Correo y contraseña son obligatorios"))
        }
        return repository.signIn(email, password)
    }
}
