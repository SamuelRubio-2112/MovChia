package com.movchia.app.data.demo

import com.movchia.app.data.model.AgeGroup
import com.movchia.app.data.model.AssistantMessage
import com.movchia.app.data.model.ContentType
import com.movchia.app.data.model.EducationalContent
import com.movchia.app.data.model.Forum
import com.movchia.app.data.model.ForumPost
import com.movchia.app.data.model.QuizQuestion
import com.movchia.app.data.model.ServicePoint
import com.movchia.app.data.model.TrafficCodeArticle
import com.movchia.app.data.repository.ContentRepository
import com.movchia.app.data.repository.ForumRepository
import com.movchia.app.data.repository.TrafficCodeRepository
import kotlinx.coroutines.delay

/**
 * Repositorios en memoria con [DemoData]. Solo se activan en compilaciones
 * debug mediante el "Modo demo" del selector de desarrollo, para probar las
 * pantallas sin un proyecto de Firebase conectado.
 */
class DemoContentRepository : ContentRepository {

    private val items = DemoData.contents.associateBy { it.id }.toMutableMap()

    override suspend fun getContentFor(group: AgeGroup, type: ContentType?): Result<List<EducationalContent>> {
        delay(SIMULATED_LATENCY_MS)
        return Result.success(
            items.values
                .filter { it.published && group in it.targetGroups && (type == null || it.type == type) }
                .sortedByDescending { it.publishedAt },
        )
    }

    override suspend fun getContentById(id: String): Result<EducationalContent> {
        delay(SIMULATED_LATENCY_MS)
        return items[id]?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Este contenido ya no está disponible."))
    }

    override suspend fun getQuizQuestions(group: AgeGroup): Result<List<QuizQuestion>> =
        Result.success(DemoData.quizQuestions.filter { group in it.targetGroups })

    override suspend fun getServicePoints(): Result<List<ServicePoint>> =
        Result.success(DemoData.servicePoints)

    override suspend fun getServicePointById(id: String): Result<ServicePoint> =
        DemoData.servicePoints.firstOrNull { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Esta sede ya no está disponible."))

    override suspend fun getAllContents(): Result<List<EducationalContent>> {
        delay(SIMULATED_LATENCY_MS)
        return Result.success(items.values.sortedByDescending { it.publishedAt })
    }

    override suspend fun upsertContent(content: EducationalContent): Result<Unit> {
        val id = content.id.ifBlank { "demo-${System.currentTimeMillis()}" }
        items[id] = content.copy(id = id)
        return Result.success(Unit)
    }

    override suspend fun deleteContent(id: String): Result<Unit> {
        items.remove(id)
        return Result.success(Unit)
    }

    private companion object {
        const val SIMULATED_LATENCY_MS = 300L
    }
}

class DemoTrafficCodeRepository(
    private val assistant: suspend (String, List<AssistantMessage>) -> String,
) : TrafficCodeRepository {

    override suspend fun getArticles(): Result<List<TrafficCodeArticle>> =
        Result.success(DemoData.trafficCodeArticles.sortedBy { it.order })

    override suspend fun searchArticles(query: String): Result<List<TrafficCodeArticle>> {
        val q = query.trim().lowercase()
        return getArticles().map { articles ->
            if (q.isEmpty()) articles
            else articles.filter {
                it.title.lowercase().contains(q) || it.body.lowercase().contains(q) ||
                    it.tags.any { tag -> tag.lowercase().contains(q) }
            }
        }
    }

    override suspend fun askAssistant(question: String, history: List<AssistantMessage>): Result<String> =
        runCatching { assistant(question, history) }
}

/**
 * Enruta cada llamada al repositorio que indique [select] en ese momento.
 * Permite alternar entre Firebase y demo sin recrear los ViewModels.
 */
class SwitchingContentRepository(private val select: () -> ContentRepository) : ContentRepository {
    override suspend fun getContentFor(group: AgeGroup, type: ContentType?) = select().getContentFor(group, type)
    override suspend fun getContentById(id: String) = select().getContentById(id)
    override suspend fun getQuizQuestions(group: AgeGroup) = select().getQuizQuestions(group)
    override suspend fun getServicePoints() = select().getServicePoints()
    override suspend fun getServicePointById(id: String) = select().getServicePointById(id)
    override suspend fun getAllContents() = select().getAllContents()
    override suspend fun upsertContent(content: EducationalContent) = select().upsertContent(content)
    override suspend fun deleteContent(id: String) = select().deleteContent(id)
}

class DemoForumRepository : ForumRepository {
    private val forums = DemoData.forums.toMutableList()
    private val posts = DemoData.forumPosts.toMutableList()

