package com.movchia.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.movchia.app.data.model.AssistantMessage
import com.movchia.app.data.model.TrafficCodeArticle
import com.movchia.app.data.remote.FirestoreCollections
import com.movchia.app.data.remote.TrafficAssistantApi
import kotlinx.coroutines.tasks.await

/** Manual de Tránsito + Asistente de IA del Código Nacional de Tránsito. */
interface TrafficCodeRepository {
    suspend fun getArticles(): Result<List<TrafficCodeArticle>>
    suspend fun searchArticles(query: String): Result<List<TrafficCodeArticle>>
    suspend fun askAssistant(question: String, history: List<AssistantMessage>): Result<String>
}

class FirebaseTrafficCodeRepository(
    private val db: FirebaseFirestore,
    private val assistantApi: TrafficAssistantApi,
) : TrafficCodeRepository {

    override suspend fun getArticles(): Result<List<TrafficCodeArticle>> = runCatching {
        db.collection(FirestoreCollections.TRAFFIC_CODE)
            .orderBy(FirestoreCollections.FIELD_ORDER, Query.Direction.ASCENDING)
            .get().await()
            .documents
            .mapNotNull { doc -> doc.toObject(TrafficCodeArticle::class.java)?.copy(id = doc.id) }
    }

    /**
     * Búsqueda simple en memoria. Para catálogos grandes conviene migrar a
     * un índice de búsqueda (Algolia, Typesense) o a búsqueda vectorial.
     */
    override suspend fun searchArticles(query: String): Result<List<TrafficCodeArticle>> =
        getArticles().map { articles ->
            val q = query.trim().lowercase()
            if (q.isEmpty()) articles
            else articles.filter {
                it.title.lowercase().contains(q) ||
                    it.body.lowercase().contains(q) ||
                    it.tags.any { tag -> tag.lowercase().contains(q) }
            }
        }

    override suspend fun askAssistant(
        question: String,
        history: List<AssistantMessage>,
    ): Result<String> = runCatching { assistantApi.ask(question, history) }
}
