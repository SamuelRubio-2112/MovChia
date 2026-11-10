package com.movchia.app.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.movchia.app.data.demo.DemoData
import com.movchia.app.data.remote.FirestoreCollections
import kotlinx.coroutines.tasks.await

/**
 * SOLO PARA DESARROLLO. Escribe [DemoData] en Firestore con IDs fijos (se
 * puede ejecutar varias veces: sobreescribe). Requiere sesión de un usuario
 * con rol gestor o root, según `firestore.rules`.
 */
object DevSeeder {

    private val obsolete = listOf(
        FirestoreCollections.CONTENTS to "demo-news-ciclorrutas",
        FirestoreCollections.CONTENTS to "demo-news-fotomultas",
        FirestoreCollections.SERVICE_POINTS to "demo-sede-principal",
        FirestoreCollections.SERVICE_POINTS to "demo-sede-tramites",
    )

    /** Devuelve la cantidad de documentos escritos. */
    suspend fun seed(db: FirebaseFirestore): Int {
        val batch = db.batch()
        var count = 0

        // Documentos de versiones anteriores de DemoData que ya no existen.
        obsolete.forEach { (collection, id) -> batch.delete(db.collection(collection).document(id)) }

        DemoData.contents.forEach {
            batch.set(db.collection(FirestoreCollections.CONTENTS).document(it.id), it); count++
        }
        DemoData.trafficCodeArticles.forEach {
            batch.set(db.collection(FirestoreCollections.TRAFFIC_CODE).document(it.id), it); count++
        }
        DemoData.servicePoints.forEach {
            batch.set(db.collection(FirestoreCollections.SERVICE_POINTS).document(it.id), it); count++
        }
        DemoData.quizQuestions.forEach {
            batch.set(db.collection(FirestoreCollections.QUIZ_QUESTIONS).document(it.id), it); count++
        }
        DemoData.forums.forEach {
            batch.set(db.collection(FirestoreCollections.FORUMS).document(it.id), it); count++
        }
        DemoData.forumPosts.forEach {
            batch.set(
                db.collection(FirestoreCollections.FORUMS).document(it.forumId)
                    .collection(FirestoreCollections.POSTS).document(it.id),
                mapOf(
                    "authorId" to it.authorId,
                    "nickname" to it.nickname,
                    "text" to it.text,
                    "createdAt" to it.createdAt,
                    "hidden" to it.hidden,
                ),
            ); count++
        }

        batch.commit().await()
        return count
    }
}
