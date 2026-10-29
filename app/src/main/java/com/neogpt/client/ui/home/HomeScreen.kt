package com.neogpt.client.ui.home

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.neogpt.client.AppContainer
import com.neogpt.client.ui.ContainerViewModelFactory
import com.neogpt.client.ui.components.AttachmentSheet
import com.neogpt.client.ui.components.Composer
import com.neogpt.client.ui.components.NeoTopBar
import com.neogpt.client.ui.theme.NeoTheme
import com.neogpt.client.ui.theme.PillShape
import com.neogpt.client.util.AttachmentResolver
import kotlinx.coroutines.launch

private data class Suggestion(
    val label: String,
    val prompt: String,
    val icon: ImageVector,
)

private val suggestions = listOf(
    Suggestion("Create an image", "Create an image of a calm mountain lake at sunrise.", Icons.Rounded.Palette),
    Suggestion("Brainstorm", "Brainstorm ten names for a small coffee roastery.", Icons.Rounded.Lightbulb),
    Suggestion("Help me write", "Help me write a short, friendly email to reschedule a meeting.", Icons.Rounded.EditNote),
    Suggestion("Analyze data", "Explain how to analyze a CSV of monthly sales in a few steps.", Icons.Rounded.BarChart),
    Suggestion("Learn something", "Teach me the basics of how HTTPS certificates work.", Icons.Rounded.School),
    Suggestion("Code", "Write a Kotlin function that reverses a linked list.", Icons.Rounded.Code),
)

@Composable
fun HomeScreen(
    container: AppContainer,
    modelLabel: String,
    onOpenDrawer: () -> Unit,
    onOpenModelPicker: () -> Unit,
    onConversationCreated: (String) -> Unit,
    onOpenSettings: () -> Unit,
    sharedDraft: String? = null,
    onSharedDraftConsumed: () -> Unit = {},
) {
    val viewModel: HomeViewModel = viewModel(
        factory = ContainerViewModelFactory { HomeViewModel(container) },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showAttachSheet by remember { mutableStateOf(false) }

    LaunchedEffect(sharedDraft) {
        if (!sharedDraft.isNullOrBlank()) {
            viewModel.setDraft(sharedDraft)
            onSharedDraftConsumed()
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(4),
    ) { uris ->
        uris.take(4).forEach { uri ->
            viewModel.addAttachment(AttachmentResolver.fromUri(context, uri))
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        uris.take(4).forEach { uri ->
            viewModel.addAttachment(AttachmentResolver.fromUri(context, uri))
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                viewModel.setDraft(listOf(state.draft, spoken).filter { it.isNotBlank() }.joinToString(" "))
            }
        }
    }

    fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to type")
        }
        runCatching { voiceLauncher.launch(intent) }
            .onFailure { Toast.makeText(context, "No speech recognizer available", Toast.LENGTH_SHORT).show() }
    }

    fun submit() {
        if (state.draft.isBlank() && state.attachments.isEmpty()) return
        scope.launch {
            val id = viewModel.send()
            if (id != null) onConversationCreated(id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
    ) {
        NeoTopBar(
            modelLabel = modelLabel,
            onMenuClick = onOpenDrawer,
            onModelClick = onOpenModelPicker,
            onNewChatClick = viewModel::clearDraft,
        )

        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "What can I help with?",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(28.dp))

                Composer(
                    value = state.draft,
                    onValueChange = viewModel::setDraft,
                    attachments = state.attachments,
                    onAddAttachment = { showAttachSheet = true },
                    onRemoveAttachment = viewModel::removeAttachment,
                    onSend = { submit() },
                    onVoiceInput = { startVoiceInput() },
                    streaming = false,
                    onStop = {},
                    sendOnEnter = state.sendOnEnter,
                )

                if (!state.providerConfigured) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .clip(PillShape)
                            .clickable(onClick = onOpenSettings)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Connect a provider in Settings to chat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    suggestions.forEach { suggestion ->
                        SuggestionChip(
                            suggestion = suggestion,
                            onClick = { viewModel.setDraft(suggestion.prompt) },
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showAttachSheet) {
        AttachmentSheet(
            onDismiss = { showAttachSheet = false },
            onPickPhotos = {
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onPickFiles = { filePicker.launch(arrayOf("*/*")) },
        )
    }
}

@Composable
private fun SuggestionChip(
    suggestion: Suggestion,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(PillShape)
            .border(1.dp, NeoTheme.colors.chipBorder, PillShape)
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 14.dp, top = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = suggestion.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = suggestion.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
