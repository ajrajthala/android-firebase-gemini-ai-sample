package com.aj.geminiproj.features.chat.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    isTablet: Boolean = false,
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    val rowPadding = remember(isTablet){if (isTablet) 12.dp else 8.dp}
    val textFieldMinHeight = remember(isTablet){if (isTablet) 64.dp else 56.dp}
    val textFieldMaxHeight = remember(isTablet){if (isTablet) 160.dp else 120.dp}
    val sendIconSize = remember(isTablet){if (isTablet) 64.dp else 56.dp}

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(rowPadding),
            verticalAlignment = Alignment.Bottom
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = textFieldMinHeight, max = textFieldMaxHeight),
                placeholder = { Text("Type your message...") },
                enabled = enabled,
                textStyle = if (isTablet) {
                    MaterialTheme.typography.bodyLarge
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.surface,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.surface,
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            FilledIconButton(
                onClick = {
                    keyboardController?.hide()
                    onSendClick()
                },
                enabled = enabled && text.isNotBlank(),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                )
            }
        }
    }
}

@Preview
@Composable
fun ChatInputPreview() {
    ChatInput(
        text = "",
        onTextChange = {},
        onSendClick = {},
        enabled = true
    )
}