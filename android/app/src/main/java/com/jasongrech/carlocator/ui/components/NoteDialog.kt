package com.jasongrech.carlocator.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import com.jasongrech.carlocator.ui.theme.Accent
import com.jasongrech.carlocator.ui.theme.TextMuted
import com.jasongrech.carlocator.ui.theme.TextPrimary
import com.jasongrech.carlocator.ui.theme.TextSecondary

/** Free-text note on a spot — mainly for the floor/section a garage's GPS fix can't capture. */
@Composable
fun NoteDialog(initialNote: String?, onDismiss: () -> Unit, onSave: (String?) -> Unit) {
    var text by remember { mutableStateOf(initialNote ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Note", color = TextPrimary) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("e.g. Level 3, Section B", color = TextMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = TextSecondary,
                    cursorColor = Accent
                )
            )
        },
        confirmButton = {
            TactileButton(
                text = "Save",
                onClick = { onSave(text.trim().ifBlank { null }) }
            )
        },
        dismissButton = {
            TactileButton(text = "Cancel", onClick = onDismiss, variant = ButtonVariant.Secondary)
        }
    )
}
