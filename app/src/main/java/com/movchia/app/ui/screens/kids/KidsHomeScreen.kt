package com.movchia.app.ui.screens.kids

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movchia.app.data.model.EducationalContent
import com.movchia.app.ui.UiState
import com.movchia.app.ui.components.VideoPlaceholderComponent
import com.movchia.app.ui.components.VideoSource
import com.movchia.app.ui.theme.ChiaGreen
import com.movchia.app.ui.theme.ChiaNavy
import com.movchia.app.ui.theme.ChiaPink
import com.movchia.app.ui.theme.ChiaSky
import com.movchia.app.ui.theme.ChiaYellow
import com.movchia.app.ui.theme.KidsBanner
import com.movchia.app.ui.theme.White

@Composable
fun KidsHomeScreen(
    viewModel: KidsHomeViewModel,
    onOpenQuiz: () -> Unit,
    onOpenVideos: () -> Unit,
    onOpenStory: (EducationalContent?) -> Unit,
    onOpenCartoon: (EducationalContent?) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = White) {
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Filled.Home, null) }, label = { Text("Inicio") })
                NavigationBarItem(selected = false, onClick = onOpenVideos, icon = { Icon(Icons.Filled.PlayArrow, null) }, label = { Text("Videos") })
                NavigationBarItem(selected = false, onClick = onOpenQuiz, icon = { Icon(Icons.Filled.Star, null) }, label = { Text("Juegos") })
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Encabezado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KidsBanner)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("¡Hola, Futuro Conductor! 🚗", style = MaterialTheme.typography.titleMedium, color = ChiaNavy)
                val points = (state as? UiState.Success)?.data?.points ?: 0
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(White)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text("Puntos: $points ⭐", style = MaterialTheme.typography.labelLarge, color = ChiaNavy)
                }
            }

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                when (val s = state) {
                    UiState.Loading -> Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    is UiState.Error -> StoryOfTheDayCard(
                        title = "Don Semáforo nos enseña",
                        quote = "\"Rojo para parar, verde para cruzar de la mano de mamá.\"",
                        onRead = { onOpenStory(null) },
                    )
                    is UiState.Success -> {
                        val story = s.data.stories.firstOrNull()
                        StoryOfTheDayCard(
                            title = story?.title ?: "Don Semáforo nos enseña",
                            quote = story?.description?.take(120) ?: "\"Rojo para parar, verde para cruzar de la mano de mamá.\"",
                            onRead = { onOpenStory(story) },
                        )
                    }
                }

                // Dibujos de Seguridad
                SectionTitle("🎬 Dibujos de Seguridad")
                val cartoon = (state as? UiState.Success)?.data?.cartoons?.firstOrNull()
                VideoPlaceholderComponent(
                    source = cartoon?.let { VideoSource.Url(it.mediaUrl, it.thumbnailUrl) } ?: VideoSource.None,
                    title = cartoon?.title ?: "Las Aventuras del Casco Mágico",
                    onPlayClick = { onOpenCartoon(cartoon) },
                )

                // Juegos
                SectionTitle("🎮 ¡A Jugar y Aprender!")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GameTile("Adivina la Señal", ChiaGreen, Modifier.weight(1f), onOpenQuiz)
                    GameTile("Cruza Seguro", ChiaPink, Modifier.weight(1f), onOpenQuiz)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = ChiaNavy)
}

@Composable
private fun StoryOfTheDayCard(title: String, quote: String, onRead: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = ChiaSky),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("CUENTO DEL DÍA", style = MaterialTheme.typography.labelLarge, color = ChiaYellow)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Semáforo decorativo
                Column(
                    modifier = Modifier.clip(MaterialTheme.shapes.small).background(White).padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(Modifier.size(12.dp).clip(CircleShape).background(ChiaPink))
                    Box(Modifier.size(12.dp).clip(CircleShape).background(ChiaYellow))
                    Box(Modifier.size(12.dp).clip(CircleShape).background(ChiaGreen))
                }
                Spacer(Modifier.size(16.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = White)
                    Text(quote, style = MaterialTheme.typography.bodySmall, color = White.copy(alpha = 0.9f))
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onRead,
                colors = ButtonDefaults.buttonColors(containerColor = ChiaYellow, contentColor = ChiaNavy),
                shape = MaterialTheme.shapes.small,
            ) {
                Text("¡Leer Cuento! 📖", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun GameTile(title: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(150.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = color),
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(White), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = color)
            }
            Text(title, style = MaterialTheme.typography.titleMedium, color = White)
        }
    }
}
