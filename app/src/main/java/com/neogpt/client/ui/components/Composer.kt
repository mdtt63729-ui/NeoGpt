package com.neogpt.client.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.neogpt.client.domain.model.Attachment
import com.neogpt.client.ui.theme.NeoTheme

private val ComposerRadius = RoundedCornerShape(24.dp)

@Composable
fun Composer(
    value: String,
    onValueChange: (String) -> Unit,
    attachments: List<Attachment>,
    onAddAttachment: () -> Unit,
    onRemoveAttachment: (Attachment) -> Unit,
    onSend: () -> Unit,
    onVoiceInput: () -> Unit,
    streaming: Boolean,
    onStop: () -> Unit,
    sendOnEnter: Boolean,
    modifier: Modifier = Modifier,
    placeholder: String = "Ask ChatGPT",
    autoFocus: Boolean = false,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val canSend = value.isNotBlank() || attachments.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = ComposerRadius, clip = false)
            .clip(ComposerRadius)
            .background(NeoTheme.colors.composerBackground)
            .border(1.dp, NeoTheme.colors.composerBorder, ComposerRadius)
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
    ) {
        if (attachments.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(attachments, key = { it.uri }) { attachment ->
                    AttachmentChip(
                        attachment = attachment,
                        onRemove = { onRemoveAttachment(attachment) },
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeoIconButton(
                icon = Icons.Outlined.Add,
                contentDescription = "Add photos and files",
                onClick = onAddAttachment,
                tint = MaterialTheme.colorScheme.onSurface,
                containerSize = 32.dp,
                iconSize = 24.dp,
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = NeoTheme.colors.placeholder,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.bodyLarge.merge(
                        TextStyle(color = MaterialTheme.colorScheme.onSurface)
                    ),
                    cursorBrush = SolidColor(NeoTheme.colors.voiceAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 32.dp, max = 180.dp),
                    maxLines = 8,
                    keyboardOptions = KeyboardOptions(
                        imeAction = if (sendOnEnter) ImeAction.Send else ImeAction.Default,
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (canSend && !streaming) {
                                onSend()
                                keyboard?.hide()
                            }
                        },
                    ),
                )
            }

            when {
                streaming -> CircleAction(
                    icon = Icons.Rounded.Stop,
                    contentDescription = "Stop generating",
                    onClick = onStop,
                    container = NeoTheme.colors.sendEnabled,
                    tint = NeoTheme.colors.onSend,
                )

                canSend -> CircleAction(
                    icon = Icons.Rounded.ArrowUpward,
                    contentDescription = "Send",
                    onClick = {
                        onSend()
                        keyboard?.hide()
                    },
                    container = NeoTheme.colors.sendEnabled,
                    tint = NeoTheme.colors.onSend,
                )

                else -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    NeoIconButton(
                        icon = Icons.Outlined.Mic,
                        contentDescription = "Voice input",
                        onClick = onVoiceInput,
                        tint = MaterialTheme.colorScheme.onSurface,
                        containerSize = 36.dp,
                        iconSize = 22.dp,
                    )
                    CircleAction(
                        icon = Icons.Rounded.GraphicEq,
                        contentDescription = "Voice mode",
                        onClick = onVoiceInput,
                        container = NeoTheme.colors.voiceAccent,
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    container: Color,
    tint: Color,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(container)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** Small centered line shown under the composer, matching the original app. */
@Composable
fun ComposerDisclaimer(modifier: Modifier = Modifier) {
    Text(
        text = "ChatGPT can make mistakes, so check anything important.",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 24.dp, end = 24.dp),
    )
}

@Composable
private fun AttachmentChip(
    attachment: Attachment,
    onRemove: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(width = 132.dp, height = 72.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, NeoTheme.colors.chipBorder, RoundedCornerShape(14.dp)),
    ) {
        if (attachment.isImage) {
            AsyncImage(
                model = attachment.uri,
                contentDescription = attachment.name,
                modifier = Modifier
                    .size(width = 132.dp, height = 72.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = attachment.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(4.dp)
                .align(Alignment.TopEnd)
                .size(22.dp)
                .clip(CircleShape)
                .background(NeoTheme.colors.scrim)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Remove attachment",
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
