package com.aj.geminiproj.features.chat.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ToolStepsSummary(toolSteps: List<String>, modifier: Modifier = Modifier) {
    if (toolSteps.isEmpty()) return
    Text(
        text = "Tools used: ${toolSteps.joinToString(" • ")}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline,
        modifier = modifier.padding(
            start = 12.dp,
            end = 48.dp,
            bottom = 4.dp
        )
    )
}