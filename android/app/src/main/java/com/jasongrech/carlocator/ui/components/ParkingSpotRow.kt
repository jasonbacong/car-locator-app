package com.jasongrech.carlocator.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jasongrech.carlocator.data.ParkingSpot
import com.jasongrech.carlocator.ui.theme.Mono
import com.jasongrech.carlocator.ui.theme.TextPrimary
import com.jasongrech.carlocator.ui.theme.TextSecondary
import com.jasongrech.carlocator.util.NavIntents
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ParkingSpotRow(
    spot: ParkingSpot,
    onDelete: () -> Unit,
    onLocate: () -> Unit,
    onSaveNote: (String?) -> Unit,
    onScheduleReminder: (Long) -> Unit
) {
    val context = LocalContext.current
    val dateText = remember(spot.timestamp) {
        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(spot.timestamp))
    }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }

    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ParkingBadge(size = 40.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    spot.address ?: "%.5f, %.5f".format(spot.lat, spot.lng),
                    style = if (spot.address != null)
                        MaterialTheme.typography.titleMedium
                    else
                        MaterialTheme.typography.titleMedium.copy(fontFamily = Mono, fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(dateText, style = MaterialTheme.typography.bodySmall.copy(fontFamily = Mono), color = TextSecondary)
            }
        }

        if (spot.note != null) {
            Text(
                "Note: ${spot.note}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chipPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)

            TactileButton(
                text = "Maps",
                onClick = { context.startActivity(NavIntents.resolvedMapsIntent(context, spot.lat, spot.lng)) },
                variant = ButtonVariant.Secondary,
                contentPadding = chipPadding
            )
            TactileButton(
                text = "Waze",
                onClick = { context.startActivity(NavIntents.resolvedWazeIntent(context, spot.lat, spot.lng)) },
                variant = ButtonVariant.Secondary,
                contentPadding = chipPadding
            )
            TactileButton(
                text = "Locate",
                onClick = onLocate,
                variant = ButtonVariant.Secondary,
                contentPadding = chipPadding
            )
            TactileButton(
                text = "Share",
                onClick = {
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            android.content.Intent.EXTRA_TEXT,
                            "My car is parked here: https://maps.google.com/?q=${spot.lat},${spot.lng}"
                        )
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share parking spot"))
                },
                variant = ButtonVariant.Secondary,
                contentPadding = chipPadding
            )
            TactileButton(
                text = "Remind",
                onClick = { showReminderDialog = true },
                variant = ButtonVariant.Secondary,
                contentPadding = chipPadding
            )
            TactileButton(
                text = if (spot.note == null) "Add note" else "Edit note",
                onClick = { showNoteDialog = true },
                variant = ButtonVariant.Secondary,
                contentPadding = chipPadding
            )
            TactileButton(
                text = "Delete",
                onClick = onDelete,
                variant = ButtonVariant.Danger,
                contentPadding = chipPadding
            )
        }
    }

    if (showNoteDialog) {
        NoteDialog(
            initialNote = spot.note,
            onDismiss = { showNoteDialog = false },
            onSave = {
                onSaveNote(it)
                showNoteDialog = false
            }
        )
    }

    if (showReminderDialog) {
        ReminderDialog(
            onDismiss = { showReminderDialog = false },
            onSchedule = {
                onScheduleReminder(it)
                showReminderDialog = false
            }
        )
    }
}
