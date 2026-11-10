package com.movchia.app.ui.screens.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.movchia.app.data.demo.DemoData
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.ui.UiState
import com.movchia.app.ui.components.LargeAccessibleButton
import com.movchia.app.ui.components.MovChiaTopBar
import com.movchia.app.ui.theme.ChiaGreenDark
import com.movchia.app.ui.theme.ChiaNavy
import com.movchia.app.ui.theme.ChiaPink
import com.movchia.app.ui.theme.ChiaRed
import com.movchia.app.ui.theme.ChiaSky
import com.movchia.app.ui.theme.ChiaTeal
import com.movchia.app.ui.theme.ChiaYellow
import com.movchia.app.ui.theme.LocalHighContrast
import com.movchia.app.ui.theme.MovChiaTheme
import com.movchia.app.ui.theme.Slate800
import com.movchia.app.ui.theme.White

/**
 * Pantalla de quiz reutilizable. El grupo de edad del ViewModel decide el tono:
 * - Niños ("Adivina la Señal"): colores vivos, emojis y puntos ⭐.
 * - Jóvenes y Adultos ("Quiz de Tránsito"): sobrio e institucional.
 * - Adultos Mayores: hereda el alto contraste y los botones grandes del tema.
 *
 * La retroalimentación y el botón "Siguiente" van en la barra inferior para
 * que siempre queden visibles, aunque las opciones ocupen toda la pantalla.
 */
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val totalPoints by viewModel.totalPoints.collectAsStateWithLifecycle()
    val isKids = viewModel.group == AgeGroup.KIDS

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MovChiaTopBar(title = if (isKids) "Adivina la Señal" else "Quiz de Tránsito", onBack = onBack)
        },
        bottomBar = {
            val progress = (state as? UiState.Success)?.data
            if (progress != null && progress.questions.isNotEmpty()) {
                QuizBottomBar(
                    progress = progress,
                    isKids = isKids,
                    onNext = viewModel::next,
                    onRestart = viewModel::restart,
                    onBack = onBack,
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                is UiState.Error -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        s.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = viewModel::load) { Text("Reintentar") }
                }

                is UiState.Success -> when {
                    s.data.questions.isEmpty() -> Text(
                        text = "Aún no hay preguntas para tu grupo de edad. ¡Pronto habrá más!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    )
                    s.data.finished -> ResultsContent(s.data, isKids, totalPoints)
                    else -> QuestionContent(s.data, isKids, onSelect = viewModel::selectOption)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Pregunta en curso
// ---------------------------------------------------------------------------

@Composable
private fun QuestionContent(
    progress: QuizProgress,
    isKids: Boolean,
    onSelect: (Int) -> Unit,
) {
    val question = progress.current ?: return
    val total = progress.questions.size
    val answeredCount = progress.currentIndex + if (progress.answered) 1 else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Pregunta ${progress.currentIndex + 1} de $total",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ScoreChip(
                text = if (isKids) "⭐ ${progress.score} pts" else "Aciertos: ${progress.correctAnswers}",
                isKids = isKids,
            )
        }

        LinearProgressIndicator(
            progress = { answeredCount / total.toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = if (isKids) ChiaTeal else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        if (question.imageUrl.isNotBlank()) {
            AsyncImage(
                model = question.imageUrl,
                contentDescription = "Señal de tránsito de la pregunta",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(MaterialTheme.shapes.medium),
            )
        }

        val questionContainer = if (isKids) ChiaSky else MaterialTheme.colorScheme.primary
        val questionContent = if (isKids) White else MaterialTheme.colorScheme.onPrimary
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = questionContainer),
        ) {
            Text(
                text = question.question,
                style = if (isKids) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                color = questionContent,
                modifier = Modifier.fillMaxWidth().padding(20.dp),
            )
        }

        question.options.forEachIndexed { index, option ->
            OptionRow(
                index = index,
                text = option,
                state = optionState(progress, index),
                isKids = isKids,
                enabled = !progress.answered,
                onClick = { onSelect(index) },
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ScoreChip(text: String, isKids: Boolean) {
    val container = if (isKids) ChiaYellow else MaterialTheme.colorScheme.primaryContainer
    val content = if (isKids) ChiaNavy else MaterialTheme.colorScheme.onPrimaryContainer
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(container)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = content)
    }
}

private enum class OptionState { IDLE, CORRECT, WRONG, DIMMED }

private fun optionState(progress: QuizProgress, index: Int): OptionState {
    val selected = progress.selectedOption ?: return OptionState.IDLE
    return when (index) {
        progress.current?.correctIndex -> OptionState.CORRECT
        selected -> OptionState.WRONG
        else -> OptionState.DIMMED
    }
}

/** Colores de las letras A, B, C, D en modo Niños. */
private val kidsLetterColors = listOf(ChiaTeal, ChiaPink, ChiaYellow, ChiaSky)

@Composable
private fun OptionRow(
    index: Int,
    text: String,
    state: OptionState,
    isKids: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val container = when (state) {
        OptionState.CORRECT -> ChiaGreenDark
        OptionState.WRONG -> ChiaRed
        else -> scheme.surface
    }
    val content = when (state) {
        OptionState.CORRECT, OptionState.WRONG -> White
        else -> scheme.onSurface
    }
    val border = when (state) {
        OptionState.CORRECT -> ChiaGreenDark
        OptionState.WRONG -> ChiaRed
        OptionState.IDLE -> if (isKids) kidsLetterColors[index % kidsLetterColors.size] else scheme.outline
        OptionState.DIMMED -> scheme.outlineVariant
    }
    val letterBackground = when {
        state == OptionState.CORRECT || state == OptionState.WRONG -> White.copy(alpha = 0.25f)
        isKids -> kidsLetterColors[index % kidsLetterColors.size]
        else -> scheme.primaryContainer
    }
    val letterColor = when {
        state == OptionState.CORRECT || state == OptionState.WRONG -> White
        isKids -> if (letterBackground == ChiaYellow) ChiaNavy else White
        else -> scheme.onPrimaryContainer
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        color = container,
        contentColor = content,
        border = BorderStroke(2.dp, border),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (isKids) 64.dp else 56.dp)
            .alpha(if (state == OptionState.DIMMED) 0.55f else 1f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(letterBackground),
                contentAlignment = Alignment.Center,
            ) {
                when (state) {
                    OptionState.CORRECT -> Icon(Icons.Filled.Check, contentDescription = "Correcta", tint = White)
                    OptionState.WRONG -> Icon(Icons.Filled.Close, contentDescription = "Incorrecta", tint = White)
                    else -> Text(
                        text = ('A' + index).toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = letterColor,
                    )
                }
            }
            Text(
                text = text,
                style = if (isKids) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Barra inferior: retroalimentación + Siguiente / Jugar de nuevo
// ---------------------------------------------------------------------------

@Composable
private fun QuizBottomBar(
    progress: QuizProgress,
    isKids: Boolean,
    onNext: () -> Unit,
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp, shadowElevation = 8.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            if (progress.finished) {
                LargeAccessibleButton(
                    text = if (isKids) "¡Jugar de nuevo!" else "Jugar de nuevo",
                    onClick = onRestart,
                    leadingIcon = Icons.Filled.Refresh,
                )
                TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text("Volver al inicio", style = MaterialTheme.typography.labelLarge)
                }
            } else {
                AnimatedVisibility(visible = progress.answered) {
                    Column {
                        FeedbackCard(progress, isKids)
                        Spacer(Modifier.height(12.dp))
                    }
                }
                val nextLabel = when {
                    progress.isLastQuestion && isKids -> "¡Ver mi puntaje!"
                    progress.isLastQuestion -> "Ver resultados"
                    isKids -> "¡Siguiente!"
                    else -> "Siguiente"
                }
                LargeAccessibleButton(
                    text = nextLabel,
                    onClick = onNext,
                    enabled = progress.answered,
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                )
            }
        }
    }
}

@Composable
private fun FeedbackCard(progress: QuizProgress, isKids: Boolean) {
    val correct = progress.lastAnswerCorrect ?: return
    val question = progress.current ?: return
    val highContrast = LocalHighContrast.current

    val container = when {
        correct && highContrast -> ChiaGreenDark
        correct -> Color(0xFFD1FAE5)
        highContrast -> ChiaRed
        else -> Color(0xFFFEE2E2)
    }
    val content = if (highContrast) White else Slate800
    val accent = when {
        highContrast -> White
        correct -> ChiaGreenDark
        else -> ChiaRed
    }
    val headline = when {
        correct && isKids -> "¡Correcto! 🎉 +${question.points} puntos"
        correct -> "¡Correcto! +${question.points} puntos"
        isKids -> "¡Uy, casi! 😅"
        else -> "Respuesta incorrecta"
    }

    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = container),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = if (correct) Icons.Filled.CheckCircle else Icons.Filled.Info,
                    contentDescription = null,
                    tint = accent,
                )
                Text(headline, style = MaterialTheme.typography.titleMedium, color = content)
            }
            if (!correct) {
                Text(
                    text = "La respuesta correcta era: ${question.options.getOrNull(question.correctIndex).orEmpty()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = content,
                )
            }
            if (question.explanation.isNotBlank()) {
                Text(question.explanation, style = MaterialTheme.typography.bodyMedium, color = content)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Resultados
// ---------------------------------------------------------------------------

@Composable
private fun ResultsContent(progress: QuizProgress, isKids: Boolean, totalPoints: Int) {
    val stars = when {
        progress.accuracy >= 0.8f -> 3
        progress.accuracy >= 0.5f -> 2
        progress.accuracy > 0f -> 1
        else -> 0
    }
    val headline = when (stars) {
        3 -> if (isKids) "¡Excelente! 🏆" else "¡Excelente!"
        2 -> "¡Muy bien!"
        else -> "¡Sigue practicando!"
    }
    val message = when (stars) {
        3 -> "Conoces muy bien las normas de tránsito."
        2 -> "Vas por buen camino. Repasa las preguntas que fallaste."
        else -> "Revisa el Manual de Tránsito y vuelve a intentarlo."
    }
    val cardContainer = if (isKids) ChiaSky else MaterialTheme.colorScheme.primary
    val cardContent = if (isKids) White else MaterialTheme.colorScheme.onPrimary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) { i ->
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = if (i < stars) ChiaYellow else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(if (isKids) 64.dp else 48.dp),
                )
            }
        }
        Text(
            text = headline,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = cardContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = if (isKids) "Ganaste" else "Puntaje de la ronda",
                    style = MaterialTheme.typography.labelLarge,
                    color = cardContent.copy(alpha = 0.85f),
                )
                Text(
                    text = if (isKids) "${progress.score} ⭐" else "${progress.score} / ${progress.maxScore}",
                    style = MaterialTheme.typography.displaySmall,
                    color = cardContent,
                )
                Text(
                    text = "Acertaste ${progress.correctAnswers} de ${progress.questions.size} preguntas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cardContent,
                )
            }
        }
        Text(
            text = if (isKids) "Tus puntos en total: $totalPoints ⭐" else "Puntos acumulados: $totalPoints",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun QuizKidsQuestionPreview() {
    MovChiaTheme(ageGroup = AgeGroup.KIDS) {
        val progress = QuizProgress(DemoData.quizQuestions.take(3)).answer(0)
        Column {
            QuestionContent(progress, isKids = true, onSelect = {})
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun QuizAdultsResultsPreview() {
    MovChiaTheme(ageGroup = AgeGroup.ADULTS) {
        val progress = QuizProgress(DemoData.quizQuestions, currentIndex = 4, score = 35, correctAnswers = 3)
        ResultsContent(progress, isKids = false, totalPoints = 120)
    }
}
