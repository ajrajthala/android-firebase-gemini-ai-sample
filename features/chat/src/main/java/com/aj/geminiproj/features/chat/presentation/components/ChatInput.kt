package com.aj.geminiproj.features.chat.presentation.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean,
    onGalleryClicked: () -> Unit,
    onCameraClicked: () -> Unit,
    selectedImageUri: Uri?,
    onRemoveImage: () -> Unit,
    modifier: Modifier = Modifier,
    isTablet: Boolean = false,
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val isMenuExpanded = remember { mutableStateOf(false) }
    val rotationAngle = animateFloatAsState(if (isMenuExpanded.value) 45f else 0f)

    val rowPadding = remember(isTablet) { if (isTablet) 12.dp else 8.dp }
    val textFieldMinHeight = remember(isTablet) { if (isTablet) 64.dp else 56.dp }
    val textFieldMaxHeight = remember(isTablet) { if (isTablet) 160.dp else 120.dp }
    val sendIconSize = remember(isTablet) { if (isTablet) 64.dp else 56.dp }

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(rowPadding)) {

            if (selectedImageUri != null) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .size(96.dp)
                ) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(24.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                            .clickable(onClick = onRemoveImage),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Image",
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            //INPUT

            // Animated menu row
            AnimatedVisibility(
                visible = isMenuExpanded.value,
                enter = expandVertically(expandFrom = Alignment.Bottom),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Gallery Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onGalleryClicked()
                                isMenuExpanded.value = false
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Add image from Gallery",
                            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.5f
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Gallery",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.5f
                            ),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Camera Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCameraClicked()
                                isMenuExpanded.value = false
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Take a photo with Camera",
                            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.5f
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Camera",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.5f
                            ),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Input row with plus button, text field, and send button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Plus/Close Icon Button
                IconButton(
                    onClick = { isMenuExpanded.value = !isMenuExpanded.value },
                    enabled = enabled,
                    modifier = Modifier
                        .then(
                            if (isMenuExpanded.value) Modifier else Modifier
                        )
                ) {
                    Icon(
                        imageVector = if (isMenuExpanded.value) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (isMenuExpanded.value) "Close menu" else "Add media",
                        tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.5f
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                }

                TextField(
                    value = text,

                    onValueChange = {
                        onTextChange(it)
                        isMenuExpanded.value = false
                    },
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

                Spacer(modifier = Modifier.width(8.dp))
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
}

@Preview
@Composable
fun ChatInputPreview() {
    ChatInput(
        text = "",
        onTextChange = {},
        onSendClick = {},
        onGalleryClicked = {},
        onCameraClicked = {},
        selectedImageUri = null,
        onRemoveImage = {},
        enabled = true
    )
}