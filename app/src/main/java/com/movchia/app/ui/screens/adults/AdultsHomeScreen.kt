package com.movchia.app.ui.screens.adults

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movchia.app.data.model.EducationalContent
import com.movchia.app.ui.UiState
import com.movchia.app.ui.components.MovChiaTopBar
import com.movchia.app.ui.components.VideoPlaceholderComponent
import com.movchia.app.ui.components.VideoSource
import com.movchia.app.ui.theme.ChiaBlue
import com.movchia.app.ui.theme.ChiaSkyMuted
import com.movchia.app.ui.theme.ChiaTeal
import com.movchia.app.ui.theme.White

@Composable
fun AdultsHomeScreen(
    viewModel: AdultsHomeViewModel,
    onOpenTrafficCode: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenServicePoints: () -> Unit,
    onOpenNews: () -> Unit,
    onOpenHeadline: (EducationalContent?) -> Unit,
    onOpenCampaign: (EducationalContent?) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MovChiaTopBar(title = "MovChia") {
                IconButton(onClick = {}) { Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones") }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = White) {
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Filled.Home, null) }, label = { Text("Inicio") })
                NavigationBarItem(selected = false, onClick = onOpenNews, icon = { Icon(Icons.AutoMirrored.Filled.List, null) }, label = { Text("Contenido") })
                NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Filled.ThumbUp, null) }, label = { Text("Opiniones") })
                NavigationBarItem(selected = false, onClick = viewModel::load, icon = { Icon(Icons.Filled.Refresh, null) }, label = { Text("Recargar") })
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            val headline = (state as? UiState.Success)?.data?.news?.firstOrNull()
            NewsBanner(
                tag = "NOTICIA LOCAL",
                title = headline?.title ?: "Nuevas ciclorrutas habilitadas en el centro de Chía",
                onClick = { onOpenHeadline(headline) },
            )

            Text("Trámites y Aprendizaje", style = MaterialTheme.typography.titleMedium, color = ChiaBlue)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAccessCard("Manual de Tránsito", Icons.AutoMirrored.Filled.List, Modifier.weight(1f), onOpenTrafficCode)
                QuickAccessCard("Quizes y Test", Icons.Filled.Star, Modifier.weight(1f), onOpenQuiz)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAccessCard("Sedes y Puntos", Icons.Filled.Place, Modifier.weight(1f), onOpenServicePoints)
                QuickAccessCard("Noticias Chía", Icons.Filled.Notifications, Modifier.weight(1f), onOpenNews)
            }

            Text("🎬 Campaña del Mes", style = MaterialTheme.typography.titleMedium, color = ChiaBlue)
            val campaign = (state as? UiState.Success)?.data?.campaign
            VideoPlaceholderComponent(
                source = campaign?.let { VideoSource.Url(it.mediaUrl, it.thumbnailUrl) } ?: VideoSource.None,
                title = campaign?.title ?: "Campaña: Respeta al Peatón",
                onPlayClick = { onOpenCampaign(campaign) },
            )

            if (state is UiState.Error) {
                Text(
                    text = (state as UiState.Error).message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun NewsBanner(tag: String, title: String, onClick: () -> Unit) {
    Card(onClick = onClick, shape = MaterialTheme.shapes.medium) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Brush.verticalGradient(listOf(ChiaTeal, ChiaSkyMuted, ChiaBlue)))
                .padding(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(ChiaBlue)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(tag, style = MaterialTheme.typography.labelSmall, color = White)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = White,
                modifier = Modifier.align(Alignment.BottomStart),
            )
        }
    }
}

@Composable
private fun QuickAccessCard(title: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, contentDescription = null, tint = ChiaBlue)
            Text(title, style = MaterialTheme.typography.titleSmall, color = ChiaBlue)
        }
    }
}
