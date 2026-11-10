package com.movchia.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

/**
 * Punto único de acceso a los servicios de Firebase.
 * Las instancias son perezosas: no se inicializan hasta el primer uso, de
 * modo que la app arranca aunque `google-services.json` sea un placeholder.
 */
object FirebaseModule {
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
}

/** Nombres de colecciones y campos en Firestore. */
object FirestoreCollections {
    const val CONTENTS = "contents"
    const val QUIZ_QUESTIONS = "quiz_questions"
    const val TRAFFIC_CODE = "traffic_code"
    const val SERVICE_POINTS = "service_points"
    const val OPINIONS = "opinions"
    const val FORUMS = "forums"
    const val POSTS = "posts"
    const val USERS = "users"

    const val FIELD_TARGET_GROUPS = "targetGroups"
    const val FIELD_PUBLISHED = "published"
    const val FIELD_PUBLISHED_AT = "publishedAt"
    const val FIELD_ORDER = "order"
    const val FIELD_OPEN = "open"
    const val FIELD_HIDDEN = "hidden"
    const val FIELD_CREATED_AT = "createdAt"
    const val FIELD_ROLE = "role"
}
