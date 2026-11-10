package com.movchia.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.movchia.app.data.model.Forum
import com.movchia.app.data.model.ForumPost
import com.movchia.app.data.remote.FirestoreCollections
import kotlinx.coroutines.tasks.await

/** Foros de opinión y sus comentarios. */
interface ForumRepository {
    suspend fun getForums(): Result<List<Forum>>
    suspend fun getForum(id: String): Result<Forum>

    /** Comentarios visibles del foro, del más reciente al más antiguo. */
    suspend fun getPosts(forumId: String, limit: Int = DEFAULT_POST_LIMIT): Result<List<ForumPost>>

    /** Publica un comentario y devuelve el documento creado. */
    suspend fun publishPost(post: ForumPost): Result<ForumPost>

    // Panel de gestión (solo personal)
    /** Todos los foros, abiertos y cerrados. */
    suspend fun getAllForums(): Result<List<Forum>>
    suspend fun upsertForum(forum: Forum): Result<Forum>
    /** Comentarios del foro incluidos los ocultos (moderación posterior). */
    suspend fun getAllPosts(forumId: String, limit: Int = MODERATION_POST_LIMIT): Result<List<ForumPost>>
    suspend fun setPostHidden(forumId: String, postId: String, hidden: Boolean): Result<Unit>
    suspend fun deletePost(forumId: String, postId: String): Result<Unit>

    companion object {
        const val DEFAULT_POST_LIMIT = 50
        const val MODERATION_POST_LIMIT = 200
    }
}

class FirebaseForumRepository(
    private val db: FirebaseFirestore,
) : ForumRepository {

    private fun posts(forumId: String) =
        db.collection(FirestoreCollections.FORUMS).document(forumId).collection(FirestoreCollections.POSTS)

    override suspend fun getForums(): Result<List<Forum>> = runCatching {
        db.collection(FirestoreCollections.FORUMS)
            .whereEqualTo(FirestoreCollections.FIELD_OPEN, true)
            .orderBy(FirestoreCollections.FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .get().await()
            .documents
            .mapNotNull { doc -> doc.toObject(Forum::class.java)?.copy(id = doc.id) }
    }

    override suspend fun getForum(id: String): Result<Forum> = runCatching {
        db.collection(FirestoreCollections.FORUMS).document(id).get().await()
            .let { doc -> doc.toObject(Forum::class.java)?.copy(id = doc.id) }
            ?: throw NoSuchElementException("Este foro ya no está disponible.")
    }

    override suspend fun getPosts(forumId: String, limit: Int): Result<List<ForumPost>> = runCatching {
        posts(forumId)
            .whereEqualTo(FirestoreCollections.FIELD_HIDDEN, false)
            .orderBy(FirestoreCollections.FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get().await()
            .documents
            .mapNotNull { doc -> doc.toObject(ForumPost::class.java)?.copy(id = doc.id, forumId = forumId) }
    }

    override suspend fun publishPost(post: ForumPost): Result<ForumPost> = runCatching {
        val ref = posts(post.forumId).document()
        val saved = post.copy(id = ref.id, hidden = false)
        // El id y el forumId viajan en la ruta; no se guardan como campos
        // (las reglas de seguridad validan el conjunto exacto de claves).
        ref.set(
            mapOf(
                "authorId" to saved.authorId,
                "nickname" to saved.nickname,
                "text" to saved.text,
                "createdAt" to saved.createdAt,
                "hidden" to false,
            ),
        ).await()
        saved
    }

    override suspend fun getAllForums(): Result<List<Forum>> = runCatching {
        db.collection(FirestoreCollections.FORUMS)
            .orderBy(FirestoreCollections.FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .get().await()
            .documents
            .mapNotNull { doc -> doc.toObject(Forum::class.java)?.copy(id = doc.id) }
    }

    override suspend fun upsertForum(forum: Forum): Result<Forum> = runCatching {
        val collection = db.collection(FirestoreCollections.FORUMS)
        val ref = if (forum.id.isBlank()) collection.document() else collection.document(forum.id)
        val saved = forum.copy(id = ref.id)
        ref.set(saved).await()
        saved
    }

    override suspend fun getAllPosts(forumId: String, limit: Int): Result<List<ForumPost>> = runCatching {
        posts(forumId)
            .orderBy(FirestoreCollections.FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get().await()
            .documents
            .mapNotNull { doc -> doc.toObject(ForumPost::class.java)?.copy(id = doc.id, forumId = forumId) }
    }

    override suspend fun setPostHidden(forumId: String, postId: String, hidden: Boolean): Result<Unit> = runCatching {
        posts(forumId).document(postId).update(FirestoreCollections.FIELD_HIDDEN, hidden).await()
    }

    override suspend fun deletePost(forumId: String, postId: String): Result<Unit> = runCatching {
        posts(forumId).document(postId).delete().await()
    }
}
