package com.movchia.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.data.model.ContentType
import com.movchia.app.data.model.EducationalContent
import com.movchia.app.data.model.QuizQuestion
import com.movchia.app.data.model.ServicePoint
import com.movchia.app.data.remote.FirestoreCollections
import kotlinx.coroutines.tasks.await

/**
 * Acceso al contenido educativo (videos, cuentos, noticias, quizzes, sedes).
 */
interface ContentRepository {
    suspend fun getContentFor(group: AgeGroup, type: ContentType? = null): Result<List<EducationalContent>>
    suspend fun getContentById(id: String): Result<EducationalContent>
    suspend fun getQuizQuestions(group: AgeGroup): Result<List<QuizQuestion>>
    suspend fun getServicePoints(): Result<List<ServicePoint>>

    // Operaciones del CMS (requieren sesión de administrador)
    suspend fun upsertContent(content: EducationalContent): Result<Unit>
    suspend fun deleteContent(id: String): Result<Unit>
}

class FirebaseContentRepository(
    private val db: FirebaseFirestore,
) : ContentRepository {

    override suspend fun getContentFor(
        group: AgeGroup,
        type: ContentType?,
    ): Result<List<EducationalContent>> = runCatching {
        var query: Query = db.collection(FirestoreCollections.CONTENTS)
            .whereEqualTo(FirestoreCollections.FIELD_PUBLISHED, true)
            .whereArrayContains(FirestoreCollections.FIELD_TARGET_GROUPS, group.name)
        if (type != null) query = query.whereEqualTo("type", type.name)

        query.orderBy(FirestoreCollections.FIELD_PUBLISHED_AT, Query.Direction.DESCENDING)
            .get().await()
            .documents
            .mapNotNull { doc -> doc.toObject(EducationalContent::class.java)?.copy(id = doc.id) }
    }

    override suspend fun getContentById(id: String): Result<EducationalContent> = runCatching {
        val doc = db.collection(FirestoreCollections.CONTENTS).document(id).get().await()
        doc.toObject(EducationalContent::class.java)?.copy(id = doc.id)
            ?: throw NoSuchElementException("Este contenido ya no está disponible.")
    }

    override suspend fun getQuizQuestions(group: AgeGroup): Result<List<QuizQuestion>> = runCatching {
        db.collection(FirestoreCollections.QUIZ_QUESTIONS)
            .whereArrayContains(FirestoreCollections.FIELD_TARGET_GROUPS, group.name)
            .get().await()
            .documents
            .mapNotNull { doc -> doc.toObject(QuizQuestion::class.java)?.copy(id = doc.id) }
    }

    override suspend fun getServicePoints(): Result<List<ServicePoint>> = runCatching {
        db.collection(FirestoreCollections.SERVICE_POINTS)
            .get().await()
            .documents
            .mapNotNull { doc -> doc.toObject(ServicePoint::class.java)?.copy(id = doc.id) }
    }

    override suspend fun upsertContent(content: EducationalContent): Result<Unit> = runCatching {
        val collection = db.collection(FirestoreCollections.CONTENTS)
        val ref = if (content.id.isBlank()) collection.document() else collection.document(content.id)
        ref.set(content.copy(id = ref.id)).await()
    }

    override suspend fun deleteContent(id: String): Result<Unit> = runCatching {
        db.collection(FirestoreCollections.CONTENTS).document(id).delete().await()
    }
}
