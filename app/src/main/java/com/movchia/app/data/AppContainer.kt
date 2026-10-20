package com.movchia.app.data

import android.content.Context
import com.movchia.app.data.local.AgeGroupLocalStore
import com.movchia.app.data.remote.FirebaseModule
import com.movchia.app.data.remote.PlaceholderTrafficAssistantApi
import com.movchia.app.data.repository.AuthRepository
import com.movchia.app.data.repository.ContentRepository
import com.movchia.app.data.repository.FirebaseAuthRepository
import com.movchia.app.data.repository.FirebaseContentRepository
import com.movchia.app.data.repository.FirebaseTrafficCodeRepository
import com.movchia.app.data.repository.TrafficCodeRepository
import com.movchia.app.domain.usecases.AskTrafficAssistantUseCase
import com.movchia.app.domain.usecases.GetContentForAgeGroupUseCase
import com.movchia.app.domain.usecases.GetQuizQuestionsUseCase
import com.movchia.app.domain.usecases.GetServicePointsUseCase
import com.movchia.app.domain.usecases.GetTrafficCodeArticlesUseCase
import com.movchia.app.domain.usecases.SaveAgeGroupUseCase
import com.movchia.app.domain.usecases.SignInAdminUseCase
import com.movchia.app.utils.TextToSpeechHelper

/**
 * Contenedor manual de dependencias (sin Hilt/Koin para mantener la base ligera).
 * Los ViewModels reciben sus casos de uso desde aquí a través de
 * [com.movchia.app.navigation.AppNavigation].
 */
interface AppContainer {
    val ageGroupStore: AgeGroupLocalStore
    val contentRepository: ContentRepository
    val authRepository: AuthRepository
    val trafficCodeRepository: TrafficCodeRepository
    val textToSpeech: TextToSpeechHelper

    val saveAgeGroup: SaveAgeGroupUseCase
    val getContentForAgeGroup: GetContentForAgeGroupUseCase
    val getQuizQuestions: GetQuizQuestionsUseCase
    val getServicePoints: GetServicePointsUseCase
    val getTrafficCodeArticles: GetTrafficCodeArticlesUseCase
    val askTrafficAssistant: AskTrafficAssistantUseCase
    val signInAdmin: SignInAdminUseCase
}

class DefaultAppContainer(context: Context) : AppContainer {

    override val ageGroupStore = AgeGroupLocalStore(context)

    override val contentRepository: ContentRepository by lazy {
        FirebaseContentRepository(FirebaseModule.firestore)
    }
    override val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(FirebaseModule.auth)
    }
    override val trafficCodeRepository: TrafficCodeRepository by lazy {
        FirebaseTrafficCodeRepository(FirebaseModule.firestore, PlaceholderTrafficAssistantApi())
    }
    override val textToSpeech: TextToSpeechHelper by lazy { TextToSpeechHelper(context) }

    override val saveAgeGroup by lazy { SaveAgeGroupUseCase(ageGroupStore) }
    override val getContentForAgeGroup by lazy { GetContentForAgeGroupUseCase(contentRepository) }
    override val getQuizQuestions by lazy { GetQuizQuestionsUseCase(contentRepository) }
    override val getServicePoints by lazy { GetServicePointsUseCase(contentRepository) }
    override val getTrafficCodeArticles by lazy { GetTrafficCodeArticlesUseCase(trafficCodeRepository) }
    override val askTrafficAssistant by lazy { AskTrafficAssistantUseCase(trafficCodeRepository) }
    override val signInAdmin by lazy { SignInAdminUseCase(authRepository) }
}
