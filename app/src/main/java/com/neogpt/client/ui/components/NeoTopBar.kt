package com.neogpt.client.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NeoTopBar(
    modelLabel: String,
    onMenuClick: () -> Unit,
    onModelClick: () -> Unit,
    onNewChatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            NeoIconButton(
                icon = Icons.Rounded.Menu,
                contentDescription = "Open sidebar",
                onClick = onMenuClick,
                iconSize = 24.dp,
            )
            Box {
                ModelPill(label = modelLabel, onClick = onModelClick)
            }
            NeoIconButton(
                icon = Icons.Rounded.Add,
                contentDescription = "New chat",
                onClick = onNewChatClick,
                iconSize = 24.dp,
            )
        }
    }
}
