package com.movchia.app.data

import android.content.Context
import com.movchia.app.data.demo.DemoContentRepository
import com.movchia.app.data.demo.DemoTrafficCodeRepository
import com.movchia.app.data.demo.SwitchingContentRepository
import com.movchia.app.data.demo.SwitchingTrafficCodeRepository
import com.movchia.app.data.local.AgeGroupLocalStore
import com.movchia.app.data.local.PointsLocalStore
import com.movchia.app.data.remote.FirebaseModule
import com.movchia.app.data.remote.PlaceholderTrafficAssistantApi
import com.movchia.app.data.repository.AuthRepository
import com.movchia.app.data.repository.ContentRepository
import com.movchia.app.data.repository.FirebaseAuthRepository
import com.movchia.app.data.repository.FirebaseContentRepository
import com.movchia.app.data.repository.FirebaseTrafficCodeRepository
import com.movchia.app.data.repository.TrafficCodeRepository
import com.movchia.app.domain.usecases.AskTrafficAssistantUseCase
import com.movchia.app.domain.usecases.GetContentByIdUseCase
import com.movchia.app.domain.usecases.GetContentForAgeGroupUseCase
import com.movchia.app.domain.usecases.GetQuizQuestionsUseCase
import com.movchia.app.domain.usecases.GetServicePointsUseCase
import com.movchia.app.domain.usecases.GetTrafficCodeArticlesUseCase
import com.movchia.app.domain.usecases.SaveAgeGroupUseCase
import com.movchia.app.domain.usecases.SignInAdminUseCase
import com.movchia.app.utils.TextToSpeechHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Contenedor manual de dependencias (sin Hilt/Koin para mantener la base ligera).
 * Los ViewModels reciben sus casos de uso desde aquí a través de
 * [com.movchia.app.navigation.AppNavigation].
 */
interface AppContainer {
    val ageGroupStore: AgeGroupLocalStore
    val pointsStore: PointsLocalStore
    val contentRepository: ContentRepository
    val authRepository: AuthRepository
    val trafficCodeRepository: TrafficCodeRepository
    val textToSpeech: TextToSpeechHelper

    /**
     * Modo demo (solo debug): sirve [com.movchia.app.data.demo.DemoData] desde
     * memoria en lugar de Firebase. En release siempre es `false`.
     */
    val demoMode: StateFlow<Boolean>
    fun setDemoMode(enabled: Boolean)

    val saveAgeGroup: SaveAgeGroupUseCase
    val getContentForAgeGroup: GetContentForAgeGroupUseCase
    val getContentById: GetContentByIdUseCase
    val getQuizQuestions: GetQuizQuestionsUseCase
    val getServicePoints: GetServicePointsUseCase
    val getTrafficCodeArticles: GetTrafficCodeArticlesUseCase
    val askTrafficAssistant: AskTrafficAssistantUseCase
    val signInAdmin: SignInAdminUseCase
}

class DefaultAppContainer(
    context: Context,
    private val allowDemoMode: Boolean,
) : AppContainer {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override val ageGroupStore = AgeGroupLocalStore(context)
    override val pointsStore = PointsLocalStore(context)

    private val _demoMode = MutableStateFlow(allowDemoMode && prefs.getBoolean(KEY_DEMO_MODE, false))
    override val demoMode: StateFlow<Boolean> = _demoMode.asStateFlow()

    override fun setDemoMode(enabled: Boolean) {
        if (!allowDemoMode) return
        prefs.edit().putBoolean(KEY_DEMO_MODE, enabled).apply()
        _demoMode.value = enabled
    }

    private val firebaseContent: ContentRepository by lazy { FirebaseContentRepository(FirebaseModule.firestore) }
    private val demoContent: ContentRepository by lazy { DemoContentRepository() }

    private val assistantApi by lazy { PlaceholderTrafficAssistantApi() }
    private val firebaseTrafficCode: TrafficCodeRepository by lazy {
        FirebaseTrafficCodeRepository(FirebaseModule.firestore, assistantApi)
    }
    private val demoTrafficCode: TrafficCodeRepository by lazy {
        DemoTrafficCodeRepository { q, h -> assistantApi.ask(q, h) }
    }

    override val contentRepository: ContentRepository =
        SwitchingContentRepository { if (_demoMode.value) demoContent else firebaseContent }

    override val trafficCodeRepository: TrafficCodeRepository =
        SwitchingTrafficCodeRepository { if (_demoMode.value) demoTrafficCode else firebaseTrafficCode }

    override val authRepository: AuthRepository by lazy { FirebaseAuthRepository(FirebaseModule.auth) }
    override val textToSpeech: TextToSpeechHelper by lazy { TextToSpeechHelper(context) }

    override val saveAgeGroup by lazy { SaveAgeGroupUseCase(ageGroupStore) }
    override val getContentForAgeGroup by lazy { GetContentForAgeGroupUseCase(contentRepository) }
    override val getContentById by lazy { GetContentByIdUseCase(contentRepository) }
    override val getQuizQuestions by lazy { GetQuizQuestionsUseCase(contentRepository) }
    override val getServicePoints by lazy { GetServicePointsUseCase(contentRepository) }
    override val getTrafficCodeArticles by lazy { GetTrafficCodeArticlesUseCase(trafficCodeRepository) }
    override val askTrafficAssistant by lazy { AskTrafficAssistantUseCase(trafficCodeRepository) }
    override val signInAdmin by lazy { SignInAdminUseCase(authRepository) }

    private companion object {
        const val PREFS_NAME = "movchia_dev_prefs"
        const val KEY_DEMO_MODE = "demo_mode"
    }
}
