package com.neogpt.client.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neogpt.client.R
import com.neogpt.client.ui.theme.NeoTheme

/**
 * Welcome gate shown on first launch. There is no account server in this
 * build: "Log in" and "Sign up" only create a local display name, and
 * "Skip now" opens the app immediately without any account.
 */
@Composable
fun AuthScreen(
    onSkip: () -> Unit,
    onLocalProfile: (String) -> Unit,
) {
    var dialogTitle by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_openai_mark),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
            )

            Text(
                text = "Welcome to ChatGPT",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 22.dp),
            )

            Text(
                text = "Ask anything, plan a trip, write code, or study. Start without an account.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )

            Button(
                onClick = { dialogTitle = "Log in" },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 34.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
            ) {
                Text("Log in", style = MaterialTheme.typography.titleMedium)
            }

            OutlinedButton(
                onClick = { dialogTitle = "Sign up" },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
            ) {
                Text("Sign up", style = MaterialTheme.typography.titleMedium)
            }

            TextButton(
                onClick = onSkip,
                modifier = Modifier.padding(top = 14.dp),
            ) {
                Text(
                    text = "Skip now",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Text(
                text = "Skip now opens the app without an account. Chats stay on this device.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )

            Text(
                text = "Responses come from the OpenAI-compatible provider you configure in Settings. This build bundles no account server and no free API access.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 26.dp),
            )
        }
    }

    dialogTitle?.let { title ->
        LocalProfileDialog(
            title = title,
            onDismiss = { dialogTitle = null },
            onConfirm = { name ->
                dialogTitle = null
                onLocalProfile(name)
            },
        )
    }
}

@Composable
private fun LocalProfileDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = "There is no account server in this build. Your name is stored only on this device and is never uploaded.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim().ifBlank { "You" }) },
            ) { Text("Continue") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = NeoTheme.colors.sidebar,
    )
}
