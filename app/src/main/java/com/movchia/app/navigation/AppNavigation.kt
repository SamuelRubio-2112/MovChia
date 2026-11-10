package com.movchia.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.movchia.app.data.AppContainer
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.data.model.ContentType
import com.movchia.app.ui.screens.adults.AdultsHomeScreen
import com.movchia.app.ui.screens.adults.AdultsHomeViewModel
import com.movchia.app.ui.screens.age_selection.AgeSelectionScreen
import com.movchia.app.ui.screens.age_selection.AgeSelectionViewModel
import com.movchia.app.ui.screens.cms_admin.AdminDashboardScreen
import com.movchia.app.ui.screens.cms_admin.AdminLoginScreen
import com.movchia.app.ui.screens.cms_admin.CmsAdminViewModel
import com.movchia.app.ui.screens.content.ContentDetailScreen
import com.movchia.app.ui.screens.content.ContentDetailViewModel
import com.movchia.app.ui.screens.content.ContentListScreen
import com.movchia.app.ui.screens.content.ContentListViewModel
import com.movchia.app.ui.screens.forum.ForumDetailScreen
import com.movchia.app.ui.screens.forum.ForumDetailViewModel
import com.movchia.app.ui.screens.forum.ForumsScreen
import com.movchia.app.ui.screens.forum.ForumsViewModel
import com.movchia.app.ui.screens.kids.KidsHomeScreen
import com.movchia.app.ui.screens.kids.KidsHomeViewModel
import com.movchia.app.ui.screens.notifications.NotificationsScreen
import com.movchia.app.ui.screens.quiz.QuizScreen
import com.movchia.app.ui.screens.quiz.QuizViewModel
import com.movchia.app.ui.screens.seniors.SeniorsHomeScreen
import com.movchia.app.ui.screens.service_points.ServicePointDetailScreen
import com.movchia.app.ui.screens.service_points.ServicePointDetailViewModel
import com.movchia.app.ui.screens.service_points.ServicePointsListScreen
import com.movchia.app.ui.screens.service_points.ServicePointsViewModel
import com.movchia.app.ui.screens.seniors.SeniorsHomeViewModel
import com.movchia.app.ui.screens.splash.SplashScreen
import com.movchia.app.ui.screens.traffic_code.TrafficCodeScreen
import com.movchia.app.ui.screens.traffic_code.TrafficCodeViewModel

/**
 * Grafo de navegación de MovChia.
 *
 * Splash → (edad guardada ? Home del grupo : AgeSelection)
 * AgeSelection → KidsHome | AdultsHome | SeniorsHome | AdminLogin
 * Homes → TrafficCode (manual + IA) | ContentList (por tipo) | ContentDetail | Quiz
 * ContentList → ContentDetail
 * AdminLogin → AdminDashboard → ContentDetail (vista previa)
 */
