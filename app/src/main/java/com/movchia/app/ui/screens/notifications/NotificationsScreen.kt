package com.movchia.app.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.ui.components.MovChiaTopBar
import com.movchia.app.ui.theme.ChiaBlue
import com.movchia.app.ui.theme.ChiaGreenDark
import com.movchia.app.ui.theme.ChiaRed
import com.movchia.app.ui.theme.ChiaSkyBackground
import com.movchia.app.ui.theme.ChiaTeal
import com.movchia.app.ui.theme.ChiaYellow
import com.movchia.app.ui.theme.MovChiaTheme
import com.movchia.app.ui.theme.White

/** Categoría visual de un aviso. Define icono y color del distintivo. */
enum class NotificationKind(val icon: ImageVector, val color: Color, val label: String) {
    ALERT(Icons.Filled.Warning, ChiaRed, "Alerta vial"),
    WORKS(Icons.Filled.Build, ChiaYellow, "Obras y cierres"),
    EVENT(Icons.Filled.Star, ChiaTeal, "Evento"),
    INFO(Icons.Filled.Info, ChiaBlue, "Información"),
    PLACE(Icons.Filled.Place, ChiaGreenDark, "Sedes"),
}

/** Aviso mostrado en el centro de notificaciones (por ahora solo de muestra). */
data class AppNotification(
    val id: String,
    val kind: NotificationKind,
    val title: String,
    val body: String,
    val timeLabel: String,
    val unread: Boolean = false,
)

/**
 * Datos de muestra para diseñar la pantalla. Cuando el centro sea funcional
 * saldrán de una colección `notifications` administrada desde el CMS.
 */
val sampleNotifications: List<AppNotification> = listOf(
    AppNotification(
        id = "n1",
        kind = NotificationKind.ALERT,
        title = "Cierre parcial en la Avenida Pradilla",
        body = "Entre la Calle 10 y la Calle 12 por obras de repavimentación. Use la Carrera 9 como desvío.",
        timeLabel = "Hace 2 h",
        unread = true,
    ),
    AppNotification(
        id = "n2",
        kind = NotificationKind.INFO,
        title = "Nuevo aviso: traspaso a persona indeterminada",
        body = "2.026 vehículos matriculados en Chía deben regularizar su traspaso en un plazo de seis meses.",
        timeLabel = "Hoy, 8:30 a.m.",
        unread = true,
    ),
    AppNotification(
        id = "n3",
        kind = NotificationKind.EVENT,
        title = "Ciclovía dominical este fin de semana",
        body = "Domingo de 7:00 a.m. a 1:00 p.m. en la Avenida Chilacos. ¡Lleve casco y chaleco!",
        timeLabel = "Ayer",
    ),
    AppNotification(
        id = "n4",
        kind = NotificationKind.WORKS,
        title = "Mantenimiento de semáforos en el centro",
        body = "Intermitencia en los cruces de la Calle 11 durante la madrugada del jueves.",
        timeLabel = "Ayer",
    ),
    AppNotification(
        id = "n5",
        kind = NotificationKind.PLACE,
        title = "Horario especial de la Secretaría de Movilidad",
        body = "El viernes festivo no habrá atención presencial. Los trámites en línea siguen disponibles.",
        timeLabel = "Hace 3 días",
    ),
)

/**
 * Centro de notificaciones (Jóvenes y Adultos). SOLO VISTA: muestra avisos
 * de ejemplo para validar el diseño; no consulta Firestore ni recibe push.
 */
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    notifications: List<AppNotification> = sampleNotifications,
) {
    val unread = notifications.count { it.unread }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MovChiaTopBar(title = "Notificaciones", onBack = onBack) {
                TextButton(onClick = { /* Pendiente: marcar todas como leídas */ }) {
                    Text("Marcar leídas", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
    ) { padding ->
        if (notifications.isEmpty()) {
            EmptyNotifications(Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                UnreadBanner(unread)
                Spacer(Modifier.height(4.dp))
            }
            items(notifications, key = { it.id }) { notification ->
                NotificationCard(notification)
            }
            item {
                Text(
                    text = "Vista previa: los avisos reales los publicará la Secretaría desde el panel de gestión.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun UnreadBanner(unread: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(ChiaSkyBackground)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Filled.Notifications, contentDescription = null, tint = ChiaBlue)
        Text(
            text = if (unread == 0) "Estás al día" else if (unread == 1) "1 aviso nuevo" else "$unread avisos nuevos",
            style = MaterialTheme.typography.labelLarge,
            color = ChiaBlue,
        )
    }
}

@Composable
private fun NotificationCard(notification: AppNotification) {
    Card(
        onClick = { /* Pendiente: abrir el contenido relacionado */ },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.unread) 2.dp else 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(notification.kind.color),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = notification.kind.icon,
                    contentDescription = notification.kind.label,
                    tint = if (notification.kind == NotificationKind.WORKS) MaterialTheme.colorScheme.onTertiary else White,
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = notification.kind.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = notification.kind.color,
                        fontWeight = FontWeight.Bold,
                    )
                    Text("·", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = notification.timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.weight(1f))
                    if (notification.unread) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(ChiaRed))
                    }
                }
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (notification.unread) FontWeight.Bold else FontWeight.SemiBold,
                )
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptyNotifications(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Filled.Notifications,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "No tienes avisos por ahora.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun NotificationsPreview() {
    MovChiaTheme(ageGroup = AgeGroup.ADULTS) {
        NotificationsScreen(onBack = {})
    }
}
