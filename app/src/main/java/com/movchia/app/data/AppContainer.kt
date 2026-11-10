package com.movchia.app.data

import android.content.Context
import com.movchia.app.data.demo.DemoContentRepository
import com.movchia.app.data.demo.DemoForumRepository
import com.movchia.app.data.demo.SwitchingForumRepository
import com.movchia.app.data.demo.DemoTrafficCodeRepository
import com.movchia.app.data.demo.SwitchingContentRepository
import com.movchia.app.data.demo.SwitchingTrafficCodeRepository
import com.movchia.app.data.local.AgeGroupLocalStore
import com.movchia.app.data.local.PointsLocalStore
import com.movchia.app.data.local.UserProfileLocalStore
import com.movchia.app.data.remote.FirebaseModule
import com.movchia.app.data.remote.PlaceholderTrafficAssistantApi
import com.movchia.app.data.repository.AuthRepository
import com.movchia.app.data.repository.ContentRepository
import com.movchia.app.data.repository.FirebaseAuthRepository
import com.movchia.app.data.repository.FirebaseContentRepository
import com.movchia.app.data.repository.FirebaseForumRepository
import com.movchia.app.data.repository.ForumRepository
import com.movchia.app.data.repository.FirebaseTrafficCodeRepository
import com.movchia.app.data.repository.TrafficCodeRepository
import com.movchia.app.data.model.StaffRole
import com.movchia.app.data.session.StaffSessionStore
import com.movchia.app.domain.usecases.DeleteContentUseCase
import com.movchia.app.domain.usecases.DeleteForumPostUseCase
import com.movchia.app.domain.usecases.GetAllContentsUseCase
import com.movchia.app.domain.usecases.GetAllForumsUseCase
import com.movchia.app.domain.usecases.GetForumPostsForModerationUseCase
import com.movchia.app.domain.usecases.SaveContentUseCase
import com.movchia.app.domain.usecases.SaveForumUseCase
import com.movchia.app.domain.usecases.SetPostHiddenUseCase
import com.movchia.app.domain.usecases.SignOutAdminUseCase
import com.movchia.app.domain.usecases.AskTrafficAssistantUseCase
import com.movchia.app.domain.usecases.GetContentByIdUseCase
import com.movchia.app.domain.usecases.GetForumPostsUseCase
import com.movchia.app.domain.usecases.GetForumsUseCase
import com.movchia.app.domain.usecases.PublishForumPostUseCase
import com.movchia.app.domain.usecases.GetContentForAgeGroupUseCase
import com.movchia.app.domain.usecases.GetQuizQuestionsUseCase
import com.movchia.app.domain.usecases.GetServicePointByIdUseCase
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
    val userProfileStore: UserProfileLocalStore
    val forumRepository: ForumRepository
    val contentRepository: ContentRepository
    val authRepository: AuthRepository
    val staffSession: StaffSessionStore
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
    val getServicePointById: GetServicePointByIdUseCase
    val getTrafficCodeArticles: GetTrafficCodeArticlesUseCase
    val askTrafficAssistant: AskTrafficAssistantUseCase
    val signInAdmin: SignInAdminUseCase
    val getForums: GetForumsUseCase
    val getForumPosts: GetForumPostsUseCase
    val publishForumPost: PublishForumPostUseCase

    // Panel de gestión (CMS)
    val signOutAdmin: SignOutAdminUseCase
    val getAllContents: GetAllContentsUseCase
    val saveContent: SaveContentUseCase
    val deleteContent: DeleteContentUseCase
    val getAllForums: GetAllForumsUseCase
    val saveForum: SaveForumUseCase
    val getForumPostsForModeration: GetForumPostsForModerationUseCase
    val setPostHidden: SetPostHiddenUseCase
    val deleteForumPost: DeleteForumPostUseCase
}

class DefaultAppContainer(
    context: Context,
    private val allowDemoMode: Boolean,
) : AppContainer {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override val ageGroupStore = AgeGroupLocalStore(context)
    override val pointsStore = PointsLocalStore(context)
    override val userProfileStore = UserProfileLocalStore(context)

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

    private val firebaseForums: ForumRepository by lazy { FirebaseForumRepository(FirebaseModule.firestore) }
    private val demoForums: ForumRepository by lazy { DemoForumRepository() }
    override val forumRepository: ForumRepository =
        SwitchingForumRepository { if (_demoMode.value) demoForums else firebaseForums }

    override val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(FirebaseModule.auth, FirebaseModule.firestore)
    }
    override val staffSession = StaffSessionStore()
    override val textToSpeech: TextToSpeechHelper by lazy { TextToSpeechHelper(context) }

    override val saveAgeGroup by lazy { SaveAgeGroupUseCase(ageGroupStore) }
    override val getContentForAgeGroup by lazy { GetContentForAgeGroupUseCase(contentRepository) }
    override val getContentById by lazy { GetContentByIdUseCase(contentRepository) }
    override val getQuizQuestions by lazy { GetQuizQuestionsUseCase(contentRepository) }
    override val getServicePoints by lazy { GetServicePointsUseCase(contentRepository) }
    override val getServicePointById by lazy { GetServicePointByIdUseCase(contentRepository) }
    override val getTrafficCodeArticles by lazy { GetTrafficCodeArticlesUseCase(trafficCodeRepository) }
    override val askTrafficAssistant by lazy { AskTrafficAssistantUseCase(trafficCodeRepository) }
    override val signInAdmin by lazy {
        // En modo demo el panel se abre con cualquier correo y rol root (sin Firebase).
        SignInAdminUseCase(authRepository, staffSession) { if (_demoMode.value) StaffRole.ROOT else null }
    }
    override val signOutAdmin by lazy { SignOutAdminUseCase(authRepository, staffSession) }
    override val getAllContents by lazy { GetAllContentsUseCase(contentRepository) }
    override val saveContent by lazy { SaveContentUseCase(contentRepository) }
    override val deleteContent by lazy { DeleteContentUseCase(contentRepository) }
    override val getAllForums by lazy { GetAllForumsUseCase(forumRepository) }
    override val saveForum by lazy { SaveForumUseCase(forumRepository) }
    override val getForumPostsForModeration by lazy { GetForumPostsForModerationUseCase(forumRepository) }
    override val setPostHidden by lazy { SetPostHiddenUseCase(forumRepository) }
    override val deleteForumPost by lazy { DeleteForumPostUseCase(forumRepository) }
    override val getForums by lazy { GetForumsUseCase(forumRepository) }
    override val getForumPosts by lazy { GetForumPostsUseCase(forumRepository) }
    override val publishForumPost by lazy {
        PublishForumPostUseCase(forumRepository) {
            if (_demoMode.value) Result.success("demo-user") else authRepository.ensureSession()
        }
    }

    private companion object {
        const val PREFS_NAME = "movchia_dev_prefs"
        const val KEY_DEMO_MODE = "demo_mode"
    }
}
