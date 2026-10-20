package com.movchia.app.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movchia.app.R
import com.movchia.app.ui.theme.ChiaBlue
import com.movchia.app.ui.theme.ChiaPink
import com.movchia.app.ui.theme.ChiaTeal
import com.movchia.app.ui.theme.ChiaYellow
import com.movchia.app.ui.theme.White
import com.movchia.app.utils.Constants
import kotlinx.coroutines.delay

/**
 * Pantalla 01_SplashScreen del Figma. Muestra la marca y, tras una breve
 * espera, delega la navegación al llamador.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(Constants.SPLASH_DURATION_MS)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChiaBlue),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Los tres círculos de la marca "Vías Seguras" (rosado, turquesa, amarillo).
            Box {
                Box(Modifier.size(56.dp).clip(CircleShape).background(ChiaPink))
                Box(Modifier.padding(start = 36.dp).size(56.dp).clip(CircleShape).background(ChiaTeal))
                Box(Modifier.padding(start = 72.dp).size(56.dp).clip(CircleShape).background(ChiaYellow))
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = White,
            )
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = White.copy(alpha = 0.85f),
            )
        }
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .size(28.dp),
            color = ChiaYellow,
            strokeWidth = 3.dp,
        )
    }
}
