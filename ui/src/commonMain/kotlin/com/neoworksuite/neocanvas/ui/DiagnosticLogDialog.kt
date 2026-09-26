package com.neoworksuite.neocanvas.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.sp

@Composable
internal fun DiagnosticLogDialog(state: EditorState) {
    val clipboard = LocalClipboardManager.current
    var text by remember(state.diagnosticsVisible) { mutableStateOf(state.diagnosticText) }
    AlertDialog(
        onDismissRequest = { state.diagnosticsVisible = false },
        containerColor = NeoCanvasColors.panel,
        title = { Text("NeoCanvas diagnostics", color = NeoCanvasColors.paper) },
        text = {
            SelectionContainer {
                Text(
                    text.ifBlank { "No diagnostic events recorded." },
                    color = NeoCanvasColors.muted,
                    fontSize = 11.sp,
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                clipboard.setText(AnnotatedString(text))
                state.statusMessage = "Diagnostic log copied"
            }) { Text("Copy log", color = NeoCanvasColors.accent) }
        },
        dismissButton = {
            TextButton(onClick = {
                state.clearDiagnostics()
                text = state.diagnosticText
            }) { Text("Clear", color = NeoCanvasColors.muted) }
            TextButton(onClick = { state.diagnosticsVisible = false }) {
                Text("Done", color = NeoCanvasColors.accent)
            }
        },
    )
}
