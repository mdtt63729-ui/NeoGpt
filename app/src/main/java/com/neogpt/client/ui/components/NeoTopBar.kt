package com.neogpt.client.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.neogpt.client.R
import com.neogpt.client.ui.theme.NeoTheme

@Composable
fun NeoTopBar(
    modelLabel: String,
    onMenuClick: () -> Unit,
    onModelClick: () -> Unit,
    onNewChatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TopBarCircle(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Rounded.DragHandle,
                contentDescription = "Open sidebar",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp),
                autoMirrored = true,
            )
        }

        Spacer(Modifier.width(8.dp))

        ModelPill(label = modelLabel, onClick = onModelClick)

        Spacer(Modifier.weight(1f))

        TopBarCircle(onClick = onNewChatClick) {
            Image(
                painter = painterResource(id = R.drawable.ic_openai_mark),
                contentDescription = "New chat",
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun TopBarCircle(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .shadow(elevation = 2.dp, shape = CircleShape, clip = false)
            .clip(CircleShape)
            .background(NeoTheme.colors.topBarCircle)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
