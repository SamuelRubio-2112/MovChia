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

    /** Devuelve la cantidad de documentos escritos. */
    suspend fun seed(db: FirebaseFirestore): Int {
        val batch = db.batch()
        var count = 0

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

        batch.commit().await()
        return count
    }
}
