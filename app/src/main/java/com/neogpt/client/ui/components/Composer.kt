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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.neogpt.client.domain.model.Attachment
import com.neogpt.client.ui.theme.ComposerShape
import com.neogpt.client.ui.theme.NeoTheme
import androidx.compose.ui.text.TextStyle

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
    placeholder: String = "Ask anything",
    autoFocus: Boolean = false,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val canSend = value.isNotBlank() || attachments.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(ComposerShape)
            .background(NeoTheme.colors.composerField)
            .padding(horizontal = 8.dp, vertical = 8.dp),
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
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeoIconButton(
                icon = Icons.Rounded.Add,
                contentDescription = "Add photos and files",
                onClick = onAddAttachment,
                tint = MaterialTheme.colorScheme.onSurface,
                containerSize = 36.dp,
                iconSize = 22.dp,
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.bodyLarge.merge(
                        TextStyle(color = MaterialTheme.colorScheme.onSurface)
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 36.dp, max = 180.dp),
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
                streaming -> NeoIconButton(
                    icon = Icons.Rounded.Stop,
                    contentDescription = "Stop generating",
                    onClick = onStop,
                    tint = NeoTheme.colors.onSend,
                    background = NeoTheme.colors.sendEnabled,
                    containerSize = 36.dp,
                    iconSize = 20.dp,
                )

                canSend -> NeoIconButton(
                    icon = Icons.Rounded.ArrowUpward,
                    contentDescription = "Send",
                    onClick = {
                        onSend()
                        keyboard?.hide()
                    },
                    tint = NeoTheme.colors.onSend,
                    background = NeoTheme.colors.sendEnabled,
                    containerSize = 36.dp,
                    iconSize = 20.dp,
                )

                else -> NeoIconButton(
                    icon = Icons.Rounded.Mic,
                    contentDescription = "Voice input",
                    onClick = onVoiceInput,
                    tint = MaterialTheme.colorScheme.onSurface,
                    containerSize = 36.dp,
                    iconSize = 22.dp,
                )
            }
        }
    }
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
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
