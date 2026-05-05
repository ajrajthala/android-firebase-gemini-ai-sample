package com.aj.geminiproj.features.chat.presentation.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aj.geminiproj.core.model.ChatMessage
import com.aj.geminiproj.core.model.MessageRole
import com.aj.geminiproj.core.model.MessageStatus

@Composable
fun MessageItem(message: ChatMessage, modifier: Modifier = Modifier, isTablet: Boolean = false) {
    val isUser = message.role == MessageRole.USER

    // Responsive values
    val maxBubbleWidth = if (isTablet) 500.dp else 300.dp
    val padding = if (isTablet) 16.dp else 12.dp
    val progressIndicatorSize = if (isTablet) 20.dp else 16.dp
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        message.imageUri?.let { Log.d("MessageItem", "Loading image: $it") }
        Box(
            modifier = Modifier
                .widthIn(max = maxBubbleWidth)
                .background(
                    color = if (isUser) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 16.dp
                    )
                )
                .padding(padding)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                message.imageUri?.let { uri ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Message Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 240.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                if (message.content.isNotBlank()) {
                    Text(
                        text = message.content,
                        style = if (isTablet) {
                            MaterialTheme.typography.bodyLarge
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                        color = if (isUser) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                if (message.status == MessageStatus.STREAMING) {
                    Spacer(modifier = Modifier.height(4.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(progressIndicatorSize),
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun MessageItemPreview() {
    MessageItem(
        message = ChatMessage(
            id = "1",
            content = "Hello, how can I assist you today?",
            role = MessageRole.ASSISTANT,
            status = MessageStatus.SENT,
            timeStamp = System.currentTimeMillis()
        ),
        modifier = Modifier.padding(8.dp)
    )
}