@Composable
fun AppNavigation(
    container: AppContainer,
    navController: NavHostController = rememberNavController(),
) {
    fun openList(type: ContentType, title: String) =
        navController.navigate(Screen.ContentList(type = type.name, title = title))

    fun openDetail(id: String) {
        if (id.isNotBlank()) navController.navigate(Screen.ContentDetail(id))
    }

    NavHost(navController = navController, startDestination = Screen.Splash) {

        composable<Screen.Splash> {
            SplashScreen(
                onFinished = {
                    val saved = container.ageGroupStore.ageGroup.value
                    navController.navigate(saved?.homeScreen() ?: Screen.AgeSelection) {
                        popUpTo<Screen.Splash> { inclusive = true }
                    }
                },
            )
        }

        composable<Screen.AgeSelection> {
            val viewModel: AgeSelectionViewModel = viewModel(
                factory = viewModelFactory { initializer { AgeSelectionViewModel(container.saveAgeGroup) } },
            )
            AgeSelectionScreen(
                viewModel = viewModel,
                onContinue = { group ->
                    navController.navigate(group.homeScreen()) {
                        popUpTo<Screen.AgeSelection> { inclusive = true }
                    }
                },
                onAdminAccess = { navController.navigate(Screen.AdminLogin) },
            )
        }

        composable<Screen.KidsHome> {
            val viewModel: KidsHomeViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { KidsHomeViewModel(container.getContentForAgeGroup, container.pointsStore) }
                },
            )
            KidsHomeScreen(
                viewModel = viewModel,
                onOpenQuiz = { navController.navigate(Screen.Quiz) },
                onOpenVideos = { openList(ContentType.CARTOON, "Dibujos de Seguridad") },
                onOpenStory = { story ->
                    if (story != null) openDetail(story.id) else openList(ContentType.STORY, "Cuentos")
                },
                onOpenCartoon = { cartoon ->
                    if (cartoon != null) openDetail(cartoon.id) else openList(ContentType.CARTOON, "Dibujos de Seguridad")
                },
            )
        }

        composable<Screen.AdultsHome> {
            val viewModel: AdultsHomeViewModel = viewModel(
                factory = viewModelFactory { initializer { AdultsHomeViewModel(container.getContentForAgeGroup) } },
            )
            AdultsHomeScreen(
                viewModel = viewModel,
                onOpenTrafficCode = { navController.navigate(Screen.TrafficCode()) },
                onOpenQuiz = { navController.navigate(Screen.Quiz) },
                onOpenServicePoints = { navController.navigate(Screen.ServicePoints("Sedes y Puntos")) },
                onOpenNews = { openList(ContentType.NEWS, "Noticias de Chía") },
                onOpenNotifications = { navController.navigate(Screen.Notifications) },
                onOpenOpinions = { navController.navigate(Screen.Forums) },
                onOpenHeadline = { headline ->
                    if (headline != null) openDetail(headline.id) else openList(ContentType.NEWS, "Noticias de Chía")
                },
                onOpenCampaign = { campaign ->
                    if (campaign != null) openDetail(campaign.id) else openList(ContentType.CAMPAIGN, "Campañas")
                },
            )
        }

        composable<Screen.SeniorsHome> {
            val viewModel: SeniorsHomeViewModel = viewModel(
                factory = viewModelFactory { initializer { SeniorsHomeViewModel(container.textToSpeech) } },
            )
            SeniorsHomeScreen(
                viewModel = viewModel,
                onOpenSimpleManual = { navController.navigate(Screen.TrafficCode(simplified = true)) },
                onOpenServicePoints = { navController.navigate(Screen.ServicePoints("Puntos de atención")) },
                onOpenVideos = { openList(ContentType.VIDEO, "Videos explicativos") },
                onOpenFaq = { openList(ContentType.FAQ, "Preguntas frecuentes") },
            )
        }

        composable<Screen.ContentList> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.ContentList>()
            val type = ContentType.entries.firstOrNull { it.name == route.type } ?: ContentType.NEWS
            val group = container.ageGroupStore.ageGroup.value ?: AgeGroup.ADULTS
            val viewModel: ContentListViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { ContentListViewModel(container.getContentForAgeGroup, group, type) }
                },
            )
            ContentListScreen(
                viewModel = viewModel,
                title = route.title,
                onBack = { navController.popBackStack() },
                onOpenContent = { openDetail(it.id) },
            )
        }

        composable<Screen.ContentDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.ContentDetail>()
            val viewModel: ContentDetailViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { ContentDetailViewModel(container.getContentById, container.textToSpeech, route.id) }
                },
            )
            ContentDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable<Screen.Quiz> {
            val group = container.ageGroupStore.ageGroup.value ?: AgeGroup.ADULTS
            val viewModel: QuizViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { QuizViewModel(container.getQuizQuestions, container.pointsStore, group) }
                },
            )
            QuizScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable<Screen.Notifications> {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }

        composable<Screen.Forums> {
            val viewModel: ForumsViewModel = viewModel(
                factory = viewModelFactory { initializer { ForumsViewModel(container.getForums) } },
            )
            ForumsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenForum = { navController.navigate(Screen.ForumDetail(it.id, it.title)) },
            )
        }

        composable<Screen.ForumDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.ForumDetail>()
            val viewModel: ForumDetailViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        ForumDetailViewModel(
                            container.getForumPosts,
                            container.publishForumPost,
                            container.userProfileStore,
                            route.id,
                        )
                    }
                },
            )
            ForumDetailScreen(viewModel = viewModel, title = route.title, onBack = { navController.popBackStack() })
        }

        composable<Screen.ServicePoints> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.ServicePoints>()
            val viewModel: ServicePointsViewModel = viewModel(
                factory = viewModelFactory { initializer { ServicePointsViewModel(container.getServicePoints) } },
            )
            ServicePointsListScreen(
                viewModel = viewModel,
                title = route.title,
                onBack = { navController.popBackStack() },
                onOpenPoint = { navController.navigate(Screen.ServicePointDetail(it.id)) },
            )
        }

        composable<Screen.ServicePointDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.ServicePointDetail>()
            val viewModel: ServicePointDetailViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        ServicePointDetailViewModel(container.getServicePointById, container.textToSpeech, route.id)
                    }
                },
            )
            ServicePointDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable<Screen.TrafficCode> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.TrafficCode>()
            val viewModel: TrafficCodeViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        TrafficCodeViewModel(
                            getArticles = container.getTrafficCodeArticles,
                            askAssistant = container.askTrafficAssistant,
                            simplified = route.simplified,
                        )
                    }
                },
            )
            TrafficCodeScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                initialTab = route.initialTab,
            )
        }

        composable<Screen.AdminLogin> {
            val viewModel: CmsAdminViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        CmsAdminViewModel(container.signInAdmin, container.authRepository, container.contentRepository)
                    }
                },
            )
            AdminLoginScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onLoggedIn = {
                    navController.navigate(Screen.AdminDashboard) {
                        popUpTo<Screen.AdminLogin> { inclusive = true }
                    }
                },
            )
        }

        composable<Screen.AdminDashboard> {
            val viewModel: CmsAdminViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        CmsAdminViewModel(container.signInAdmin, container.authRepository, container.contentRepository)
                    }
                },
            )
            AdminDashboardScreen(
                viewModel = viewModel,
                onSignedOut = {
                    navController.navigate(Screen.AgeSelection) {
                        popUpTo<Screen.AdminDashboard> { inclusive = true }
                    }
                },
                onCreateContent = { /* TODO: pantalla 13_EditorContenido */ },
                onOpenContent = { openDetail(it.id) },
            )
        }
    }
}