    override suspend fun getForums(): Result<List<Forum>> =
        Result.success(forums.filter { it.open }.sortedByDescending { it.createdAt })

    override suspend fun getForum(id: String): Result<Forum> =
        forums.firstOrNull { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Este foro ya no está disponible."))

    override suspend fun getAllForums(): Result<List<Forum>> =
        Result.success(forums.sortedByDescending { it.createdAt })

    override suspend fun upsertForum(forum: Forum): Result<Forum> {
        val saved = forum.copy(id = forum.id.ifBlank { "demo-forum-${System.currentTimeMillis()}" })
        val i = forums.indexOfFirst { it.id == saved.id }
        if (i >= 0) forums[i] = saved else forums.add(saved)
        return Result.success(saved)
    }

    override suspend fun getAllPosts(forumId: String, limit: Int): Result<List<ForumPost>> =
        Result.success(posts.filter { it.forumId == forumId }.sortedByDescending { it.createdAt }.take(limit))

    override suspend fun getPosts(forumId: String, limit: Int): Result<List<ForumPost>> =
        Result.success(
            posts.filter { it.forumId == forumId && !it.hidden }.sortedByDescending { it.createdAt }.take(limit),
        )

    override suspend fun publishPost(post: ForumPost): Result<ForumPost> {
        val saved = post.copy(id = "demo-post-${System.currentTimeMillis()}", hidden = false)
        posts.add(saved)
        return Result.success(saved)
    }

    override suspend fun setPostHidden(forumId: String, postId: String, hidden: Boolean): Result<Unit> {
        val i = posts.indexOfFirst { it.id == postId }
        if (i >= 0) posts[i] = posts[i].copy(hidden = hidden)
        return Result.success(Unit)
    }

    override suspend fun deletePost(forumId: String, postId: String): Result<Unit> {
        posts.removeAll { it.id == postId }
        return Result.success(Unit)
    }
}

class SwitchingForumRepository(private val select: () -> ForumRepository) : ForumRepository {
    override suspend fun getForums() = select().getForums()
    override suspend fun getForum(id: String) = select().getForum(id)
    override suspend fun getPosts(forumId: String, limit: Int) = select().getPosts(forumId, limit)
    override suspend fun publishPost(post: ForumPost) = select().publishPost(post)
    override suspend fun getAllForums() = select().getAllForums()
    override suspend fun upsertForum(forum: Forum) = select().upsertForum(forum)
    override suspend fun getAllPosts(forumId: String, limit: Int) = select().getAllPosts(forumId, limit)
    override suspend fun setPostHidden(forumId: String, postId: String, hidden: Boolean) =
        select().setPostHidden(forumId, postId, hidden)
    override suspend fun deletePost(forumId: String, postId: String) = select().deletePost(forumId, postId)
}

class SwitchingTrafficCodeRepository(private val select: () -> TrafficCodeRepository) : TrafficCodeRepository {
    override suspend fun getArticles() = select().getArticles()
    override suspend fun searchArticles(query: String) = select().searchArticles(query)
    override suspend fun askAssistant(question: String, history: List<AssistantMessage>) =
        select().askAssistant(question, history)
}
