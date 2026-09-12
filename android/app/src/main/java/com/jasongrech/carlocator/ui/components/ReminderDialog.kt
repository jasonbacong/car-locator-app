package com.jasongrech.carlocator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.jasongrech.carlocator.ui.theme.TextPrimary

private val DURATIONS = listOf(
    "30 minutes" to 30L,
    "1 hour" to 60L,
    "2 hours" to 120L,
    "4 hours" to 240L
)

/** Schedules a parking-meter style reminder relative to now, via WorkManager. */
@Composable
fun ReminderDialog(onDismiss: () -> Unit, onSchedule: (minutes: Long) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remind me in…", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DURATIONS.forEach { (label, minutes) ->
                    TactileButton(
                        text = label,
                        onClick = { onSchedule(minutes) },
                        variant = ButtonVariant.Secondary,
                        fullWidth = true
                    )
                }
            }
        },
        confirmButton = {
            TactileButton(text = "Cancel", onClick = onDismiss, variant = ButtonVariant.Secondary)
        }
    )
}
