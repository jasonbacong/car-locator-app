package com.jasongrech.carlocator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jasongrech.carlocator.ui.theme.Accent
import com.jasongrech.carlocator.ui.theme.TextPrimary
import com.jasongrech.carlocator.ui.theme.TextSecondary

private data class PermissionExplainer(val name: String, val why: String)

private val PERMISSIONS = listOf(
    PermissionExplainer(
        "Location — \"Allow all the time\"",
        "So it can grab a GPS fix and save your spot the moment your phone disconnects from the car, even if the app isn't open."
    ),
    PermissionExplainer(
        "Bluetooth",
        "To notice exactly when your phone disconnects from your car. That disconnect is the whole trigger for this app."
    ),
    PermissionExplainer(
        "Notifications",
        "So it can tell you a spot was saved, with one-tap buttons to navigate back or undo it."
    )
)

/**
 * Shown automatically on first launch, and reachable again later from the Status
 * card. Explains what the app actually does before asking for anything, and why
 * each permission is needed rather than just what to tap.
 */
@Composable
fun OnboardingDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How Car Locator works", color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Car Locator watches for your phone disconnecting from your car's " +
                        "Bluetooth. Unless you're near home or another safe zone you've " +
                        "saved, it saves your GPS location as a parking spot and notifies " +
                        "you with one-tap buttons back into Maps or Waze.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionEyebrow("Permissions it needs, and why")
                    PERMISSIONS.forEach { permission ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                permission.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Accent
                            )
                            Text(
                                permission.why,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SectionEyebrow("How to turn them on")
                    Text(
                        "Tap \"Grant core permissions\" on the main screen, then work " +
                            "through the system prompts. For location, Android will likely " +
                            "send you into Settings and ask you to pick \"Allow all the " +
                            "time\" there rather than \"While using the app\". Also worth " +
                            "turning off battery optimization and \"Remove permissions if " +
                            "unused\" for this app under Settings, so Android doesn't " +
                            "quietly undo any of this later.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        },
        confirmButton = {
            TactileButton(text = "Got it", onClick = onDismiss)
        }
    )
}
